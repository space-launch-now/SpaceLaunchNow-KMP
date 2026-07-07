# Firebase / GA4 Analytics via MCP — Setup TODO

**Goal:** Query the app's Firebase Analytics funnel (`paywall_viewed → paywall_tier_selected →
purchase_started → purchase_completed/failed`) directly from Claude Code, so the P0 funnel work in
[`specs/014-instrument-conversion-funnel/spec.md`](../../specs/014-instrument-conversion-funnel/spec.md)
(FR-7) becomes queryable without a manual dashboard round-trip.

**Created:** 2026-07-01 · **Owner:** _unassigned_ · **Status:** 🟡 Open

---

## Why this tool

Firebase Analytics **is** GA4 under the hood — every Firebase project links to a GA4 property, and
that's where the funnel events land. The path to "Firebase Analytics via MCP" is therefore the
**official Google Analytics MCP server** (read-only), which exposes exactly the tools the funnel needs:

| Tool | Use |
|---|---|
| `run_funnel_report` | The FR-7 funnel view (viewed → tier → started → completed/failed) |
| `run_report` | Ad-hoc event/dimension queries, sliced by FR-4 dimensions |
| `run_realtime_report` | Verify events fire during manual testing (DebugView equivalent) |
| `get_account_summaries` | List accounts/properties, confirm the linked GA4 property |

Source: <https://github.com/googleanalytics/google-analytics-mcp> ·
<https://developers.google.com/analytics/devguides/MCP>

---

## ✅ Setup steps

### 1. Gather the GA4 property + credentials
- [ ] Find the **GA4 property ID** linked to Firebase: Firebase console → Project settings →
      Integrations → Google Analytics (numeric, e.g. `123456789`).
- [ ] Create a **Google Cloud service account** (or reuse an existing analytics one) in the same GCP
      project backing Firebase.
- [ ] Grant that service account **Viewer** on the GA4 property (GA Admin → Property Access Management).
- [ ] Enable the **Google Analytics Data API** and **Admin API** on the GCP project.
- [ ] Download the service-account JSON key. **Do not commit it** — store it outside the repo (e.g.
      `~/.config/gcloud/sln-ga4.json`) and reference by absolute path. Add to the secrets list if it
      ever lands near the repo.

### 2. Register the MCP server
- [ ] Add a project `.mcp.json` at repo root (none exists yet — current servers come from
      `~/.claude.json`) **or** add to `~/.claude.json` if it should be user-global. Draft entry:
  ```jsonc
  {
    "mcpServers": {
      "google-analytics": {
        "command": "npx",
        "args": ["-y", "google-analytics-mcp"],
        "env": {
          "GOOGLE_APPLICATION_CREDENTIALS": "/absolute/path/to/sln-ga4.json",
          "GA4_PROPERTY_ID": "<numeric-property-id>"
        }
      }
    }
  }
  ```
  *(Confirm exact package name / env var names against the repo README — they may differ; the README
  is the source of truth.)*
- [ ] If `.mcp.json` is committed, ensure it contains **no secrets** — only the credential file *path*,
      never the key contents.

### 3. Verify the connection
- [ ] Restart Claude Code / re-approve the server, then confirm the `google-analytics__*` tools load.
- [ ] Run `get_account_summaries` → confirm the SLN GA4 property appears.
- [ ] Run a smoke `run_report` (last 7 days, event count by `eventName`) → confirm known events
      (`session_start`, existing `paywall_viewed`) return data.

### 4. Wire it to the funnel work (depends on spec 014 shipping)
- [ ] Once FR-1…FR-6 land and events flow, build the FR-7 funnel via `run_funnel_report`, filtered to
      `source = "support_us"`, segmented by `subscription_type` / `is_trial` / `platform`.
- [ ] Save the canonical query/params into `specs/014-instrument-conversion-funnel/quickstart.md` so
      P1 paywall changes can be evaluated against it.
- [ ] Resolve spec **Q3** (funnel view in Firebase vs Datadog vs both) — record that GA4-MCP gives the
      funnel directly from Claude Code.

---

## ⚠️ Caveats / limits
- **Read-only.** The server cannot modify GA4 config — data retrieval only.
- **Ingestion latency.** GA4 event data lags several hours; use `run_realtime_report` for live
      verification during manual testing.
- **Sampling / cardinality.** Free GA4 applies sampling and cardinality limits. For *exact*,
      unsampled per-tier revenue (FR-5), the robust path is Firebase's **BigQuery export** queried via
      a BigQuery MCP — heavier setup, deferred until sampling actually bites.

## Reference
- Conversion funnel spec: [`specs/014-instrument-conversion-funnel/spec.md`](../../specs/014-instrument-conversion-funnel/spec.md)
- Monetization TODO (P0 parent): [`MONETIZATION_TODO.md`](MONETIZATION_TODO.md)
- RevenueCat MCP (already connected) covers the *revenue* side; this covers the *event/funnel* side.
