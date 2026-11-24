#!/usr/bin/env node
// Uploads the purchase texts to App Store Connect (Tasks#1269).
// DRY RUN BY DEFAULT: prints create / update / unchanged per locale and product, sends nothing.
// --live sends POST and PATCH, and only when env IAP_UPLOAD_APPROVED=yes. It never deletes.
// A changed localization goes to App Review. Read iosApp/fastlane/iap/README.md first.
import { pathToFileURL } from 'node:url';
import { createClient, resolveTargets, readRemote, tokenFromEnv, LOCALIZATION } from './asc.mjs';
import { readTree, validateTree, diffTrees, parseArgs, defaultRoot } from './iap-lib.mjs';

/** Build the work list: create and update items only (extra remote locales are left alone). */
export function buildPlan(want, remote) {
  return diffTrees(want, remote);
}

/** Send one change. Returns the HTTP-level result for logging. */
export async function applyItem({ client, item, want, targets, ids }) {
  const t = targets[item.entry];
  const spec = LOCALIZATION[t.kind];
  const value = want[item.entry][item.locale];
  if (item.status === 'create') {
    const attributes = { locale: item.locale };
    for (const f of spec.fields) if (value[f] !== undefined) attributes[f] = value[f];
    return client.post(spec.createPath, {
      data: {
        type: spec.type,
        attributes,
        relationships: { [spec.parent.key]: { data: { type: spec.parent.type, id: t.id } } },
      },
    });
  }
  // update: send only the changed fields
  const attributes = {};
  for (const f of item.fields) attributes[f] = value[f];
  const id = ids[item.entry][item.locale];
  return client.patch(spec.patchPath(id), { data: { type: spec.type, id, attributes } });
}

export async function main(argv, env = process.env, client, log = console.log) {
  const args = parseArgs(argv);
  const live = args.live === true;
  if (live && env.IAP_UPLOAD_APPROVED !== 'yes') {
    log('error --live refused: set IAP_UPLOAD_APPROVED=yes (see iosApp/fastlane/iap/README.md)');
    return 3;
  }
  const { tree: want, errors } = readTree(defaultRoot);
  const bad = [...errors, ...validateTree(want)];
  if (bad.length) {
    bad.forEach((e) => log(`error ${e}`));
    return 1;
  }
  client = client ?? createClient({ getToken: () => tokenFromEnv(env) });
  const { targets } = await resolveTargets(client);
  const { tree: remote, ids } = await readRemote(client, targets);
  const plan = buildPlan(want, remote);
  const counts = { create: 0, update: 0, unchanged: 0, extra: 0 };
  for (const item of plan) {
    counts[item.status]++;
    if (item.status === 'extra') continue; // never delete
    const detail = item.fields ? ` (${item.fields.join(', ')})` : '';
    log(`${live ? 'live ' : 'dry  '} ${item.status.padEnd(9)} ${item.entry}/${item.locale}${detail}`);
    if (live && (item.status === 'create' || item.status === 'update')) {
      await applyItem({ client, item, want, targets, ids });
    }
  }
  log(`${live ? 'LIVE' : 'DRY RUN'}: ${counts.create} create, ${counts.update} update, ${counts.unchanged} unchanged, ${counts.extra} left alone (in App Store Connect only)`);
  return 0;
}

if (import.meta.url === pathToFileURL(process.argv[1] ?? '').href) {
  main(process.argv.slice(2)).then(
    (code) => process.exit(code),
    (e) => {
      console.error(`error ${e.message}`);
      process.exit(2);
    },
  );
}
