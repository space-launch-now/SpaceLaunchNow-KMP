# iOS Secrets Generation Script

This script automatically generates the `Secrets.plist` file for iOS from your `.env` file.

## Usage

```bash
./scripts/generate-ios-secrets.sh
```

## What it does

1. Reads the `API_KEY` from `.env` file
2. Generates `iosApp/iosApp/Secrets.plist` with the API key
3. Creates a properly formatted plist file that iOS can read

## When to run

- After cloning the repository (first time setup)
- When your API key changes in `.env`
- If `Secrets.plist` is accidentally deleted

## Requirements

- `.env` file must exist in project root
- `.env` must contain `API_KEY=your_key_here`

## After running

You must add `Secrets.plist` to your Xcode project:

1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Right-click on the `iosApp` folder
3. Select "Add Files to 'iosApp'..."
4. Navigate to `iosApp/iosApp/Secrets.plist`
5. ✅ Check "Add to targets: iosApp"
6. Click "Add"

## Example

```bash
$ ./scripts/generate-ios-secrets.sh
✅ Successfully created Secrets.plist with API key from .env
📁 Location: /Users/you/SpaceLaunchNow-KMP/iosApp/iosApp/Secrets.plist
```

## See Also

- [iOS API Key Setup Guide](../docs/IOS_API_KEY_SETUP.md)
