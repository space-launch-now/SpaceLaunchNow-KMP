import Foundation
import WidgetKit
import ComposeApp

// MARK: - Launch Data Models
struct LaunchEntry: TimelineEntry {
    let date: Date
    let launches: [LaunchData]
    let isPlaceholder: Bool
    let errorMessage: String?
    
    static var placeholder: LaunchEntry {
        LaunchEntry(
            date: Date(),
            launches: [LaunchData.placeholder],
            isPlaceholder: true,
            errorMessage: nil
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
            return "Launched"
        }
        
        let days = Int(interval / 86400)
        let hours = Int((interval.truncatingRemainder(dividingBy: 86400)) / 3600)
        let minutes = Int((interval.truncatingRemainder(dividingBy: 3600)) / 60)
        
        if days > 0 {
            return "\(days)d \(hours)h"
        } else if hours > 0 {
            return "\(hours)h \(minutes)m"
        } else {
            return "\(minutes)m"
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
            
            // Fetch launches - this calls the Kotlin wrapper that unwraps Result<T>
            print("🚀 Widget: Calling fetchUpcomingLaunchesOrNull...")
            let paginatedList = try await helper.fetchUpcomingLaunchesOrNull(limit: 10)
            print("🚀 Widget: Got result from fetchUpcomingLaunchesOrNull")
            
            guard let paginatedList = paginatedList else {
                let errorMsg = "fetchUpcomingLaunchesOrNull returned nil - API call failed"
                print("🚀 Widget: \(errorMsg)")
                return LaunchEntry(date: Date(), launches: [], isPlaceholder: false, errorMessage: errorMsg)
            }
            
            print("🚀 Widget: Successfully got PaginatedLaunchNormalList with \(paginatedList.results.count) launches")
            return processPaginatedList(paginatedList)
            
        } catch {
            let errorMsg = "Error: \(error.localizedDescription)"
            print("🚀 Widget error: \(errorMsg)")
            return LaunchEntry(date: Date(), launches: [], isPlaceholder: false, errorMessage: errorMsg)
        }
    }
    
    // Helper to process the paginated list
    private func processPaginatedList(_ paginatedList: PaginatedLaunchNormalList) -> LaunchEntry {
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
            
            print("🚀 Widget: Processing launch: \(name) at \(launchDate)")
            
            return LaunchData(
                id: launch.id,
                name: name,
                agency: launch.launchServiceProvider.name,
                location: launch.pad?.location?.name ?? "Unknown Location",
                launchTime: launchDate,
                status: launch.status?.name ?? "Unknown",
                imageUrl: launch.image?.imageUrl
            )
        }
        
        print("🚀 Widget: Successfully processed \(launches.count) launches")
        
        if launches.isEmpty {
            return LaunchEntry(date: Date(), launches: [], isPlaceholder: false, errorMessage: "API returned \(results.count) items but 0 were valid")
        }
        
        return LaunchEntry(date: Date(), launches: launches, isPlaceholder: false, errorMessage: nil)
    }
}
