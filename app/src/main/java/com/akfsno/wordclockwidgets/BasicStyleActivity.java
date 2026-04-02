package com.akfsno.wordclockwidgets;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.util.TypedValue;
import android.widget.Toast;

import java.util.Calendar;

public class BasicStyleActivity extends Activity {

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private String widgetProviderClass = WordClockWidgetProvider.class.getName();
    private TextView previewHour, previewMinute, previewDayNight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_style);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        if (appWidgetManager != null) {
            android.appwidget.AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
            if (info != null && info.provider != null) {
                widgetProviderClass = info.provider.getClassName();
            }
        }

        // Initialize preview views
        previewHour = findViewById(R.id.preview_hour);
        previewMinute = findViewById(R.id.preview_minute);
        previewDayNight = findViewById(R.id.preview_day_night);

        // Setup UI elements
        setupBackgroundColor();
        setupBackgroundAlpha();
        setupTextColor();
        setupBorderColor();
        setupBorderWidth();
        setupDayNightSize();
        setupUse12Hour();
        setupHourSize();
        setupMinuteSize();

        // Set basic style defaults
        WidgetPreferences.saveShowHour(this, appWidgetId, true);
        WidgetPreferences.saveShowMinute(this, appWidgetId, true);
        WidgetPreferences.saveShowDayNight(this, appWidgetId, true);
        WidgetPreferences.saveShowDate(this, appWidgetId, false);
        WidgetPreferences.saveShowDayOfWeek(this, appWidgetId, false);

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> saveAndFinish());

        updatePreview();
        updatePreviewText();
    }

    private void setupBackgroundColor() {
        Spinner spinner = findViewById(R.id.background_color_spinner);
        TextView valueText = findViewById(R.id.background_color_value);
        int[] colors = {0xFFFFFFFF, 0xFF000000, 0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00};
        String[] names = {"Белый", "Чёрный", "Красный", "Зелёный", "Синий", "Жёлтый"};
        int current = WidgetPreferences.getBackgroundColor(this, appWidgetId, 0xFFFFFFFF);
        int currentIndex = 0;
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] == current) {
                currentIndex = i;
                break;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(currentIndex);
        valueText.setText(names[currentIndex]);

        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                WidgetPreferences.saveBackgroundColor(BasicStyleActivity.this, appWidgetId, colors[position]);
                valueText.setText(names[position]);
                updatePreview();
                updateWidget();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
    }

    private void setupBackgroundAlpha() {
        SeekBar seekBar = findViewById(R.id.background_alpha_seekbar);
        TextView valueText = findViewById(R.id.background_alpha_value);
        int current = WidgetPreferences.getBackgroundAlpha(this, appWidgetId, 255);
        seekBar.setMax(255);
        seekBar.setProgress(current);
        valueText.setText(String.valueOf(current));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                WidgetPreferences.saveBackgroundAlpha(BasicStyleActivity.this, appWidgetId, progress);
                valueText.setText(String.valueOf(progress));
                updatePreview();
                updateWidget();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupTextColor() {
        Spinner spinner = findViewById(R.id.text_color_spinner);
        TextView valueText = findViewById(R.id.text_color_value);
        int[] colors = {0xFF000000, 0xFFFFFFFF, 0xFFFF0000, 0xFF00FF00, 0xFF0000FF};
        String[] names = {"Чёрный", "Белый", "Красный", "Зелёный", "Синий"};
        int current = WidgetPreferences.getHourTextColor(this, appWidgetId, 0xFF000000);
        int currentIndex = 0;
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] == current) {
                currentIndex = i;
                break;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(currentIndex);
        valueText.setText(names[currentIndex]);

        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                int color = colors[position];
                WidgetPreferences.saveHourTextColor(BasicStyleActivity.this, appWidgetId, color);
                WidgetPreferences.saveMinuteTextColor(BasicStyleActivity.this, appWidgetId, color);
                WidgetPreferences.saveDayNightTextColor(BasicStyleActivity.this, appWidgetId, color);
                valueText.setText(names[position]);
                updatePreview();
                updateWidget();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
    }

    private void setupBorderColor() {
        Spinner spinner = findViewById(R.id.border_color_spinner);
        TextView valueText = findViewById(R.id.border_color_value);
        int[] colors = {0xFF000000, 0xFFFFFFFF, 0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00};
        String[] names = {"Чёрный", "Белый", "Красный", "Зелёный", "Синий", "Жёлтый"};
        int current = WidgetPreferences.getBorderColor(this, appWidgetId, getResources().getColor(android.R.color.holo_red_dark));
        int currentIndex = 0;
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] == current) {
                currentIndex = i;
                break;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(currentIndex);
        valueText.setText(names[currentIndex]);

        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                WidgetPreferences.saveBorderColor(BasicStyleActivity.this, appWidgetId, colors[position]);
                valueText.setText(names[position]);
                updatePreview();
                updateWidget();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
    }

    private void setupBorderWidth() {
        SeekBar seekBar = findViewById(R.id.border_width_seekbar);
        TextView valueText = findViewById(R.id.border_width_value);
        int current = WidgetPreferences.getBorderWidth(this, appWidgetId, 2);
        seekBar.setMax(20);
        seekBar.setProgress(current);
        valueText.setText(String.valueOf(current));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int width = Math.max(1, progress);
                WidgetPreferences.saveBorderWidth(BasicStyleActivity.this, appWidgetId, width);
                valueText.setText(String.valueOf(width));
                updatePreview();
                updateWidget();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupDayNightSize() {
        SeekBar seekBar = findViewById(R.id.day_night_size_seekbar);
        TextView valueText = findViewById(R.id.day_night_size_value);
        float current = WidgetPreferences.getDayNightFontSize(this, appWidgetId, 18f);
        seekBar.setMax(50);
        seekBar.setProgress((int) (current - 10));
        valueText.setText(String.valueOf((int) current));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float size = 10 + progress;
                WidgetPreferences.saveDayNightFontSize(BasicStyleActivity.this, appWidgetId, size);
                valueText.setText(String.valueOf((int) size));
                updatePreview();
                updateWidget();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupUse12Hour() {
        CheckBox checkBox = findViewById(R.id.use_12_hour_checkbox);
        boolean current = WidgetPreferences.getUse12HourFormat(this, appWidgetId, true);
        checkBox.setChecked(current);
        checkBox.setText(current ? "12-часовой формат" : "24-часовой формат");

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            WidgetPreferences.saveUse12HourFormat(BasicStyleActivity.this, appWidgetId, isChecked);
            checkBox.setText(isChecked ? "12-часовой формат" : "24-часовой формат");
            updatePreview();
            updateWidget();
        });
    }

    private void setupHourSize() {
        SeekBar seekBar = findViewById(R.id.hour_size_seekbar);
        TextView valueText = findViewById(R.id.hour_size_value);
        float current = WidgetPreferences.getFontSize(this, appWidgetId, 24f);
        seekBar.setMax(50);
        seekBar.setProgress((int) (current - 10));
        valueText.setText(String.valueOf((int) current));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float size = 10 + progress;
                WidgetPreferences.saveFontSize(BasicStyleActivity.this, appWidgetId, size);
                valueText.setText(String.valueOf((int) size));
                updatePreview();
                updateWidget();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupMinuteSize() {
        SeekBar seekBar = findViewById(R.id.minute_size_seekbar);
        TextView valueText = findViewById(R.id.minute_size_value);
        float current = WidgetPreferences.getMinuteFontSize(this, appWidgetId, 24f);
        seekBar.setMax(50);
        seekBar.setProgress((int) (current - 10));
        valueText.setText(String.valueOf((int) current));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float size = 10 + progress;
                WidgetPreferences.saveMinuteFontSize(BasicStyleActivity.this, appWidgetId, size);
                valueText.setText(String.valueOf((int) size));
                updatePreview();
                updateWidget();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updatePreview() {
        int bgColor = WidgetPreferences.getBackgroundColor(this, appWidgetId, 0xFFFFFFFF);
        int alpha = WidgetPreferences.getBackgroundAlpha(this, appWidgetId, 255);
        bgColor = (bgColor & 0x00FFFFFF) | ((alpha & 0xFF) << 24);

        View container = findViewById(R.id.preview_container);
        android.graphics.drawable.Drawable bg = container.getBackground();
        if (bg instanceof android.graphics.drawable.GradientDrawable) {
            android.graphics.drawable.GradientDrawable drawable = (android.graphics.drawable.GradientDrawable) bg.mutate();
            drawable.setColor(bgColor);
            int borderColor = WidgetPreferences.getBorderColor(this, appWidgetId, getResources().getColor(android.R.color.holo_red_dark));
            drawable.setStroke(WidgetPreferences.getBorderWidth(this, appWidgetId, 2), borderColor);
        } else {
            container.setBackgroundColor(bgColor);
        }

        updatePreviewText();
    }

    private void updatePreviewText() {
        Calendar calendar = Calendar.getInstance();
        int hour24 = calendar.get(Calendar.HOUR_OF_DAY);
        boolean use12 = WidgetPreferences.getUse12HourFormat(this, appWidgetId, true);

        String hourText = use12 ? NumberToWords.convertHour(hour24) : NumberToWords.convertHour24(hour24);
        String minuteText = NumberToWords.convertMinute(calendar.get(Calendar.MINUTE), WidgetPreferences.getAddZeroMinute(this, appWidgetId, false), use12);

        if (!use12 && hour24 == 0 && calendar.get(Calendar.MINUTE) == 0) {
            hourText = "двенадцать";
            minuteText = "ноль-ноль";
        }
        String dayNightText = NumberToWords.getDayNight(hour24);

        previewHour.setText(hourText);
        previewMinute.setText(minuteText);
        previewDayNight.setText(dayNightText);

        int textColor = WidgetPreferences.getHourTextColor(this, appWidgetId, getResources().getColor(android.R.color.black));
        previewHour.setTextColor(textColor);
        previewMinute.setTextColor(textColor);
        previewDayNight.setTextColor(WidgetPreferences.getDayNightTextColor(this, appWidgetId, getResources().getColor(android.R.color.holo_red_dark)));

        previewHour.setTextSize(WidgetPreferences.getFontSize(this, appWidgetId, 24f));
        previewMinute.setTextSize(WidgetPreferences.getMinuteFontSize(this, appWidgetId, 24f));
        previewDayNight.setTextSize(WidgetPreferences.getDayNightFontSize(this, appWidgetId, 18f));

        previewHour.setVisibility(View.VISIBLE);
        previewMinute.setVisibility(View.VISIBLE);
        previewDayNight.setVisibility(View.VISIBLE);

        adjustPreviewTextSizes();
    }

    private void adjustPreviewTextSizes() {
        float hourSize = WidgetPreferences.getFontSize(this, appWidgetId, 24f);
        float minuteSize = WidgetPreferences.getMinuteFontSize(this, appWidgetId, 24f);
        float dayNightSize = WidgetPreferences.getDayNightFontSize(this, appWidgetId, 18f);

        boolean showHour = WidgetPreferences.getShowHour(this, appWidgetId, true);
        boolean showMinute = WidgetPreferences.getShowMinute(this, appWidgetId, true);
        boolean showDayNight = WidgetPreferences.getShowDayNight(this, appWidgetId, true);

        float totalPx = 0f;
        int visibleCount = 0;

        if (showHour) {
            totalPx += TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, hourSize, getResources().getDisplayMetrics());
            visibleCount++;
        }
        if (showDayNight) {
            totalPx += TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dayNightSize, getResources().getDisplayMetrics());
            visibleCount++;
        }
        if (showMinute) {
            totalPx += TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, minuteSize, getResources().getDisplayMetrics());
            visibleCount++;
        }

        if (visibleCount > 1) {
            // spacing between rows from layout margins
            totalPx += (visibleCount - 1) * dpToPx(4);
        }

        float availablePx = getPreviewContainerAvailableHeightPx();

        float scale = 1f;
        if (totalPx > 0 && totalPx > availablePx) {
            scale = availablePx / totalPx;
        }

        if (showHour) {
            previewHour.setTextSize(TypedValue.COMPLEX_UNIT_SP, hourSize * scale);
        }
        if (showDayNight) {
            previewDayNight.setTextSize(TypedValue.COMPLEX_UNIT_SP, dayNightSize * scale);
        }
        if (showMinute) {
            previewMinute.setTextSize(TypedValue.COMPLEX_UNIT_SP, minuteSize * scale);
        }
    }

    private float getPreviewContainerAvailableHeightPx() {
        View container = findViewById(R.id.preview_container);
        int heightPx = (container != null && container.getHeight() > 0) ? container.getHeight() : dpToPx(150);
        // subtract padding from BasicStyleActivity template, if any
        int padding = container != null ? container.getPaddingTop() + container.getPaddingBottom() : dpToPx(16);
        return Math.max(0, heightPx - padding - dpToPx(16));
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void updateWidget() {
        Intent intent = new Intent();
        try {
            intent.setComponent(new android.content.ComponentName(this, widgetProviderClass));
        } catch (Exception e) {
            intent.setComponent(new android.content.ComponentName(this, WordClockWidgetProvider.class));
        }
        intent.setAction(BaseWordClockWidgetProvider.UPDATE_ACTION);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        sendBroadcast(intent);
    }

    private void saveAndFinish() {
        WidgetPreferences.saveUseConstructorLayout(this, appWidgetId, false);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    private String getColorName(int color) {
        if (color == 0xFFFFFFFF) return "Белый";
        if (color == 0xFF000000) return "Чёрный";
        if (color == 0xFFFF0000) return "Красный";
        if (color == 0xFF00FF00) return "Зелёный";
        if (color == 0xFF0000FF) return "Синий";
        if (color == 0xFFFFFF00) return "Жёлтый";
        return "Польз.";
    }
}