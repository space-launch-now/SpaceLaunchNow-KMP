import SwiftUI
import UIKit
import WidgetKit

// MARK: - Launch List Widget
struct LaunchListWidget: Widget {
    let kind: String = "LaunchListWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: LaunchProvider()) { entry in
            LaunchListWidgetView(entry: entry)
        }
        .configurationDisplayName("Launch List")
        .description("Premium · Shows upcoming space launches")
        .supportedFamilies([.systemMedium, .systemLarge, .systemExtraLarge])
    }
}

// MARK: - Launch List Widget View
struct LaunchListWidgetView: View {
    var entry: LaunchEntry
    @Environment(\.widgetFamily) var family
    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        ZStack(alignment: .top) {
            // Content
            if entry.isPlaceholder {
                placeholderView
            } else if !entry.hasWidgetAccess {
                lockedView  // Show paywall for non-premium users
            } else if let errorMessage = entry.errorMessage {
                errorView(message: errorMessage)
            } else if !entry.launches.isEmpty {
                launchListView
            } else {
                emptyView
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .containerBackground(for: .widget) {
            Color(
                colorScheme == .dark ? UIColor.secondarySystemBackground : UIColor.systemBackground)
        }
    }

    // MARK: - Launch List View
    private var launchListView: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Header
            HStack {
                Image(systemName: "list.bullet.rectangle")
                    .foregroundStyle(.orange)
                Text("UPCOMING LAUNCHES")
                    .font(.caption2)
                    .fontWeight(.bold)
                    .foregroundStyle(.secondary)
                Spacer()
            }
            .padding(.horizontal)
            .padding(.top)

            if family == .systemExtraLarge {
                // Two-column grid for extra-large widget
                let launches = Array(entry.launches.prefix(maxLaunches))
                let half = (launches.count + 1) / 2
                let left = Array(launches.prefix(half))
                let right = Array(launches.dropFirst(half))

                HStack(alignment: .top, spacing: 0) {
                    VStack(spacing: 12) {
                        ForEach(Array(left.enumerated()), id: \.element.id) { index, launch in
                            LaunchRow(launch: launch, showDivider: index < left.count - 1)
                        }
                        Spacer(minLength: 0)
                    }
                    .padding(.horizontal)

                    Divider()

                    VStack(spacing: 12) {
                        ForEach(Array(right.enumerated()), id: \.element.id) { index, launch in
                            LaunchRow(launch: launch, showDivider: index < right.count - 1)
                        }
                        Spacer(minLength: 0)
                    }
                    .padding(.horizontal)
                }
                .padding(.bottom)
            } else {
                // Single-column list for medium/large
                VStack(spacing: 12) {
                    ForEach(
                        Array(entry.launches.prefix(maxLaunches).enumerated()), id: \.element.id
                    ) { index, launch in
                        LaunchRow(launch: launch, showDivider: index < entry.launches.count - 1)
                    }
                }
                .padding(.horizontal)
                .padding(.bottom)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
    }

    // MARK: - Max launches based on widget size
    private var maxLaunches: Int {
        switch family {
        case .systemMedium:
            return 2
        case .systemLarge:
            return 5
        case .systemExtraLarge:
            return 10
        default:
            return 2
        }
    }

    // MARK: - Placeholder View
    private var placeholderView: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "list.bullet.rectangle")
                    .foregroundStyle(.orange)
                Text("UPCOMING LAUNCHES")
                    .font(.caption2)
                    .fontWeight(.bold)
                    .foregroundStyle(.secondary)
                Spacer()
            }
            .padding(.horizontal)
            .padding(.top)

            ForEach(0..<maxLaunches, id: \.self) { _ in
                LaunchRow(launch: .placeholder, showDivider: true)
                    .redacted(reason: .placeholder)
            }
            .padding(.horizontal)

            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
    }

    // MARK: - Empty View
    private var emptyView: some View {
        VStack(spacing: 12) {
            Image(systemName: "list.bullet.rectangle")
                .font(.largeTitle)
                .foregroundStyle(.secondary)

            Text("No Upcoming Launches")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding()
    }

    // MARK: - Error View
    private func errorView(message: String) -> some View {
        VStack(spacing: 8) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.largeTitle)
                .foregroundStyle(.red)

            Text("Widget Error")
                .font(.headline)
                .foregroundStyle(.primary)

            Text(message)
                .font(.caption)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
                .lineLimit(10)

            Divider()
                .padding(.vertical, 4)

            HStack(spacing: 4) {
                Image(systemName: "arrow.clockwise")
                    .font(.caption2)
                Text("Tap to refresh or wait 15 min")
                    .font(.caption2)
            }
            .foregroundStyle(.blue)

            Text("Last tried: \(entry.date.formatted(date: .omitted, time: .shortened))")
                .font(.caption2)
                .foregroundStyle(.tertiary)
        }
        .padding()
    }

    // MARK: - Locked View (Premium Paywall)
    private var lockedView: some View {
        VStack(spacing: 12) {
            Image(systemName: "lock.fill")
                .font(.system(size: 40))
                .foregroundStyle(.orange)

            Text("Premium Widget")
                .font(.headline)
                .fontWeight(.bold)
                .foregroundStyle(.primary)

            Text("Upgrade to Premium to unlock the Launch List widget")
                .font(.caption)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
                .lineLimit(3)

            Divider()
                .padding(.vertical, 4)

            HStack(spacing: 4) {
                Image(systemName: "arrow.up.circle.fill")
                    .font(.caption2)
                Text("Tap to upgrade")
                    .font(.caption2)
                    .fontWeight(.medium)
            }
            .foregroundStyle(.blue)
        }
        .padding()
        .widgetURL(URL(string: "spacelaunchnow://subscription"))  // Deep link to subscription screen
    }
}

// MARK: - Launch Row
struct LaunchRow: View {
    let launch: LaunchData
    let showDivider: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 3) {
            HStack(alignment: .center, spacing: 8) {
                // Thumbnail image
                if let uiImage = launch.image {
                    Image(uiImage: uiImage)
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(width: 32, height: 32)
                        .clipShape(RoundedRectangle(cornerRadius: 6))
                } else {
                    RoundedRectangle(cornerRadius: 6)
                        .fill(Color.secondary.opacity(0.15))
                        .frame(width: 32, height: 32)
                        .overlay(
                            Image(systemName: "rocket")
                                .font(.system(size: 14))
                                .foregroundStyle(.secondary)
                        )
                }

                VStack(alignment: .leading, spacing: 2) {
                    // Launch name
                    Text(launch.formattedName)
                        .font(.caption)
                        .fontWeight(.semibold)
                        .lineLimit(1)
                        .minimumScaleFactor(0.85)

                    // Agency
                    HStack(spacing: 4) {
                        Image(systemName: "building.2")
                            .font(.system(size: 9))
                        Text(launch.displayAgency)
                            .font(.system(size: 10))
                    }
                    .foregroundStyle(.secondary)

                    // Location
                    HStack(spacing: 4) {
                        Image(systemName: "location")
                            .font(.system(size: 9))
                        Text(launch.location)
                            .font(.system(size: 10))
                            .lineLimit(1)
                    }
                    .foregroundStyle(.secondary)
                }

                Spacer()

                // Countdown + status
                VStack(alignment: .trailing, spacing: 1) {
                    Text(launch.timeUntilLaunch)
                        .font(.system(size: 11, weight: .bold))
                        .foregroundStyle(.orange)

                    HStack(spacing: 3) {
                        Circle()
                            .fill(statusColor(for: launch.status))
                            .frame(width: 5, height: 5)
                        Text(launch.status)
                            .font(.system(size: 9))
                            .foregroundStyle(.secondary)
                    }
                }
            }

            if showDivider {
                Divider()
                    .padding(.top, 1)
            }
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
#Preview(as: .systemMedium) {
    LaunchListWidget()
} timeline: {
    LaunchEntry.placeholder
    LaunchEntry(
        date: Date(),
        launches: [
            LaunchData.placeholder,
            LaunchData(
                id: "2",
                name: "Atlas V 551 | USSF-51",
                agency: "United Launch Alliance",
                agencyAbbrev: "ULA",
                location: "Cape Canaveral, FL",
                launchTime: Date().addingTimeInterval(7200),
                status: "Go",
                imageUrl: nil
            ),
        ],
        isPlaceholder: false,
        errorMessage: nil,
        hasWidgetAccess: true
    )
}
