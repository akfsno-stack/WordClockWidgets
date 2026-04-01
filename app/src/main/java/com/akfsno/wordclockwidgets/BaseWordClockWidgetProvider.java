package com.akfsno.wordclockwidgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
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
                                     String dayNightText,
                                     String dayOfWeekText,
                                     String dateText);

    protected abstract int getDefaultTextColor();

    protected abstract int getDefaultBorderColor();

    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), getLayoutResource(context, appWidgetId));

        Calendar calendar = Calendar.getInstance();
        String hourText = new SimpleDateFormat("HH", Locale.getDefault()).format(calendar.getTime());
        String minuteText = new SimpleDateFormat("mm", Locale.getDefault()).format(calendar.getTime());
        String dayNightText = new SimpleDateFormat("a", Locale.getDefault()).format(calendar.getTime());
        String dayOfWeekText = new SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.getTime());
        String dateText = new SimpleDateFormat("dd MMM", Locale.getDefault()).format(calendar.getTime());

        setTexts(views, hourText, minuteText, dayNightText, dayOfWeekText, dateText);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}