package com.akfsno.wordclockwidgets;

import android.content.Context;
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
        Button resetButton = convertView.findViewById(R.id.reset_button);

        label.setText(child);

        String blockKey = getBlockKey(groupPosition);
        if (child.equals("Размер шрифта")) {
            seekBar.setMax(50); // 10 to 60
            float currentSize = getFontSize(blockKey);
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
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            resetButton.setOnClickListener(v -> {
                seekBar.setProgress(14); // default 24
                setFontSize(blockKey, 24f);
                valueText.setText("24");
            });
        } else if (child.equals("Позиция X")) {
            seekBar.setMax(1000); // -500 to 500
            int currentX = getOffsetX(blockKey);
            seekBar.setProgress(currentX + 500);
            valueText.setText(String.valueOf(currentX));
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int x = progress - 500;
                    valueText.setText(String.valueOf(x));
                    setOffsetX(blockKey, x);
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            resetButton.setOnClickListener(v -> {
                seekBar.setProgress(500);
                setOffsetX(blockKey, 0);
                valueText.setText("0");
            });
        } else if (child.equals("Позиция Y")) {
            seekBar.setMax(1000);
            int currentY = getOffsetY(blockKey);
            seekBar.setProgress(currentY + 500);
            valueText.setText(String.valueOf(currentY));
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int y = progress - 500;
                    valueText.setText(String.valueOf(y));
                    setOffsetY(blockKey, y);
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            resetButton.setOnClickListener(v -> {
                seekBar.setProgress(500);
                setOffsetY(blockKey, 0);
                valueText.setText("0");
            });
        } else {
            // For other items, hide seekbar
            seekBar.setVisibility(View.GONE);
            valueText.setVisibility(View.GONE);
            resetButton.setVisibility(View.GONE);
        }

        return convertView;
    }

    private String getBlockKey(int groupPosition) {
        switch (groupPosition) {
            case 0: return "hour";
            case 1: return "minute";
            case 2: return "second";
            case 3: return "dayNight";
            case 4: return "date";
            case 5: return "dayOfWeek";
            default: return "hour";
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

    private int getOffsetX(String key) {
        return WidgetPreferences.getOffsetX(context, appWidgetId, key, 0);
    }

    private void setOffsetX(String key, int value) {
        WidgetPreferences.setOffsetX(context, appWidgetId, key, value);
    }

    private int getOffsetY(String key) {
        return WidgetPreferences.getOffsetY(context, appWidgetId, key, 0);
    }

    private void setOffsetY(String key, int value) {
        WidgetPreferences.setOffsetY(context, appWidgetId, key, value);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}