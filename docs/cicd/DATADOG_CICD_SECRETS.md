# Datadog CI/CD Secrets Setup

## Required GitHub Secrets

To enable Datadog logging in CI/CD builds, you need to add the following secrets to your GitHub repository:

### Navigate to Repository Settings

1. Go to your repository on GitHub
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**

### Add These Secrets:

#### DATADOG_ENABLED
- **Value**: `true`
- **Description**: Enables/disables Datadog logging globally

#### DATADOG_CLIENT_TOKEN  
- **Value**: `<YOUR_DATADOG_CLIENT_TOKEN>`
- **Description**: Your Datadog client token for authentication
- **Where to find**: Datadog Dashboard → Organization Settings → Client Tokens

#### DATADOG_APPLICATION_ID
- **Value**: `<YOUR_DATADOG_APP_ID>`  
- **Description**: Your Datadog RUM application ID
- **Where to find**: Datadog Dashboard → RUM Applications → Your App → Application Details

#### DATADOG_ENVIRONMENT
- **Value**: `production` (or `development`, `staging`, etc.)
- **Description**: The environment name for filtering logs in Datadog

## Workflow Files Updated

The following CI/CD workflow files have been updated to include Datadog configuration:

- `.github/workflows/release-main.yml` (2 occurrences)
- `.github/workflows/pr-validation.yml`
- `.github/workflows/release-ios.yml`

## How It Works

1. When a CI build runs, the workflow creates a `.env` file with all secrets
2. The Gradle build reads from `.env` and generates `BuildConfig` fields
3. The app reads `BuildConfig.DATADOG_ENABLED` and initializes Datadog if true
4. All logging from `DatadogLogger` is sent to your Datadog dashboard

## Testing

After adding the secrets:

1. Push a commit to trigger a CI build
2. Check the build logs for: `Datadog Enabled: true`
3. Check your Datadog Logs Explorer for logs from the build
4. Look for logs tagged with:
   - `service:space-launch-now`
   - `env:production` (or whatever you set)

## Environments

You can use different Datadog environments for different workflows:

- **Production builds**: `DATADOG_ENVIRONMENT=production`
- **PR validation**: `DATADOG_ENVIRONMENT=development`
- **iOS builds**: `DATADOG_ENVIRONMENT=ios-production`

This allows you to filter logs by environment in Datadog.

## Security Notes

- ✅ Client tokens are safe to include in builds (they're meant for client apps)
- ✅ Application IDs are not sensitive
- ⚠️ Keep the `DATADOG_ENABLED` toggle to control costs if needed
- ⚠️ Don't commit `.env` file to git (already in `.gitignore`)

## Disabling Datadog in CI

If you need to temporarily disable Datadog logging in CI builds:

1. Go to GitHub Secrets
2. Update `DATADOG_ENABLED` to `false`
3. Next build will skip Datadog initialization

## Local Development

For local development, your `.env` file should already have:

```properties
DATADOG_ENABLED=true
DATADOG_CLIENT_TOKEN=<YOUR_DATADOG_CLIENT_TOKEN>
DATADOG_APPLICATION_ID=<YOUR_DATADOG_APP_ID>
DATADOG_ENVIRONMENT=development
```

This is separate from CI/CD secrets and only used when building locally.
