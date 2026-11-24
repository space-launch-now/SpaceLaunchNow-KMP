import { test } from 'node:test';
import assert from 'node:assert/strict';
import { generateKeyPairSync, verify } from 'node:crypto';
import { validateEntry, validateTree, diffTrees, readTree, defaultRoot, parseArgs, REQUIRED_LOCALES, ENTRIES } from './iap-lib.mjs';
import { buildJwt, normalizeKey, createClient } from './asc.mjs';
import { checkFolder } from './check-iap-texts.mjs';

test('validateEntry counts Unicode code points', () => {
  assert.deepEqual(validateEntry('yearly', { name: 'x'.repeat(30), description: 'y'.repeat(45) }), []);
  assert.match(validateEntry('yearly', { name: 'x'.repeat(31), description: 'ok' })[0], /31 characters/);
  assert.match(validateEntry('yearly', { name: 'ok', description: 'y'.repeat(46) })[0], /limit 45/);
  // an emoji is one code point but two UTF-16 units
  assert.deepEqual(validateEntry('yearly', { name: '😀'.repeat(30), description: 'ok' }), []);
});

test('validateEntry rejects stray, missing and non-string keys', () => {
  assert.ok(validateEntry('yearly', { name: 'a', description: 'b', price: '1' }).some((e) => /stray key "price"/.test(e)));
  assert.ok(validateEntry('yearly', { name: 'a' }).some((e) => /missing key "description"/.test(e)));
  assert.ok(validateEntry('yearly', { name: 1, description: 'b' }).some((e) => /not a string/.test(e)));
  assert.ok(validateEntry('_group', { name: 'a', description: 'b' }).some((e) => /stray key "description"/.test(e)));
  assert.deepEqual(validateEntry('_group', { name: 'a', customAppName: 'b' }), []);
});

test('validateTree demands all locales and rejects ru', () => {
  const full = () => Object.fromEntries(REQUIRED_LOCALES.map((l) => [l, { name: 'a', description: 'b' }]));
  const tree = Object.fromEntries(ENTRIES.map((e) => [e, full()]));
  for (const l of Object.values(tree._group)) delete l.description;
  assert.deepEqual(validateTree(tree), []);
  delete tree.yearly.cs;
  tree.yearly.ru = { name: 'a', description: 'b' };
  tree.yearly.xx = { name: 'a', description: 'b' };
  const errors = validateTree(tree);
  assert.ok(errors.some((e) => e === 'yearly/cs.json: missing'));
  assert.ok(errors.some((e) => /yearly\/ru\.json: ru is not allowed/.test(e)));
  assert.ok(errors.some((e) => /yearly\/xx\.json: locale not in the allowed list/.test(e)));
});

test('diffTrees reports create, update, unchanged and extra', () => {
  const want = { p: { de: { name: 'A', description: 'x' }, fr: { name: 'B', description: 'y' }, it: { name: 'C', description: 'z' } } };
  const have = { p: { de: { name: 'A', description: 'x' }, fr: { name: 'B', description: 'Q' }, 'en-US': { name: 'E', description: 'e' } } };
  assert.deepEqual(diffTrees(want, have), [
    { entry: 'p', locale: 'de', status: 'unchanged' },
    { entry: 'p', locale: 'en-US', status: 'extra' },
    { entry: 'p', locale: 'fr', status: 'update', fields: ['description'] },
    { entry: 'p', locale: 'it', status: 'create' },
  ]);
});

test('parseArgs', () => {
  assert.deepEqual(parseArgs(['--live', '--out', 'x', '--check-against-git']), { live: true, out: 'x', 'check-against-git': true });
  assert.deepEqual(parseArgs(['--out=y']), { out: 'y' });
});

test('buildJwt makes a valid ES256 token from raw PEM and from base64', () => {
  const { privateKey, publicKey } = generateKeyPairSync('ec', { namedCurve: 'prime256v1' });
  const pem = privateKey.export({ type: 'pkcs8', format: 'pem' });
  for (const content of [pem, Buffer.from(pem).toString('base64')]) {
    const jwt = buildJwt({ keyId: 'KID123', issuerId: 'ISS', privateKey: content, now: 1000, ttl: 600 });
    const [h, p, s] = jwt.split('.');
    assert.deepEqual(JSON.parse(Buffer.from(h, 'base64url')), { alg: 'ES256', kid: 'KID123', typ: 'JWT' });
    assert.deepEqual(JSON.parse(Buffer.from(p, 'base64url')), { iss: 'ISS', iat: 1000, exp: 1600, aud: 'appstoreconnect-v1' });
    const sig = Buffer.from(s, 'base64url');
    assert.equal(sig.length, 64);
    assert.ok(verify('sha256', Buffer.from(`${h}.${p}`), { key: publicKey, dsaEncoding: 'ieee-p1363' }, sig));
  }
  assert.throws(() => buildJwt({ keyId: 'k', issuerId: 'i', privateKey: pem, ttl: 5000 }), /20 minutes/);
  assert.throws(() => normalizeKey(''), /empty/);
});

test('client list follows links.next and sends the bearer token', async () => {
  const seen = [];
  const fetchImpl = async (url, init) => {
    seen.push([url, init.headers.Authorization]);
    const second = url.includes('page=2');
    return {
      ok: true,
      status: 200,
      text: async () => JSON.stringify({ data: [{ id: second ? 'b' : 'a' }], links: second ? {} : { next: 'https://x.test/list?page=2' } }),
    };
  };
  const client = createClient({ fetchImpl, getToken: () => 'TOKEN', baseUrl: 'https://x.test' });
  const items = await client.list('/list');
  assert.deepEqual(items.map((i) => i.id), ['a', 'b']);
  assert.equal(seen[0][0], 'https://x.test/list?limit=200');
  assert.ok(seen.every(([, auth]) => auth === 'Bearer TOKEN'));
});

test('client error message holds no token', async () => {
  const fetchImpl = async () => ({ ok: false, status: 401, text: async () => '{"errors":[]}' });
  const client = createClient({ fetchImpl, getToken: () => 'SECRET-TOKEN' });
  await assert.rejects(client.get('/v1/apps'), (e) => /HTTP 401/.test(e.message) && !e.message.includes('SECRET-TOKEN'));
});

test('the files in git pass the check', () => {
  const { errors, files } = checkFolder(defaultRoot);
  assert.deepEqual(errors, []);
  assert.ok(files >= 42);
  const { tree } = readTree(defaultRoot);
  assert.ok(!('ru' in tree._group));
  assert.ok(!('en-US' in tree.base_plan) && !('en-US' in tree.yearly));
  assert.deepEqual(tree.spacelaunchnow_pro['en-US'], { name: 'All Access Lifetime', description: 'Unlock all features once and for all!' });
});
