package com.akfsno.wordclockwidgets;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;
import java.util.Calendar;
import java.util.TimeZone;

public abstract class BaseWordClockWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_UPDATE_TICK = "com.akfsno.wordclockwidgets.ACTION_UPDATE_TICK";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_UPDATE_TICK.equals(intent.getAction())) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName provider = new ComponentName(context, getClass());
            int[] appWidgetIds = manager.getAppWidgetIds(provider);
            if (appWidgetIds.length > 0) {
                onUpdate(context, manager, appWidgetIds);
            }
            scheduleNextTick(context);
        }
    }

    @Override
    public void onEnabled(Context context) {
        scheduleNextTick(context);
    }

    @Override
    public void onDisabled(Context context) {
        cancelTick(context);
    }

    private void scheduleNextTick(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, getClass());
        intent.setAction(ACTION_UPDATE_TICK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long triggerAtMillis = System.currentTimeMillis() + 1000;
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        }
    }

    private void cancelTick(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, getClass());
        intent.setAction(ACTION_UPDATE_TICK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    protected abstract int getLayoutResource();

    protected abstract void setTexts(RemoteViews views, String hourText, String minuteText, String dayNightText, String dayOfWeekText, String dateText);

    protected abstract int getDefaultTextColor();

    protected abstract int getDefaultBorderColor();

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+3"));
        boolean use12Hour = WidgetPreferences.getUse12HourFormat(context, appWidgetId, false);
        int rawHour = calendar.get(Calendar.HOUR_OF_DAY);
        int hour = use12Hour ? calendar.get(Calendar.HOUR) : rawHour;
        if (!use12Hour && hour < 0) hour = 0;
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        String hourText = use12Hour ? NumberToWords.convertHour(hour) : NumberToWords.convertHour24(rawHour);
        String minuteText = NumberToWords.convertMinute(minute);
        String dayNightText = NumberToWords.getDayNight(rawHour);
        String dayOfWeekText = NumberToWords.getDayOfWeek(dayOfWeek);
        String dateText = NumberToWords.convertDate(day, month, year);

        boolean showSeconds = WidgetPreferences.getShowSeconds(context, appWidgetId, false);
        boolean showDate = WidgetPreferences.getShowDate(context, appWidgetId, false);
        boolean showDayOfWeek = WidgetPreferences.getShowDayOfWeek(context, appWidgetId, false);

        if (showSeconds) {
            minuteText = minuteText + " " + NumberToWords.convertMinute(second);
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), getLayoutResource());
        setTexts(views, hourText, minuteText, dayNightText, dayOfWeekText, dateText);

        int textColor = WidgetPreferences.getColor(context, appWidgetId, getDefaultTextColor());
        float fontSize = WidgetPreferences.getFontSize(context, appWidgetId, 24f);
        int borderColor = WidgetPreferences.getBorderColor(context, appWidgetId, getDefaultBorderColor());
        int backgroundColor = WidgetPreferences.getBackgroundColor(context, appWidgetId, Color.WHITE);
        int backgroundAlpha = WidgetPreferences.getBackgroundAlpha(context, appWidgetId, 255);
        int bgColor = Color.argb(backgroundAlpha, Color.red(backgroundColor), Color.green(backgroundColor), Color.blue(backgroundColor));
        views.setInt(R.id.widget_container, "setBackgroundColor", bgColor);

        int hourOffsetX = WidgetPreferences.getOffsetX(context, appWidgetId, "hour", 0);
        int hourOffsetY = WidgetPreferences.getOffsetY(context, appWidgetId, "hour", 0);
        int minuteOffsetX = WidgetPreferences.getOffsetX(context, appWidgetId, "minute", 0);
        int minuteOffsetY = WidgetPreferences.getOffsetY(context, appWidgetId, "minute", 0);

        if (getLayoutResource() == R.layout.horizontal_widget_layout) {
            views.setTextColor(R.id.time_text, textColor);
            views.setTextViewTextSize(R.id.time_text, 0, fontSize);
        } else {
            views.setTextColor(R.id.hour_text, textColor);
            views.setTextViewTextSize(R.id.hour_text, 0, fontSize);
            views.setViewPadding(R.id.hour_text, hourOffsetX, hourOffsetY, 0, 0);

            views.setTextColor(R.id.minute_text, textColor);
            views.setTextViewTextSize(R.id.minute_text, 0, fontSize);
            views.setViewPadding(R.id.minute_text, minuteOffsetX, minuteOffsetY, 0, 0);

            views.setTextColor(R.id.day_night_text, borderColor);
            views.setTextViewTextSize(R.id.day_night_text, 0, fontSize * 0.75f);
            views.setViewPadding(R.id.day_night_text, hourOffsetX / 2, hourOffsetY / 2, 0, 0);

            views.setTextColor(R.id.day_of_week_text, textColor);
            views.setTextViewTextSize(R.id.day_of_week_text, 0, fontSize * 0.6f);
            views.setTextColor(R.id.date_text, textColor);
            views.setTextViewTextSize(R.id.date_text, 0, fontSize * 0.5f);

            views.setTextViewText(R.id.day_of_week_text, showDayOfWeek ? dayOfWeekText : "");
            views.setTextViewText(R.id.date_text, showDate ? dateText : "");
            views.setViewVisibility(R.id.day_of_week_text, showDayOfWeek ? View.VISIBLE : View.GONE);
            views.setViewVisibility(R.id.date_text, showDate ? View.VISIBLE : View.GONE);
        }

        Intent configIntent = new Intent(context, WidgetConfigureActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_container, configPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}