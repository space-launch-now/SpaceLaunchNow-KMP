#!/usr/bin/env node
// Downloads the live purchase texts from App Store Connect (Tasks#1269). Read only.
// Usage: node scripts/appstore-iap/download-iap.mjs [--out <folder>] [--check-against-git]
// Default folder: iosApp/fastlane/iap_downloaded. Needs APPLE_API_KEY_ID, APPLE_API_ISSUER_ID, APPLE_API_KEY_CONTENT.
// With --check-against-git it exits 1 on any difference to iosApp/fastlane/iap and prints it.
import { join } from 'node:path';
import { pathToFileURL } from 'node:url';
import { createClient, resolveTargets, readRemote, tokenFromEnv } from './asc.mjs';
import { readTree, writeTree, diffTrees, parseArgs, defaultRoot } from './iap-lib.mjs';

export const defaultOut = join(defaultRoot, '..', 'iap_downloaded');

/** Download remote texts, write them to `out`. Returns { tree }. */
export async function download({ client, out }) {
  const { targets } = await resolveTargets(client);
  const { tree } = await readRemote(client, targets);
  writeTree(out, tree);
  return { tree };
}

/** Format a diff for printing: only entries that are not 'unchanged'. */
export function formatDifferences(diff) {
  return diff
    .filter((d) => d.status !== 'unchanged')
    .map((d) => {
      const what = { create: 'in git, not in App Store Connect', extra: 'in App Store Connect, not in git', update: `differs (${(d.fields || []).join(', ')})` }[d.status];
      return `${d.entry}/${d.locale}: ${what}`;
    });
}

export async function main(argv, env = process.env, client) {
  const args = parseArgs(argv);
  const out = typeof args.out === 'string' ? args.out : defaultOut;
  client = client ?? createClient({ getToken: () => tokenFromEnv(env) });
  const { tree } = await download({ client, out });
  console.log(`downloaded ${Object.values(tree).reduce((n, l) => n + Object.keys(l).length, 0)} localization(s) to ${out}`);
  if (args['check-against-git']) {
    const git = readTree(defaultRoot).tree;
    const lines = formatDifferences(diffTrees(git, tree));
    lines.forEach((l) => console.log(`diff  ${l}`));
    console.log(`${lines.length} difference(s) against git`);
    return lines.length ? 1 : 0;
  }
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
