import WidgetKit
import SwiftUI
import UIKit

// MARK: - Next Up Widget
struct NextUpWidget: Widget {
    let kind: String = "NextUpWidget"
    
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: LaunchProvider()) { entry in
            NextUpWidgetView(entry: entry)
                .containerBackground(.fill.tertiary, for: .widget)
                .widgetBackground(backgroundView: Color.clear)
        }
        .configurationDisplayName("Next Launch")
        .description("Shows the next upcoming space launch")
        .supportedFamilies([.systemSmall, .systemMedium, .systemLarge])
    }
}

// MARK: - Next Up Widget View
struct NextUpWidgetView: View {
    var entry: LaunchEntry
    @Environment(\.colorScheme) var colorScheme
    
    var body: some View {
        ZStack {
            // Custom background with user-configured transparency that respects light/dark mode
            Color(colorScheme == .dark ? UIColor.secondarySystemBackground : UIColor.systemBackground)
                .opacity(entry.backgroundAlpha)
            
            // Content
            if entry.isPlaceholder {
                placeholderView
            } else if let errorMessage = entry.errorMessage {
                errorView(message: errorMessage)
            } else if let launch = entry.launches.first {
                launchView(launch: launch)
            } else {
                emptyView
            }
        }
        .cornerRadius(entry.cornerRadius)
    }
    
    // MARK: - Launch View
    @ViewBuilder
    private func launchView(launch: LaunchData) -> some View {
        GeometryReader { geometry in
            if isSmallWidget(for: geometry.size) {
                // SMALL WIDGET: Radically minimal design
                VStack(alignment: .leading, spacing: 4) {
                    // Compact header with status
                    HStack {
                        Image(systemName: getRocketIcon(for: launch.agency))
                            .font(.caption)
                            .foregroundStyle(.orange)
                        Text(launch.status)
                            .font(.caption2)
                            .fontWeight(.medium)
                            .foregroundStyle(.orange)
                        Spacer()
                    }
                    
                    Spacer()
                    
                    // Launch name (very compact)
                    Text(launch.name)
                        .font(.footnote)
                        .fontWeight(.medium)
                        .lineLimit(3)
                        .minimumScaleFactor(0.6)
                    
                    Spacer()
                    
                    // Prominent countdown
                    Text(launch.timeUntilLaunch)
                        .font(.title3)
                        .fontWeight(.bold)
                        .foregroundStyle(.primary)
                        .lineLimit(2)
                        .minimumScaleFactor(0.7)
                    
                    Spacer(minLength: 0)
                }
                .padding(8)
            } else {
                // MEDIUM/LARGE WIDGET: Full design
                VStack(alignment: .leading, spacing: adaptiveSpacing(for: geometry.size)) {
                    // Header
                    HStack {
                        Text("NEXT LAUNCH")
                            .font(adaptiveHeaderFont(for: geometry.size))
                            .fontWeight(.bold)
                            .foregroundStyle(.secondary)
                        Spacer()
                        // Status with orange color
                        Text(launch.status)
                            .font(adaptiveHeaderFont(for: geometry.size))
                            .fontWeight(.medium)
                            .foregroundStyle(.orange)
                    }
                    
                    // Launch name
                    Text(launch.name)
                        .font(adaptiveTitleFont(for: geometry.size))
                        .lineLimit(adaptiveTitleLines(for: geometry.size))
                        .minimumScaleFactor(0.7)
                    
                    // Countdown section with better visual hierarchy
                    VStack(alignment: .leading, spacing: 2) {
                        Text(isLaunchInPast(launch: launch) ? "Launched" : "In")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                            .fontWeight(.medium)
                        
                        Text(launch.timeUntilLaunch)
                            .font(adaptiveCountdownFont(for: geometry.size))
                            .fontWeight(.bold)
                            .foregroundStyle(.primary)
                            .lineLimit(2)
                            .minimumScaleFactor(0.8)
                    }
                    
                    // Details (adaptive based on space)
                    if shouldShowDetails(for: geometry.size) {
                        VStack(alignment: .leading, spacing: 2) {
                            // Agency
                            HStack {
                                Image(systemName: "building.2")
                                    .font(adaptiveDetailFont(for: geometry.size))
                                Text(launch.agency)
                                    .font(adaptiveDetailFont(for: geometry.size))
                            }
                            .foregroundStyle(.secondary)
                            
                            // Location
                            HStack {
                                Image(systemName: "location")
                                    .font(adaptiveDetailFont(for: geometry.size))
                                Text(launch.location)
                                    .font(adaptiveDetailFont(for: geometry.size))
                                    .lineLimit(1)
                            }
                            .foregroundStyle(.secondary)
                        }
                    } else {
                        // Compact view - show only agency
                        HStack {
                            Image(systemName: "building.2")
                                .font(adaptiveDetailFont(for: geometry.size))
                            Text(launch.agency)
                                .font(adaptiveDetailFont(for: geometry.size))
                        }
                        .foregroundStyle(.secondary)
                    }
                    
                    Spacer(minLength: 0)
                }
                .padding(adaptivePadding(for: geometry.size))
            }
        }
    }
    
    // MARK: - Placeholder View
    private var placeholderView: some View {
        GeometryReader { geometry in
            if isSmallWidget(for: geometry.size) {
                // SMALL WIDGET PLACEHOLDER: Minimal design
                VStack(alignment: .leading, spacing: 4) {
                    // Compact header with status
                    HStack {
                        Image(systemName: "rocket")
                            .font(.caption)
                            .foregroundStyle(.orange)
                        Text("Go")
                            .font(.caption2)
                            .fontWeight(.medium)
                            .foregroundStyle(.orange)
                        Spacer()
                    }
                    
                    Spacer()
                    
                    // Launch name (very compact)
                    Text("Loading Launch...")
                        .font(.footnote)
                        .fontWeight(.medium)
                        .lineLimit(3)
                        .minimumScaleFactor(0.6)
                    
                    Spacer()
                    
                    // Prominent countdown
                    Text("One day 2 hr, 45 min")
                        .font(.title3)
                        .fontWeight(.bold)
                        .foregroundStyle(.primary)
                        .lineLimit(2)
                        .minimumScaleFactor(0.7)
                    
                    Spacer(minLength: 0)
                }
                .padding(8)
                .redacted(reason: .placeholder)
            } else {
                // MEDIUM/LARGE WIDGET PLACEHOLDER
                VStack(alignment: .leading, spacing: adaptiveSpacing(for: geometry.size)) {
                    HStack {
                        Image(systemName: "rocket.fill")
                            .foregroundStyle(.orange)
                            .font(adaptiveHeaderFont(for: geometry.size))
                        Text("NEXT LAUNCH")
                            .font(adaptiveHeaderFont(for: geometry.size))
                            .fontWeight(.bold)
                            .foregroundStyle(.secondary)
                        Spacer()
                        // Status with orange color (placeholder)
                        Text("Go")
                            .font(adaptiveHeaderFont(for: geometry.size))
                            .fontWeight(.medium)
                            .foregroundStyle(.orange)
                    }
                    
                    Text("Loading Launch...")
                        .font(adaptiveTitleFont(for: geometry.size))
                        .redacted(reason: .placeholder)
                    
                    // Countdown section with better visual hierarchy (placeholder)
                    VStack(alignment: .leading, spacing: 2) {
                        Text("In")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                            .fontWeight(.medium)
                        
                        Text("One day 2 hr, 45 min")
                            .font(adaptiveCountdownFont(for: geometry.size))
                            .fontWeight(.bold)
                            .foregroundStyle(.primary)
                            .lineLimit(2)
                            .minimumScaleFactor(0.8)
                    }
                    .redacted(reason: .placeholder)
                    
                    if shouldShowDetails(for: geometry.size) {
                        HStack {
                            Image(systemName: "building.2")
                            Text("Space Agency")
                                .font(adaptiveDetailFont(for: geometry.size))
                        }
                        .foregroundStyle(.secondary)
                        .redacted(reason: .placeholder)
                        
                        HStack {
                            Image(systemName: "location")
                            Text("Launch Location")
                                .font(adaptiveDetailFont(for: geometry.size))
                        }
                        .foregroundStyle(.secondary)
                        .redacted(reason: .placeholder)
                    } else {
                        HStack {
                            Image(systemName: "building.2")
                            Text("Agency")
                                .font(adaptiveDetailFont(for: geometry.size))
                        }
                        .foregroundStyle(.secondary)
                        .redacted(reason: .placeholder)
                    }
                    
                    Spacer(minLength: 0)
                }
                .padding(adaptivePadding(for: geometry.size))
            }
        }
    }
    
    // MARK: - Error View
    private func errorView(message: String) -> some View {
        VStack(spacing: 8) {
            Image(systemName: "exclamationmark.triangle")
                .font(.largeTitle)
                .foregroundStyle(.orange)
            
            Text("Error")
                .font(.headline)
            
            Text(message)
                .font(.caption)
                .multilineTextAlignment(.center)
        }
        .padding()
    }
    
    // MARK: - Empty View
    private var emptyView: some View {
        VStack(spacing: 8) {
            Image(systemName: "rocket")
                .font(.largeTitle)
                .foregroundStyle(.secondary)
            
            Text("No Launches")
                .font(.headline)
            
            Text("No upcoming launches found")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding()
    }
    
    // MARK: - Adaptive Layout Helpers
    private func isSmallWidget(for size: CGSize) -> Bool {
        size.width < 180 && size.height < 180 // Small (systemSmall) widget detection
    }
    
    private func isLaunchInPast(launch: LaunchData) -> Bool {
        launch.launchTime.timeIntervalSinceNow < 0
    }
    
    private func shouldShowImage(for size: CGSize) -> Bool {
        size.height > 120 // Only show image in medium/large widgets
    }
    
    private func shouldShowDetails(for size: CGSize) -> Bool {
        size.height > 140 // Show full details in larger widgets
    }
    
    private func adaptiveSpacing(for size: CGSize) -> CGFloat {
        if size.height < 120 { return 4 }
        else if size.height < 200 { return 6 }
        else { return 8 }
    }
    
    private func adaptivePadding(for size: CGSize) -> CGFloat {
        if size.width < 200 { return 12 }
        else { return 16 }
    }
    
    private func adaptiveImageHeight(for size: CGSize) -> CGFloat {
        if size.height < 200 { return 50 }
        else if size.height < 300 { return 60 }
        else { return 80 }
    }
    
    private func adaptiveCornerRadius(for size: CGSize) -> CGFloat {
        if size.width < 200 { return 6 }
        else { return 8 }
    }
    
    private func adaptiveTitleLines(for size: CGSize) -> Int {
        size.height > 160 ? 3 : 2
    }
    
    private func adaptiveHeaderFont(for size: CGSize) -> Font {
        if size.width < 200 { return .caption2 }
        else { return .caption }
    }
    
    private func adaptiveTitleFont(for size: CGSize) -> Font {
        if size.height < 120 { return .subheadline }
        else if size.height < 200 { return .headline }
        else { return .title3 }
    }
    
    private func adaptiveCountdownFont(for size: CGSize) -> Font {
        if size.height < 120 { return .title3 }
        else if size.height < 200 { return .title2 }
        else { return .title }
    }
    
    private func adaptiveDetailFont(for size: CGSize) -> Font {
        if size.width < 200 { return .caption2 }
        else { return .caption }
    }
    
    private func adaptiveIconFont(for size: CGSize) -> Font {
        if size.width < 200 { return .caption }
        else { return .body }
    }
    
    // Get appropriate SF Symbol for launch agency
    private func getRocketIcon(for agency: String) -> String {
        let lowercaseAgency = agency.lowercased()
        
        if lowercaseAgency.contains("spacex") {
            return "airplane.departure"
        } else if lowercaseAgency.contains("nasa") {
            return "moon.stars"
        } else if lowercaseAgency.contains("ula") || lowercaseAgency.contains("united launch") {
            return "airplane.circle"
        } else if lowercaseAgency.contains("blue origin") {
            return "arrow.up.circle"
        } else if lowercaseAgency.contains("virgin") {
            return "airplane"
        } else if lowercaseAgency.contains("rocket lab") {
            return "paperplane.circle"
        } else if lowercaseAgency.contains("china") || lowercaseAgency.contains("cnsa") {
            return "globe.asia.australia"
        } else if lowercaseAgency.contains("roscosmos") || lowercaseAgency.contains("russia") {
            return "globe.europe.africa"
        } else if lowercaseAgency.contains("isro") || lowercaseAgency.contains("india") {
            return "star.circle"
        } else if lowercaseAgency.contains("esa") || lowercaseAgency.contains("arianespace") {
            return "globe.europe.africa"
        } else {
            return "rocket"
        }
    }
}

// MARK: - Preview
@available(iOS 17.0, *)
#Preview("NextUpWidget", as: .systemSmall) {
    NextUpWidget()
} timeline: {
    LaunchEntry.placeholder
}
