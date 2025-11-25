import WidgetKit
import SwiftUI
import UIKit

// Helper extension to apply custom background with opacity (shared with NextUpWidget)
extension View {
    func widgetBackground(backgroundView: some View) -> some View {
        if #available(iOSApplicationExtension 17.0, *) {
            return containerBackground(for: .widget) {
                backgroundView
            }
        } else {
            return background(backgroundView)
        }
    }
}

// MARK: - Launch List Widget
struct LaunchListWidget: Widget {
    let kind: String = "LaunchListWidget"
    
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: LaunchProvider()) { entry in
            LaunchListWidgetView(entry: entry)
                .containerBackground(.fill.tertiary, for: .widget)
                .widgetBackground(backgroundView: Color.clear)
        }
        .configurationDisplayName("Launch List")
        .description("Shows upcoming space launches")
        .supportedFamilies([.systemMedium, .systemLarge, .systemExtraLarge])
    }
}

// MARK: - Launch List Widget View
struct LaunchListWidgetView: View {
    var entry: LaunchEntry
    @Environment(\.widgetFamily) var family
    @Environment(\.colorScheme) var colorScheme
    
    var body: some View {
        ZStack {
            // Custom background with user-configured transparency that respects light/dark mode
            Color(colorScheme == .dark ? UIColor.secondarySystemBackground : UIColor.systemBackground)
                .opacity(entry.backgroundAlpha)
            
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
        .cornerRadius(entry.cornerRadius)
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
            
            // Launch list
            VStack(spacing: 12) {
                ForEach(Array(entry.launches.prefix(maxLaunches).enumerated()), id: \.element.id) { index, launch in
                    LaunchRow(launch: launch, showDivider: index < entry.launches.count - 1)
                }
            }
            .padding(.horizontal)
            .padding(.bottom)
        }
    }
    
    // MARK: - Max launches based on widget size
    private var maxLaunches: Int {
        switch family {
        case .systemMedium:
            return 2
        case .systemLarge:
            return 4
        case .systemExtraLarge:
            return 8
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
        VStack(alignment: .leading, spacing: 6) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    // Launch name
                    Text(launch.name)
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .lineLimit(2)
                    
                    // Agency and location
                    HStack(spacing: 4) {
                        Image(systemName: "building.2")
                            .font(.caption2)
                        Text(launch.agency)
                            .font(.caption)
                    }
                    .foregroundStyle(.secondary)
                    
                    HStack(spacing: 4) {
                        Image(systemName: "location")
                            .font(.caption2)
                        Text(launch.location)
                            .font(.caption)
                            .lineLimit(1)
                    }
                    .foregroundStyle(.secondary)
                }
                
                Spacer()
                
                // Countdown
                VStack(alignment: .trailing, spacing: 2) {
                    Text(launch.timeUntilLaunch)
                        .font(.caption)
                        .fontWeight(.bold)
                        .foregroundStyle(.orange)
                    
                    // Status indicator
                    HStack(spacing: 4) {
                        Circle()
                            .fill(statusColor(for: launch.status))
                            .frame(width: 6, height: 6)
                        Text(launch.status)
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                    }
                }
            }
            
            if showDivider {
                Divider()
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
                location: "Cape Canaveral, FL",
                launchTime: Date().addingTimeInterval(7200),
                status: "Go",
                imageUrl: nil
            )
        ],
        isPlaceholder: false,
        errorMessage: nil,
        hasWidgetAccess: true,
        backgroundAlpha: 0.75,
        cornerRadius: 16.0
    )
}
