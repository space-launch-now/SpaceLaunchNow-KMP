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

if [ -z "$API_KEY" ]; then
    echo "❌ Error: API_KEY not found in .env file"
    exit 1
fi

# Create Secrets.plist
cat > "$SECRETS_PLIST" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>apiKey</key>
	<string>$API_KEY</string>
</dict>
</plist>
EOF

echo "✅ Successfully created Secrets.plist with API key from .env"
echo "📁 Location: $SECRETS_PLIST"
