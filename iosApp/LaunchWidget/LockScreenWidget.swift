import SwiftUI
import WidgetKit

// MARK: - Lock Screen Widget
struct LockScreenWidget: Widget {
    let kind: String = "LockScreenWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: LaunchProvider()) { entry in
            LockScreenWidgetEntryView(entry: entry)
        }
        .configurationDisplayName("Lock Screen Launch")
        .description("Premium · Shows next launch on your Lock Screen")
        .supportedFamilies([.accessoryRectangular, .accessoryInline, .accessoryCircular])
    }
}

// MARK: - Entry View (dispatches by family)
struct LockScreenWidgetEntryView: View {
    var entry: LaunchEntry
    @Environment(\.widgetFamily) var family

    var body: some View {
        if !entry.hasWidgetAccess && !entry.isPlaceholder {
            lockedView
        } else {
            switch family {
            case .accessoryRectangular:
                AccessoryRectangularView(entry: entry)
            case .accessoryInline:
                AccessoryInlineView(entry: entry)
            case .accessoryCircular:
                AccessoryCircularView(entry: entry)
            default:
                Text("Unsupported")
            }
        }
    }

    // MARK: - Locked View
    private var lockedView: some View {
        Group {
            switch family {
            case .accessoryRectangular:
                VStack(alignment: .leading, spacing: 2) {
                    HStack(spacing: 4) {
                        Image(systemName: "lock.fill")
                            .font(.caption)
                        Text("Premium")
                            .font(.caption)
                            .fontWeight(.semibold)
                    }
                    Text("Upgrade to unlock")
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                }
                .widgetURL(URL(string: "spacelaunchnow://subscription"))
            case .accessoryInline:
                Label("Upgrade to Premium", systemImage: "lock.fill")
                    .widgetURL(URL(string: "spacelaunchnow://subscription"))
            case .accessoryCircular:
                ZStack {
                    AccessoryWidgetBackground()
                    Image(systemName: "lock.fill")
                        .font(.title3)
                }
                .widgetURL(URL(string: "spacelaunchnow://subscription"))
            default:
                Text("Premium")
            }
        }
    }
}

// MARK: - Accessory Rectangular View
struct AccessoryRectangularView: View {
    let entry: LaunchEntry

    var body: some View {
        if entry.isPlaceholder {
            placeholderView
        } else if let launch = entry.launches.first {
            launchView(launch: launch)
        } else {
            emptyView
        }
    }

    private func launchView(launch: LaunchData) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(launch.name)
                .font(.headline)
                .fontWeight(.semibold)
                .lineLimit(1)
                .minimumScaleFactor(0.7)
                .widgetAccentable()

            HStack(spacing: 4) {
                Image(systemName: "clock")
                    .font(.caption2)
                Text(launch.timeUntilLaunch)
                    .font(.caption)
                    .lineLimit(1)
            }
            .foregroundStyle(.secondary)

            Text(launch.agency)
                .font(.caption2)
                .foregroundStyle(.tertiary)
                .lineLimit(1)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private var placeholderView: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text("Loading launch...")
                .font(.headline)
                .redacted(reason: .placeholder)
            Text("Countdown here")
                .font(.caption)
                .redacted(reason: .placeholder)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private var emptyView: some View {
        VStack(alignment: .leading, spacing: 2) {
            HStack(spacing: 4) {
                Image(systemName: "rocket")
                    .font(.caption)
                Text("No Launches")
                    .font(.caption)
                    .fontWeight(.semibold)
            }
            Text("Check back later")
                .font(.caption2)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

// MARK: - Accessory Inline View
struct AccessoryInlineView: View {
    let entry: LaunchEntry

    var body: some View {
        if entry.isPlaceholder {
            Text("Loading...")
        } else if let launch = entry.launches.first {
            let shortName =
                launch.name.components(separatedBy: "|").first?.trimmingCharacters(in: .whitespaces)
                ?? launch.name
            ViewThatFits {
                Text("\(shortName) in \(hoursUntilLaunch(launch: launch))")
                Text("\(shortName) \(hoursUntilLaunch(launch: launch))")
            }
        } else {
            Label("No Launches", systemImage: "rocket")
        }
    }

    private func hoursUntilLaunch(launch: LaunchData) -> String {
        let interval = launch.launchTime.timeIntervalSinceNow
        if interval < 0 {
            return "launched"
        }
        let totalHours = Int(interval / 3600)
        let days = totalHours / 24
        let remainingHours = totalHours % 24
        if days > 0 && remainingHours > 0 {
            return "\(days)d \(remainingHours)hr"
        } else if days > 0 {
            return "\(days)d"
        } else if totalHours > 0 {
            return "\(totalHours)hr"
        } else {
            return "<1hr"
        }
    }
}

// MARK: - Accessory Circular View
struct AccessoryCircularView: View {
    let entry: LaunchEntry

    var body: some View {
        if entry.isPlaceholder {
            ZStack {
                AccessoryWidgetBackground()
                Image(systemName: "rocket")
                    .font(.title3)
            }
        } else if let launch = entry.launches.first {
            ZStack {
                AccessoryWidgetBackground()
                VStack(spacing: 1) {
                    Image(systemName: "rocket")
                        .font(.caption2)
                        .widgetAccentable()
                    Text(compactCircularCountdown(launch: launch))
                        .font(.caption)
                        .fontWeight(.semibold)
                        .minimumScaleFactor(0.6)
                        .lineLimit(1)
                }
            }
        } else {
            ZStack {
                AccessoryWidgetBackground()
                Image(systemName: "rocket")
                    .font(.title3)
                    .foregroundStyle(.secondary)
            }
        }
    }

    private func compactCircularCountdown(launch: LaunchData) -> String {
        let interval = launch.launchTime.timeIntervalSinceNow
        if interval < 0 {
            return "GO"
        }
        let totalMinutes = Int(interval / 60)
        let hours = totalMinutes / 60
        let minutes = totalMinutes % 60
        let days = hours / 24
        let remainingHours = hours % 24
        if days > 0 {
            return "\(days)d"
        } else if hours > 0 {
            return "\(hours)h"
        } else {
            return "\(minutes)m"
        }
    }
}

// MARK: - Previews
#Preview("Rectangular", as: .accessoryRectangular) {
    LockScreenWidget()
} timeline: {
    LaunchEntry(
        date: Date(),
        launches: [LaunchData.placeholder],
        isPlaceholder: false,
        errorMessage: nil,
        hasWidgetAccess: true
    )
    LaunchEntry(
        date: Date(),
        launches: [],
        isPlaceholder: false,
        errorMessage: nil,
        hasWidgetAccess: true
    )
    LaunchEntry(
        date: Date(),
        launches: [LaunchData.placeholder],
        isPlaceholder: false,
        errorMessage: nil,
        hasWidgetAccess: false
    )
}

#Preview("Inline", as: .accessoryInline) {
    LockScreenWidget()
} timeline: {
    LaunchEntry(
        date: Date(),
        launches: [LaunchData.placeholder],
        isPlaceholder: false,
        errorMessage: nil,
        hasWidgetAccess: true
    )
    LaunchEntry(
        date: Date(),
        launches: [],
        isPlaceholder: false,
        errorMessage: nil,
        hasWidgetAccess: true
    )
}

#Preview("Circular", as: .accessoryCircular) {
    LockScreenWidget()
} timeline: {
    LaunchEntry(
        date: Date(),
        launches: [LaunchData.placeholder],
        isPlaceholder: false,
        errorMessage: nil,
        hasWidgetAccess: true
    )
    LaunchEntry(
        date: Date(),
        launches: [],
        isPlaceholder: false,
        errorMessage: nil,
        hasWidgetAccess: true
    )
    LaunchEntry(
        date: Date(),
        launches: [LaunchData.placeholder],
        isPlaceholder: false,
        errorMessage: nil,
        hasWidgetAccess: false
    )
}
