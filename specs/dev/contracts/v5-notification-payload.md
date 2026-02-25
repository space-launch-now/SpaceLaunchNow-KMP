# V5 Notification Payload Contract

**Version**: 1.0.0  
**Date**: 2026-01-26

## Overview

This document defines the contract for V5 notification payloads sent from the server to mobile clients via Firebase Cloud Messaging (FCM).

## Message Format

### Android (Data-Only Message)

FCM message MUST NOT include a `notification` block. All data goes in `data` payload.

```json
{
  "message": {
    "topic": "prod_v5_android",
    "data": {
      "notification_type": "string",
      "title": "string",
      "body": "string",
      "launch_uuid": "string",
      "launch_id": "string",
      "launch_name": "string",
      "launch_image": "string",
      "launch_net": "string",
      "launch_location": "string",
      "webcast": "string",
      "webcast_live": "string",
      "lsp_id": "string",
      "location_id": "string",
      "program_ids": "string",
      "status_id": "string",
      "orbit_id": "string",
      "mission_type_id": "string",
      "launcher_family_id": "string"
    }
  }
}
```

### iOS (Alert with Mutable Content)

FCM message includes APNs alert with `mutable-content: 1` for NSE interception.

```json
{
  "message": {
    "topic": "prod_v5_ios",
    "notification": {
      "title": "string",
      "body": "string"
    },
    "apns": {
      "payload": {
        "aps": {
          "mutable-content": 1,
          "content-available": 1
        }
      }
    },
    "data": {
      "notification_type": "string",
      "launch_uuid": "string",
      "launch_id": "string",
      "launch_name": "string",
      "launch_image": "string",
      "launch_net": "string",
      "launch_location": "string",
      "webcast": "string",
      "webcast_live": "string",
      "lsp_id": "string",
      "location_id": "string",
      "program_ids": "string",
      "status_id": "string",
      "orbit_id": "string",
      "mission_type_id": "string",
      "launcher_family_id": "string"
    }
  }
}
```

## Field Definitions

### Required Fields

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `notification_type` | string | Type of notification | `"tenMinutes"` |
| `title` | string | Notification title | `"🚀 SpaceX Falcon 9"` |
| `body` | string | Notification body | `"Launch in 10 minutes from Florida"` |
| `launch_uuid` | string | UUID for deep linking | `"550e8400-e29b-41d4-a716-446655440000"` |
| `launch_id` | string | Library ID (legacy) | `"12345"` |
| `launch_name` | string | Launch display name | `"SpaceX Falcon 9 Block 5 \| Starlink 4-20"` |
| `launch_net` | string | ISO 8601 datetime | `"2026-01-26T14:30:00Z"` |
| `launch_location` | string | Location display name | `"Kennedy Space Center, FL, USA"` |

### Optional Fields

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `launch_image` | string | Image URL | `"https://cdn.example.com/launch.jpg"` |
| `webcast` | string | Has webcast ("true"/"false") | `"true"` |
| `webcast_live` | string | Is live now ("true"/"false") | `"false"` |

### V5 Filtering Fields

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `lsp_id` | string | Launch Service Provider ID | `"121"` |
| `location_id` | string | Launch location ID | `"27"` |
| `program_ids` | string | Comma-separated program IDs | `"1,5,12"` |
| `status_id` | string | Launch status ID | `"1"` |
| `orbit_id` | string | Target orbit ID | `"8"` |
| `mission_type_id` | string | Mission type ID | `"2"` |
| `launcher_family_id` | string | Launcher family ID | `"5"` |

## Notification Types

| Type | Description | Timing |
|------|-------------|--------|
| `tenMinutes` | 10-minute warning | ~10 min before T-0 |
| `oneHour` | 1-hour warning | ~60 min before T-0 |
| `twentyFourHour` | 24-hour warning | ~24 hrs before T-0 |
| `netstampChanged` | Schedule update | On NET change |
| `inFlight` | Liftoff confirmation | At T+0 |
| `success` | Mission success | Post-landing/orbit |
| `failure` | Mission failure | On anomaly |
| `webcastLive` | Webcast started | Webcast go-live |

## ID Reference Tables

### LSP IDs (Launch Service Providers)

| ID | Name |
|----|------|
| 44 | NASA |
| 121 | SpaceX |
| 124 | ULA |
| 141 | Blue Origin |
| 147 | Rocket Lab |
| 115 | Arianespace |
| 111 | Roscosmos |
| 257 | Northrop Grumman |
| 88 | CASC (China) |
| 31 | ISRO (India) |

### Location IDs

| ID | Name |
|----|------|
| 11 | Vandenberg SFB, CA |
| 12 | Cape Canaveral SFS, FL |
| 27 | Kennedy Space Center, FL |
| 143 | SpaceX Starbase, TX |
| 15 | Baikonur Cosmodrome |
| 13 | Guiana Space Centre |
| 10 | Rocket Lab LC-1, NZ |
| 24 | Tanegashima, Japan |
| 14 | Satish Dhawan, India |
| 17 | Jiuquan, China |

## Topics

### Platform Topics

| Topic | Platform | Environment |
|-------|----------|-------------|
| `prod_v5_android` | Android | Production |
| `debug_v5_android` | Android | Debug/Dev |
| `prod_v5_ios` | iOS | Production |
| `debug_v5_ios` | iOS | Debug/Dev |

### Type Topics (for server-side condition filtering)

```
tenMinutes, oneHour, twentyFourHour, netstampChanged, inFlight, success, failure, webcastLive
```

## Example Payloads

### 10-Minute Warning (Android)

```json
{
  "message": {
    "topic": "prod_v5_android",
    "data": {
      "notification_type": "tenMinutes",
      "title": "🚀 SpaceX Falcon 9",
      "body": "Launch in 10 minutes from Kennedy Space Center",
      "launch_uuid": "550e8400-e29b-41d4-a716-446655440000",
      "launch_id": "12345",
      "launch_name": "SpaceX Falcon 9 Block 5 | Starlink 4-20",
      "launch_image": "https://spacelaunchnow-prod-east.nyc3.cdn.digitaloceanspaces.com/media/launch_images/falcon2520925_image_20210801145048.jpeg",
      "launch_net": "2026-01-26T14:30:00Z",
      "launch_location": "Kennedy Space Center, FL, USA",
      "webcast": "true",
      "webcast_live": "false",
      "lsp_id": "121",
      "location_id": "27",
      "program_ids": "1",
      "status_id": "1",
      "orbit_id": "8",
      "mission_type_id": "2",
      "launcher_family_id": "5"
    }
  }
}
```

### Webcast Live (iOS)

```json
{
  "message": {
    "topic": "prod_v5_ios",
    "notification": {
      "title": "🔴 SpaceX Falcon 9",
      "body": "Webcast is now LIVE!"
    },
    "apns": {
      "payload": {
        "aps": {
          "mutable-content": 1,
          "content-available": 1,
          "sound": "default"
        }
      }
    },
    "data": {
      "notification_type": "webcastLive",
      "launch_uuid": "550e8400-e29b-41d4-a716-446655440000",
      "launch_id": "12345",
      "launch_name": "SpaceX Falcon 9 Block 5 | Starlink 4-20",
      "launch_image": "https://example.com/launch.jpg",
      "launch_net": "2026-01-26T14:30:00Z",
      "launch_location": "Kennedy Space Center, FL, USA",
      "webcast": "true",
      "webcast_live": "true",
      "lsp_id": "121",
      "location_id": "27",
      "program_ids": "1",
      "status_id": "1",
      "orbit_id": "8",
      "mission_type_id": "2",
      "launcher_family_id": "5"
    }
  }
}
```

## Backward Compatibility

V5 clients MUST also support V4 payloads during migration period:

**V4 Detection**: Payload has `agency_id` but NOT `lsp_id`

**V4 → V5 Field Mapping**:
- `agency_id` → `lsp_id` (same values)
- `location_id` → `location_id` (unchanged)
- Other V5 fields → null/empty (not present in V4)
