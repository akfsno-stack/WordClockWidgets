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
        String action = intent.getAction();
        if (action != null && action.startsWith(ACTION_UPDATE_TICK)) {
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
        
        // Create unique action for each provider class to avoid conflicts
        String action = ACTION_UPDATE_TICK + "." + getClass().getName();
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        
        // Use a unique request code based on action and class name
        int requestCode = (getClass().getName().hashCode()) & 0x7fffffff;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Calculate next second boundary for more accurate updates
        long currentTimeMillis = System.currentTimeMillis();
        long millisUntilNextSecond = 1000 - (currentTimeMillis % 1000);
        long triggerAtMillis = currentTimeMillis + millisUntilNextSecond;
        
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // For Android 12 and above, use setAndAllowWhileIdle
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        }
    }

    private void cancelTick(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        String action = ACTION_UPDATE_TICK + "." + getClass().getName();
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        
        int requestCode = (getClass().getName().hashCode()) & 0x7fffffff;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        scheduleNextTick(context);
    }

    protected abstract int getLayoutResource(Context context, int appWidgetId);

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
        String minuteText = NumberToWords.convertMinute(minute, addZero);
        String dayNightText = NumberToWords.getDayNight(rawHour);
        String dayOfWeekText = NumberToWords.getDayOfWeek(dayOfWeek);
        String dateText = NumberToWords.convertDate(day, month, year);

        boolean showSeconds = WidgetPreferences.getShowSeconds(context, appWidgetId, false);
        boolean showDate = WidgetPreferences.getShowDate(context, appWidgetId, false);
        boolean showDayOfWeek = WidgetPreferences.getShowDayOfWeek(context, appWidgetId, false);
        boolean secondsAsWords = WidgetPreferences.getSecondsAsWords(context, appWidgetId, true);
        boolean addZero = WidgetPreferences.getAddZero(context, appWidgetId, false);
        String secondsDisplayMode = WidgetPreferences.getSecondsDisplayMode(context, appWidgetId, "Горизонтально");

        String secondText;
        if (secondsDisplayMode.equals("Вертикально")) {
            if (secondsAsWords) {
                secondText = android.text.TextUtils.join("\n", NumberToWords.convertSecondVertical(second, true, addZero));
            } else {
                secondText = String.format("%02d", second);
                secondText = secondText.charAt(0) + "\n" + secondText.charAt(1);
            }
        } else {
            secondText = NumberToWords.convertSecond(second, secondsAsWords, addZero);
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), getLayoutResource(context, appWidgetId));
        setTexts(views, hourText, minuteText, dayNightText, dayOfWeekText, dateText);

        int textColor = WidgetPreferences.getColor(context, appWidgetId, getDefaultTextColor());
        float fontSize = WidgetPreferences.getFontSize(context, appWidgetId, 24f);
        float minuteFontSize = WidgetPreferences.getMinuteFontSize(context, appWidgetId, 24f);
        float secondFontSize = WidgetPreferences.getSecondFontSize(context, appWidgetId, 18f);
        int borderColor = WidgetPreferences.getBorderColor(context, appWidgetId, getDefaultBorderColor());
        int backgroundColor = WidgetPreferences.getBackgroundColor(context, appWidgetId, Color.WHITE);
        int backgroundAlpha = WidgetPreferences.getBackgroundAlpha(context, appWidgetId, 255);
        int blockBackgroundColor = WidgetPreferences.getBlockBackgroundColor(context, appWidgetId, Color.TRANSPARENT);
        int blockBorderColor = WidgetPreferences.getBlockBorderColor(context, appWidgetId, Color.GRAY);
        String blockMode = WidgetPreferences.getBlockMode(context, appWidgetId, "Обычный");
        boolean blockEnabled = "Блочная система".equals(blockMode);

        int bgColor = Color.argb(backgroundAlpha, Color.red(backgroundColor), Color.green(backgroundColor), Color.blue(backgroundColor));
        views.setInt(R.id.widget_container, "setBackgroundColor", bgColor);

        int hourOffsetX = WidgetPreferences.getOffsetX(context, appWidgetId, "hour", 0);
        int hourOffsetY = WidgetPreferences.getOffsetY(context, appWidgetId, "hour", 0);
        int minuteOffsetX = WidgetPreferences.getOffsetX(context, appWidgetId, "minute", 0);
        int minuteOffsetY = WidgetPreferences.getOffsetY(context, appWidgetId, "minute", 0);
        int secondOffsetX = WidgetPreferences.getSecondOffsetX(context, appWidgetId, 0);
        int secondOffsetY = WidgetPreferences.getSecondOffsetY(context, appWidgetId, 0);
        int dateOffsetX = WidgetPreferences.getDateOffsetX(context, appWidgetId, 0);
        int dateOffsetY = WidgetPreferences.getDateOffsetY(context, appWidgetId, 0);
        int dayOfWeekOffsetX = WidgetPreferences.getDayOfWeekOffsetX(context, appWidgetId, 0);
        int dayOfWeekOffsetY = WidgetPreferences.getDayOfWeekOffsetY(context, appWidgetId, 0);
        int dayNightOffsetX = WidgetPreferences.getDayNightOffsetX(context, appWidgetId, 0);
        int dayNightOffsetY = WidgetPreferences.getDayNightOffsetY(context, appWidgetId, 0);

        // Constrain offsets to valid range
        hourOffsetX = WidgetPreferences.constrainOffset(hourOffsetX);
        hourOffsetY = WidgetPreferences.constrainOffset(hourOffsetY);
        minuteOffsetX = WidgetPreferences.constrainOffset(minuteOffsetX);
        minuteOffsetY = WidgetPreferences.constrainOffset(minuteOffsetY);
        secondOffsetX = WidgetPreferences.constrainOffset(secondOffsetX);
        secondOffsetY = WidgetPreferences.constrainOffset(secondOffsetY);
        dateOffsetX = WidgetPreferences.constrainOffset(dateOffsetX);
        dateOffsetY = WidgetPreferences.constrainOffset(dateOffsetY);
        dayOfWeekOffsetX = WidgetPreferences.constrainOffset(dayOfWeekOffsetX);
        dayOfWeekOffsetY = WidgetPreferences.constrainOffset(dayOfWeekOffsetY);
        dayNightOffsetX = WidgetPreferences.constrainOffset(dayNightOffsetX);
        dayNightOffsetY = WidgetPreferences.constrainOffset(dayNightOffsetY);

        if (getLayoutResource(context, appWidgetId) == R.layout.horizontal_widget_layout) {
            String timeText = hourText + " : " + minuteText;
            if (showSeconds) {
                timeText += " : " + secondText.replace("\n", " ");
            }
            views.setTextViewText(R.id.time_text, timeText);
            views.setTextColor(R.id.time_text, blockEnabled ? blockBorderColor : textColor);
            views.setTextViewTextSize(R.id.time_text, 0, fontSize);
            if (blockEnabled) {
                views.setInt(R.id.time_text, "setBackgroundColor", blockBackgroundColor);
            }
        } else {
            views.setTextColor(R.id.hour_text, blockEnabled ? blockBorderColor : textColor);
            views.setTextViewTextSize(R.id.hour_text, 0, fontSize);
            views.setViewTranslationX(R.id.hour_text, hourOffsetX);
            views.setViewTranslationY(R.id.hour_text, hourOffsetY);
            if (blockEnabled) {
                views.setInt(R.id.hour_text, "setBackgroundColor", blockBackgroundColor);
            }

            views.setTextColor(R.id.minute_text, blockEnabled ? blockBorderColor : textColor);
            views.setTextViewTextSize(R.id.minute_text, 0, minuteFontSize);
            views.setViewTranslationX(R.id.minute_text, minuteOffsetX);
            views.setViewTranslationY(R.id.minute_text, minuteOffsetY);
            if (blockEnabled) {
                views.setInt(R.id.minute_text, "setBackgroundColor", blockBackgroundColor);
            }

            views.setTextColor(R.id.day_night_text, blockEnabled ? blockBorderColor : textColor);
            views.setTextViewTextSize(R.id.day_night_text, 0, fontSize * 0.75f);
            views.setViewTranslationX(R.id.day_night_text, dayNightOffsetX);
            views.setViewTranslationY(R.id.day_night_text, dayNightOffsetY);
            if (blockEnabled) {
                views.setInt(R.id.day_night_text, "setBackgroundColor", blockBackgroundColor);
            }

            views.setTextColor(R.id.day_of_week_text, blockEnabled ? blockBorderColor : textColor);
            views.setTextViewTextSize(R.id.day_of_week_text, 0, fontSize * 0.6f);
            views.setViewTranslationX(R.id.day_of_week_text, dayOfWeekOffsetX);
            views.setViewTranslationY(R.id.day_of_week_text, dayOfWeekOffsetY);
            if (blockEnabled) {
                views.setInt(R.id.day_of_week_text, "setBackgroundColor", blockBackgroundColor);
            }
            
            views.setTextColor(R.id.date_text, blockEnabled ? blockBorderColor : textColor);
            views.setTextViewTextSize(R.id.date_text, 0, fontSize * 0.5f);
            views.setViewTranslationX(R.id.date_text, dateOffsetX);
            views.setViewTranslationY(R.id.date_text, dateOffsetY);
            if (blockEnabled) {
                views.setInt(R.id.date_text, "setBackgroundColor", blockBackgroundColor);
            }

            views.setTextViewText(R.id.day_of_week_text, showDayOfWeek ? dayOfWeekText : "");
            views.setTextViewText(R.id.date_text, showDate ? dateText : "");
            views.setViewVisibility(R.id.day_of_week_text, showDayOfWeek ? View.VISIBLE : View.GONE);
            views.setViewVisibility(R.id.date_text, showDate ? View.VISIBLE : View.GONE);

            views.setTextColor(R.id.second_text, blockEnabled ? blockBorderColor : textColor);
            views.setTextViewTextSize(R.id.second_text, 0, secondFontSize);
            views.setViewTranslationX(R.id.second_text, secondOffsetX);
            views.setViewTranslationY(R.id.second_text, secondOffsetY);
            if (blockEnabled) {
                views.setInt(R.id.second_text, "setBackgroundColor", blockBackgroundColor);
            }
            views.setTextViewText(R.id.second_text, showSeconds ? secondText : "");
            views.setViewVisibility(R.id.second_text, showSeconds ? View.VISIBLE : View.GONE);
        }

        Intent configIntent = new Intent(context, WidgetConfigureActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_container, configPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}