//
//  LaunchWidgetBundle.swift
//  iosApp
//
//  Created by Caleb Jones on 10/5/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import WidgetKit
import SwiftUI

@main
struct LaunchWidgetBundle: WidgetBundle {
    var body: some Widget {
        NextUpWidget()
        LaunchListWidget()
    }
}
