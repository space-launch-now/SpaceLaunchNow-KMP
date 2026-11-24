// Minimal App Store Connect API client (Tasks#1269). No npm dependencies.
// Credentials come from env: APPLE_API_KEY_ID, APPLE_API_ISSUER_ID, APPLE_API_KEY_CONTENT
// (base64 of the .p8, as in iosApp/fastlane/Fastfile `api_key`; raw PEM is accepted too).
// Nothing here prints the key or the token.
import { createPrivateKey, sign } from 'node:crypto';
import { BUNDLE_ID, PRODUCTS, GROUP } from './iap-lib.mjs';

export const API = 'https://api.appstoreconnect.apple.com';

const b64url = (buf) => Buffer.from(buf).toString('base64').replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');

/** Accept base64 of a .p8, or a raw PEM. Returns a PEM string. */
export function normalizeKey(content) {
  const text = String(content ?? '').trim();
  if (!text) throw new Error('APPLE_API_KEY_CONTENT is empty');
  if (text.includes('-----BEGIN')) return text.replace(/\\n/g, '\n');
  return Buffer.from(text, 'base64').toString('utf8');
}

/** Build an ES256 JWT for the App Store Connect API. `now` is in seconds. Apple allows at most 20 minutes. */
export function buildJwt({ keyId, issuerId, privateKey, now = Math.floor(Date.now() / 1000), ttl = 600 }) {
  if (!keyId || !issuerId) throw new Error('APPLE_API_KEY_ID and APPLE_API_ISSUER_ID are required');
  if (ttl > 1200) throw new Error('ttl above 20 minutes is rejected by Apple');
  const header = { alg: 'ES256', kid: keyId, typ: 'JWT' };
  const payload = { iss: issuerId, iat: now, exp: now + ttl, aud: 'appstoreconnect-v1' };
  const input = `${b64url(JSON.stringify(header))}.${b64url(JSON.stringify(payload))}`;
  const key = createPrivateKey(normalizeKey(privateKey));
  // ieee-p1363 gives the raw r||s signature that JWT ES256 needs (not DER).
  const sig = sign('sha256', Buffer.from(input), { key, dsaEncoding: 'ieee-p1363' });
  return `${input}.${b64url(sig)}`;
}

export function tokenFromEnv(env = process.env, now) {
  return buildJwt({
    keyId: env.APPLE_API_KEY_ID,
    issuerId: env.APPLE_API_ISSUER_ID,
    privateKey: env.APPLE_API_KEY_CONTENT,
    now,
  });
}

/** Small client. `fetchImpl` and `getToken` are injectable so tests need no network. */
export function createClient({ fetchImpl = globalThis.fetch, getToken, baseUrl = API } = {}) {
  async function request(method, path, body) {
    const url = path.startsWith('http') ? path : baseUrl + path;
    const res = await fetchImpl(url, {
      method,
      headers: {
        Authorization: `Bearer ${getToken()}`,
        ...(body ? { 'Content-Type': 'application/json' } : {}),
      },
      body: body ? JSON.stringify(body) : undefined,
    });
    const text = await res.text();
    if (!res.ok) {
      // The body holds Apple's error list; it holds no secrets.
      throw new Error(`${method} ${path} failed: HTTP ${res.status} ${text.slice(0, 500)}`);
    }
    return text ? JSON.parse(text) : {};
  }
  /** GET every page of a list endpoint. Follows links.next. Returns the `data` items. */
  async function list(path) {
    const items = [];
    let next = path + (path.includes('?') ? '&' : '?') + 'limit=200';
    while (next) {
      const page = await request('GET', next);
      items.push(...(page.data || []));
      next = page.links && page.links.next;
    }
    return items;
  }
  return {
    request,
    list,
    get: (p) => request('GET', p),
    post: (p, b) => request('POST', p, b),
    patch: (p, b) => request('PATCH', p, b),
  };
}

/**
 * Resolve the app, the subscription group, the subscriptions and the in-app purchase.
 * Returns { appId, groupId, targets } where targets maps an entry to { kind, id }.
 * UNSURE (no live check possible): the filter names filter[bundleId] on /v1/apps,
 * filter[productId] on /v1/apps/{id}/inAppPurchasesV2, and the productId attribute on subscriptions.
 * They follow Apple's published API reference. If one is wrong, the first live run fails loudly with HTTP 400.
 */
export async function resolveTargets(client, bundleId = BUNDLE_ID) {
  const apps = await client.list(`/v1/apps?filter[bundleId]=${encodeURIComponent(bundleId)}`);
  if (apps.length !== 1) throw new Error(`expected 1 app for ${bundleId}, found ${apps.length}`);
  const appId = apps[0].id;

  const groups = await client.list(`/v1/apps/${appId}/subscriptionGroups`);
  const targets = {};
  let groupId = null;
  const wantSubs = Object.keys(PRODUCTS).filter((p) => PRODUCTS[p].kind === 'subscription');
  for (const g of groups) {
    const subs = await client.list(`/v1/subscriptionGroups/${g.id}/subscriptions`);
    for (const s of subs) {
      const pid = s.attributes && s.attributes.productId;
      if (wantSubs.includes(pid)) {
        targets[pid] = { kind: 'subscription', id: s.id };
        groupId = groupId ?? g.id;
      }
    }
  }
  for (const p of wantSubs) if (!targets[p]) throw new Error(`subscription ${p} not found in any group`);
  targets[GROUP] = { kind: 'group', id: groupId };

  for (const p of Object.keys(PRODUCTS).filter((k) => PRODUCTS[k].kind === 'iap')) {
    const iaps = await client.list(`/v1/apps/${appId}/inAppPurchasesV2?filter[productId]=${encodeURIComponent(p)}`);
    const hit = iaps.find((i) => i.attributes && i.attributes.productId === p);
    if (!hit) throw new Error(`in-app purchase ${p} not found`);
    targets[p] = { kind: 'iap', id: hit.id };
  }
  return { appId, groupId, targets };
}

// Per kind: how to list, create and patch localizations.
// UNSURE: the list path for in-app purchase localizations. Apple documents
// GET /v2/inAppPurchases/{id}/inAppPurchaseLocalizations for the v2 model. It is one constant, so it is easy to fix.
export const LOCALIZATION = {
  group: {
    listPath: (id) => `/v1/subscriptionGroups/${id}/subscriptionGroupLocalizations`,
    type: 'subscriptionGroupLocalizations',
    createPath: '/v1/subscriptionGroupLocalizations',
    patchPath: (id) => `/v1/subscriptionGroupLocalizations/${id}`,
    parent: { key: 'subscriptionGroup', type: 'subscriptionGroups' },
    fields: ['name', 'customAppName'],
  },
  subscription: {
    listPath: (id) => `/v1/subscriptions/${id}/subscriptionLocalizations`,
    type: 'subscriptionLocalizations',
    createPath: '/v1/subscriptionLocalizations',
    patchPath: (id) => `/v1/subscriptionLocalizations/${id}`,
    parent: { key: 'subscription', type: 'subscriptions' },
    fields: ['name', 'description'],
  },
  iap: {
    listPath: (id) => `/v2/inAppPurchases/${id}/inAppPurchaseLocalizations`,
    type: 'inAppPurchaseLocalizations',
    createPath: '/v1/inAppPurchaseLocalizations',
    patchPath: (id) => `/v1/inAppPurchaseLocalizations/${id}`,
    parent: { key: 'inAppPurchaseV2', type: 'inAppPurchases' },
    fields: ['name', 'description'],
  },
};

/** Read remote localizations for every target. Returns { tree, ids } where ids[entry][locale] is the localization id. */
export async function readRemote(client, targets) {
  const tree = {};
  const ids = {};
  for (const [entry, t] of Object.entries(targets)) {
    const spec = LOCALIZATION[t.kind];
    tree[entry] = {};
    ids[entry] = {};
    for (const loc of await client.list(spec.listPath(t.id))) {
      const a = loc.attributes || {};
      const value = {};
      for (const f of spec.fields) {
        if (f === 'customAppName' && !a[f]) continue; // optional on the group
        value[f] = a[f] ?? '';
      }
      tree[entry][a.locale] = value;
      ids[entry][a.locale] = loc.id;
    }
  }
  return { tree, ids };
}
