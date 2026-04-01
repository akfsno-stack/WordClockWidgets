package com.akfsno.wordclockwidgets;

import android.content.Context;
import android.content.SharedPreferences;

public class WidgetPreferences {

    private static final String PREFS_NAME = "WidgetPrefs";

    public static void saveColor(Context context, int appWidgetId, int color) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("color_" + appWidgetId, color).apply();
    }

    public static int getColor(Context context, int appWidgetId, int defaultColor) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("color_" + appWidgetId, defaultColor);
    }

    public static void saveFontSize(Context context, int appWidgetId, float fontSize) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putFloat("fontSize_" + appWidgetId, fontSize).apply();
    }

    public static float getFontSize(Context context, int appWidgetId, float defaultSize) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat("fontSize_" + appWidgetId, defaultSize);
    }

    public static void saveOffsetX(Context context, int appWidgetId, String key, int value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(key + "_offsetX_" + appWidgetId, value).apply();
    }

    public static int getOffsetX(Context context, int appWidgetId, String key, int defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(key + "_offsetX_" + appWidgetId, defaultValue);
    }

    public static void saveOffsetY(Context context, int appWidgetId, String key, int value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(key + "_offsetY_" + appWidgetId, value).apply();
    }

    public static int getOffsetY(Context context, int appWidgetId, String key, int defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(key + "_offsetY_" + appWidgetId, defaultValue);
    }

    public static void saveFontType(Context context, int appWidgetId, String fontType) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("fontType_" + appWidgetId, fontType).apply();
    }

    public static String getFontType(Context context, int appWidgetId, String defaultType) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("fontType_" + appWidgetId, defaultType);
    }

    public static void saveLineSpacing(Context context, int appWidgetId, float spacing) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putFloat("lineSpacing_" + appWidgetId, spacing).apply();
    }

    public static float getLineSpacing(Context context, int appWidgetId, float defaultSpacing) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat("lineSpacing_" + appWidgetId, defaultSpacing);
    }

    public static void saveStyle(Context context, int appWidgetId, String style) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("style_" + appWidgetId, style).apply();
    }

    public static String getStyle(Context context, int appWidgetId, String defaultStyle) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("style_" + appWidgetId, defaultStyle);
    }

    public static void saveAddZero(Context context, int appWidgetId, boolean addZero) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("addZero_" + appWidgetId, addZero).apply();
    }

    public static boolean getAddZero(Context context, int appWidgetId, boolean defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("addZero_" + appWidgetId, defaultValue);
    }

    public static void saveAddZeroMinute(Context context, int appWidgetId, boolean addZero) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("addZeroMinute_" + appWidgetId, addZero).apply();
    }

    public static boolean getAddZeroMinute(Context context, int appWidgetId, boolean defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("addZeroMinute_" + appWidgetId, defaultValue);
    }

    public static void saveAddZeroSecond(Context context, int appWidgetId, boolean addZero) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("addZeroSecond_" + appWidgetId, addZero).apply();
    }

    public static boolean getAddZeroSecond(Context context, int appWidgetId, boolean defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("addZeroSecond_" + appWidgetId, defaultValue);
    }

    public static void saveShowHour(Context context, int appWidgetId, boolean showHour) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("showHour_" + appWidgetId, showHour).apply();
    }

    public static boolean getShowHour(Context context, int appWidgetId, boolean defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("showHour_" + appWidgetId, defaultValue);
    }

    public static void saveShowMinute(Context context, int appWidgetId, boolean showMinute) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("showMinute_" + appWidgetId, showMinute).apply();
    }

    public static boolean getShowMinute(Context context, int appWidgetId, boolean defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("showMinute_" + appWidgetId, defaultValue);
    }

    public static void saveShowSeconds(Context context, int appWidgetId, boolean showSeconds) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("showSeconds_" + appWidgetId, showSeconds).apply();
    }

    public static boolean getShowSeconds(Context context, int appWidgetId, boolean defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("showSeconds_" + appWidgetId, defaultValue);
    }

    public static void saveShowDayNight(Context context, int appWidgetId, boolean showDayNight) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("showDayNight_" + appWidgetId, showDayNight).apply();
    }

    public static boolean getShowDayNight(Context context, int appWidgetId, boolean defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("showDayNight_" + appWidgetId, defaultValue);
    }

    public static void saveShowDate(Context context, int appWidgetId, boolean showDate) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("showDate_" + appWidgetId, showDate).apply();
    }

    public static boolean getShowDate(Context context, int appWidgetId, boolean defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("showDate_" + appWidgetId, defaultValue);
    }

    public static void saveShowDayOfWeek(Context context, int appWidgetId, boolean showDayOfWeek) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("showDayOfWeek_" + appWidgetId, showDayOfWeek).apply();
    }

    public static boolean getShowDayOfWeek(Context context, int appWidgetId, boolean defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("showDayOfWeek_" + appWidgetId, defaultValue);
    }

    public static void saveHourTextColor(Context context, int appWidgetId, int color) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("hourTextColor_" + appWidgetId, color).apply();
    }

    public static int getHourTextColor(Context context, int appWidgetId, int defaultColor) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("hourTextColor_" + appWidgetId, defaultColor);
    }

    public static void saveMinuteTextColor(Context context, int appWidgetId, int color) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("minuteTextColor_" + appWidgetId, color).apply();
    }

    public static int getMinuteTextColor(Context context, int appWidgetId, int defaultColor) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("minuteTextColor_" + appWidgetId, defaultColor);
    }

    public static void saveSecondTextColor(Context context, int appWidgetId, int color) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("secondTextColor_" + appWidgetId, color).apply();
    }

    public static int getSecondTextColor(Context context, int appWidgetId, int defaultColor) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("secondTextColor_" + appWidgetId, defaultColor);
    }

    public static void saveDayNightTextColor(Context context, int appWidgetId, int color) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("dayNightTextColor_" + appWidgetId, color).apply();
    }

    public static int getDayNightTextColor(Context context, int appWidgetId, int defaultColor) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("dayNightTextColor_" + appWidgetId, defaultColor);
    }

    public static void saveDateTextColor(Context context, int appWidgetId, int color) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("dateTextColor_" + appWidgetId, color).apply();
    }

    public static int getDateTextColor(Context context, int appWidgetId, int defaultColor) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("dateTextColor_" + appWidgetId, defaultColor);
    }

    public static void saveDayOfWeekTextColor(Context context, int appWidgetId, int color) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("dayOfWeekTextColor_" + appWidgetId, color).apply();
    }

    public static int getDayOfWeekTextColor(Context context, int appWidgetId, int defaultColor) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("dayOfWeekTextColor_" + appWidgetId, defaultColor);
    }

    public static void saveBorderColor(Context context, int appWidgetId, int color) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("borderColor_" + appWidgetId, color).apply();
    }

    public static int getBorderColor(Context context, int appWidgetId, int defaultColor) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("borderColor_" + appWidgetId, defaultColor);
    }

    public static void saveBorderWidth(Context context, int appWidgetId, int width) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("borderWidth_" + appWidgetId, width).apply();
    }

    public static int getBorderWidth(Context context, int appWidgetId, int defaultWidth) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("borderWidth_" + appWidgetId, defaultWidth);
    }

    public static void saveBackgroundColor(Context context, int appWidgetId, int color) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("backgroundColor_" + appWidgetId, color).apply();
    }

    public static int getBackgroundColor(Context context, int appWidgetId, int defaultColor) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("backgroundColor_" + appWidgetId, defaultColor);
    }

    public static void saveBackgroundAlpha(Context context, int appWidgetId, int alpha) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("backgroundAlpha_" + appWidgetId, alpha).apply();
    }

    public static int getBackgroundAlpha(Context context, int appWidgetId, int defaultAlpha) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("backgroundAlpha_" + appWidgetId, defaultAlpha);
    }

    public static void saveUse12HourFormat(Context context, int appWidgetId, boolean use12Hour) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("use12HourFormat_" + appWidgetId, use12Hour).apply();
    }

    public static boolean getUse12HourFormat(Context context, int appWidgetId, boolean defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("use12HourFormat_" + appWidgetId, defaultValue);
    }

    public static void saveSecondsAsWords(Context context, int appWidgetId, boolean secondsAsWords) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("secondsAsWords_" + appWidgetId, secondsAsWords).apply();
    }

    public static boolean getSecondsAsWords(Context context, int appWidgetId, boolean defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("secondsAsWords_" + appWidgetId, defaultValue);
    }

    public static void saveMinuteFontSize(Context context, int appWidgetId, float fontSize) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putFloat("minuteFontSize_" + appWidgetId, fontSize).apply();
    }

    public static float getMinuteFontSize(Context context, int appWidgetId, float defaultSize) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat("minuteFontSize_" + appWidgetId, defaultSize);
    }

    public static void saveSecondFontSize(Context context, int appWidgetId, float fontSize) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putFloat("secondFontSize_" + appWidgetId, fontSize).apply();
    }

    public static float getSecondFontSize(Context context, int appWidgetId, float defaultSize) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat("secondFontSize_" + appWidgetId, defaultSize);
    }

    public static void saveSecondOffsetX(Context context, int appWidgetId, int value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("second_offsetX_" + appWidgetId, value).apply();
    }

    public static int getSecondOffsetX(Context context, int appWidgetId, int defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("second_offsetX_" + appWidgetId, defaultValue);
    }

    public static void saveSecondOffsetY(Context context, int appWidgetId, int value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("second_offsetY_" + appWidgetId, value).apply();
    }

    public static int getSecondOffsetY(Context context, int appWidgetId, int defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("second_offsetY_" + appWidgetId, defaultValue);
    }

    public static void saveDateOffsetX(Context context, int appWidgetId, int value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("date_offsetX_" + appWidgetId, value).apply();
    }

    public static int getDateOffsetX(Context context, int appWidgetId, int defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("date_offsetX_" + appWidgetId, defaultValue);
    }

    public static void saveDateOffsetY(Context context, int appWidgetId, int value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("date_offsetY_" + appWidgetId, value).apply();
    }

    public static int getDateOffsetY(Context context, int appWidgetId, int defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("date_offsetY_" + appWidgetId, defaultValue);
    }

    public static void saveDayOfWeekOffsetX(Context context, int appWidgetId, int value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("dayOfWeek_offsetX_" + appWidgetId, value).apply();
    }

    public static int getDayOfWeekOffsetX(Context context, int appWidgetId, int defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("dayOfWeek_offsetX_" + appWidgetId, defaultValue);
    }

    public static void saveDayOfWeekOffsetY(Context context, int appWidgetId, int value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("dayOfWeek_offsetY_" + appWidgetId, value).apply();
    }

    public static int getDayOfWeekOffsetY(Context context, int appWidgetId, int defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("dayOfWeek_offsetY_" + appWidgetId, defaultValue);
    }

    public static void saveDayNightOffsetX(Context context, int appWidgetId, int value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("dayNight_offsetX_" + appWidgetId, value).apply();
    }

    public static int getDayNightOffsetX(Context context, int appWidgetId, int defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("dayNight_offsetX_" + appWidgetId, defaultValue);
    }

    public static void saveDayNightOffsetY(Context context, int appWidgetId, int value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("dayNight_offsetY_" + appWidgetId, value).apply();
    }

    public static int getDayNightOffsetY(Context context, int appWidgetId, int defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("dayNight_offsetY_" + appWidgetId, defaultValue);
    }

    public static void saveSecondsDisplayMode(Context context, int appWidgetId, String mode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("secondsDisplayMode_" + appWidgetId, mode).apply();
    }

    public static String getSecondsDisplayMode(Context context, int appWidgetId, String defaultMode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("secondsDisplayMode_" + appWidgetId, defaultMode);
    }

    public static void saveBlockMode(Context context, int appWidgetId, String mode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("blockMode_" + appWidgetId, mode).apply();
    }

    public static String getBlockMode(Context context, int appWidgetId, String defaultMode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("blockMode_" + appWidgetId, defaultMode);
    }

    public static void saveBlockBackgroundColor(Context context, int appWidgetId, int color) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("blockBackgroundColor_" + appWidgetId, color).apply();
    }

    public static int getBlockBackgroundColor(Context context, int appWidgetId, int defaultColor) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("blockBackgroundColor_" + appWidgetId, defaultColor);
    }

    public static void saveBlockBorderColor(Context context, int appWidgetId, int color) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("blockBorderColor_" + appWidgetId, color).apply();
    }

    public static int getBlockBorderColor(Context context, int appWidgetId, int defaultColor) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("blockBorderColor_" + appWidgetId, defaultColor);
    }

    // Constants for offset bounds
    private static final int MAX_OFFSET = 500;
    private static final int MIN_OFFSET = -500;

    public static int constrainOffset(int value) {
        return Math.max(MIN_OFFSET, Math.min(MAX_OFFSET, value));
    }

    public static int getMaxOffset() {
        return MAX_OFFSET;
    }

    public static int getMinOffset() {
        return MIN_OFFSET;
    }
}