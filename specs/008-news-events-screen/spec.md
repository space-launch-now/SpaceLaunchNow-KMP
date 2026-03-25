# Feature Specification: News and Events Screen

**Feature Branch**: `008-news-events-screen`  
**Created**: 2026-03-23  
**Status**: Draft  
**Input**: User description: "Implement a News and Events screen - with filtering and searching."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Browse Space News Articles (Priority: P1)

A user wants to read the latest space industry news and stay informed about rocket launches, missions, and space companies. They can open the News and Events screen, see a list of articles from various news sources, and tap on any article to read it on the source website.

**Why this priority**: News articles drive daily engagement and are the primary content users return for. Articles are sourced from SpaceNews API (SNAPI) which has a simpler API and existing repository implementation.

**Independent Test**: Launch app, navigate to News & Events tab, verify articles display with images, titles, and source info. Tap article to confirm it opens external browser.

**Acceptance Scenarios**:

1. **Given** I am on the News & Events screen, **When** the screen loads, **Then** I see a list of recent space news articles with images, titles, and news site names
2. **Given** I see an article list, **When** I tap on an article card, **Then** the article opens in my device browser
3. **Given** articles are loading, **When** I scroll to the bottom of the list, **Then** more articles load automatically (pagination)

---

### User Story 2 - Search News and Events (Priority: P2)

A user looking for news about a specific topic (e.g., "Starship" or "ISS") can use the search bar to filter results. The search applies to both news articles and events based on the active tab.

**Why this priority**: Search is the primary discovery mechanism and enables quick access to specific topics.

**Independent Test**: Open News & Events screen, type "Starship" in search bar, verify only articles/events containing "Starship" appear.

**Acceptance Scenarios**:

1. **Given** I am on the News tab, **When** I type "Starship" in the search bar, **Then** I see only articles matching "Starship"
2. **Given** I have entered a search query, **When** I tap the clear button, **Then** the search clears and all articles are shown again
3. **Given** I search for a term with no results, **When** search completes, **Then** I see an empty state with appropriate message

---

### User Story 3 - Browse Space Events (Priority: P2)

A user wants to see upcoming space events like spacewalks, docking operations, and live broadcasts. They can switch to the Events tab to see a chronologically ordered list of events.

**Why this priority**: Events complement launch data and provide the "what's happening" context. Events API already has extension functions and repository implemented.

**Independent Test**: Navigate to Events tab, verify events display with images, titles, dates, and event types. Confirm upcoming events show first.

**Acceptance Scenarios**:

1. **Given** I am on the News & Events screen, **When** I tap the "Events" tab, **Then** I see a list of upcoming space events
2. **Given** I see an event, **When** I view the event card, **Then** I see the event name, date, type, and image
3. **Given** I tap an event card, **When** navigation completes, **Then** I am taken to the Event Detail screen

---

### User Story 4 - Filter Events by Type (Priority: P3)

A user interested only in certain event types (e.g., "Spacewalks" or "Docking") can filter the events list to show only those types. Filter options are accessible via a filter sheet.

**Why this priority**: Filtering enhances discoverability but is secondary to browsing and searching.

**Independent Test**: Open Events tab, tap filter button, select "Spacewalk" type, verify only spacewalk events display.

**Acceptance Scenarios**:

1. **Given** I am on the Events tab, **When** I tap the filter icon, **Then** I see a filter sheet with event type options
2. **Given** I select "Spacewalk" filter, **When** the filter is applied, **Then** I see only spacewalk events
3. **Given** I have active filters, **When** I view the filter icon, **Then** I see a badge indicating active filter count

---

### User Story 5 - Filter News by Source (Priority: P3)

A user who prefers news from specific sources (e.g., "SpaceNews" or "NASASpaceflight") can filter articles to show only those sources.

**Why this priority**: Source filtering is a nice-to-have for power users but not essential for MVP.

**Independent Test**: Open News tab, tap filter button, select "SpaceNews", verify only SpaceNews articles appear.

**Acceptance Scenarios**:

1. **Given** I am on the News tab, **When** I tap the filter icon, **Then** I see filter options including news source selection
2. **Given** I select "SpaceNews" filter, **When** the filter is applied, **Then** I see only articles from SpaceNews

---

### User Story 6 - Toggle Upcoming/Past Events (Priority: P3)

A user can toggle between viewing upcoming events and past events to review historical space activities.

**Why this priority**: Browsing past events is useful but not critical for primary use case.

**Independent Test**: On Events tab, toggle to "Past" events, verify events displayed have dates in the past.

**Acceptance Scenarios**:

1. **Given** I am on Events tab showing upcoming events, **When** I toggle to "Past" tab/filter, **Then** I see events that have already occurred
2. **Given** I am viewing past events, **When** I toggle to "Upcoming", **Then** I see future events

---

### Edge Cases

- What happens when no network connection? Show cached data if available, otherwise show offline error state with retry button
- What happens when API rate limit is exceeded? Show error message explaining rate limit with suggested wait time
- How does the system handle very long article titles? Truncate with ellipsis after 2 lines
- What happens when article has no image? Show placeholder image with news icon
- What happens during search with slow network? Show loading indicator with existing results visible

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST display a tabbed interface with "News" and "Events" tabs
- **FR-002**: News tab MUST display articles from SpaceNews API (SNAPI) with image, title, source, and date
- **FR-003**: Events tab MUST display events from Launch Library API with image, name, date, and type
- **FR-004**: System MUST implement search functionality that filters content by name/title
- **FR-005**: System MUST support infinite scroll pagination for both tabs (limit 10-20 per page)
- **FR-006**: Events MUST be filterable by event type (from API event_types endpoint)
- **FR-007**: News articles MUST open in external browser when tapped
- **FR-008**: Events MUST navigate to Event Detail screen when tapped
- **FR-009**: System MUST show loading states during data fetch operations
- **FR-010**: System MUST show error states with retry option when API calls fail
- **FR-011**: System MUST use caching to display stale data when network is unavailable
- **FR-012**: Filter selections MUST persist during navigation within the session
- **FR-013**: News MUST be filterable by news site source (optional, P3)
- **FR-014**: Events MUST support upcoming/past toggle filter

### Key Entities

- **Article**: News article from SNAPI - id, title, url, imageUrl, newsSite, publishedAt, summary
- **EventEndpointNormal**: Space event from LL2 API - id, name, date, type, featureImage, location, description
- **EventType**: Event category - id, name (e.g., Spacewalk, Docking, Press Event)
- **NewsSite**: Article source - name (derived from article newsSite field)

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can view and scroll through news articles within 2 seconds of screen load
- **SC-002**: Search returns filtered results within 1 second of user input debounce
- **SC-003**: Pagination loads next page within 1 second when scrolling near bottom
- **SC-004**: Filter UI opens and applies within 500ms of user interaction
- **SC-005**: 100% of tap targets meet 48dp accessibility minimum
- **SC-006**: Screen renders correctly in both light and dark themes (verified via dual previews)
- **SC-007**: All error states are recoverable via retry action
