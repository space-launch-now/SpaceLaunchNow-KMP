import UIKit
import SwiftUI
import ComposeApp

class ComposeViewController: UIViewController {
    private var composeViewController: UIViewController?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Create the Compose view controller
        composeViewController = MainViewControllerKt.MainViewController()
        
        if let composeVC = composeViewController {
            addChild(composeVC)
            view.addSubview(composeVC.view)
            composeVC.view.frame = view.bounds
            composeVC.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
            composeVC.didMove(toParent: self)
        }
    }
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        // Use light content (white text) for dark mode, dark content for light mode
        if traitCollection.userInterfaceStyle == .dark {
            return .lightContent
        } else {
            return .darkContent
        }
    }
    
    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        
        // Update status bar when appearance changes
        if traitCollection.userInterfaceStyle != previousTraitCollection?.userInterfaceStyle {
            setNeedsStatusBarAppearanceUpdate()
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    @Environment(\.colorScheme) var colorScheme
    
    func makeUIViewController(context: Context) -> ComposeViewController {
        return ComposeViewController()
    }

    func updateUIViewController(_ uiViewController: ComposeViewController, context: Context) {
        // Trigger status bar update when color scheme changes
        uiViewController.setNeedsStatusBarAppearanceUpdate()
    }
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
            .ignoresSafeArea(.all) // Allow Compose to handle all safe areas
    }
}



