#!/usr/bin/env node
// Checks the purchase text files in iosApp/fastlane/iap (Tasks#1269).
// Usage: node scripts/appstore-iap/check-iap-texts.mjs [folder]
import { pathToFileURL } from 'node:url';
import { readTree, validateTree, defaultRoot, countFiles } from './iap-lib.mjs';

export function checkFolder(root) {
  const { tree, errors } = readTree(root);
  const all = [...errors, ...validateTree(tree)];
  const files = countFiles(tree);
  return { errors: all, files };
}

if (import.meta.url === pathToFileURL(process.argv[1] ?? '').href) {
  const root = process.argv[2] || defaultRoot;
  const { errors, files } = checkFolder(root);
  errors.forEach((e) => console.error(`error ${e}`));
  console.log(`${files} file(s) checked: ${errors.length} error(s)`);
  process.exit(errors.length ? 1 : 0);
}
