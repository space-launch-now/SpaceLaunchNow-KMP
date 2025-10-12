# iOS Billing Koin Crash Fix

## Problem

The iOS app was crashing on startup with:
```
kotlin.IllegalStateException: KClass for Kotlin subclasses of Objective-C classes is not supported yet
```

### Root Cause

The `BillingClient` class on iOS was extending `NSObject()` to implement StoreKit protocols (`SKProductsRequestDelegateProtocol`, `SKPaymentTransactionObserverProtocol`).

When Koin tried to register this class using `factory { createBillingClient() }`, it attempted to get the KClass for indexing. However, Kotlin/Native **does not support KClass reflection for classes that extend Objective-C classes**.

```kotlin
// ❌ PROBLEM: This crashes Koin
actual class BillingClient : NSObject(), SKProductsRequestDelegateProtocol {
    // Koin cannot get KClass of this!
}
```

## Solution: Wrapper Pattern

Following the pattern from the [Medium article on KMM subscriptions](https://medium.com/@boobalaninfo/kotlin-multiplatform-kmm-subscriptions-how-to-implement-in-app-subscriptions-for-android-and-ios-4cd18edfbe49), we implemented a **wrapper pattern**:

### Architecture

```
┌─────────────────────────────────────────┐
│  BillingClient (actual class)          │  ← Registered in Koin ✅
│  - Does NOT extend NSObject            │
│  - Simple Kotlin class                  │
│  - Delegates to StoreKitHelper          │
└──────────────┬──────────────────────────┘
               │
               │ delegates to
               ▼
┌─────────────────────────────────────────┐
│  StoreKitHelper (private class)         │  ← NOT in Koin ✅
│  - Extends NSObject                     │
│  - Implements StoreKit protocols        │
│  - Does actual StoreKit work            │
└─────────────────────────────────────────┘
```

### Implementation

```kotlin
// ✅ SOLUTION: Wrapper doesn't extend NSObject
actual class BillingClient {
    private val helper = StoreKitHelper()
    
    actual suspend fun initialize(): Result<Unit> {
        return helper.initialize()  // Delegate to helper
    }
    // ... other methods delegate to helper
}

// ✅ Helper is private, never exposed to Koin
private class StoreKitHelper : NSObject(), 
    SKProductsRequestDelegateProtocol, 
    SKPaymentTransactionObserverProtocol {
    
    // All the StoreKit implementation
}
```

## Key Points

### Why This Works

1. **BillingClient** is a pure Kotlin class
   - No NSObject inheritance
   - Koin can get its KClass without issues
   - Safely registered in dependency injection

2. **StoreKitHelper** handles StoreKit
   - Private to the file
   - Never exposed to Koin
   - Can extend NSObject safely

3. **Delegation pattern**
   - BillingClient delegates all calls to helper
   - Clean separation of concerns
   - Same public API as Android

### Comparison to Medium Article

The Medium article uses **Swift + swift-klib bridge**:
- Swift handles StoreKit (NSObject subclass)
- Kotlin calls Swift via cinterop
- Never exposes NSObject to Koin

Our approach uses **pure Kotlin/Native**:
- Kotlin handles StoreKit directly
- Private helper class extends NSObject
- Wrapper pattern hides NSObject from Koin

Both achieve the same goal: **Keep NSObject subclasses out of Koin's KClass system**.

## Benefits of Our Approach

### ✅ Advantages

1. **No Swift code needed** - Pure Kotlin/Native
2. **No swift-klib plugin** - Simpler build setup
3. **Direct StoreKit access** - No bridging overhead
4. **Same file** - Easy to maintain
5. **Type safety** - All in Kotlin type system

### ⚠️ Limitations

1. **Kotlin/Native StoreKit API** - Uses older StoreKit 1 (StoreKit 2 is Swift-only)
2. **Manual interop** - Some APIs require manual @OptIn annotations

## Code Changes

### Before (Crashed)

```kotlin
actual class BillingClient : NSObject(), 
    SKProductsRequestDelegateProtocol, 
    SKPaymentTransactionObserverProtocol {
    
    // All implementation here
    actual suspend fun initialize(): Result<Unit> { ... }
}

// In Koin module:
factory { createBillingClient() }  // ❌ Crashes!
```

### After (Fixed)

```kotlin
// Public wrapper - Koin-safe
actual class BillingClient {
    private val helper = StoreKitHelper()
    
    actual suspend fun initialize(): Result<Unit> {
        return helper.initialize()
    }
}

// Private helper - does the real work
private class StoreKitHelper : NSObject(),
    SKProductsRequestDelegateProtocol,
    SKPaymentTransactionObserverProtocol {
    
    // All implementation here
    fun initialize(): Result<Unit> { ... }
}

// In Koin module:
factory { createBillingClient() }  // ✅ Works!
```

## Testing

After this fix:

1. ✅ App launches without crash
2. ✅ Koin initializes successfully
3. ✅ BillingClient can be injected
4. ✅ StoreKit functionality works
5. ✅ Same API as Android version

## Alternative Approaches Considered

### 1. Swift Bridge (Medium Article Approach)
```swift
// SubscriptionManager.swift
@objc public class SubscriptionManager: NSObject {
    // StoreKit code here
}
```

```kotlin
// Kotlin side
@OptIn(ExperimentalForeignApi::class)
actual class BillingClient {
    private val swiftHelper = SubscriptionManager.shared()
}
```

**Rejected because**: Requires Swift code, swift-klib plugin, and more complex build setup.

### 2. Factory Function with Type Erasure
```kotlin
fun createBillingClient(): Any {
    return BillingClientImpl()  // Returns as Any
}
```

**Rejected because**: Loses type safety and still requires casting.

### 3. Interface-Based Injection
```kotlin
interface IBillingClient { ... }
class BillingClientImpl : NSObject(), IBillingClient { ... }
```

**Rejected because**: Koin still tries to resolve the implementation class.

## Conclusion

The **wrapper pattern** is the cleanest solution:
- ✅ Pure Kotlin/Native
- ✅ No build complexity
- ✅ Koin-compatible
- ✅ Type-safe
- ✅ Maintainable

This is a common pattern for Kotlin/Native when working with Objective-C classes in dependency injection frameworks.

## Related Issues

- [Kotlin/Native KClass limitation](https://youtrack.jetbrains.com/issue/KT-44249)
- [Koin iOS compatibility](https://insert-koin.io/docs/reference/koin-mp/kmp/)

## References

- [Medium: KMM Subscriptions](https://medium.com/@boobalaninfo/kotlin-multiplatform-kmm-subscriptions-how-to-implement-in-app-subscriptions-for-android-and-ios-4cd18edfbe49)
- [StoreKit Documentation](https://developer.apple.com/documentation/storekit)
- [Kotlin/Native Objective-C Interop](https://kotlinlang.org/docs/native-objc-interop.html)
