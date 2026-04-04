// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "WordClockWidgets",
    platforms: [
        .iOS(.v14)
    ],
    targets: [
        .target(
            name: "WordClockWidgets",
            path: "WordClockWidgets",
            resources: [
                .process("Resources")
            ]
        ),
        .target(
            name: "WordClockWidgetsWidget",
            path: "WordClockWidgetsWidget",
            resources: [
                .process("Resources")
            ]
        )
    ]
)
