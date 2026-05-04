# Pinned Content via Firebase Remote Config

This feature allows featuring a launch or event at the top of the home screen without requiring an app update. The content is configured via Firebase Remote Config.

## Remote Config Key

**Key:** `pinned_content`

**Type:** String (JSON)

## JSON Schema

```json
{
  "type": "LAUNCH" | "EVENT" | "MESSAGE_OF_THE_DAY",
  "id": "<string>",
  "enabled": true | false,
  "expiresAt": "<ISO-8601 datetime>",
  "customMessage": "<optional string>"
}
```

### Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `type` | String | Yes | Content type: `"LAUNCH"`, `"EVENT"`, or `"MESSAGE_OF_THE_DAY"` |
| `id` | String | Yes | UUID for launches, integer ID (as string) for events, any unique string for MOTD (used as the dismissal key) |
| `enabled` | Boolean | No | Whether to show (default: `true`) |
| `expiresAt` | String | No | ISO-8601 date after which content auto-hides |
| `customMessage` | String | No | Custom text shown on the card instead of mission name. **Required for `MESSAGE_OF_THE_DAY`** — the banner renders nothing if absent. |

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

## Example: Message of the Day

Use this to show a slim, full-width informational banner above the home screen hero card. Unlike `LAUNCH`/`EVENT`, the MOTD banner has no image, is not tappable, and the entire payload is the `customMessage` text.

```json
{
  "type": "MESSAGE_OF_THE_DAY",
  "id": "motd-2026-05-welcome",
  "enabled": true,
  "expiresAt": "2026-06-01T00:00:00Z",
  "customMessage": "Welcome back! SpaceLaunchNow now tracks 50,000+ launches. Explore the new history feature!"
}
```

### Notes

- `id` is an arbitrary unique string. It is not looked up against any API — it is only used as the dismissal key, so changing it makes the banner reappear for users who previously dismissed it.
- `customMessage` is **required**; the banner silently renders nothing if it is missing or empty.
- Text is capped at 3 lines on screen (overflow truncates with an ellipsis), so keep messages short.
- The banner uses the Material `secondaryContainer` color and is rendered by [`MessageOfTheDayBanner`](../../composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/home/components/PinnedContentCard.kt).

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
1. **Pinned Content** (from Remote Config) - Amber-bordered "FEATURED" card for `LAUNCH`/`EVENT`, or a slim secondary-container banner for `MESSAGE_OF_THE_DAY`
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
- `MESSAGE_OF_THE_DAY` with missing/empty `customMessage` → Banner renders nothing (no API lookup is performed for MOTD)

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

### LAUNCH / EVENT card
- **Amber border** (color: `#FFB300`)
- **"FEATURED" chip** in amber with black text
- **Custom message** displayed below the title (if provided)
- **Same layout** as the Live Launch card (130dp height, image thumbnail)
- Tappable — navigates to the launch/event detail screen

### MESSAGE_OF_THE_DAY banner
- Slim, full-width strip rendered above the hero card (no image)
- Material `secondaryContainer` background with an info icon and dismiss (X) button
- Not tappable — message text only, capped at 3 lines
