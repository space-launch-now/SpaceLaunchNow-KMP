# Feature Spec: Fix Subscription Free Trial Disclosure

**Branch**: `004-fix-subscription-trial-disclosure`  
**Priority**: CRITICAL (Google Play Policy Violation)  
**Date**: 2026-03-03

## Problem Statement

Google Play has flagged SpaceLaunchNow for violating the **Subscriptions policy** (https://support.google.com/googleplay/android-developer/answer/9900533/). The violation:

> **Terms of trial offer or introductory pricing are unclear.**  
> If you have a free trial or special introductory price, make sure the offer clearly and accurately explains the terms. This includes when the offer ends, how much the paid subscription will cost once the offer ends, and how users can cancel a trial if they don't want to subscribe. All terms must be called out clearly in your subscription offer as well as the payment cart.

**Root Cause**: The yearly subscription ($9.99/year) includes a **3-day free trial** configured in Google Play/RevenueCat, but the app's subscription UI (`SupportUsScreen.kt`) does not display:
1. That a free trial exists
2. The trial duration (3 days)
3. What the subscription costs after the trial ends ($9.99/year)
4. How to cancel during the trial period

The `PricingCard` composable shows only price/period and a "Subscribe Now" button. The `FinePrint` section mentions auto-renewal but says nothing about free trials.

## Requirements

### Functional Requirements

1. **FR-1: Extract trial/intro pricing data from RevenueCat SDK**
   - Read `StoreProduct.subscriptionOptions` (Android) and `StoreProduct.introductoryDiscount` (iOS) to detect free trial offers
   - Map trial duration, intro price, and post-trial price into the `ProductInfo` data model
   - Handle cases where no trial is configured (graceful fallback)

2. **FR-2: Display trial terms on subscription cards**
   - When a product has a free trial: show "X-day free trial" prominently on the pricing card
   - Change button text from "Subscribe Now" to "Start Free Trial" when trial is available
   - Show post-trial pricing clearly: e.g., "then $9.99/year"
   - When no trial exists: show existing "Subscribe Now" behavior unchanged

3. **FR-3: Update fine print with trial disclosure**
   - Add trial-specific fine print text when any displayed product has a trial
   - Include: trial duration, post-trial auto-renew price, cancellation instructions
   - Example: "Free trial will automatically convert to a paid subscription at $9.99/year unless canceled at least 24 hours before the trial ends."

4. **FR-4: Detect if current user is in trial period**
   - Use RevenueCat customer info to detect if the active entitlement is currently in a trial period
   - Show trial status in CurrentPlanCard (e.g., "Free Trial - 2 days remaining" instead of "Free User")
   - This is secondary to FR-1 through FR-3

### Non-Functional Requirements

1. **NFR-1**: Changes must be cross-platform (Android + iOS) using common code where possible
2. **NFR-2**: No hardcoded trial durations — all trial information must come from RevenueCat/store dynamically
3. **NFR-3**: Must comply with Google Play Subscriptions policy requirements
4. **NFR-4**: Dual previews (light + dark) for any new/modified composables

## Acceptance Criteria

- [ ] Yearly pricing card shows "3-day free trial" (or whatever trial is configured) when a trial exists
- [ ] Button text changes to "Start Free Trial" when trial is available
- [ ] Post-trial pricing is clearly displayed (e.g., "then $9.99/year")  
- [ ] Fine print includes trial-specific terms (duration, auto-conversion, cancellation)
- [ ] Products without trials show standard "Subscribe Now" behavior
- [ ] Trial information is dynamically loaded from RevenueCat, not hardcoded
- [ ] Both Android and iOS extract trial data from their respective store APIs
- [ ] Dual previews exist for modified composables
