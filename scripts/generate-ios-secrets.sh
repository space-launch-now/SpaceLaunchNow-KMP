#!/bin/bash

# Script to generate iOS Secrets.plist from .env file
# This should be run whenever the .env file is updated

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."
ENV_FILE="$PROJECT_ROOT/.env"
SECRETS_PLIST="$PROJECT_ROOT/iosApp/iosApp/Secrets.plist"
TEMPLATE_FILE="$PROJECT_ROOT/iosApp/iosApp/Secrets.plist.template"
VERSION_PROPS="$PROJECT_ROOT/version.properties"

# Check if .env file exists
if [ ! -f "$ENV_FILE" ]; then
    echo "❌ Error: .env file not found at $ENV_FILE"
    echo "Please create a .env file with your API_KEY"
    exit 1
fi

# Check if version.properties exists
if [ ! -f "$VERSION_PROPS" ]; then
    echo "❌ Error: version.properties not found at $VERSION_PROPS"
    exit 1
fi

# Read API_KEY from .env
API_KEY=$(grep "^API_KEY=" "$ENV_FILE" | cut -d '=' -f2- | tr -d '"' | tr -d "'")
MAPS_API_KEY=$(grep "^MAPS_API_KEY=" "$ENV_FILE" | cut -d '=' -f2- | tr -d '"' | tr -d "'")
REVENUECAT_ANDROID_KEY=$(grep "^REVENUECAT_ANDROID_KEY=" "$ENV_FILE" | cut -d '=' -f2- | tr -d '"' | tr -d "'")
REVENUECAT_IOS_KEY=$(grep "^REVENUECAT_IOS_KEY=" "$ENV_FILE" | cut -d '=' -f2- | tr -d '"' | tr -d "'")
ANDROID_BANNER_AD_UNIT_ID=$(grep "^ANDROID_BANNER_AD_UNIT_ID=" "$ENV_FILE" | cut -d '=' -f2- | tr -d '"' | tr -d "'")
IOS_BANNER_AD_UNIT_ID=$(grep "^IOS_BANNER_AD_UNIT_ID=" "$ENV_FILE" | cut -d '=' -f2- | tr -d '"' | tr -d "'")
ANDROID_INTERSTITIAL_AD_UNIT_ID=$(grep "^ANDROID_INTERSTITIAL_AD_UNIT_ID=" "$ENV_FILE" | cut -d '=' -f2- | tr -d '"' | tr -d "'")
IOS_INTERSTITIAL_AD_UNIT_ID=$(grep "^IOS_INTERSTITIAL_AD_UNIT_ID=" "$ENV_FILE" | cut -d '=' -f2- | tr -d '"' | tr -d "'")
ANDROID_REWARDED_AD_UNIT_ID=$(grep "^ANDROID_REWARDED_AD_UNIT_ID=" "$ENV_FILE" | cut -d '=' -f2- | tr -d '"' | tr -d "'")
IOS_REWARDED_AD_UNIT_ID=$(grep "^IOS_REWARDED_AD_UNIT_ID=" "$ENV_FILE" | cut -d '=' -f2- | tr -d '"' | tr -d "'")
DEBUG=$(grep "^DEBUG=" "$ENV_FILE" | cut -d '=' -f2- | tr -d '"' | tr -d "'")
TOTP_SECRET=$(grep "^TOTP_SECRET=" "$ENV_FILE" | cut -d '=' -f2- | tr -d '"' | tr -d "'")

# Read version info from version.properties
VERSION_MAJOR=$(grep "^versionMajor=" "$VERSION_PROPS" | cut -d '=' -f2)
VERSION_MINOR=$(grep "^versionMinor=" "$VERSION_PROPS" | cut -d '=' -f2)
VERSION_PATCH=$(grep "^versionPatch=" "$VERSION_PROPS" | cut -d '=' -f2)
VERSION_BUILD_NUMBER=$(grep "^versionBuildNumber=" "$VERSION_PROPS" | cut -d '=' -f2)

# Compute version name and code (matching build.gradle.kts logic)
VERSION_NAME="${VERSION_MAJOR}.${VERSION_MINOR}.${VERSION_PATCH}-b${VERSION_BUILD_NUMBER}"
VERSION_CODE=$((VERSION_MAJOR * 1000000 + VERSION_MINOR * 100000 + VERSION_PATCH * 10000 + VERSION_BUILD_NUMBER))

if [ -z "$API_KEY" ]; then
    echo "❌ Error: API_KEY not found in .env file"
    exit 1
fi

if [ -z "$MAPS_API_KEY" ]; then
	echo "⚠️  Warning: MAPS_API_KEY not found in .env file"
fi

if [ -z "$REVENUECAT_ANDROID_KEY" ]; then
    echo "⚠️  Warning: REVENUECAT_ANDROID_KEY not found in .env file"
fi

if [ -z "$REVENUECAT_IOS_KEY" ]; then
    echo "⚠️  Warning: REVENUECAT_IOS_KEY not found in .env file"
fi
if [ -z "$ANDROID_BANNER_AD_UNIT_ID" ]; then
    echo "⚠️  Warning: ANDROID_BANNER_AD_UNIT_ID not found in .env file"
fi

if [ -z "$IOS_BANNER_AD_UNIT_ID" ]; then
    echo "⚠️  Warning: IOS_BANNER_AD_UNIT_ID not found in .env file"
fi

if [ -z "$ANDROID_INTERSTITIAL_AD_UNIT_ID" ]; then
    echo "⚠️  Warning: ANDROID_INTERSTITIAL_AD_UNIT_ID not found in .env file"
fi

if [ -z "$IOS_INTERSTITIAL_AD_UNIT_ID" ]; then
    echo "⚠️  Warning: IOS_INTERSTITIAL_AD_UNIT_ID not found in .env file"
fi

if [ -z "$ANDROID_REWARDED_AD_UNIT_ID" ]; then
    echo "⚠️  Warning: ANDROID_REWARDED_AD_UNIT_ID not found in .env file"
fi

if [ -z "$IOS_REWARDED_AD_UNIT_ID" ]; then
    echo "⚠️  Warning: IOS_REWARDED_AD_UNIT_ID not found in .env file"
fi

if [ -z "$DEBUG" ]; then
    echo "⚠️  Warning: DEBUG not found in .env file (defaulting to false for production safety)"
    DEBUG="false"
fi

if [ -z "$TOTP_SECRET" ]; then
    echo "⚠️  Warning: TOTP_SECRET not found in .env file (using default)"
    TOTP_SECRET="JBSWY3DPEHPK3PXP"
fi

# Create Secrets.plist
cat > "$SECRETS_PLIST" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>apiKey</key>
	<string>$API_KEY</string>
	<key>mapsApiKey</key>
	<string>$MAPS_API_KEY</string>
	<key>revenueCatAndroidKey</key>
	<string>$REVENUECAT_ANDROID_KEY</string>
	<key>revenueCatIosKey</key>
	<string>$REVENUECAT_IOS_KEY</string>
	<key>androidBannerAdUnitId</key>
	<string>$ANDROID_BANNER_AD_UNIT_ID</string>
	<key>iosBannerAdUnitId</key>
	<string>$IOS_BANNER_AD_UNIT_ID</string>
	<key>androidInterstitialAdUnitId</key>
	<string>$ANDROID_INTERSTITIAL_AD_UNIT_ID</string>
	<key>iosInterstitialAdUnitId</key>
	<string>$IOS_INTERSTITIAL_AD_UNIT_ID</string>
	<key>androidRewardedAdUnitId</key>
	<string>$ANDROID_REWARDED_AD_UNIT_ID</string>
	<key>iosRewardedAdUnitId</key>
	<string>$IOS_REWARDED_AD_UNIT_ID</string>
    <key>debug</key>
	<string>$DEBUG</string>
	<key>totpSecret</key>
	<string>$TOTP_SECRET</string>
	<key>versionName</key>
	<string>$VERSION_NAME</string>
	<key>versionCode</key>
	<integer>$VERSION_CODE</integer>
</dict>
</plist>
EOF

echo "✅ Successfully created Secrets.plist with keys from .env"
echo "📁 Location: $SECRETS_PLIST"
echo "✅ API_KEY: $([ -n "$API_KEY" ] && echo "Set" || echo "Not set")"
echo "✅ MAPS_API_KEY: $([ -n "$MAPS_API_KEY" ] && echo "Set" || echo "Not set")"
echo "✅ REVENUECAT_ANDROID_KEY: $([ -n "$REVENUECAT_ANDROID_KEY" ] && echo "Set" || echo "Not set")"
echo "✅ REVENUECAT_IOS_KEY: $([ -n "$REVENUECAT_IOS_KEY" ] && echo "Set" || echo "Not set")"
echo "✅ ANDROID_BANNER_AD_UNIT_ID: $([ -n "$ANDROID_BANNER_AD_UNIT_ID" ] && echo "Set" || echo "Not set")"
echo "✅ IOS_BANNER_AD_UNIT_ID: $([ -n "$IOS_BANNER_AD_UNIT_ID" ] && echo "Set" || echo "Not set")"
echo "✅ ANDROID_INTERSTITIAL_AD_UNIT_ID: $([ -n "$ANDROID_INTERSTITIAL_AD_UNIT_ID" ] && echo "Set" || echo "Not set")"
echo "✅ IOS_INTERSTITIAL_AD_UNIT_ID: $([ -n "$IOS_INTERSTITIAL_AD_UNIT_ID" ] && echo "Set" || echo "Not set")"
echo "✅ ANDROID_REWARDED_AD_UNIT_ID: $([ -n "$ANDROID_REWARDED_AD_UNIT_ID" ] && echo "Set" || echo "Not set")"
echo "✅ IOS_REWARDED_AD_UNIT_ID: $([ -n "$IOS_REWARDED_AD_UNIT_ID" ] && echo "Set" || echo "Not set")"
echo "✅ DEBUG: $([ -n "$DEBUG" ] && echo "Set" || echo "Not set")"
echo "✅ TOTP_SECRET: $([ -n "$TOTP_SECRET" ] && echo "Set" || echo "Using default")"
echo "✅ VERSION: $VERSION_NAME (code: $VERSION_CODE)"
