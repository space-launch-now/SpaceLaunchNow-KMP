// Pure logic for the App Store purchase texts (Tasks#1269). No network, no env.
import { readdirSync, readFileSync, existsSync, statSync, mkdirSync, writeFileSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

export const BUNDLE_ID = 'me.spacelaunchnow.spacelaunchnow';
export const GROUP = '_group';
// Product kinds: two subscriptions and one in-app purchase. The group is handled as its own entry.
export const PRODUCTS = {
  base_plan: { kind: 'subscription' },
  yearly: { kind: 'subscription' },
  spacelaunchnow_pro: { kind: 'iap' },
};
export const ENTRIES = [GROUP, ...Object.keys(PRODUCTS)];
// Locales every entry must have. en-US is allowed but not required.
export const REQUIRED_LOCALES = ['cs', 'de-DE', 'es-ES', 'fr-FR', 'it', 'nl-NL', 'pl', 'pt-BR', 'uk', 'zh-Hans'];
export const ALLOWED_LOCALES = ['en-US', ...REQUIRED_LOCALES];
export const LIMITS = { name: 30, description: 45 };
// Allowed keys per entry. customAppName is optional and only for the group.
export const KEYS = {
  [GROUP]: { required: ['name'], optional: ['customAppName'] },
  product: { required: ['name', 'description'], optional: [] },
};

export const defaultRoot = join(dirname(fileURLToPath(import.meta.url)), '..', '..', 'iosApp', 'fastlane', 'iap');

const len = (s) => [...s].length;

/** Validate one parsed file. Returns a list of error strings (empty = ok). */
export function validateEntry(entry, value, { checkLimits = true } = {}) {
  const errors = [];
  if (value === null || typeof value !== 'object' || Array.isArray(value)) return ['not a JSON object'];
  const spec = entry === GROUP ? KEYS[GROUP] : KEYS.product;
  const all = [...spec.required, ...spec.optional];
  for (const k of Object.keys(value)) if (!all.includes(k)) errors.push(`stray key "${k}"`);
  for (const k of spec.required) if (!(k in value)) errors.push(`missing key "${k}"`);
  for (const k of all) {
    if (!(k in value)) continue;
    const v = value[k];
    if (typeof v !== 'string') {
      errors.push(`"${k}" is not a string`);
      continue;
    }
    if (v.trim() === '') errors.push(`"${k}" is empty`);
    if (v !== v.trim()) errors.push(`"${k}" has leading or trailing space`);
    const max = k === 'description' ? LIMITS.description : LIMITS.name;
    if (checkLimits && len(v) > max) errors.push(`"${k}" has ${len(v)} characters, limit ${max}`);
  }
  return errors;
}

/** Read a tree: { entry: { locale: {..} } }. Reports unreadable JSON in `errors`. */
export function readTree(root) {
  const tree = {};
  const errors = [];
  if (!existsSync(root)) return { tree, errors: [`folder not found: ${root}`] };
  for (const entry of readdirSync(root)) {
    const dir = join(root, entry);
    if (!statSync(dir).isDirectory()) continue;
    tree[entry] = {};
    for (const f of readdirSync(dir)) {
      if (!f.endsWith('.json')) {
        errors.push(`${entry}/${f}: not a .json file`);
        continue;
      }
      const locale = f.slice(0, -5);
      try {
        tree[entry][locale] = JSON.parse(readFileSync(join(dir, f), 'utf8'));
      } catch (e) {
        errors.push(`${entry}/${f}: invalid JSON (${e.message})`);
      }
    }
  }
  return { tree, errors };
}

/** Read the folder and validate it. Returns { tree, errors } with read and validation errors together. */
export function loadAndValidate(root) {
  const { tree, errors } = readTree(root);
  return { tree, errors: [...errors, ...validateTree(tree)] };
}

/** Validate a whole tree. Returns a list of error strings. */
export function validateTree(tree) {
  const errors = [];
  for (const entry of Object.keys(tree)) {
    if (!ENTRIES.includes(entry)) errors.push(`${entry}: unknown folder (allowed: ${ENTRIES.join(', ')})`);
  }
  for (const entry of ENTRIES) {
    const locales = tree[entry];
    if (!locales) {
      errors.push(`${entry}: folder missing`);
      continue;
    }
    for (const l of REQUIRED_LOCALES) if (!(l in locales)) errors.push(`${entry}/${l}.json: missing`);
    for (const [l, value] of Object.entries(locales)) {
      if (l === 'ru') errors.push(`${entry}/${l}.json: ru is not allowed`);
      else if (!ALLOWED_LOCALES.includes(l)) errors.push(`${entry}/${l}.json: locale not in the allowed list`);
      // en-US mirrors the live App Store Connect text, which can be longer than Apple's limit today.
      for (const e of validateEntry(entry, value, { checkLimits: l !== 'en-US' })) errors.push(`${entry}/${l}.json: ${e}`);
    }
  }
  return errors;
}

/**
 * Compare two trees. Returns [{entry, locale, status, fields?}] sorted.
 * status: 'create' (only in `want`), 'update' (fields differ), 'unchanged', 'extra' (only in `have`).
 * Only keys present in `want` are compared, so extra remote keys do not count as a difference.
 */
export function diffTrees(want, have) {
  const out = [];
  const entries = new Set([...Object.keys(want), ...Object.keys(have)]);
  for (const entry of [...entries].sort()) {
    const w = want[entry] || {};
    const h = have[entry] || {};
    for (const locale of [...new Set([...Object.keys(w), ...Object.keys(h)])].sort()) {
      if (!(locale in h)) out.push({ entry, locale, status: 'create' });
      else if (!(locale in w)) out.push({ entry, locale, status: 'extra' });
      else {
        const fields = Object.keys(w[locale]).filter((k) => w[locale][k] !== h[locale][k]);
        out.push(fields.length ? { entry, locale, status: 'update', fields } : { entry, locale, status: 'unchanged' });
      }
    }
  }
  return out;
}

export function writeTree(root, tree) {
  for (const [entry, locales] of Object.entries(tree)) {
    for (const [locale, value] of Object.entries(locales)) {
      mkdirSync(join(root, entry), { recursive: true });
      writeFileSync(join(root, entry, `${locale}.json`), JSON.stringify(value, null, 2) + '\n');
    }
  }
}

/** Count the localization files in a tree. */
export function countFiles(tree) {
  return Object.values(tree).reduce((n, l) => n + Object.keys(l).length, 0);
}
