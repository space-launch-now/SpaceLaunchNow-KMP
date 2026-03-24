# Firebase Remote Config Setup for Roadmap

This guide explains how to configure Firebase Remote Config for the dynamic roadmap feature.

## Prerequisites

- Firebase project already configured (existing `google-services.json`)
- Access to Firebase Console

## Step 1: Enable Remote Config

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select your SpaceLaunchNow project
3. Navigate to **Remote Config** in the left sidebar
4. Click **Create configuration** if first time

## Step 2: Create the Roadmap Parameter

1. Click **Add parameter**
2. Set parameter name: `roadmap_data`
3. Set data type: **String**
4. Add the default value (JSON):

```json
{
  "items": [
    {
      "id": "1",
      "title": "Schedule page Sorting",
      "description": "Customizable schedule page with advanced sorting options",
      "status": "COMPLETED",
      "quarter": "Early December 2025",
      "category": "FEATURE",
      "priority": "HIGH"
    },
    {
      "id": "2",
      "title": "Starship | Rockets | Agencies | ISS Tracking",
      "description": "New pages with additional content",
      "status": "IN_TESTING",
      "quarter": "February 2026",
      "category": "FEATURE",
      "priority": "MEDIUM"
    },
    {
      "id": "3",
      "title": "Notification Filter Issues",
      "description": "Users receiving notifications for locations they did not select",
      "status": "IN_PROGRESS",
      "quarter": "February 2026",
      "category": "BUG_FIX",
      "priority": "HIGH"
    },
    {
      "id": "4",
      "title": "Initial iOS Launch",
      "description": "MVP release of the all new Space Launch Now app for iOS devices",
      "status": "IN_PROGRESS",
      "quarter": "Q1 2026",
      "category": "FEATURE",
      "priority": "HIGH"
    },
    {
      "id": "5",
      "title": "Astronauts Profiles",
      "description": "Profiles for astronauts with mission history and biographies",
      "status": "BACKLOG",
      "quarter": "Q1 2026",
      "category": "FEATURE",
      "priority": "MEDIUM"
    }
  ],
  "lastUpdated": "March 2026",
  "message": "This roadmap represents planned features and is subject to change based on user feedback and priorities."
}
```

5. Click **Save**

## Step 3: Publish Configuration

1. Review the changes
2. Click **Publish changes**
3. Confirm the publish

## Data Model Reference

### RoadmapItem Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | String | Yes | Unique identifier |
| `title` | String | Yes | Display title |
| `description` | String | Yes | Detailed description |
| `status` | Enum | Yes | Current status |
| `quarter` | String | Yes | Target timeframe |
| `category` | Enum | Yes | Item category |
| `priority` | Enum | No | Priority level (defaults to MEDIUM) |

### Status Values

- `COMPLETED` - Work finished
- `IN_TESTING` - Testing/QA phase
- `IN_PROGRESS` - Active development
- `PLANNED` - Scheduled for future
- `BACKLOG` - Not yet scheduled

### Category Values

- `FEATURE` - New functionality
- `ENHANCEMENT` - Improving existing features
- `BUG_FIX` - Fixing issues
- `INFRASTRUCTURE` - Backend/tooling work
- `DESIGN` - UI/UX improvements

### Priority Values

- `HIGH` - Critical path
- `MEDIUM` - Standard priority
- `LOW` - Nice to have

## Caching Behavior

- Default fetch interval: **1 hour**
- Pull-to-refresh: Bypasses cache
- Offline: Uses cached/default values

## Updating the Roadmap

1. Go to Firebase Console → Remote Config
2. Edit the `roadmap_data` parameter
3. Update the JSON with new items or status changes
4. Click **Publish changes**

Changes will be visible to users within 1 hour (or immediately on pull-to-refresh).

## Troubleshooting

### Data Not Updating

- Ensure parameter name is exactly `roadmap_data`
- Verify JSON is valid (use a JSON validator)
- Check that changes are published (not just draft)
- Wait for cache interval or use pull-to-refresh

### Fallback to Placeholder

The app falls back to built-in placeholder data when:
- Remote Config is not configured
- Network is unavailable
- JSON parsing fails

This ensures the roadmap screen always shows content.
