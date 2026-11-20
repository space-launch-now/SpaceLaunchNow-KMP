#!/usr/bin/env node
// Checks the App Store listing files in iosApp/fastlane/metadata against Apple's limits.
// Usage: node scripts/check-ios-metadata.mjs [--strict]
// Default: a missing optional field is a warning. --strict makes it an error.
import { readdirSync, readFileSync, existsSync, statSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = join(dirname(fileURLToPath(import.meta.url)), '..', 'iosApp', 'fastlane', 'metadata');
const strict = process.argv.includes('--strict');

// Apple limits: characters, except keywords (UTF-8 bytes).
const fields = {
  name: { max: 30, required: true },
  subtitle: { max: 30 },
  description: { max: 4000, required: true },
  keywords: { max: 100, bytes: true },
  promotional_text: { max: 170 },
};

const errors = [];
const warnings = [];
const locales = readdirSync(root).filter((d) => statSync(join(root, d)).isDirectory());
if (locales.length === 0) errors.push('no locale folders found');

for (const locale of locales) {
  for (const [field, rule] of Object.entries(fields)) {
    const file = join(root, locale, `${field}.txt`);
    const tag = `${locale}/${field}.txt`;
    if (!existsSync(file)) {
      (rule.required || strict ? errors : warnings).push(`${tag}: missing`);
      continue;
    }
    const text = readFileSync(file, 'utf8').replace(/\r?\n$/, '');
    const size = rule.bytes ? Buffer.byteLength(text, 'utf8') : [...text].length;
    if (text.trim() === '') errors.push(`${tag}: empty`);
    else if (size > rule.max) errors.push(`${tag}: ${size} > ${rule.max}${rule.bytes ? ' bytes' : ' chars'}`);
    if (field === 'keywords' && /,\s/.test(text)) errors.push(`${tag}: no space after a comma`);
  }
}

warnings.forEach((w) => console.warn(`warn  ${w}`));
errors.forEach((e) => console.error(`error ${e}`));
console.log(`${locales.length} locale(s) checked: ${errors.length} error(s), ${warnings.length} warning(s)`);
process.exit(errors.length ? 1 : 0);
