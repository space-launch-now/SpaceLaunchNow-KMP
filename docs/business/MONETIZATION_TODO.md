# Monetization & Conversion — Action TODO

**Source:** business-analysis skill run on live RevenueCat data (project `projbe17841f`, trailing 28 days).
**Created:** 2026-06-29 · **App version at analysis:** v5.33.1 (build 78)
**Owner:** _unassigned_ · **Status:** 🟡 Open

---

## 📊 Snapshot that drove these items

| Metric | Value | Derived signal |
|---|---|---|
| MRR | $832 | Recurring base is thin |
| Revenue (28d) | $1,775 | **~53% is non-recurring (lifetime)** |
| Active subscriptions | 456 | — |
| Active trials | 31 | Trial pipeline barely used |
| Active users (28d) | 51,910 | ~51.5K free-tier ad viewers |
| New customers (28d) | 2,519 | — |
| Free→paid conversion | **~0.88%** | Below 1–3% freemium norm |
| Blended ARPPU | **~$1.82/mo** | Heavy discounting / low price point |

A 0.1pt conversion lift ≈ **+52 subscriptions**.

---

## ✅ Action items (ranked by impact)

### P0 — Instrument the conversion funnel
- [ ] Add analytics events: SupportUsScreen **view** → tier **tap** → **purchase success/failure**
- [ ] Wire events through Datadog RUM + Firebase Analytics (see `analytics/DatadogConfig.kt`)
- [ ] Confirm events carry subscription-state attributes already tracked (`activeEntitlements`, `platform`)
- [ ] Build a funnel view so paywall/pricing changes become measurable
- **Files:** `ui/subscription/SupportUsScreen.kt`, `analytics/`, `util/logging/SpaceLogger.kt`
- **Why:** Every pricing/paywall change below is currently unmeasurable. Highest ROI, lowest effort.

### P1 — Reposition lifetime to stop cannibalizing recurring
- [ ] Review lifetime price relative to annual in the current `default` offering (`ofrng74226a750e`)
- [ ] Test demoting lifetime in the paywall hierarchy (target churn-risk users, not would-be subscribers)
- [ ] Measure effect on MRR vs. one-time revenue mix after funnel instrumentation lands
- **Why:** ~$943/28d of revenue is one-time; each lifetime sale permanently removes a user from the recurring pool.

### P1 — Surface trials on the high-intent path
- [ ] Audit where/whether a trial CTA appears (only 31 trials vs. 2,519 new customers/28d)
- [ ] Add a trial offer at a high-intent moment (e.g. premium-feature gate hit)
- **Files:** `ui/subscription/SupportUsScreen.kt`, `PremiumFeatureGate.kt`
- **Why:** Trials are a proven lift to subscription conversion and are currently near-zero.

### P2 — Surface ad revenue alongside subscription revenue
- [ ] Pull AdMob revenue into the same reporting view as RevenueCat
- [ ] Quantify free-tier (≈51.5K users) ad revenue before any "remove ads" pricing decision
- **Why:** Free tier is the dominant audience; AdMob is likely the largest revenue line and is invisible to RevenueCat.

### P2 — Archive stale legacy offerings (config hygiene)
- [ ] Confirm no live purchase path references the 8 legacy offerings (`legacy`, `legacy_2018`…`legacy_2024`)
- [ ] Archive unreferenced ones via RevenueCat (`archive-offering`) — reversible
- **Why:** 8 of 9 offerings are legacy but still `state: active`; reduces config surface and accidental-assignment risk.

---

## 📌 Open follow-ups (need more data before acting)
- [ ] Trend MRR / active subscriptions over time (`get-chart-data`) to confirm direction, not just a single 28-day snapshot
- [ ] Tier-level pricing breakdown (`list-products` / `list-packages`) to pressure-test P1 lifetime/annual recommendations
- [ ] Churn rate per tier (RevenueCat dashboard) to validate the "lifetime cannibalization" thesis

---

## Reference
- Analysis skill: `.claude/skills/business-analysis/`
- Monetization detail: [`.claude/skills/business-analysis/references/monetization.md`](../../.claude/skills/business-analysis/references/monetization.md)
- Analytics gaps: [`.claude/skills/business-analysis/references/analytics.md`](../../.claude/skills/business-analysis/references/analytics.md)
- RevenueCat IDs: Project `projbe17841f` · Pro `entl9fe2b6018c` · Lifetime `entleb14c06f19` · Legacy `entl3ad15261a3`
