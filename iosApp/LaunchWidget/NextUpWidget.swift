import WidgetKit
import SwiftUI

// MARK: - Next Up Widget
struct NextUpWidget: Widget {
    let kind: String = "NextUpWidget"
    
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: LaunchProvider()) { entry in
            NextUpWidgetView(entry: entry)
                .containerBackground(.fill.tertiary, for: .widget)
        }
        .configurationDisplayName("Next Launch")
        .description("Shows the next upcoming space launch")
        .supportedFamilies([.systemSmall, .systemMedium, .systemLarge])
    }
}

// MARK: - Next Up Widget View
struct NextUpWidgetView: View {
    var entry: LaunchEntry
    
    var body: some View {
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
    
    // MARK: - Launch View
    @ViewBuilder
    private func launchView(launch: LaunchData) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            // Header
            HStack {
                Image(systemName: "rocket.fill")
                    .foregroundStyle(.orange)
                Text("NEXT LAUNCH")
                    .font(.caption2)
                    .fontWeight(.bold)
                    .foregroundStyle(.secondary)
                Spacer()
            }
            
            Spacer()
            
            // Launch name
            Text(launch.name)
                .font(.headline)
                .lineLimit(2)
                .minimumScaleFactor(0.8)
            
            // Countdown
            Text(launch.timeUntilLaunch)
                .font(.title)
                .fontWeight(.bold)
                .foregroundStyle(.orange)
            
            // Agency
            HStack {
                Image(systemName: "building.2")
                    .font(.caption)
                Text(launch.agency)
                    .font(.caption)
            }
            .foregroundStyle(.secondary)
            
            // Location
            HStack {
                Image(systemName: "location")
                    .font(.caption)
                Text(launch.location)
                    .font(.caption)
                    .lineLimit(1)
            }
            .foregroundStyle(.secondary)
        }
        .padding()
    }
    
    // MARK: - Placeholder View
    private var placeholderView: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Image(systemName: "rocket.fill")
                    .foregroundStyle(.orange)
                Text("NEXT LAUNCH")
                    .font(.caption2)
                    .fontWeight(.bold)
                    .foregroundStyle(.secondary)
                Spacer()
            }
            
            Spacer()
            
            Text("Loading...")
                .font(.headline)
                .redacted(reason: .placeholder)
            
            Text("0h 0m")
                .font(.title)
                .fontWeight(.bold)
                .redacted(reason: .placeholder)
            
            HStack {
                Image(systemName: "building.2")
                Text("Agency")
                    .font(.caption)
            }
            .foregroundStyle(.secondary)
            .redacted(reason: .placeholder)
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
                .lineLimit(5)
            
            Divider()
                .padding(.vertical, 4)
            
            HStack(spacing: 4) {
                Image(systemName: "arrow.clockwise")
                    .font(.caption2)
                Text("Tap to refresh")
                    .font(.caption2)
            }
            .foregroundStyle(.blue)
            
            Text("Last updated: \(entry.date.formatted(date: .omitted, time: .shortened))")
                .font(.caption2)
                .foregroundStyle(.tertiary)
        }
        .padding()
    }
    
    // MARK: - Empty View
    private var emptyView: some View {
        VStack(spacing: 12) {
            Image(systemName: "rocket")
                .font(.largeTitle)
                .foregroundStyle(.secondary)
            
            Text("No Upcoming Launches")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding()
    }
}

// MARK: - Preview
#Preview(as: .systemSmall) {
    NextUpWidget()
} timeline: {
    LaunchEntry.placeholder
    LaunchEntry(
        date: Date(),
        launches: [LaunchData.placeholder],
        isPlaceholder: false,
        errorMessage: nil
    )
}
