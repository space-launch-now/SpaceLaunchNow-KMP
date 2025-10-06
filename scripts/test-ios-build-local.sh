#!/bin/bash
set -e

# Local iOS Build Test Script
# This mimics what GitHub Actions does, but runs locally on your Mac
# Saves you $20/day in CI costs!

echo "🧪 Testing iOS build locally..."
echo ""

# Configuration (update these for your setup)
TEAM_ID="your_team_id_here"  # Replace with your Apple Team ID
BUNDLE_ID="your.bundle.id"    # Replace with your bundle ID
PROVISIONING_PROFILE_PATH="$HOME/Desktop/sln_kmp.mobileprovision"  # Path to your profile

# Validate prerequisites
if [ ! -f "$PROVISIONING_PROFILE_PATH" ]; then
    echo "❌ Provisioning profile not found at: $PROVISIONING_PROFILE_PATH"
    echo "   Download it from Apple Developer Console and update the path above"
    exit 1
fi

# Step 1: Install provisioning profile
echo "1️⃣  Installing provisioning profile..."
PROFILE_CONTENT=$(security cms -D -i "$PROVISIONING_PROFILE_PATH")
PROFILE_UUID=$(echo "$PROFILE_CONTENT" | plutil -extract UUID xml1 - -o - | xmllint --xpath "//string/text()" -)
PROFILE_NAME=$(echo "$PROFILE_CONTENT" | plutil -extract Name xml1 - -o - | xmllint --xpath "//string/text()" -)

mkdir -p ~/Library/MobileDevice/Provisioning\ Profiles
cp "$PROVISIONING_PROFILE_PATH" ~/Library/MobileDevice/Provisioning\ Profiles/$PROFILE_UUID.mobileprovision

echo "   ✅ Installed: $PROFILE_NAME (UUID: $PROFILE_UUID)"

# Step 2: Find certificate
echo "2️⃣  Finding distribution certificate..."
CERT_IDENTITY=$(security find-identity -v -p codesigning | grep -E "(Apple Distribution|iOS Distribution)" | head -n1 | grep -o '"[^"]*"' | tr -d '"')
echo "   ✅ Using: $CERT_IDENTITY"

# Step 3: Generate Secrets.plist
echo "3️⃣  Generating Secrets.plist..."
cd iosApp
cat > Secrets.plist << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>API_KEY</key>
    <string>your_api_key_here</string>
</dict>
</plist>
EOF
echo "   ✅ Created Secrets.plist"

# Step 4: Update Config.xcconfig
cat > Configuration/Config.xcconfig << EOF
TEAM_ID=$TEAM_ID
BUNDLE_ID=$BUNDLE_ID
APP_NAME=SpaceLaunchNow
EOF
echo "   ✅ Updated Config.xcconfig"

# Step 5: Build archive
echo "4️⃣  Building archive (this takes ~25 minutes)..."
xcrun xcodebuild \
    -project iosApp.xcodeproj \
    -scheme iosApp \
    -configuration Release \
    -sdk iphoneos \
    -archivePath "$HOME/Desktop/SpaceLaunchNow.xcarchive" \
    -destination "generic/platform=iOS" \
    DEVELOPMENT_TEAM="$TEAM_ID" \
    PRODUCT_BUNDLE_IDENTIFIER="$BUNDLE_ID" \
    CODE_SIGN_STYLE="Manual" \
    PROVISIONING_PROFILE_SPECIFIER="$PROFILE_UUID" \
    CODE_SIGN_IDENTITY="$CERT_IDENTITY" \
    archive

echo "   ✅ Archive created at: $HOME/Desktop/SpaceLaunchNow.xcarchive"

# Step 6: Create ExportOptions.plist
echo "5️⃣  Creating ExportOptions.plist..."
cat > "$HOME/Desktop/ExportOptions.plist" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>method</key>
    <string>app-store</string>
    <key>teamID</key>
    <string>$TEAM_ID</string>
    <key>provisioningProfiles</key>
    <dict>
        <key>$BUNDLE_ID</key>
        <string>$PROFILE_UUID</string>
    </dict>
    <key>signingStyle</key>
    <string>manual</string>
    <key>signingCertificate</key>
    <string>Apple Distribution</string>
    <key>uploadBitcode</key>
    <false/>
    <key>uploadSymbols</key>
    <true/>
</dict>
</plist>
EOF

# Step 7: Export IPA
echo "6️⃣  Exporting IPA..."
xcrun xcodebuild \
    -exportArchive \
    -exportOptionsPlist "$HOME/Desktop/ExportOptions.plist" \
    -archivePath "$HOME/Desktop/SpaceLaunchNow.xcarchive" \
    -exportPath "$HOME/Desktop/Export/"

echo ""
echo "✅ SUCCESS! IPA exported to: $HOME/Desktop/Export/SpaceLaunchNow.ipa"
echo ""
echo "📋 Next steps:"
echo "   1. If export failed, check the error message above"
echo "   2. If successful, the ExportOptions.plist is the key - compare with your GitHub secret"
echo "   3. Update your IOS_EXPORT_OPTIONS_PLIST secret with the working version"
echo ""
