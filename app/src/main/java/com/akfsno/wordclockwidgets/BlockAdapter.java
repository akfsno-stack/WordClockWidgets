package com.akfsno.wordclockwidgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class BlockAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> groups;
    private Map<String, List<String>> children;
    private int appWidgetId;

    public BlockAdapter(Context context, List<String> groups, Map<String, List<String>> children, int appWidgetId) {
        this.context = context;
        this.groups = groups;
        this.children = children;
        this.appWidgetId = appWidgetId;
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return children.get(groups.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return children.get(groups.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String group = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
        }
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(group);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String child = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.child_item, null);
        }

        TextView label = convertView.findViewById(R.id.child_label);
        SeekBar seekBar = convertView.findViewById(R.id.seek_bar);
        TextView valueText = convertView.findViewById(R.id.value_text);
        Button actionButton = convertView.findViewById(R.id.reset_button);

        label.setText(child);
        String blockKey = getBlockKey(groupPosition);

        seekBar.setVisibility(View.VISIBLE);
        valueText.setVisibility(View.VISIBLE);
        actionButton.setVisibility(View.VISIBLE);

        if (child.equals("Цвет текста")) {
            int[] colors = {Color.BLACK, Color.RED, Color.GREEN, Color.BLUE, Color.WHITE};
            int current = getTextColor(blockKey);
            int currentIndex = 0;
            for (int i = 0; i < colors.length; i++) {
                if (colors[i] == current) {
                    currentIndex = i;
                    break;
                }
            }
            seekBar.setMax(colors.length - 1);
            seekBar.setProgress(currentIndex);
            valueText.setText("Цвет: " + getColorName(colors[currentIndex]));

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int color = colors[progress];
                    setTextColor(blockKey, color);
                    valueText.setText("Цвет: " + getColorName(color));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            actionButton.setText("Применить");
            actionButton.setOnClickListener(v -> updateWidget());
        } else if (child.equals("Размер шрифта")) {
            float currentSize = getFontSize(blockKey);
            seekBar.setMax(50);
            seekBar.setProgress((int) (currentSize - 10));
            valueText.setText(String.valueOf((int) currentSize));

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    float size = 10 + progress;
                    valueText.setText(String.valueOf((int) size));
                    setFontSize(blockKey, size);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            actionButton.setText("Применить");
            actionButton.setOnClickListener(v -> updateWidget());
        } else if (child.equals("+ 0 для цифр до 10")) {
            seekBar.setVisibility(View.GONE);
            valueText.setText((blockKey.equals("minute") ? WidgetPreferences.getAddZeroMinute(context, appWidgetId, false) : WidgetPreferences.getAddZeroSecond(context, appWidgetId, false)) ? "Включено" : "Отключено");
            actionButton.setText("Переключить");
            actionButton.setOnClickListener(v -> {
                boolean current = blockKey.equals("minute") ? WidgetPreferences.getAddZeroMinute(context, appWidgetId, false) : WidgetPreferences.getAddZeroSecond(context, appWidgetId, false);
                boolean newValue = !current;
                if (blockKey.equals("minute")) {
                    WidgetPreferences.saveAddZeroMinute(context, appWidgetId, newValue);
                } else {
                    WidgetPreferences.saveAddZeroSecond(context, appWidgetId, newValue);
                }
                valueText.setText(newValue ? "Включено" : "Отключено");
                updateWidget();
            });
        } else if (child.equals("Показать элемент")) {
            seekBar.setVisibility(View.GONE);
            boolean showValue;
            switch (blockKey) {
                case "hour": showValue = WidgetPreferences.getShowHour(context, appWidgetId, true); break;
                case "minute": showValue = WidgetPreferences.getShowMinute(context, appWidgetId, true); break;
                case "second": showValue = WidgetPreferences.getShowSeconds(context, appWidgetId, false); break;
                case "dayNight": showValue = WidgetPreferences.getShowDayNight(context, appWidgetId, true); break;
                case "date": showValue = WidgetPreferences.getShowDate(context, appWidgetId, true); break;
                case "dayOfWeek": showValue = WidgetPreferences.getShowDayOfWeek(context, appWidgetId, true); break;
                default: showValue = true; break;
            }
            valueText.setText(showValue ? "Показать" : "Скрыть");
            actionButton.setText("Переключить");
            actionButton.setOnClickListener(v -> {
                boolean current = blockKey.equals("hour") ? WidgetPreferences.getShowHour(context, appWidgetId, true)
                        : blockKey.equals("minute") ? WidgetPreferences.getShowMinute(context, appWidgetId, true)
                        : blockKey.equals("second") ? WidgetPreferences.getShowSeconds(context, appWidgetId, false)
                        : blockKey.equals("dayNight") ? WidgetPreferences.getShowDayNight(context, appWidgetId, true)
                        : blockKey.equals("date") ? WidgetPreferences.getShowDate(context, appWidgetId, true)
                        : WidgetPreferences.getShowDayOfWeek(context, appWidgetId, true);
                boolean newValue = !current;
                switch (blockKey) {
                    case "hour": WidgetPreferences.saveShowHour(context, appWidgetId, newValue); break;
                    case "minute": WidgetPreferences.saveShowMinute(context, appWidgetId, newValue); break;
                    case "second": WidgetPreferences.saveShowSeconds(context, appWidgetId, newValue); break;
                    case "dayNight": WidgetPreferences.saveShowDayNight(context, appWidgetId, newValue); break;
                    case "date": WidgetPreferences.saveShowDate(context, appWidgetId, newValue); break;
                    case "dayOfWeek": WidgetPreferences.saveShowDayOfWeek(context, appWidgetId, newValue); break;
                }
                valueText.setText(newValue ? "Показать" : "Скрыть");
                updateWidget();
            });
        } else {
            seekBar.setVisibility(View.GONE);
            valueText.setVisibility(View.GONE);
            actionButton.setVisibility(View.GONE);
        }

        return convertView;
    }

    private void updateWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        new WordClockWidgetProvider().onUpdate(context, appWidgetManager, new int[]{appWidgetId});
    }

    private String getColorName(int color) {
        if (color == Color.BLACK) return "Чёрный";
        if (color == Color.WHITE) return "Белый";
        if (color == Color.RED) return "Красный";
        if (color == Color.GREEN) return "Зелёный";
        if (color == Color.BLUE) return "Синий";
        return "Польз. цвет";
    }

    private String getBlockKey(int groupPosition) {
        switch (groupPosition) {
            case 0:
                return "hour";
            case 1:
                return "minute";
            case 2:
                return "second";
            case 3:
                return "dayNight";
            case 4:
                return "date";
            case 5:
                return "dayOfWeek";
            default:
                return "hour";
        }
    }

    private float getFontSize(String key) {
        if (key.equals("hour")) return WidgetPreferences.getFontSize(context, appWidgetId, 24f);
        if (key.equals("minute")) return WidgetPreferences.getMinuteFontSize(context, appWidgetId, 24f);
        if (key.equals("second")) return WidgetPreferences.getSecondFontSize(context, appWidgetId, 18f);
        return 24f;
    }

    private void setFontSize(String key, float size) {
        if (key.equals("hour")) WidgetPreferences.saveFontSize(context, appWidgetId, size);
        else if (key.equals("minute")) WidgetPreferences.saveMinuteFontSize(context, appWidgetId, size);
        else if (key.equals("second")) WidgetPreferences.saveSecondFontSize(context, appWidgetId, size);
    }

    private int getTextColor(String key) {
        switch (key) {
            case "hour": return WidgetPreferences.getHourTextColor(context, appWidgetId, Color.BLACK);
            case "minute": return WidgetPreferences.getMinuteTextColor(context, appWidgetId, Color.BLACK);
            case "second": return WidgetPreferences.getSecondTextColor(context, appWidgetId, Color.BLACK);
            case "dayNight": return WidgetPreferences.getDayNightTextColor(context, appWidgetId, Color.RED);
            case "date": return WidgetPreferences.getDateTextColor(context, appWidgetId, Color.BLACK);
            case "dayOfWeek": return WidgetPreferences.getDayOfWeekTextColor(context, appWidgetId, Color.BLACK);
            default: return Color.BLACK;
        }
    }

    private void setTextColor(String key, int color) {
        switch (key) {
            case "hour": WidgetPreferences.saveHourTextColor(context, appWidgetId, color); break;
            case "minute": WidgetPreferences.saveMinuteTextColor(context, appWidgetId, color); break;
            case "second": WidgetPreferences.saveSecondTextColor(context, appWidgetId, color); break;
            case "dayNight": WidgetPreferences.saveDayNightTextColor(context, appWidgetId, color); break;
            case "date": WidgetPreferences.saveDateTextColor(context, appWidgetId, color); break;
            case "dayOfWeek": WidgetPreferences.saveDayOfWeekTextColor(context, appWidgetId, color); break;
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
