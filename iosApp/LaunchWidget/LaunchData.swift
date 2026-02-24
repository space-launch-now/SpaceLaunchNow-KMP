import Foundation
import WidgetKit
import SwiftUI
import ComposeApp

// MARK: - Launch Data Models
struct LaunchEntry: TimelineEntry {
    let date: Date
    let launches: [LaunchData]
    let isPlaceholder: Bool
    let errorMessage: String?
    let hasWidgetAccess: Bool  // Premium entitlement check
    let backgroundAlpha: Double  // Widget background transparency (0.0 to 1.0)
    let cornerRadius: Double  // Widget corner radius in dp
    
    static var placeholder: LaunchEntry {
        LaunchEntry(
            date: Date(),
            launches: [LaunchData.placeholder],
            isPlaceholder: true,
            errorMessage: nil,
            hasWidgetAccess: true,  // Show placeholder as if they have access
            backgroundAlpha: 0.75,
            cornerRadius: 16.0
        )
    }
}

struct LaunchData: Identifiable {
    let id: String
    let name: String
    let agency: String
    let location: String
    let launchTime: Date
    let status: String
    let imageUrl: String?
    
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
            location: "Kennedy Space Center, FL",
            launchTime: Date().addingTimeInterval(3600),
            status: "Go",
            imageUrl: nil
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
            
            // Fetch widget preferences
            print("🚀 Widget: Fetching widget preferences...")
            let backgroundAlpha = try await helper.getWidgetBackgroundAlpha()
            let cornerRadius = try await helper.getWidgetCornerRadius()
            print("🚀 Widget: Got preferences - alpha: \(backgroundAlpha), cornerRadius: \(cornerRadius)")
            
            // Check if user has widget access (premium entitlement)
            print("🚀 Widget: Checking widget access...")
            let hasAccess = try await helper.hasWidgetAccess()
            let hasAccessBool = hasAccess.boolValue // Convert KotlinBoolean to Bool
            print("🚀 Widget: Widget access: \(hasAccessBool)")
            
            // If no access, return locked entry
            if !hasAccessBool {
                print("🚀 Widget: User does not have widget access - showing paywall")
                return LaunchEntry(
                    date: Date(),
                    launches: [],
                    isPlaceholder: false,
                    errorMessage: nil,
                    hasWidgetAccess: false,
                    backgroundAlpha: Double(truncating: backgroundAlpha),
                    cornerRadius: Double(truncating: cornerRadius)
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
                    hasWidgetAccess: true,
                    backgroundAlpha: Double(truncating: backgroundAlpha),
                    cornerRadius: Double(truncating: cornerRadius)
                )
            }
            
            print("🚀 Widget: Successfully got PaginatedLaunchNormalList with \(paginatedList.results.count) launches")
            return processPaginatedList(
                paginatedList,
                hasAccess: hasAccessBool,
                backgroundAlpha: Double(truncating: backgroundAlpha),
                cornerRadius: Double(truncating: cornerRadius)
            )
            
        } catch {
            let errorMsg = "Error: \(error.localizedDescription)"
            print("🚀 Widget error: \(errorMsg)")
            return LaunchEntry(
                date: Date(),
                launches: [],
                isPlaceholder: false,
                errorMessage: errorMsg,
                hasWidgetAccess: false,
                backgroundAlpha: 0.75,
                cornerRadius: 16.0
            )
        }
    }
    
    // Helper to process the paginated list
    private func processPaginatedList(
        _ paginatedList: PaginatedLaunchNormalList,
        hasAccess: Bool,
        backgroundAlpha: Double,
        cornerRadius: Double
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
                location: launch.pad?.location?.name ?? "Unknown Location",
                launchTime: launchDate,
                status: launch.status?.name ?? "Unknown",
                imageUrl: finalImageUrl
            )
        }
        
        print("🚀 Widget: Successfully processed \(launches.count) launches")
        
        if launches.isEmpty {
            return LaunchEntry(
                date: Date(),
                launches: [],
                isPlaceholder: false,
                errorMessage: "API returned \(results.count) items but 0 were valid",
                hasWidgetAccess: hasAccess,
                backgroundAlpha: backgroundAlpha,
                cornerRadius: cornerRadius
            )
        }
        
        return LaunchEntry(
            date: Date(),
            launches: launches,
            isPlaceholder: false,
            errorMessage: nil,
            hasWidgetAccess: hasAccess,
            backgroundAlpha: backgroundAlpha,
            cornerRadius: cornerRadius
        )
    }
}
