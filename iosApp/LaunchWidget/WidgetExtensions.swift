import SwiftUI
import WidgetKit

// MARK: - Shared Widget Extensions

/// Helper extension to apply custom background with opacity
/// This is shared across all widgets to avoid code duplication
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
