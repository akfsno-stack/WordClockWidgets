package com.akfsno.wordclockwidgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public abstract class BaseWordClockWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    protected abstract int getLayoutResource(Context context, int appWidgetId);

    protected abstract void setTexts(RemoteViews views,
                                     String hourText,
                                     String minuteText,
                                     String secondText,
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
        int hour12 = calendar.get(Calendar.HOUR);
        if (hour12 == 0) hour12 = 12;

        boolean showHour = WidgetPreferences.getShowHour(context, appWidgetId, true);
        boolean showMinute = WidgetPreferences.getShowMinute(context, appWidgetId, true);
        boolean showSecond = WidgetPreferences.getShowSeconds(context, appWidgetId, false);
        boolean showDayNight = WidgetPreferences.getShowDayNight(context, appWidgetId, true);
        boolean showDate = WidgetPreferences.getShowDate(context, appWidgetId, true);
        boolean showDayOfWeek = WidgetPreferences.getShowDayOfWeek(context, appWidgetId, true);

        boolean addZeroMinute = WidgetPreferences.getAddZeroMinute(context, appWidgetId, false);
        boolean addZeroSecond = WidgetPreferences.getAddZeroSecond(context, appWidgetId, false);

        String hourText = use12Hour ? NumberToWords.convertHour(hour24) : NumberToWords.convertHour24(hour24);
        String minuteText = NumberToWords.convertMinute(calendar.get(Calendar.MINUTE), addZeroMinute);
        String secondText = NumberToWords.convertSecond(calendar.get(Calendar.SECOND), true, addZeroSecond);
        String dayNightText = NumberToWords.getDayNight(hour24);
        String dayOfWeekText = NumberToWords.getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK) - 1);
        String dateText = NumberToWords.convertDate(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));

        setTexts(views, hourText, minuteText, secondText, dayNightText, dayOfWeekText, dateText);

        int hourColor = WidgetPreferences.getHourTextColor(context, appWidgetId, getDefaultTextColor());
        int minuteColor = WidgetPreferences.getMinuteTextColor(context, appWidgetId, getDefaultTextColor());
        int secondColor = WidgetPreferences.getSecondTextColor(context, appWidgetId, getDefaultTextColor());
        int dayNightColor = WidgetPreferences.getDayNightTextColor(context, appWidgetId, getDefaultBorderColor());
        int dateColor = WidgetPreferences.getDateTextColor(context, appWidgetId, getDefaultTextColor());
        int dayOfWeekColor = WidgetPreferences.getDayOfWeekTextColor(context, appWidgetId, getDefaultTextColor());

        views.setTextColor(R.id.hour_text, hourColor);
        views.setTextColor(R.id.minute_text, minuteColor);
        views.setTextColor(R.id.second_text, secondColor);
        views.setTextColor(R.id.day_night_text, dayNightColor);
        views.setTextColor(R.id.date_text, dateColor);
        views.setTextColor(R.id.day_of_week_text, dayOfWeekColor);

        views.setViewVisibility(R.id.hour_text, showHour ? android.view.View.VISIBLE : android.view.View.GONE);
        views.setViewVisibility(R.id.minute_text, showMinute ? android.view.View.VISIBLE : android.view.View.GONE);
        views.setViewVisibility(R.id.second_text, showSecond ? android.view.View.VISIBLE : android.view.View.GONE);
        views.setViewVisibility(R.id.day_night_text, showDayNight ? android.view.View.VISIBLE : android.view.View.GONE);
        views.setViewVisibility(R.id.date_text, showDate ? android.view.View.VISIBLE : android.view.View.GONE);
        views.setViewVisibility(R.id.day_of_week_text, showDayOfWeek ? android.view.View.VISIBLE : android.view.View.GONE);

        int bgColor = WidgetPreferences.getBackgroundColor(context, appWidgetId, 0xFFFFFFFF);
        int alpha = WidgetPreferences.getBackgroundAlpha(context, appWidgetId, 255);
        bgColor = (bgColor & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
        views.setInt(R.id.widget_container, "setBackgroundColor", bgColor);

        int borderColor = WidgetPreferences.getBorderColor(context, appWidgetId, getDefaultBorderColor());
        int borderWidth = WidgetPreferences.getBorderWidth(context, appWidgetId, 2);
        // Note: we keep frame decoration by setting a solid background tint (if shape not fully supported)
        views.setInt(R.id.widget_border, "setBackgroundColor", borderColor);

        Intent configIntent = new Intent(context, WidgetConfigureActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        configIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);

        // force new update for the next minute
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_container);
    }
}