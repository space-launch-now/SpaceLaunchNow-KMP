#!/bin/bash

# Script to generate iOS Secrets.plist from .env file
# This should be run whenever the .env file is updated

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."
ENV_FILE="$PROJECT_ROOT/.env"
SECRETS_PLIST="$PROJECT_ROOT/iosApp/iosApp/Secrets.plist"
TEMPLATE_FILE="$PROJECT_ROOT/iosApp/iosApp/Secrets.plist.template"

# Check if .env file exists
if [ ! -f "$ENV_FILE" ]; then
    echo "❌ Error: .env file not found at $ENV_FILE"
    echo "Please create a .env file with your API_KEY"
    exit 1
fi

# Read API_KEY from .env
API_KEY=$(grep "^API_KEY=" "$ENV_FILE" | cut -d '=' -f2- | tr -d '"' | tr -d "'")
REVENUECAT_ANDROID_KEY=$(grep "^REVENUECAT_ANDROID_KEY=" "$ENV_FILE" | cut -d '=' -f2- | tr -d '"' | tr -d "'")
REVENUECAT_IOS_KEY=$(grep "^REVENUECAT_IOS_KEY=" "$ENV_FILE" | cut -d '=' -f2- | tr -d '"' | tr -d "'")

if [ -z "$API_KEY" ]; then
    echo "❌ Error: API_KEY not found in .env file"
    exit 1
fi

if [ -z "$REVENUECAT_ANDROID_KEY" ]; then
    echo "⚠️  Warning: REVENUECAT_ANDROID_KEY not found in .env file"
fi

if [ -z "$REVENUECAT_IOS_KEY" ]; then
    echo "⚠️  Warning: REVENUECAT_IOS_KEY not found in .env file"
fi

# Create Secrets.plist
cat > "$SECRETS_PLIST" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>apiKey</key>
	<string>$API_KEY</string>
	<key>revenueCatAndroidKey</key>
	<string>$REVENUECAT_ANDROID_KEY</string>
	<key>revenueCatIosKey</key>
	<string>$REVENUECAT_IOS_KEY</string>
</dict>
</plist>
EOF

echo "✅ Successfully created Secrets.plist with keys from .env"
echo "📁 Location: $SECRETS_PLIST"
echo "✅ API_KEY: $([ -n "$API_KEY" ] && echo "Set" || echo "Not set")"
echo "✅ REVENUECAT_ANDROID_KEY: $([ -n "$REVENUECAT_ANDROID_KEY" ] && echo "Set" || echo "Not set")"
echo "✅ REVENUECAT_IOS_KEY: $([ -n "$REVENUECAT_IOS_KEY" ] && echo "Set" || echo "Not set")"
