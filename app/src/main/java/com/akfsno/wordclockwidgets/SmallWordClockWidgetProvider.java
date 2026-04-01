package com.akfsno.wordclockwidgets;

import android.content.Context;
import android.graphics.Color;
import android.widget.RemoteViews;

public class SmallWordClockWidgetProvider extends BaseWordClockWidgetProvider {

    @Override
    protected int getLayoutResource(Context context, int appWidgetId) {
        return R.layout.widget_layout; // Or create a small layout
    }

    @Override
    protected void setTexts(RemoteViews views, String hourText, String minuteText, String secondText, String dayNightText, String dayOfWeekText, String dateText) {
        views.setTextViewText(R.id.hour_text, hourText);
        views.setTextViewText(R.id.minute_text, minuteText);
        views.setTextViewText(R.id.second_text, secondText);
        views.setTextViewText(R.id.day_night_text, dayNightText);
        views.setTextViewText(R.id.day_of_week_text, dayOfWeekText);
        views.setTextViewText(R.id.date_text, dateText);
    }

    @Override
    protected int getDefaultTextColor() {
        return Color.BLACK;
    }

    @Override
    protected int getDefaultBorderColor() {
        return Color.RED;
    }
}