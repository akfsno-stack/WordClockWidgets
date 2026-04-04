//
//  WidgetPreferences.swift
//  WordClockWidgets
//
//  Created by GitHub Copilot on 2026-04-04.
//

import UIKit

class WidgetPreferences {
    private static let defaults = UserDefaults.standard

    static func setShowHour(_ show: Bool, widgetId: String) {
        defaults.set(show, forKey: "show_hour_\(widgetId)")
    }

    static func getShowHour(widgetId: String, default: Bool = true) -> Bool {
        defaults.bool(forKey: "show_hour_\(widgetId)")
    }

    static func setShowMinute(_ show: Bool, widgetId: String) {
        defaults.set(show, forKey: "show_minute_\(widgetId)")
    }

    static func getShowMinute(widgetId: String, default: Bool = true) -> Bool {
        defaults.bool(forKey: "show_minute_\(widgetId)")
    }

    static func setShowDayNight(_ show: Bool, widgetId: String) {
        defaults.set(show, forKey: "show_daynight_\(widgetId)")
    }

    static func getShowDayNight(widgetId: String, default: Bool = true) -> Bool {
        defaults.bool(forKey: "show_daynight_\(widgetId)")
    }

    static func setShowDate(_ show: Bool, widgetId: String) {
        defaults.set(show, forKey: "show_date_\(widgetId)")
    }

    static func getShowDate(widgetId: String, default: Bool = false) -> Bool {
        defaults.bool(forKey: "show_date_\(widgetId)")
    }

    static func setShowDayOfWeek(_ show: Bool, widgetId: String) {
        defaults.set(show, forKey: "show_dayofweek_\(widgetId)")
    }

    static func getShowDayOfWeek(widgetId: String, default: Bool = false) -> Bool {
        defaults.bool(forKey: "show_dayofweek_\(widgetId)")
    }

    static func setUse12HourFormat(_ use12: Bool, widgetId: String) {
        defaults.set(use12, forKey: "use_12h_\(widgetId)")
    }

    static func getUse12HourFormat(widgetId: String, default: Bool = true) -> Bool {
        defaults.bool(forKey: "use_12h_\(widgetId)")
    }

    static func setBlockOffset(_ x: CGFloat, _ y: CGFloat, blockName: String, widgetId: String) {
        defaults.set(["x": x, "y": y], forKey: "block_\(blockName)_\(widgetId)")
    }

    static func getBlockOffset(blockName: String, widgetId: String) -> CGPoint? {
        guard let dict = defaults.dictionary(forKey: "block_\(blockName)_\(widgetId)") as? [String: CGFloat] else {
            return nil
        }
        return CGPoint(x: dict["x"] ?? 0, y: dict["y"] ?? 0)
    }

    static func setBackgroundColor(_ color: UIColor, widgetId: String) {
        if let colorData = try? NSKeyedArchiver.archivedData(withRootObject: color, requiringSecureCoding: false) {
            defaults.set(colorData, forKey: "bg_color_\(widgetId)")
        }
    }

    static func getBackgroundColor(widgetId: String) -> UIColor? {
        guard let colorData = defaults.data(forKey: "bg_color_\(widgetId)") else {
            return nil
        }
        return try? NSKeyedUnarchiver.unarchivedObject(ofClass: UIColor.self, from: colorData)
    }

    static func setBackgroundAlpha(_ alpha: Float, widgetId: String) {
        defaults.set(alpha, forKey: "bg_alpha_\(widgetId)")
    }

    static func getBackgroundAlpha(widgetId: String) -> Float {
        let alpha = defaults.float(forKey: "bg_alpha_\(widgetId)")
        return alpha > 0 ? alpha : 1.0
    }

    static func setBorderColor(_ color: UIColor, widgetId: String) {
        if let colorData = try? NSKeyedArchiver.archivedData(withRootObject: color, requiringSecureCoding: false) {
            defaults.set(colorData, forKey: "border_color_\(widgetId)")
        }
    }

    static func getBorderColor(widgetId: String) -> UIColor? {
        guard let colorData = defaults.data(forKey: "border_color_\(widgetId)") else {
            return nil
        }
        return try? NSKeyedUnarchiver.unarchivedObject(ofClass: UIColor.self, from: colorData)
    }

    static func setBorderWidth(_ width: Float, widgetId: String) {
        defaults.set(width, forKey: "border_width_\(widgetId)")
    }

    static func getBorderWidth(widgetId: String) -> Float {
        let width = defaults.float(forKey: "border_width_\(widgetId)")
        return width > 0 ? width : 1.0
    }
}