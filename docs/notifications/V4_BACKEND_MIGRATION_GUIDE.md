# Backend Migration Guide for v4 Notifications

## Overview

This guide helps backend developers migrate the notification system from v3 (topic-based) to v4 (client-side filtering).

## Changes Required

### 1. New FCM Topics

**Old Topics (v3)**: `prod_v3`, `debug_v3` + 30+ filter topics
**New Topics (v4)**: `k_prod_v4`, `k_debug_v4` ONLY

```python
# Old v3 system
TOPICS = {
    'version': 'prod_v3',
    'agencies': ['spacex', 'nasa', 'blueOrigin', ...],
    'locations': ['ksc', 'vandenberg', 'texas', ...],
    'timing': ['twentyFourHour', 'oneHour', 'tenMinutes', ...],
    'matching': ['all', 'strict', 'not_strict'],
    # ... 30+ topics total
}

# New v4 system
TOPICS = {
    'version': 'k_prod_v4',  # That's it!
}
```

### 2. Updated Data Payload

All notifications must include this data structure:

```python
def build_v4_notification_data(launch, notification_type):
    """Build v4 notification data payload with all filtering information"""
    
    # Determine webcast availability
    webcast = launch.webcast_live or bool(launch.vid_urls.first())
    
    # Get agency and location IDs
    agency_id = get_agency_id(launch)
    location_id = get_location_id(launch)
    
    # Get launch image URL
    image = get_launch_image_url(launch)
    
    return {
        "notification_type": notification_type,  # e.g., "twentyFourHour", "oneHour"
        "launch_id": str(launch.launch_library_id),
        "launch_uuid": str(launch.id),
        "launch_name": launch.name,
        "launch_image": image,
        "launch_net": launch.net.strftime("%B %d, %Y %H:%M:%S %Z"),
        "launch_location": launch.pad.location.name,
        "webcast": str(webcast).lower(),  # "true" or "false"
        "agency_id": str(agency_id),
        "location_id": str(location_id),
    }
```

### 3. Helper Functions

```python
def get_agency_id(launch):
    """Get agency ID for the launch's LSP (Launch Service Provider)"""
    if launch.launch_service_provider:
        return launch.launch_service_provider.id
    return 0  # Unknown agency

def get_location_id(launch):
    """Get location ID for the launch pad"""
    if launch.pad and launch.pad.location:
        return launch.pad.location.id
    return 20  # "Other" location

def get_launch_image_url(launch):
    """Get the best available image for the launch"""
    if launch.image:
        return launch.image.image_url
    if launch.rocket and launch.rocket.configuration and launch.rocket.configuration.image_url:
        return launch.rocket.configuration.image_url
    return None  # No image available
```

### 4. Updated Notification Sending

```python
def send_launch_notification_v4(launch, notification_type):
    """
    Send notification using v4 system (client-side filtering)
    
    Args:
        launch: Launch object
        notification_type: Type of notification (e.g., "twentyFourHour", "oneHour")
    """
    
    # Build v4 data payload
    v4_data = build_v4_notification_data(launch, notification_type)
    
    # Determine which topic to use
    topic = get_notification_topic()  # Returns "k_prod_v4" or "k_debug_v4"
    
    # Build FCM message
    message = messaging.Message(
        notification=messaging.Notification(
            title=build_notification_title(launch, notification_type),
            body=build_notification_body(launch, notification_type),
        ),
        data=v4_data,  # Include all data for client-side filtering
        topic=topic,
        android=messaging.AndroidConfig(
            priority='high',
            notification=messaging.AndroidNotification(
                channel_id='space_launch_notifications',
                icon='ic_notification',
            ),
        ),
        apns=messaging.APNSConfig(
            headers={'apns-priority': '10'},
            payload=messaging.APNSPayload(
                aps=messaging.Aps(
                    alert=messaging.ApsAlert(
                        title=build_notification_title(launch, notification_type),
                        body=build_notification_body(launch, notification_type),
                    ),
                    sound='default',
                    badge=1,
                ),
            ),
        ),
    )
    
    # Send notification
    response = messaging.send(message)
    return response

def get_notification_topic():
    """Get the appropriate notification topic based on environment"""
    if settings.DEBUG:
        return "k_debug_v4"
    return "k_prod_v4"

def build_notification_title(launch, notification_type):
    """Build notification title based on type"""
    lsp_name = launch.launch_service_provider.name if launch.launch_service_provider else "Unknown"
    rocket_name = launch.rocket.configuration.name if launch.rocket and launch.rocket.configuration else "Unknown"
    
    return f"{lsp_name} | {rocket_name}"

def build_notification_body(launch, notification_type):
    """Build notification body based on type"""
    type_messages = {
        "twentyFourHour": "Launching in 24 hours",
        "oneHour": "Launching in 1 hour",
        "tenMinutes": "Launching in 10 minutes",
        "oneMinute": "Launching in 1 minute",
        "inFlight": "Launch is in progress",
        "success": "Launch successful!",
        "netstampChanged": "Launch time updated",
    }
    
    message = type_messages.get(notification_type, "Launch notification")
    location = launch.pad.location.name if launch.pad and launch.pad.location else "Unknown location"
    
    return f"{message} from {location}"
```

## Notification Type Reference

Map your existing notification events to these types:

| Event | notification_type | Description |
|-------|------------------|-------------|
| T-24 hours | `twentyFourHour` | 24 hours before launch |
| T-1 hour | `oneHour` | 1 hour before launch |
| T-10 minutes | `tenMinutes` | 10 minutes before launch |
| T-1 minute | `oneMinute` | 1 minute before launch |
| In-flight | `inFlight` | Launch is happening now |
| Success | `success` | Launch completed successfully |
| Time changed | `netstampChanged` | Launch time was updated |
| Event | `event` | Space event notification |

## Agency ID Reference

Major agencies with their IDs:

```python
AGENCY_IDS = {
    'SpaceX': 121,
    'NASA': 44,
    'Blue Origin': 141,
    'Rocket Lab': 147,
    'ULA': 124,
    'Arianespace': 115,
    'Roscosmos': 111,
    'Northrop Grumman': 257,
    # See Launch Library API for complete list
}
```

## Location ID Reference

Major launch locations with their IDs:

```python
LOCATION_IDS = {
    'Vandenberg SFB': 11,
    'Kennedy Space Center': 27,
    'Wallops': 21,
    'Starbase Texas': 143,
    'Baikonur': 15,
    'Guiana Space Centre': 13,
    'Rocket Lab LC': 10,
    'Tanegashima': 24,
    'Satish Dhawan': 14,
    'Jiuquan': 17,
    'Kodiak': 25,
    'Other': 20,
    # See Launch Library API for complete list
}
```

## Migration Strategy

### Phase 1: Dual System (RECOMMENDED)
Run both v3 and v4 systems simultaneously:

```python
def send_launch_notification(launch, notification_type):
    """Send notification to both v3 and v4 systems"""
    
    # Send v3 (old system - to specific topics)
    send_v3_notification(launch, notification_type)
    
    # Send v4 (new system - to version topic with data)
    send_v4_notification(launch, notification_type)
```

**Benefits**:
- ✅ Old clients (v3) continue working
- ✅ New clients (v4) get new system
- ✅ Gradual migration
- ✅ Easy rollback

### Phase 2: v4 Only
After all users upgrade, switch to v4 only:

```python
def send_launch_notification(launch, notification_type):
    """Send notification using v4 system only"""
    send_v4_notification(launch, notification_type)
```

## Testing

### 1. Test Notification Format

```python
# Example test data
test_data = {
    "notification_type": "twentyFourHour",
    "launch_id": "999",
    "launch_uuid": "550e8400-e29b-41d4-a716-446655440000",
    "launch_name": "Falcon 9 Block 5 | Starlink Group 6-30",
    "launch_image": "https://example.com/image.jpg",
    "launch_net": "October 13, 2025 14:30:00 UTC",
    "launch_location": "Kennedy Space Center, FL, USA",
    "webcast": "true",
    "agency_id": "121",  # SpaceX
    "location_id": "27"   # KSC
}
```

### 2. Send Test Notification

```python
import firebase_admin
from firebase_admin import messaging

def send_test_notification():
    """Send test notification to k_debug_v4 topic"""
    
    message = messaging.Message(
        notification=messaging.Notification(
            title="SpaceX | Falcon 9 Block 5",
            body="Launching in 24 hours from Kennedy Space Center",
        ),
        data={
            "notification_type": "twentyFourHour",
            "launch_id": "999",
            "launch_uuid": "550e8400-e29b-41d4-a716-446655440000",
            "launch_name": "Falcon 9 Block 5 | Starlink Group 6-30",
            "launch_image": "https://example.com/image.jpg",
            "launch_net": "October 13, 2025 14:30:00 UTC",
            "launch_location": "Kennedy Space Center, FL, USA",
            "webcast": "true",
            "agency_id": "121",
            "location_id": "27",
        },
        topic="k_debug_v4",
    )
    
    response = messaging.send(message)
    print(f"Successfully sent message: {response}")
```

### 3. Verify in Firebase Console

1. Go to Firebase Console → Cloud Messaging
2. Click "Send test message"
3. Enter topic: `k_debug_v4`
4. Add all data fields as custom data
5. Send and verify client receives and filters correctly

## Data Validation

Add validation to ensure data quality:

```python
def validate_v4_notification_data(data):
    """Validate v4 notification data before sending"""
    
    required_fields = [
        'notification_type',
        'launch_id',
        'launch_uuid',
        'launch_name',
        'launch_net',
        'launch_location',
        'webcast',
        'agency_id',
        'location_id',
    ]
    
    # Check all required fields present
    missing = [f for f in required_fields if f not in data]
    if missing:
        raise ValueError(f"Missing required fields: {missing}")
    
    # Validate notification_type
    valid_types = [
        'twentyFourHour', 'oneHour', 'tenMinutes', 'oneMinute',
        'inFlight', 'success', 'netstampChanged', 'event'
    ]
    if data['notification_type'] not in valid_types:
        raise ValueError(f"Invalid notification_type: {data['notification_type']}")
    
    # Validate webcast boolean string
    if data['webcast'] not in ['true', 'false']:
        raise ValueError(f"webcast must be 'true' or 'false', got: {data['webcast']}")
    
    # Validate IDs are numeric strings
    try:
        int(data['agency_id'])
        int(data['location_id'])
    except ValueError:
        raise ValueError("agency_id and location_id must be numeric strings")
    
    return True
```

## Monitoring

Add logging to track v4 notifications:

```python
import logging

logger = logging.getLogger(__name__)

def send_v4_notification_with_logging(launch, notification_type):
    """Send v4 notification with comprehensive logging"""
    
    try:
        # Build data
        v4_data = build_v4_notification_data(launch, notification_type)
        
        # Validate
        validate_v4_notification_data(v4_data)
        
        # Log
        logger.info(
            f"Sending v4 notification: type={notification_type}, "
            f"launch_id={v4_data['launch_id']}, "
            f"agency={v4_data['agency_id']}, "
            f"location={v4_data['location_id']}"
        )
        
        # Send
        response = send_launch_notification_v4(launch, notification_type)
        
        # Log success
        logger.info(f"v4 notification sent successfully: {response}")
        
        return response
        
    except Exception as e:
        logger.error(f"Failed to send v4 notification: {e}", exc_info=True)
        raise
```

## Rollback Plan

If v4 has issues:

1. **Keep v3 running**: Continue sending to old topics
2. **Stop v4**: Stop sending to `k_prod_v4`
3. **Client rollback**: Users can downgrade app version
4. **Data preserved**: v3 topic subscriptions still work

## Questions?

Contact the mobile team with questions about:
- Data format requirements
- Client-side filtering logic
- Testing procedures
- Migration timeline
