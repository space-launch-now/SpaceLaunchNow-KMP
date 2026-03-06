import ComposeApp
import CryptoKit
import Foundation
import SwiftUI
import UIKit
import WidgetKit

// MARK: - Launch Data Models
struct LaunchEntry: TimelineEntry {
    let date: Date
    let launches: [LaunchData]
    let isPlaceholder: Bool
    let errorMessage: String?
    let hasWidgetAccess: Bool  // Premium entitlement check

    static var placeholder: LaunchEntry {
        LaunchEntry(
            date: Date(),
            launches: [LaunchData.placeholder],
            isPlaceholder: true,
            errorMessage: nil,
            hasWidgetAccess: true  // Show placeholder as if they have access
        )
    }
}

struct LaunchData: Identifiable {
    let id: String
    let name: String
    let agency: String
    let agencyAbbrev: String?
    let location: String
    let launchTime: Date
    let status: String
    let imageUrl: String?
    var image: UIImage?

    var displayAgency: String {
        if agency.count > 15, let abbrev = agencyAbbrev, !abbrev.isEmpty {
            return abbrev
        }
        return agency
    }
    /// Splits "Provider | Vehicle" into two lines
    var formattedName: String {
        name
    }
    var timeUntilLaunch: String {
        let interval = launchTime.timeIntervalSinceNow

        if interval < 0 {
            // Launch has already happened - show time since launch
            let timeSince = abs(interval)
            let totalDays = Int(timeSince / 86400)
            let hours = Int((timeSince.truncatingRemainder(dividingBy: 86400)) / 3600)
            let minutes = Int((timeSince.truncatingRemainder(dividingBy: 3600)) / 60)

            if totalDays > 0 {
                let dayText = totalDays == 1 ? "One day ago" : "\(totalDays) days ago"
                if hours > 0 {
                    return "\(dayText)"
                } else {
                    return "\(dayText)"
                }
            } else if hours > 0 {
                if minutes > 0 {
                    return "\(hours) hr, \(minutes) min ago"
                } else {
                    return "\(hours) hr ago"
                }
            } else if minutes > 0 {
                return "\(minutes) min ago"
            } else {
                return "Just launched"
            }
        }

        let totalDays = Int(interval / 86400)
        let hours = Int((interval.truncatingRemainder(dividingBy: 86400)) / 3600)
        let minutes = Int((interval.truncatingRemainder(dividingBy: 3600)) / 60)

        // Format like "One day 2 hr, 45 min" or "2 days 5 hr, 30 min"
        if totalDays > 0 {
            let dayText = totalDays == 1 ? "One day" : "\(totalDays) days"
            if hours > 0 && minutes > 0 {
                return "\(dayText) \(hours) hr, \(minutes) min"
            } else if hours > 0 {
                return "\(dayText) \(hours) hr"
            } else {
                return "\(dayText)"
            }
        } else if hours > 0 {
            if minutes > 0 {
                return "\(hours) hr, \(minutes) min"
            } else {
                return "\(hours) hr"
            }
        } else if minutes > 0 {
            return "\(minutes) min"
        } else {
            return "Less than 1 min"
        }
    }

    static var placeholder: LaunchData {
        LaunchData(
            id: "1",
            name: "Falcon 9 Block 5 | Starlink Group",
            agency: "SpaceX",
            agencyAbbrev: nil,
            location: "Kennedy Space Center, FL",
            launchTime: Date().addingTimeInterval(3600),
            status: "Go",
            imageUrl: nil,
            image: nil
        )
    }
}

// MARK: - Widget Provider
struct LaunchProvider: TimelineProvider {
    typealias Entry = LaunchEntry

    // Placeholder while widget is loading
    func placeholder(in context: Context) -> LaunchEntry {
        print("🚀 Widget: placeholder() called")
        return .placeholder
    }

    // Shown in widget gallery
    func getSnapshot(in context: Context, completion: @escaping (LaunchEntry) -> Void) {
        print("🚀 Widget: getSnapshot() called, isPreview: \(context.isPreview)")
        if context.isPreview {
            completion(.placeholder)
        } else {
            Task {
                let entry = await fetchLaunches()
                completion(entry)
            }
        }
    }

    // Provides timeline of entries
    func getTimeline(in context: Context, completion: @escaping (Timeline<LaunchEntry>) -> Void) {
        print("🚀 Widget: getTimeline() called")
        Task {
            let entry = await fetchLaunches()

            // Refresh every 15 minutes
            let nextUpdate = Calendar.current.date(byAdding: .minute, value: 15, to: Date())!
            let timeline = Timeline(entries: [entry], policy: .after(nextUpdate))

            completion(timeline)
        }
    }

    // Fetch launches from shared Kotlin code
    private func fetchLaunches() async -> LaunchEntry {
        do {
            print("🚀 Widget: Starting to fetch launches...")

            // Initialize Koin if needed
            KoinInitializerKt.doInitKoin()
            print("🚀 Widget: Koin initialized")

            // Get helper
            let helper = KoinHelper.Companion().instance()
            print("🚀 Widget: Got helper")

            // Check if user has widget access using fail-safe cache
            print("🚀 Widget: Checking widget access...")
            let accessState = WidgetAccessState.readFromCache()
            print(
                "🚀 Widget: Widget access: \(accessState.shouldShowUnlocked) (hasAccess=\(accessState.hasAccess), wasEverPremium=\(accessState.wasEverPremium), expiry=\(String(describing: accessState.subscriptionExpiry)))"
            )

            // If no access, return locked entry
            if !accessState.shouldShowUnlocked {
                print("🚀 Widget: User does not have widget access - showing paywall")
                return LaunchEntry(
                    date: Date(),
                    launches: [],
                    isPlaceholder: false,
                    errorMessage: nil,
                    hasWidgetAccess: false
                )
            }

            // Fetch launches - this calls the Kotlin wrapper that unwraps Result<T>
            print("🚀 Widget: Calling fetchUpcomingLaunchesOrNull...")
            let paginatedList = try await helper.fetchUpcomingLaunchesOrNull(limit: 10)
            print("🚀 Widget: Got result from fetchUpcomingLaunchesOrNull")

            guard let paginatedList = paginatedList else {
                let errorMsg = "fetchUpcomingLaunchesOrNull returned nil - API call failed"
                print("🚀 Widget: \(errorMsg)")
                return LaunchEntry(
                    date: Date(),
                    launches: [],
                    isPlaceholder: false,
                    errorMessage: errorMsg,
                    hasWidgetAccess: true
                )
            }

            print(
                "🚀 Widget: Successfully got PaginatedLaunchNormalList with \(paginatedList.results.count) launches"
            )
            var entry = processPaginatedList(
                paginatedList,
                hasAccess: accessState.shouldShowUnlocked
            )

            // Download images for launches
            var updatedLaunches = entry.launches
            for i in updatedLaunches.indices {
                if let urlString = updatedLaunches[i].imageUrl {
                    updatedLaunches[i].image = await Self.downloadImage(
                        from: urlString, maxSize: 200)
                }
            }
            entry = LaunchEntry(
                date: entry.date,
                launches: updatedLaunches,
                isPlaceholder: entry.isPlaceholder,
                errorMessage: entry.errorMessage,
                hasWidgetAccess: entry.hasWidgetAccess
            )

            return entry

        } catch {
            let errorMsg = "Error: \(error.localizedDescription)"
            print("🚀 Widget error: \(errorMsg)")
            return LaunchEntry(
                date: Date(),
                launches: [],
                isPlaceholder: false,
                errorMessage: errorMsg,
                hasWidgetAccess: false
            )
        }
    }

    // MARK: - Image Download & Cache

    private static var imageCacheDir: URL {
        let container =
            FileManager.default.containerURL(
                forSecurityApplicationGroupIdentifier: "group.me.calebjones.spacelaunchnow"
            ) ?? FileManager.default.temporaryDirectory
        let cacheDir = container.appendingPathComponent("widget_image_cache")
        try? FileManager.default.createDirectory(at: cacheDir, withIntermediateDirectories: true)
        return cacheDir
    }

    static func cacheFile(for urlString: String) -> URL {
        let data = Data(urlString.utf8)
        let hash = SHA256.hash(data: data)
        let hashString = hash.compactMap { String(format: "%02x", $0) }.joined()
        return imageCacheDir.appendingPathComponent(hashString + ".jpg")
    }

    private static func downloadImage(from urlString: String, maxSize: CGFloat) async -> UIImage? {
        let cacheFile = cacheFile(for: urlString)

        // Check cache (valid for 1 hour)
        if let attrs = try? FileManager.default.attributesOfItem(atPath: cacheFile.path),
            let modified = attrs[.modificationDate] as? Date,
            Date().timeIntervalSince(modified) < 3600,
            let cached = UIImage(contentsOfFile: cacheFile.path)
        {
            print("🖼️ Widget: Using cached image for \(urlString)")
            return cached
        }

        guard let url = URL(string: urlString) else { return nil }

        do {
            let (data, _) = try await URLSession.shared.data(from: url)
            guard let original = UIImage(data: data) else { return nil }

            // Downscale to save memory
            let scale = min(maxSize / original.size.width, maxSize / original.size.height, 1.0)
            let newSize = CGSize(
                width: original.size.width * scale, height: original.size.height * scale)
            let renderer = UIGraphicsImageRenderer(size: newSize)
            let resized = renderer.image { _ in
                original.draw(in: CGRect(origin: .zero, size: newSize))
            }

            // Cache to disk
            if let jpegData = resized.jpegData(compressionQuality: 0.7) {
                try? jpegData.write(to: cacheFile)
            }

            print("🖼️ Widget: Downloaded & cached image for \(urlString)")
            return resized
        } catch {
            print("🖼️ Widget: Failed to download image: \(error.localizedDescription)")
            return nil
        }
    }

    // Helper to process the paginated list
    private func processPaginatedList(
        _ paginatedList: PaginatedLaunchNormalList,
        hasAccess: Bool
    ) -> LaunchEntry {
        let results = paginatedList.results
        print("🚀 Widget: Got \(results.count) launches")

        let launches = results.compactMap { launch -> LaunchData? in
            guard let net = launch.net else {
                print("🚀 Widget: Skipping launch with no NET time")
                return nil
            }

            // name is optional in Kotlin, provide default if nil
            let name = launch.name ?? "Unknown Launch"

            // Convert Kotlinx_datetimeInstant to Date
            let launchDate = Date(timeIntervalSince1970: TimeInterval(net.epochSeconds))

            // Try multiple image sources in order of preference
            let primaryImageUrl = launch.image?.imageUrl

            let finalImageUrl = primaryImageUrl

            print("🚀 Widget: Processing launch: \(name) at \(launchDate)")
            print("🚀 Widget: Final image URL for \(name): \(finalImageUrl ?? "nil")")

            return LaunchData(
                id: launch.id,
                name: name,
                agency: launch.launchServiceProvider.name,
                agencyAbbrev: launch.launchServiceProvider.abbrev,
                location: launch.pad?.location?.name ?? "Unknown Location",
                launchTime: launchDate,
                status: launch.status?.name ?? "Unknown",
                imageUrl: finalImageUrl,
                image: nil
            )
        }

        print("🚀 Widget: Successfully processed \(launches.count) launches")

        if launches.isEmpty {
            return LaunchEntry(
                date: Date(),
                launches: [],
                isPlaceholder: false,
                errorMessage: "API returned \(results.count) items but 0 were valid",
                hasWidgetAccess: hasAccess
            )
        }

        return LaunchEntry(
            date: Date(),
            launches: launches,
            isPlaceholder: false,
            errorMessage: nil,
            hasWidgetAccess: hasAccess
        )
    }
}
