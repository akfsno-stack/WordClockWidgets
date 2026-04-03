package com.akfsno.wordclockwidgets;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.util.Calendar;

public abstract class BaseWordClockWidgetProvider extends AppWidgetProvider {

    public static final String UPDATE_ACTION = "UPDATE_WIDGET";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (Intent.ACTION_TIME_TICK.equals(intent.getAction()) || Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction()) || UPDATE_ACTION.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, this.getClass());
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            if (appWidgetIds != null && appWidgetIds.length > 0) {
                onUpdate(context, appWidgetManager, appWidgetIds);
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, this.getClass());
        intent.setAction(UPDATE_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    protected abstract int getLayoutResource(Context context, int appWidgetId);

    protected abstract void setTexts(RemoteViews views,
                                     String hourText,
                                     String minuteText,
                                     String dayNightText,
                                     String dayOfWeekText,
                                     String dateText);

    protected abstract int getDefaultTextColor();

    protected abstract int getDefaultBorderColor();

    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), getLayoutResource(context, appWidgetId));

        Calendar calendar = Calendar.getInstance();

        boolean use12Hour = WidgetPreferences.getUse12HourFormat(context, appWidgetId, true);
        int hour24 = calendar.get(Calendar.HOUR_OF_DAY);

        boolean showHour = WidgetPreferences.getShowHour(context, appWidgetId, true);
        boolean showMinute = WidgetPreferences.getShowMinute(context, appWidgetId, true);
        boolean showDayNight = WidgetPreferences.getShowDayNight(context, appWidgetId, true);
        boolean showDate = WidgetPreferences.getShowDate(context, appWidgetId, true);
        boolean showDayOfWeek = WidgetPreferences.getShowDayOfWeek(context, appWidgetId, true);

        boolean addZeroMinute = WidgetPreferences.getAddZeroMinute(context, appWidgetId, false);

        String hourText = use12Hour ? NumberToWords.convertHour(hour24) : NumberToWords.convertHour24(hour24);
        String minuteText = NumberToWords.convertMinute(calendar.get(Calendar.MINUTE), addZeroMinute, use12Hour);

        if (!use12Hour && hour24 == 0 && calendar.get(Calendar.MINUTE) == 0) {
            hourText = "двенадцать";
            minuteText = "ноль-ноль";
        }

        String dayNightText = NumberToWords.getDayNight(hour24);
        String dayOfWeekText = NumberToWords.getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK) - 1);
        String dateText = NumberToWords.convertDate(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));

        setTexts(views, hourText, minuteText, dayNightText, dayOfWeekText, dateText);

        int hourDx = 0;
        int hourDy = 0;
        int minuteDx = 0;
        int minuteDy = 0;
        int dayNightDx = 0;
        int dayNightDy = 0;
        int dateDx = 0;
        int dateDy = 0;
        int dayOfWeekDx = 0;
        int dayOfWeekDy = 0;

        boolean useConstructor = WidgetPreferences.getUseConstructorLayout(context, appWidgetId, false);
        if (useConstructor) {
            hourDx = WidgetPreferences.constrainOffset(WidgetPreferences.getOffsetX(context, appWidgetId, "hour", 0));
            hourDy = WidgetPreferences.constrainOffset(WidgetPreferences.getOffsetY(context, appWidgetId, "hour", 0));
            minuteDx = WidgetPreferences.constrainOffset(WidgetPreferences.getOffsetX(context, appWidgetId, "minute", 0));
            minuteDy = WidgetPreferences.constrainOffset(WidgetPreferences.getOffsetY(context, appWidgetId, "minute", 0));
            dayNightDx = WidgetPreferences.constrainOffset(WidgetPreferences.getDayNightOffsetX(context, appWidgetId, 0));
            dayNightDy = WidgetPreferences.constrainOffset(WidgetPreferences.getDayNightOffsetY(context, appWidgetId, 0));
            dateDx = WidgetPreferences.constrainOffset(WidgetPreferences.getDateOffsetX(context, appWidgetId, 0));
            dateDy = WidgetPreferences.constrainOffset(WidgetPreferences.getDateOffsetY(context, appWidgetId, 0));
            dayOfWeekDx = WidgetPreferences.constrainOffset(WidgetPreferences.getDayOfWeekOffsetX(context, appWidgetId, 0));
            dayOfWeekDy = WidgetPreferences.constrainOffset(WidgetPreferences.getDayOfWeekOffsetY(context, appWidgetId, 0));
        }

        // Apply user-specified offset modifiers on top of constructor values (or center defaults).
        if (use12Hour) {
            hourDx += 40;
            hourDy += 40;
        } else {
            hourDy += 40;
        }
        minuteDy += 40;

        applyTranslationToWrapper(views, R.id.hour_wrapper, hourDx, hourDy);
        applyTranslationToWrapper(views, R.id.minute_wrapper, minuteDx, minuteDy);
        applyTranslationToWrapper(views, R.id.day_night_wrapper, dayNightDx, dayNightDy);
        applyTranslationToWrapper(views, R.id.date_wrapper, dateDx, dateDy);
        applyTranslationToWrapper(views, R.id.day_of_week_wrapper, dayOfWeekDx, dayOfWeekDy);

        int hourColor = WidgetPreferences.getHourTextColor(context, appWidgetId, getDefaultTextColor());
        int minuteColor = WidgetPreferences.getMinuteTextColor(context, appWidgetId, getDefaultTextColor());
        int dayNightColor = WidgetPreferences.getDayNightTextColor(context, appWidgetId, getDefaultBorderColor());
        int dateColor = WidgetPreferences.getDateTextColor(context, appWidgetId, getDefaultTextColor());
        int dayOfWeekColor = WidgetPreferences.getDayOfWeekTextColor(context, appWidgetId, getDefaultTextColor());

        views.setTextColor(R.id.hour_text, hourColor);
        views.setTextColor(R.id.minute_text, minuteColor);
        views.setTextColor(R.id.day_night_text, dayNightColor);
        views.setTextColor(R.id.date_text, dateColor);
        views.setTextColor(R.id.day_of_week_text, dayOfWeekColor);

        views.setViewVisibility(R.id.hour_text, showHour ? android.view.View.VISIBLE : android.view.View.GONE);
        views.setViewVisibility(R.id.minute_text, showMinute ? android.view.View.VISIBLE : android.view.View.GONE);
        views.setViewVisibility(R.id.day_night_text, showDayNight ? android.view.View.VISIBLE : android.view.View.GONE);
        views.setViewVisibility(R.id.date_text, showDate ? android.view.View.VISIBLE : android.view.View.GONE);
        views.setViewVisibility(R.id.day_of_week_text, showDayOfWeek ? android.view.View.VISIBLE : android.view.View.GONE);

        int bgColor = WidgetPreferences.getBackgroundColor(context, appWidgetId, 0xFFFFFFFF);
        int alpha = WidgetPreferences.getBackgroundAlpha(context, appWidgetId, 255);
        bgColor = (bgColor & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
        views.setInt(R.id.widget_container, "setBackgroundColor", bgColor);

        int borderColor = WidgetPreferences.getBorderColor(context, appWidgetId, getDefaultBorderColor());
        int borderWidth = WidgetPreferences.getBorderWidth(context, appWidgetId, 2);
        // RemoteViews cannot easily change drawable stroke, but at least apply padding to mimic border width and update bg
        views.setViewPadding(R.id.widget_border, borderWidth, borderWidth, borderWidth, borderWidth);
        views.setInt(R.id.widget_border, "setBackgroundColor", borderColor);

        Intent configIntent = new Intent(context, WidgetConfigureActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        configIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);

        scheduleNextMinute(context);
    }

    private void applyPaddingToWrapper(RemoteViews views, int wrapperViewId, int offsetX, int offsetY) {
        int left = Math.max(0, offsetX);
        int top = Math.max(0, offsetY);
        int right = Math.max(0, -offsetX);
        int bottom = Math.max(0, -offsetY);
        views.setViewPadding(wrapperViewId, left, top, right, bottom);
    }

    private void applyTranslationToWrapper(RemoteViews views, int wrapperViewId, int offsetX, int offsetY) {
        // RemoteViews does not support direct translation, so use padding as approximation
        applyPaddingToWrapper(views, wrapperViewId, offsetX, offsetY);
    }

    public static void updateLocalWidgetView(Context context, android.view.View rootView, int appWidgetId) {
        if (context == null || rootView == null) return;

        java.util.Calendar calendar = java.util.Calendar.getInstance();

        boolean use12Hour = WidgetPreferences.getUse12HourFormat(context, appWidgetId, true);
        int hour24 = calendar.get(java.util.Calendar.HOUR_OF_DAY);

        boolean showHour = WidgetPreferences.getShowHour(context, appWidgetId, true);
        boolean showMinute = WidgetPreferences.getShowMinute(context, appWidgetId, true);
        boolean showDayNight = WidgetPreferences.getShowDayNight(context, appWidgetId, true);
        boolean showDate = WidgetPreferences.getShowDate(context, appWidgetId, false);
        boolean showDayOfWeek = WidgetPreferences.getShowDayOfWeek(context, appWidgetId, false);

        boolean addZeroMinute = WidgetPreferences.getAddZeroMinute(context, appWidgetId, false);

        String hourText = use12Hour ? NumberToWords.convertHour(hour24) : NumberToWords.convertHour24(hour24);
        String minuteText = NumberToWords.convertMinute(calendar.get(java.util.Calendar.MINUTE), addZeroMinute, use12Hour);

        if (!use12Hour && hour24 == 0 && calendar.get(java.util.Calendar.MINUTE) == 0) {
            hourText = "двенадцать";
            minuteText = "ноль-ноль";
        }

        String dayNightText = NumberToWords.getDayNight(hour24);
        String dayOfWeekText = NumberToWords.getDayOfWeek(calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1);
        String dateText = NumberToWords.convertDate(calendar.get(java.util.Calendar.DAY_OF_MONTH), calendar.get(java.util.Calendar.MONTH) + 1, calendar.get(java.util.Calendar.YEAR));

        android.widget.TextView hourView = rootView.findViewById(R.id.hour_text);
        android.widget.TextView minuteView = rootView.findViewById(R.id.minute_text);
        android.widget.TextView dayNightView = rootView.findViewById(R.id.day_night_text);
        android.widget.TextView dateView = rootView.findViewById(R.id.date_text);
        android.widget.TextView dayOfWeekView = rootView.findViewById(R.id.day_of_week_text);

        if (hourView != null) {
            hourView.setText(hourText);
            hourView.setTextSize(WidgetPreferences.getFontSize(context, appWidgetId, 24f));
            hourView.setTextColor(WidgetPreferences.getHourTextColor(context, appWidgetId, android.graphics.Color.BLACK));
            hourView.setVisibility(showHour ? android.view.View.VISIBLE : android.view.View.GONE);
        }
        if (minuteView != null) {
            minuteView.setText(minuteText);
            minuteView.setTextSize(WidgetPreferences.getMinuteFontSize(context, appWidgetId, 24f));
            minuteView.setTextColor(WidgetPreferences.getMinuteTextColor(context, appWidgetId, android.graphics.Color.BLACK));
            minuteView.setVisibility(showMinute ? android.view.View.VISIBLE : android.view.View.GONE);
        }
        if (dayNightView != null) {
            dayNightView.setText(dayNightText);
            dayNightView.setTextSize(WidgetPreferences.getDayNightFontSize(context, appWidgetId, 18f));
            dayNightView.setTextColor(WidgetPreferences.getDayNightTextColor(context, appWidgetId, android.graphics.Color.RED));
            dayNightView.setVisibility(showDayNight ? android.view.View.VISIBLE : android.view.View.GONE);
        }
        if (dateView != null) {
            dateView.setText(dateText);
            dateView.setTextSize(WidgetPreferences.getDateFontSize(context, appWidgetId, 18f));
            dateView.setTextColor(WidgetPreferences.getDateTextColor(context, appWidgetId, android.graphics.Color.BLACK));
            dateView.setVisibility(showDate ? android.view.View.VISIBLE : android.view.View.GONE);
        }
        if (dayOfWeekView != null) {
            dayOfWeekView.setText(dayOfWeekText);
            dayOfWeekView.setTextSize(WidgetPreferences.getDayOfWeekFontSize(context, appWidgetId, 18f));
            dayOfWeekView.setTextColor(WidgetPreferences.getDayOfWeekTextColor(context, appWidgetId, android.graphics.Color.BLACK));
            dayOfWeekView.setVisibility(showDayOfWeek ? android.view.View.VISIBLE : android.view.View.GONE);
        }

        int hourDx = 0;
        int hourDy = 0;
        int minuteDx = 0;
        int minuteDy = 0;
        int dayNightDx = 0;
        int dayNightDy = 0;
        int dateDx = 0;
        int dateDy = 0;
        int dayOfWeekDx = 0;
        int dayOfWeekDy = 0;

        boolean useConstructor = WidgetPreferences.getUseConstructorLayout(context, appWidgetId, false);
        if (useConstructor) {
            hourDx = WidgetPreferences.constrainOffset(WidgetPreferences.getOffsetX(context, appWidgetId, "hour", 0));
            hourDy = WidgetPreferences.constrainOffset(WidgetPreferences.getOffsetY(context, appWidgetId, "hour", 0));
            minuteDx = WidgetPreferences.constrainOffset(WidgetPreferences.getOffsetX(context, appWidgetId, "minute", 0));
            minuteDy = WidgetPreferences.constrainOffset(WidgetPreferences.getOffsetY(context, appWidgetId, "minute", 0));
            dayNightDx = WidgetPreferences.constrainOffset(WidgetPreferences.getDayNightOffsetX(context, appWidgetId, 0));
            dayNightDy = WidgetPreferences.constrainOffset(WidgetPreferences.getDayNightOffsetY(context, appWidgetId, 0));
            dateDx = WidgetPreferences.constrainOffset(WidgetPreferences.getDateOffsetX(context, appWidgetId, 0));
            dateDy = WidgetPreferences.constrainOffset(WidgetPreferences.getDateOffsetY(context, appWidgetId, 0));
            dayOfWeekDx = WidgetPreferences.constrainOffset(WidgetPreferences.getDayOfWeekOffsetX(context, appWidgetId, 0));
            dayOfWeekDy = WidgetPreferences.constrainOffset(WidgetPreferences.getDayOfWeekOffsetY(context, appWidgetId, 0));
        }

        if (use12Hour) {
            hourDx += 40;
            hourDy += 40;
        } else {
            hourDy += 40;
        }
        minuteDy += 40;

        if (!useConstructor) {
            dateDx = 0;
            dateDy = 0;
            dayOfWeekDx = 0;
            dayOfWeekDy = 0;
        }

        hourDx = WidgetPreferences.constrainOffset(hourDx);
        hourDy = WidgetPreferences.constrainOffset(hourDy);
        minuteDx = WidgetPreferences.constrainOffset(minuteDx);
        minuteDy = WidgetPreferences.constrainOffset(minuteDy);
        dayNightDx = WidgetPreferences.constrainOffset(dayNightDx);
        dayNightDy = WidgetPreferences.constrainOffset(dayNightDy);
        dateDx = WidgetPreferences.constrainOffset(dateDx);
        dateDy = WidgetPreferences.constrainOffset(dateDy);
        dayOfWeekDx = WidgetPreferences.constrainOffset(dayOfWeekDx);
        dayOfWeekDy = WidgetPreferences.constrainOffset(dayOfWeekDy);

        applyLocalPadding(rootView, R.id.hour_wrapper, hourDx, hourDy);
        applyLocalPadding(rootView, R.id.minute_wrapper, minuteDx, minuteDy);
        applyLocalPadding(rootView, R.id.day_night_wrapper, dayNightDx, dayNightDy);
        applyLocalPadding(rootView, R.id.date_wrapper, dateDx, dateDy);
        applyLocalPadding(rootView, R.id.day_of_week_wrapper, dayOfWeekDx, dayOfWeekDy);

        int bgColor = WidgetPreferences.getBackgroundColor(context, appWidgetId, 0xFFFFFFFF);
        int alpha = WidgetPreferences.getBackgroundAlpha(context, appWidgetId, 255);
        bgColor = (bgColor & 0x00FFFFFF) | ((alpha & 0xFF) << 24);

        android.view.View container = rootView.findViewById(R.id.widget_container);
        if (container != null) {
            container.setBackgroundColor(bgColor);
        }

        android.view.View borderView = rootView.findViewById(R.id.widget_border);
        if (borderView != null && borderView.getBackground() instanceof android.graphics.drawable.GradientDrawable) {
            android.graphics.drawable.GradientDrawable drawable = (android.graphics.drawable.GradientDrawable) borderView.getBackground().mutate();
            int borderColor = WidgetPreferences.getBorderColor(context, appWidgetId, 0xFF0000FF);
            int borderWidth = WidgetPreferences.getBorderWidth(context, appWidgetId, 2);
            drawable.setStroke(borderWidth, borderColor);
            borderView.setBackground(drawable);
        }
    }

    private static void applyLocalPadding(android.view.View rootView, int viewId, int offsetX, int offsetY) {
        android.view.View wrapper = rootView.findViewById(viewId);
        if (wrapper != null) {
            int left = Math.max(0, offsetX);
            int top = Math.max(0, offsetY);
            int right = Math.max(0, -offsetX);
            int bottom = Math.max(0, -offsetY);
            wrapper.setPadding(left, top, right, bottom);
        }
    }

    private void scheduleNextMinute(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent updateIntent = new Intent(context, this.getClass());
        updateIntent.setAction(UPDATE_ACTION);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        long currentMillis = System.currentTimeMillis();
        long currentSeconds = currentMillis / 1000;
        long secondsToNextMinute = 60 - (currentSeconds % 60);
        long nextUpdateMillis = currentMillis + secondsToNextMinute * 1000;
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC, nextUpdateMillis, 60000, alarmPendingIntent);
        }
    }
}
