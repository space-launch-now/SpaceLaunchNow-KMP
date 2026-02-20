# API Contracts

**Note**: This feature does not introduce new API contracts. It modifies client-side notification filtering logic only.

## No External API Changes

The V5 notification system uses Firebase Cloud Messaging (FCM) for push notifications. The server-side API contract is already defined and this bug fix does not change it.

## Server-Side Payload Format (Reference)

**Topic**: `prod_v5_android` (production) or `debug_v5_android` (debug)

**Payload Structure** (from server):
```json
{
  "notification_type": "twentyFourHour | tenMinutes | netstampChanged | etc.",
  "title": "Launch Title",
  "body": "Launch description and timing",
  "launch_uuid": "UUID string",
  "launch_id": "Launch ID string",
  "launch_name": "Launch Name",
  "launch_image": "Image URL (optional)",
  "launch_net": "ISO 8601 datetime",
  "launch_location": "Location name",
  "webcast": "true | false",
  "webcast_live": "true | false",
  "lsp_id": "121",          // String! Not Int
  "location_id": "12",      // String! Not Int
  "program_id": "25",       // String! Not Int
  "status_id": "1",         // String! Not Int
  "orbit_id": "8",          // String! Not Int
  "mission_type_id": "10",  // String! Not Int
  "launcher_family_id": "1" // String! Not Int
}
```

**Key Observation**: All fields are String type (FCM limitation).

## Client-Side Data Model (Internal)

See [../data-model.md](../data-model.md) for full details on V5NotificationPayload structure.

---

**No contracts to generate** - This is purely a client-side bug fix.
