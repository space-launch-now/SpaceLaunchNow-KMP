import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, readFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { createClient } from './asc.mjs';
import { main as upload } from './upload-iap.mjs';
import { main as download, formatDifferences } from './download-iap.mjs';
import { readTree, defaultRoot } from './iap-lib.mjs';

// A fake App Store Connect. `remote` maps "kind:parentId" to a list of { id, locale, ...attrs }.
function fakeAsc(remote) {
  const calls = [];
  const routes = [
    [/^\/v1\/apps\?/, () => [{ id: 'app1' }]],
    [/^\/v1\/apps\/app1\/subscriptionGroups\?/, () => [{ id: 'grp1' }]],
    [/^\/v1\/subscriptionGroups\/grp1\/subscriptions\?/, () => [
      { id: 'sub-m', attributes: { productId: 'base_plan' } },
      { id: 'sub-y', attributes: { productId: 'yearly' } },
      { id: 'sub-x', attributes: { productId: 'other' } },
    ]],
    [/^\/v1\/apps\/app1\/inAppPurchasesV2\?/, () => [{ id: 'iap1', attributes: { productId: 'spacelaunchnow_pro' } }]],
    [/^\/v1\/subscriptionGroups\/grp1\/subscriptionGroupLocalizations\?/, () => remote.group],
    [/^\/v1\/subscriptions\/sub-m\/subscriptionLocalizations\?/, () => remote.base_plan],
    [/^\/v1\/subscriptions\/sub-y\/subscriptionLocalizations\?/, () => remote.yearly],
    [/^\/v2\/inAppPurchases\/iap1\/inAppPurchaseLocalizations\?/, () => remote.spacelaunchnow_pro],
  ];
  const fetchImpl = async (url, init) => {
    const path = url.replace('https://asc.test', '');
    calls.push({ method: init.method, path, body: init.body ? JSON.parse(init.body) : null });
    if (init.method !== 'GET') return { ok: true, status: 200, text: async () => '{}' };
    const hit = routes.find(([re]) => re.test(path));
    if (!hit) return { ok: false, status: 404, text: async () => `no route ${path}` };
    return { ok: true, status: 200, text: async () => JSON.stringify({ data: hit[1]() }) };
  };
  return { calls, client: createClient({ fetchImpl, getToken: () => 'T', baseUrl: 'https://asc.test' }) };
}

const loc = (id, locale, attributes) => ({ id, attributes: { locale, ...attributes } });
const git = readTree(defaultRoot).tree;
const fromGit = (entry, locale) => ({ locale, ...git[entry][locale] });

function remoteMostlyMatching() {
  return {
    // group: all but cs present; de-DE differs; base_plan has only an en-US whose description differs
    group: [
      ...Object.keys(git._group).filter((l) => l !== 'cs' && l !== 'de-DE').map((l, i) => loc(`g${i}`, l, fromGit('_group', l))),
      loc('gde', 'de-DE', { locale: 'de-DE', name: 'Old name' }),
    ],
    base_plan: [loc('m-en', 'en-US', { ...fromGit('base_plan', 'en-US'), description: 'old english' })],
    yearly: Object.keys(git.yearly).map((l, i) => loc(`y${i}`, l, fromGit('yearly', l))),
    spacelaunchnow_pro: [loc('p-fr', 'fr-FR', { ...fromGit('spacelaunchnow_pro', 'fr-FR'), description: 'old' })],
  };
}

test('dry run prints the plan and sends nothing but GETs', async () => {
  const { calls, client } = fakeAsc(remoteMostlyMatching());
  const lines = [];
  const code = await upload([], {}, client, (l) => lines.push(l));
  assert.equal(code, 0);
  assert.ok(calls.every((c) => c.method === 'GET'));
  assert.ok(lines.some((l) => /create\s+_group\/cs/.test(l)));
  assert.ok(lines.some((l) => /update\s+_group\/de-DE \(name\)/.test(l)));
  assert.ok(lines.some((l) => /unchanged\s+yearly\/cs/.test(l)));
  assert.ok(lines.some((l) => /update\s+spacelaunchnow_pro\/fr-FR \(description\)/.test(l)));
  assert.ok(lines.some((l) => /update\s+base_plan\/en-US \(description\)/.test(l)));
  assert.match(lines.at(-1), /^DRY RUN: 21 create, 3 update, 20 unchanged/);
  console.log(lines.join('\n'));
});

test('--live is refused without IAP_UPLOAD_APPROVED=yes', async () => {
  const { calls, client } = fakeAsc(remoteMostlyMatching());
  const lines = [];
  assert.equal(await upload(['--live'], {}, client, (l) => lines.push(l)), 3);
  assert.equal(await upload(['--live'], { IAP_UPLOAD_APPROVED: 'true' }, client, (l) => lines.push(l)), 3);
  assert.equal(calls.length, 0);
  assert.match(lines[0], /refused/);
});

test('--live approved sends POST and PATCH, never DELETE', async () => {
  const { calls, client } = fakeAsc(remoteMostlyMatching());
  const code = await upload(['--live'], { IAP_UPLOAD_APPROVED: 'yes' }, client, () => {});
  assert.equal(code, 0);
  const writes = calls.filter((c) => c.method !== 'GET');
  assert.ok(writes.length > 0);
  assert.ok(writes.every((c) => c.method === 'POST' || c.method === 'PATCH'));
  const patchGroup = writes.find((c) => c.method === 'PATCH' && c.path === '/v1/subscriptionGroupLocalizations/gde');
  assert.deepEqual(patchGroup.body, { data: { type: 'subscriptionGroupLocalizations', id: 'gde', attributes: { name: 'All Access' } } });
  const createGroupCs = writes.find((c) => c.method === 'POST' && c.body.data.attributes.locale === 'cs' && c.body.data.type === 'subscriptionGroupLocalizations');
  assert.deepEqual(createGroupCs.body.data.relationships, { subscriptionGroup: { data: { type: 'subscriptionGroups', id: 'grp1' } } });
  const createIap = writes.find((c) => c.method === 'POST' && c.body.data.type === 'inAppPurchaseLocalizations');
  assert.deepEqual(createIap.body.data.relationships, { inAppPurchaseV2: { data: { type: 'inAppPurchases', id: 'iap1' } } });
});

test('download writes the tree and --check-against-git reports differences', async () => {
  const { client } = fakeAsc(remoteMostlyMatching());
  const out = mkdtempSync(join(tmpdir(), 'iap-'));
  const lines = [];
  const orig = console.log;
  console.log = (l) => lines.push(l);
  let code;
  try {
    code = await download(['--out', out, '--check-against-git'], {}, client);
  } finally {
    console.log = orig;
  }
  assert.equal(code, 1);
  assert.ok(lines.some((l) => /base_plan\/en-US: differs \(description\)/.test(l)));
  assert.deepEqual(JSON.parse(readFileSync(join(out, 'base_plan', 'en-US.json'), 'utf8')), { name: 'Pro Access Monthly', description: 'old english' });
  assert.deepEqual(formatDifferences([{ entry: 'a', locale: 'b', status: 'unchanged' }]), []);
});
