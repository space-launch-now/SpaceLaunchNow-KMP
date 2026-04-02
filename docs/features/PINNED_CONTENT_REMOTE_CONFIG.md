# Pinned Content via Firebase Remote Config

This feature allows featuring a launch or event at the top of the home screen without requiring an app update. The content is configured via Firebase Remote Config.

## Remote Config Key

**Key:** `pinned_content`

**Type:** String (JSON)

## JSON Schema

```json
{
  "type": "LAUNCH" | "EVENT",
  "id": "<string>",
  "enabled": true | false,
  "expiresAt": "<ISO-8601 datetime>",
  "customMessage": "<optional string>"
}
```

### Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `type` | String | Yes | Content type: `"LAUNCH"` or `"EVENT"` |
| `id` | String | Yes | UUID for launches, integer ID (as string) for events |
| `enabled` | Boolean | No | Whether to show (default: `true`) |
| `expiresAt` | String | No | ISO-8601 date after which content auto-hides |
| `customMessage` | String | No | Custom text shown on the card instead of mission name |

## Example: Featured Launch

Use this to pin a specific launch to the top of the home screen.

```json
{
  "type": "LAUNCH",
  "id": "e3df2ecd-c239-472f-95e4-2b89b4f75800",
  "enabled": true,
  "expiresAt": "2026-04-15T00:00:00Z",
  "customMessage": "Historic 100th Falcon 9 Landing!"
}
```

### Finding Launch IDs

Launch IDs are UUIDs. You can find them:
1. In the app's launch detail URL
2. Via the Launch Library API: `https://ll.thespacedevs.com/2.4.0/launches/?search=falcon`
3. In the `id` field of any launch response

## Example: Featured Event

Use this to pin a specific event (spacewalk, docking, etc.) to the top.

```json
{
  "type": "EVENT",
  "id": "1234",
  "enabled": true,
  "expiresAt": "2026-04-10T12:00:00Z",
  "customMessage": "Watch the ISS Spacewalk Live!"
}
```

### Finding Event IDs

Event IDs are integers. You can find them:
1. Via the Launch Library API: `https://ll.thespacedevs.com/2.4.0/events/`
2. In the `id` field of any event response

> **Note:** Event pinning is not yet fully implemented in the app. Currently only launches are supported.

## Example: Disabled/Empty Config

To show no pinned content, use an empty string or disabled config:

```json
""
```

Or explicitly disabled:

```json
{
  "type": "LAUNCH",
  "id": "e3df2ecd-c239-472f-95e4-2b89b4f75800",
  "enabled": false
}
```

## Example: Minimal Config (No Expiration)

For content that should stay pinned until manually changed:

```json
{
  "type": "LAUNCH",
  "id": "e3df2ecd-c239-472f-95e4-2b89b4f75800",
  "enabled": true
}
```

## Behavior

### Display Priority
Content appears in this order on the home screen:
1. **Pinned Content** (from Remote Config) - Amber border, "FEATURED" chip
2. **In-Flight Launch** (if any) - Blue border, status chip, optional "LIVE" indicator
3. **Featured/Next Up Launch** - Standard hero card

### Deduplication
If the pinned launch is the same as:
- The in-flight launch → Only shown as pinned (not duplicated in in-flight section)
- The featured/hero launch → Only shown as pinned (hero shows next launch instead)

### Expiration
- `expiresAt` is checked client-side on each app load
- After expiration, the pinned content silently disappears
- Use UTC timezone in the ISO-8601 format

### User Dismissal
- Users can dismiss pinned content by tapping the X button in the top-right corner
- Dismissed content IDs are stored locally in app preferences
- The same content won't show again until:
  - The remote config changes to a different content ID
  - The user reinstalls the app (clears preferences)
- Dismissal is per-content-ID, not global

### Error Handling
- Invalid JSON → No pinned content shown (silent failure)
- Launch/Event not found → No pinned content shown
- Network error fetching launch details → No pinned content shown

## Testing

1. In Firebase Console, go to Remote Config
2. Add parameter `pinned_content` with your JSON
3. Publish changes
4. In the app, pull-to-refresh or restart to fetch new config

### Test JSON for Development

```json
{
  "type": "LAUNCH",
  "id": "e3df2ecd-c239-472f-95e4-2b89b4f75800",
  "enabled": true,
  "expiresAt": "2030-12-31T23:59:59Z",
  "customMessage": "Test Featured Launch"
}
```

## Visual Appearance

The pinned content card features:
- **Amber border** (color: `#FFB300`)
- **"FEATURED" chip** in amber with black text
- **Custom message** displayed below the title (if provided)
- **Same layout** as the Live Launch card (130dp height, image thumbnail)
