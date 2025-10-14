# RevenueCat Configuration

This directory contains the configuration files for RevenueCat integration.

## How to Add Your API Keys

### Step 1: Get Your API Keys from RevenueCat Dashboard

1. Go to your [RevenueCat Dashboard](https://app.revenuecat.com/)
2. Select your project
3. Go to **Project Settings** > **API keys**
4. Copy your **Public API keys** for each platform (iOS and Android)

⚠️ **Important**: Only use **public** API keys, never secret keys in your client app!

### Step 2: Update the Configuration Files

#### For Android:

Edit `RevenueCatConfig.android.kt` and replace:

```kotlin
actual val apiKey: String = "rc_android_your_api_key_here"
```

With your actual Android API key:

```kotlin
actual val apiKey: String = "appl_abc123your_actual_android_key"
```

#### For iOS:

Edit `RevenueCatConfig.ios.kt` and replace:

```kotlin
actual val apiKey: String = "rc_ios_your_api_key_here"
```

With your actual iOS API key:

```kotlin
actual val apiKey: String = "appl_def456your_actual_ios_key"
```

### Step 3: Test Your Integration

After updating the API keys:

1. Build and run your app
2. Check the logs for RevenueCat initialization messages
3. Verify that offerings and customer info are loaded properly

### Example Usage

The RevenueCatManager is automatically initialized when your app starts. You can inject it anywhere
in your app:

```kotlin
@Composable
fun MyScreen() {
    val revenueCatManager = koinInject<RevenueCatManager>()
    val customerInfo by revenueCatManager.customerInfo.collectAsState()
    val currentOffering by revenueCatManager.currentOffering.collectAsState()
    
    // Check if user has premium access
    val isPremium = revenueCatManager.hasEntitlement("premium")
    
    // Use the data in your UI
}
```

### Debug Logs

When running in debug mode, you'll see detailed RevenueCat logs including:

- Initialization status
- Available offerings
- Active entitlements
- Purchase events

Look for logs that start with "RevenueCat:" in your console.