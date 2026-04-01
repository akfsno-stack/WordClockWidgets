package com.akfsno.wordclockwidgets;

import android.content.Context;
import android.graphics.Color;
import android.widget.RemoteViews;

public class HorizontalWordClockWidgetProvider extends BaseWordClockWidgetProvider {

    @Override
    protected int getLayoutResource(Context context, int appWidgetId) {
        return R.layout.horizontal_widget_layout;
    }

    @Override
    protected void setTexts(RemoteViews views, String hourText, String minuteText, String dayNightText, String dayOfWeekText, String dateText) {
        String timeText = hourText + " : " + minuteText.toLowerCase();
        views.setTextViewText(R.id.second_text, timeText);
        views.setTextViewText(R.id.date_text, dateText);
        views.setTextViewText(R.id.day_of_week_text, dayOfWeekText);
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