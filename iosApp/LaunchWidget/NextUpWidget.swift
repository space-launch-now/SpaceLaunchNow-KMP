import SwiftUI
import UIKit
import WidgetKit

// MARK: - Next Up Widget
struct NextUpWidget: Widget {
    let kind: String = "NextUpWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: LaunchProvider()) { entry in
            NextUpWidgetView(entry: entry)
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
        .containerBackground(for: .widget) {
            Color(
                colorScheme == .dark ? UIColor.secondarySystemBackground : UIColor.systemBackground)
        }
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
                    Text(launch.formattedName)
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
            } else if isLargeWidget(for: geometry.size) {
                // LARGE WIDGET: Vertical layout with hero image
                VStack(alignment: .leading, spacing: 0) {
                    // Hero image across top
                    if let uiImage = launch.image {
                        Image(uiImage: uiImage)
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                            .frame(height: geometry.size.height * 0.4)
                            .clipped()
                    }

                    // Content below image
                    VStack(alignment: .leading, spacing: 8) {
                        // Header row
                        HStack {
                            Text("NEXT LAUNCH")
                                .font(.caption)
                                .fontWeight(.bold)
                                .foregroundStyle(.secondary)
                            Spacer()
                            Text(launch.status.uppercased())
                                .font(.system(size: 10, weight: .bold))
                                .foregroundStyle(.white)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background(statusColor(for: launch.status))
                                .clipShape(Capsule())
                        }

                        // Launch name
                        Text(launch.formattedName)
                            .font(.title3)
                            .fontWeight(.semibold)
                            .lineLimit(2)
                            .minimumScaleFactor(0.8)

                        // Countdown
                        Text(launch.timeUntilLaunch)
                            .font(.title)
                            .fontWeight(.bold)
                            .foregroundStyle(.orange)
                            .lineLimit(1)
                            .minimumScaleFactor(0.7)

                        Spacer(minLength: 0)

                        // Agency & Location
                        HStack(spacing: 0) {
                            VStack(alignment: .leading, spacing: 3) {
                                HStack(spacing: 4) {
                                    Image(systemName: "building.2")
                                        .font(.caption)
                                    Text(launch.displayAgency)
                                        .font(.caption)
                                }
                                HStack(spacing: 4) {
                                    Image(systemName: "location")
                                        .font(.caption)
                                    Text(launch.location)
                                        .font(.caption)
                                        .lineLimit(1)
                                }
                            }
                            .foregroundStyle(.secondary)
                            Spacer()
                        }
                    }
                    .padding(16)
                }
            } else {
                // MEDIUM WIDGET
                HStack(spacing: 10) {
                    // Launch image (left side, square)
                    if let uiImage = launch.image {
                        Image(uiImage: uiImage)
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                            .frame(
                                width: adaptiveImageHeight(for: geometry.size),
                                height: adaptiveImageHeight(for: geometry.size)
                            )
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                    }

                    // Content (right side)
                    VStack(alignment: .leading, spacing: 0) {
                        // Section label
                        Text("NEXT LAUNCH")
                            .font(adaptiveHeaderFont(for: geometry.size))
                            .fontWeight(.bold)
                            .foregroundStyle(.secondary)
                            .padding(.bottom, 6)

                        // Launch name
                        Text(launch.formattedName)
                            .font(adaptiveTitleFont(for: geometry.size))
                            .fontWeight(.semibold)
                            .lineLimit(3)
                            .minimumScaleFactor(0.7)
                            .padding(.bottom, 8)

                        // Status pill
                        HStack(alignment: .firstTextBaseline, spacing: 6) {
                            Text(launch.status.uppercased())
                                .font(.system(size: 9, weight: .bold))
                                .foregroundStyle(.white)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(statusColor(for: launch.status))
                                .clipShape(Capsule())
                        }
                        .padding(.bottom, 4)

                        // Countdown
                        Text(launch.timeUntilLaunch)
                            .font(adaptiveCountdownFont(for: geometry.size))
                            .fontWeight(.bold)
                            .foregroundStyle(.primary)
                            .lineLimit(1)
                            .minimumScaleFactor(0.6)

                        Spacer(minLength: 8)

                        // Agency & Location
                        HStack(spacing: 4) {
                            Image(systemName: "building.2")
                                .font(adaptiveDetailFont(for: geometry.size))
                            Text(launch.displayAgency)
                                .font(adaptiveDetailFont(for: geometry.size))
                            Text("·")
                                .font(adaptiveDetailFont(for: geometry.size))
                            Image(systemName: "location")
                                .font(adaptiveDetailFont(for: geometry.size))
                            Text(launch.location)
                                .font(adaptiveDetailFont(for: geometry.size))
                                .lineLimit(1)
                        }
                        .foregroundStyle(.secondary)
                    }
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
                        Image(systemName: "paperplane.fill")
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
            Image(systemName: "paperplane.fill")
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
        size.width < 180 && size.height < 180  // Small (systemSmall) widget detection
    }

    private func isLargeWidget(for size: CGSize) -> Bool {
        size.height > 280
    }

    private func isLaunchInPast(launch: LaunchData) -> Bool {
        launch.launchTime.timeIntervalSinceNow < 0
    }

    private func shouldShowImage(for size: CGSize) -> Bool {
        size.height > 120  // Only show image in medium/large widgets
    }

    private func shouldShowDetails(for size: CGSize) -> Bool {
        size.height > 140  // Show full details in larger widgets
    }

    private func adaptiveSpacing(for size: CGSize) -> CGFloat {
        if size.height < 120 { return 4 } else if size.height < 200 { return 6 } else { return 8 }
    }

    private func adaptivePadding(for size: CGSize) -> CGFloat {
        if size.width < 200 { return 12 } else { return 16 }
    }

    private func adaptiveImageHeight(for size: CGSize) -> CGFloat {
        if size.height < 200 {
            return 65
        } else if size.height < 300 {
            return 80
        } else {
            return 100
        }
    }

    private func adaptiveTitleLines(for size: CGSize) -> Int {
        size.height > 160 ? 3 : 2
    }

    private func adaptiveHeaderFont(for size: CGSize) -> Font {
        if size.width < 200 { return .caption2 } else { return .caption }
    }

    private func adaptiveTitleFont(for size: CGSize) -> Font {
        if size.height < 120 {
            return .subheadline
        } else if size.height < 200 {
            return .headline
        } else {
            return .title3
        }
    }

    private func adaptiveCountdownFont(for size: CGSize) -> Font {
        if size.height < 120 {
            return .title3
        } else if size.height < 200 {
            return .title2
        } else {
            return .title
        }
    }

    private func adaptiveDetailFont(for size: CGSize) -> Font {
        if size.width < 200 { return .caption2 } else { return .caption }
    }

    private func adaptiveIconFont(for size: CGSize) -> Font {
        if size.width < 200 { return .caption } else { return .body }
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
            return "paperplane.fill"
        }
    }

    private func statusColor(for status: String) -> Color {
        switch status.lowercased() {
        case "go", "success":
            return .green
        case "tbc", "tbd":
            return .orange
        case "hold", "failure":
            return .red
        default:
            return .gray
        }
    }
}

// MARK: - Preview
@available(iOS 17.0, *)
#Preview("Small", as: .systemSmall) {
    NextUpWidget()
} timeline: {
    LaunchEntry.placeholder
}

@available(iOS 17.0, *)
#Preview("Medium", as: .systemMedium) {
    NextUpWidget()
} timeline: {
    LaunchEntry.placeholder
    LaunchEntry(
        date: Date(),
        launches: [LaunchData.placeholder],
        isPlaceholder: false,
        errorMessage: nil,
        hasWidgetAccess: true
    )
}

@available(iOS 17.0, *)
#Preview("Large", as: .systemLarge) {
    NextUpWidget()
} timeline: {
    LaunchEntry.placeholder
    LaunchEntry(
        date: Date(),
        launches: [LaunchData.placeholder],
        isPlaceholder: false,
        errorMessage: nil,
        hasWidgetAccess: true
    )
}
