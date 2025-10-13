import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    handleDeepLink(url)
                }
        }
    }
    
    private func handleDeepLink(_ url: URL) {
        guard url.scheme == "spacelaunchnow" else { return }
        
        switch url.host {
        case "subscription":
            // Navigate to subscription screen
            MainViewControllerKt.setNavigationDestination(destination: "subscription")
            print("Deep link: Navigating to subscription screen")
            
        default:
            print("Deep link: Unknown host: \(url.host ?? "nil")")
        }
    }
}
