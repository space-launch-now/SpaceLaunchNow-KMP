# Contracts: Onboarding Paywall

No external API contracts required. This feature only touches UI (Compose) and local storage (DataStore).

All data flows through existing contracts:
- **SubscriptionViewModel** → RevenueCat SDK → Product pricing and purchase flows
- **AppPreferences** → DataStore → Shown-once persistence flag
