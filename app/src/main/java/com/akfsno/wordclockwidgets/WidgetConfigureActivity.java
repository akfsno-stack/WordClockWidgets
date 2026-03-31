package com.akfsno.wordclockwidgets;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WidgetConfigureActivity extends Activity {

    private int appWidgetId;
    private ExpandableListView blockList;
    private TextView previewHour, previewMinute, previewSecond, previewDayNight, previewDate, previewDayOfWeek;
    private Button joystickUp, joystickDown, joystickLeft, joystickRight;
    private TextView coordinates;
    private Button saveButton, applyButton, resetAllButton;
    private CheckBox addZeroCheckbox;
    private Handler handler = new Handler();
    private Runnable moveRunnable;

    private String selectedBlock = "hour";
    private Map<String, int[]> blockOffsets = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constructor);

        appWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        initializeViews();
        setupBlockList();
        loadOffsets();
        addZeroCheckbox.setChecked(WidgetPreferences.getAddZero(this, appWidgetId, false));
        updatePreview();
        setupJoystick();
        setupDragAndDrop();
        setupButtons();

        // Set result for widget configuration
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
    }

    private void initializeViews() {
        blockList = findViewById(R.id.block_list);
        previewHour = findViewById(R.id.preview_hour);
        previewMinute = findViewById(R.id.preview_minute);
        previewSecond = findViewById(R.id.preview_second);
        previewDayNight = findViewById(R.id.preview_day_night);
        previewDate = findViewById(R.id.preview_date);
        previewDayOfWeek = findViewById(R.id.preview_day_of_week);
        joystickUp = findViewById(R.id.joystick_up);
        joystickDown = findViewById(R.id.joystick_down);
        joystickLeft = findViewById(R.id.joystick_left);
        joystickRight = findViewById(R.id.joystick_right);
        coordinates = findViewById(R.id.coordinates);
        saveButton = findViewById(R.id.save_button);
        applyButton = findViewById(R.id.apply_button);
        resetAllButton = findViewById(R.id.reset_all_button);
        addZeroCheckbox = findViewById(R.id.add_zero_checkbox);
    }

    private void setupBlockList() {
        List<String> groups = new ArrayList<>();
        groups.add("Часы");
        groups.add("Минуты");
        groups.add("Секунды");
        groups.add("День/Ночь");
        groups.add("Дата");
        groups.add("День недели");

        Map<String, List<String>> children = new HashMap<>();
        for (String group : groups) {
            List<String> childList = new ArrayList<>();
            childList.add("Цвет текста");
            childList.add("Размер шрифта");
            childList.add("Позиция X");
            childList.add("Позиция Y");
            children.put(group, childList);
        }

        BlockAdapter adapter = new BlockAdapter(this, groups, children, appWidgetId);
        blockList.setAdapter(adapter);

        blockList.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            selectedBlock = getBlockKey(groupPosition);
            updateCoordinates();
            return true;
        });
    }

    private String getBlockKey(int position) {
        switch (position) {
            case 0: return "hour";
            case 1: return "minute";
            case 2: return "second";
            case 3: return "dayNight";
            case 4: return "date";
            case 5: return "dayOfWeek";
            default: return "hour";
        }
    }

    private void loadOffsets() {
        blockOffsets.put("hour", new int[]{WidgetPreferences.getOffsetX(this, appWidgetId, "hour", 0), WidgetPreferences.getOffsetY(this, appWidgetId, "hour", 0)});
        blockOffsets.put("minute", new int[]{WidgetPreferences.getOffsetX(this, appWidgetId, "minute", 0), WidgetPreferences.getOffsetY(this, appWidgetId, "minute", 0)});
        blockOffsets.put("second", new int[]{WidgetPreferences.getSecondOffsetX(this, appWidgetId, 0), WidgetPreferences.getSecondOffsetY(this, appWidgetId, 0)});
        blockOffsets.put("dayNight", new int[]{WidgetPreferences.getDayNightOffsetX(this, appWidgetId, 0), WidgetPreferences.getDayNightOffsetY(this, appWidgetId, 0)});
        blockOffsets.put("date", new int[]{WidgetPreferences.getDateOffsetX(this, appWidgetId, 0), WidgetPreferences.getDateOffsetY(this, appWidgetId, 0)});
        blockOffsets.put("dayOfWeek", new int[]{WidgetPreferences.getDayOfWeekOffsetX(this, appWidgetId, 0), WidgetPreferences.getDayOfWeekOffsetY(this, appWidgetId, 0)});
    }

    private void updatePreview() {
        // Set translations
        int[] hourOff = blockOffsets.get("hour");
        previewHour.setTranslationX(hourOff[0]);
        previewHour.setTranslationY(hourOff[1]);

        int[] minOff = blockOffsets.get("minute");
        previewMinute.setTranslationX(minOff[0]);
        previewMinute.setTranslationY(minOff[1]);

        int[] secOff = blockOffsets.get("second");
        previewSecond.setTranslationX(secOff[0]);
        previewSecond.setTranslationY(secOff[1]);

        int[] dnOff = blockOffsets.get("dayNight");
        previewDayNight.setTranslationX(dnOff[0]);
        previewDayNight.setTranslationY(dnOff[1]);

        int[] dateOff = blockOffsets.get("date");
        previewDate.setTranslationX(dateOff[0]);
        previewDate.setTranslationY(dateOff[1]);

        int[] dowOff = blockOffsets.get("dayOfWeek");
        previewDayOfWeek.setTranslationX(dowOff[0]);
        previewDayOfWeek.setTranslationY(dowOff[1]);
    }

    private void setupDragAndDrop() {
        View.OnTouchListener dragListener = new View.OnTouchListener() {
            private float dX, dY;
            private String draggedBlock;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        draggedBlock = getBlockFromView(view);
                        selectedBlock = draggedBlock;
                        updateCoordinates();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        view.animate()
                            .x(event.getRawX() + dX)
                            .y(event.getRawY() + dY)
                            .setDuration(0)
                            .start();
                        return true;
                    case MotionEvent.ACTION_UP:
                        // Update offsets
                        int[] off = blockOffsets.get(draggedBlock);
                        off[0] = (int) view.getTranslationX();
                        off[1] = (int) view.getTranslationY();
                        off[0] = WidgetPreferences.constrainOffset(off[0]);
                        off[1] = WidgetPreferences.constrainOffset(off[1]);
                        view.setTranslationX(off[0]);
                        view.setTranslationY(off[1]);
                        updateCoordinates();
                        return true;
                }
                return false;
            }
        };

        previewHour.setOnTouchListener(dragListener);
        previewMinute.setOnTouchListener(dragListener);
        previewSecond.setOnTouchListener(dragListener);
        previewDayNight.setOnTouchListener(dragListener);
        previewDate.setOnTouchListener(dragListener);
        previewDayOfWeek.setOnTouchListener(dragListener);
    }

    private String getBlockFromView(View view) {
        if (view == previewHour) return "hour";
        if (view == previewMinute) return "minute";
        if (view == previewSecond) return "second";
        if (view == previewDayNight) return "dayNight";
        if (view == previewDate) return "date";
        if (view == previewDayOfWeek) return "dayOfWeek";
        return "hour";
    }

    private void moveBlock(int dx, int dy) {
        int[] off = blockOffsets.get(selectedBlock);
        off[0] += dx;
        off[1] += dy;
        off[0] = WidgetPreferences.constrainOffset(off[0]);
        off[1] = WidgetPreferences.constrainOffset(off[1]);
        updatePreview();
        updateCoordinates();
    }

    private void updateCoordinates() {
        int[] off = blockOffsets.get(selectedBlock);
        coordinates.setText("(" + off[0] + "," + off[1] + ")");
    }

    private void setupButtons() {
        saveButton.setOnClickListener(v -> saveOffsets());
        applyButton.setOnClickListener(v -> applyOffsets());
        resetAllButton.setOnClickListener(v -> resetAll());
    }

    private void setupJoystick() {
        View.OnClickListener clickListener = v -> {
            int dx = 0, dy = 0;
            if (v == joystickUp) dy = -10;
            else if (v == joystickDown) dy = 10;
            else if (v == joystickLeft) dx = -10;
            else if (v == joystickRight) dx = 10;
            moveBlock(dx, dy);
        };

        View.OnLongClickListener longClickListener = v -> {
            int dx = 0, dy = 0;
            if (v == joystickUp) dy = -5;
            else if (v == joystickDown) dy = 5;
            else if (v == joystickLeft) dx = -5;
            else if (v == joystickRight) dx = 5;
            moveRunnable = () -> {
                moveBlock(dx, dy);
                handler.postDelayed(moveRunnable, 100);
            };
            handler.post(moveRunnable);
            return true;
        };

        View.OnTouchListener touchListener = (v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                handler.removeCallbacks(moveRunnable);
            }
            return false;
        };

        joystickUp.setOnClickListener(clickListener);
        joystickUp.setOnLongClickListener(longClickListener);
        joystickUp.setOnTouchListener(touchListener);

        joystickDown.setOnClickListener(clickListener);
        joystickDown.setOnLongClickListener(longClickListener);
        joystickDown.setOnTouchListener(touchListener);

        joystickLeft.setOnClickListener(clickListener);
        joystickLeft.setOnLongClickListener(longClickListener);
        joystickLeft.setOnTouchListener(touchListener);

        joystickRight.setOnClickListener(clickListener);
        joystickRight.setOnLongClickListener(longClickListener);
        joystickRight.setOnTouchListener(touchListener);
    }

    private void saveOffsets() {
        WidgetPreferences.setOffsetX(this, appWidgetId, "hour", blockOffsets.get("hour")[0]);
        WidgetPreferences.setOffsetY(this, appWidgetId, "hour", blockOffsets.get("hour")[1]);
        WidgetPreferences.setOffsetX(this, appWidgetId, "minute", blockOffsets.get("minute")[0]);
        WidgetPreferences.setOffsetY(this, appWidgetId, "minute", blockOffsets.get("minute")[1]);
        WidgetPreferences.setSecondOffsetX(this, appWidgetId, blockOffsets.get("second")[0]);
        WidgetPreferences.setSecondOffsetY(this, appWidgetId, blockOffsets.get("second")[1]);
        WidgetPreferences.setDayNightOffsetX(this, appWidgetId, blockOffsets.get("dayNight")[0]);
        WidgetPreferences.setDayNightOffsetY(this, appWidgetId, blockOffsets.get("dayNight")[1]);
        WidgetPreferences.setDateOffsetX(this, appWidgetId, blockOffsets.get("date")[0]);
        WidgetPreferences.setDateOffsetY(this, appWidgetId, blockOffsets.get("date")[1]);
        WidgetPreferences.setDayOfWeekOffsetX(this, appWidgetId, blockOffsets.get("dayOfWeek")[0]);
        WidgetPreferences.setDayOfWeekOffsetY(this, appWidgetId, blockOffsets.get("dayOfWeek")[1]);
        WidgetPreferences.saveAddZero(this, appWidgetId, addZeroCheckbox.isChecked());
        Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show();
    }

    private void applyOffsets() {
        saveOffsets();
        // Update widget
        Intent updateIntent = new Intent(this, WordClockWidgetProvider.class);
        updateIntent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int[] ids = {appWidgetId};
        updateIntent.putExtra("appWidgetIds", ids);
        sendBroadcast(updateIntent);
        Toast.makeText(this, "Применено", Toast.LENGTH_SHORT).show();
    }

    private void resetAll() {
        // Reset all offsets to 0
        for (String key : blockOffsets.keySet()) {
            blockOffsets.get(key)[0] = 0;
            blockOffsets.get(key)[1] = 0;
        }
        addZeroCheckbox.setChecked(false);
        updatePreview();
        updateCoordinates();
        saveOffsets();
        Toast.makeText(this, "Сброшено", Toast.LENGTH_SHORT).show();
    }
}
        fontSizeSeekBar.setProgress((int) WidgetPreferences.getFontSize(this, appWidgetId, 24f));

        borderWidthSeekBar = findViewById(R.id.border_width_seekbar);
        borderWidthSeekBar.setProgress(WidgetPreferences.getBorderWidth(this, appWidgetId, 2));

        backgroundAlphaSeekBar = findViewById(R.id.background_alpha_seekbar);
        backgroundAlphaSeekBar.setProgress(WidgetPreferences.getBackgroundAlpha(this, appWidgetId, 0));

        hourOffsetXSeekBar = findViewById(R.id.hour_offset_x_seekbar);
        hourOffsetYSeekBar = findViewById(R.id.hour_offset_y_seekbar);
        minuteOffsetXSeekBar = findViewById(R.id.minute_offset_x_seekbar);
        minuteOffsetYSeekBar = findViewById(R.id.minute_offset_y_seekbar);

        // Set seekbar ranges and initial positions
        int maxOffset = WidgetPreferences.getMaxOffset();
        int minOffset = WidgetPreferences.getMinOffset();
        int offsetRange = maxOffset - minOffset; // 400

        hourOffsetXSeekBar.setMax(offsetRange);
        hourOffsetYSeekBar.setMax(offsetRange);
        minuteOffsetXSeekBar.setMax(offsetRange);
        minuteOffsetYSeekBar.setMax(offsetRange);

        // Set initial progress with center position (progress 200 = 0 offset)
        int centerProgress = (0 - minOffset); // Center position = 200

        int hourOffsetX = WidgetPreferences.getOffsetX(this, appWidgetId, "hour", 0);
        int hourOffsetY = WidgetPreferences.getOffsetY(this, appWidgetId, "hour", 0);
        int minuteOffsetX = WidgetPreferences.getOffsetX(this, appWidgetId, "minute", 0);
        int minuteOffsetY = WidgetPreferences.getOffsetY(this, appWidgetId, "minute", 0);

        hourOffsetXSeekBar.setProgress(hourOffsetX - minOffset);
        hourOffsetYSeekBar.setProgress(hourOffsetY - minOffset);
        minuteOffsetXSeekBar.setProgress(minuteOffsetX - minOffset);
        minuteOffsetYSeekBar.setProgress(minuteOffsetY - minOffset);

        showSecondsCheckbox = findViewById(R.id.show_seconds_checkbox);
        showDateCheckbox = findViewById(R.id.show_date_checkbox);
        showDayOfWeekCheckbox = findViewById(R.id.show_day_of_week_checkbox);
        use12HourCheckbox = findViewById(R.id.use_12hour_checkbox);
        secondsAsWordsCheckbox = findViewById(R.id.seconds_as_words_checkbox);

        minuteFontSizeSeekBar = findViewById(R.id.minute_font_size_seekbar);
        secondFontSizeSeekBar = findViewById(R.id.second_font_size_seekbar);

        secondOffsetXSeekBar = findViewById(R.id.second_offset_x_seekbar);
        secondOffsetYSeekBar = findViewById(R.id.second_offset_y_seekbar);
        dateOffsetXSeekBar = findViewById(R.id.date_offset_x_seekbar);
        dateOffsetYSeekBar = findViewById(R.id.date_offset_y_seekbar);
        dayOfWeekOffsetXSeekBar = findViewById(R.id.day_of_week_offset_x_seekbar);
        dayOfWeekOffsetYSeekBar = findViewById(R.id.day_of_week_offset_y_seekbar);
        dayNightOffsetXSeekBar = findViewById(R.id.day_night_offset_x_seekbar);
        dayNightOffsetYSeekBar = findViewById(R.id.day_night_offset_y_seekbar);

        // Set ranges for additional offset seekbars
        secondOffsetXSeekBar.setMax(offsetRange);
        secondOffsetYSeekBar.setMax(offsetRange);
        dateOffsetXSeekBar.setMax(offsetRange);
        dateOffsetYSeekBar.setMax(offsetRange);
        dayOfWeekOffsetXSeekBar.setMax(offsetRange);
        dayOfWeekOffsetYSeekBar.setMax(offsetRange);
        dayNightOffsetXSeekBar.setMax(offsetRange);
        dayNightOffsetYSeekBar.setMax(offsetRange);

        // Set initial progress with bounded offsets
        int secondOffsetX = WidgetPreferences.getSecondOffsetX(this, appWidgetId, 0);
        int secondOffsetY = WidgetPreferences.getSecondOffsetY(this, appWidgetId, 0);
        int dateOffsetX = WidgetPreferences.getDateOffsetX(this, appWidgetId, 0);
        int dateOffsetY = WidgetPreferences.getDateOffsetY(this, appWidgetId, 0);
        int dayOfWeekOffsetX = WidgetPreferences.getDayOfWeekOffsetX(this, appWidgetId, 0);
        int dayOfWeekOffsetY = WidgetPreferences.getDayOfWeekOffsetY(this, appWidgetId, 0);
        int dayNightOffsetX = WidgetPreferences.getDayNightOffsetX(this, appWidgetId, 0);
        int dayNightOffsetY = WidgetPreferences.getDayNightOffsetY(this, appWidgetId, 0);

        secondOffsetXSeekBar.setProgress(secondOffsetX - minOffset);
        secondOffsetYSeekBar.setProgress(secondOffsetY - minOffset);
        dateOffsetXSeekBar.setProgress(dateOffsetX - minOffset);
        dateOffsetYSeekBar.setProgress(dateOffsetY - minOffset);
        dayOfWeekOffsetXSeekBar.setProgress(dayOfWeekOffsetX - minOffset);
        dayOfWeekOffsetYSeekBar.setProgress(dayOfWeekOffsetY - minOffset);
        dayNightOffsetXSeekBar.setProgress(dayNightOffsetX - minOffset);
        dayNightOffsetYSeekBar.setProgress(dayNightOffsetY - minOffset);

        showSecondsCheckbox.setChecked(WidgetPreferences.getShowSeconds(this, appWidgetId, false));
        showDateCheckbox.setChecked(WidgetPreferences.getShowDate(this, appWidgetId, false));
        showDayOfWeekCheckbox.setChecked(WidgetPreferences.getShowDayOfWeek(this, appWidgetId, false));
        use12HourCheckbox.setChecked(WidgetPreferences.getUse12HourFormat(this, appWidgetId, false));
        secondsAsWordsCheckbox.setChecked(WidgetPreferences.getSecondsAsWords(this, appWidgetId, true));

        String secondsDisplayMode = WidgetPreferences.getSecondsDisplayMode(this, appWidgetId, "Горизонтально");
        secondsDisplayModeSpinner.setSelection(secondsDisplayMode.equals("Вертикально") ? 1 : 0);

        String blockMode = WidgetPreferences.getBlockMode(this, appWidgetId, "Обычный");
        blockModeSpinner.setSelection(blockMode.equals("Блочная система") ? 1 : 0);

        blockBackgroundColorSpinner.setSelection(getPositionForColor(WidgetPreferences.getBlockBackgroundColor(this, appWidgetId, Color.TRANSPARENT)));
        blockBorderColorSpinner.setSelection(getPositionForColor(WidgetPreferences.getBlockBorderColor(this, appWidgetId, Color.GRAY)));

        minuteFontSizeSeekBar.setProgress((int) WidgetPreferences.getMinuteFontSize(this, appWidgetId, 24f));
        secondFontSizeSeekBar.setProgress((int) WidgetPreferences.getSecondFontSize(this, appWidgetId, 18f));

        String savedStyle = WidgetPreferences.getStyle(this, appWidgetId, "Базовый");
        int stylePosition = getStylePosition(savedStyle);
        if (stylePosition >= 0) styleSpinner.setSelection(stylePosition);

        int savedColor = WidgetPreferences.getColor(this, appWidgetId, Color.BLACK);
        colorSpinner.setSelection(getPositionForColor(savedColor));
        borderColorSpinner.setSelection(getPositionForColor(WidgetPreferences.getBorderColor(this, appWidgetId, Color.RED)));
        backgroundColorSpinner.setSelection(getPositionForColor(WidgetPreferences.getBackgroundColor(this, appWidgetId, Color.TRANSPARENT)));

        // Add listeners for live preview
        SeekBar.OnSeekBarChangeListener previewListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    updatePreview();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };

        CheckBox.OnCheckedChangeListener checkboxListener = (buttonView, isChecked) -> updatePreview();
        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePreview();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        fontSizeSeekBar.setOnSeekBarChangeListener(previewListener);
        colorSpinner.setOnItemSelectedListener(spinnerListener);
        borderColorSpinner.setOnItemSelectedListener(spinnerListener);
        backgroundColorSpinner.setOnItemSelectedListener(spinnerListener);
        secondsDisplayModeSpinner.setOnItemSelectedListener(spinnerListener);
        blockModeSpinner.setOnItemSelectedListener(spinnerListener);
        blockBackgroundColorSpinner.setOnItemSelectedListener(spinnerListener);
        blockBorderColorSpinner.setOnItemSelectedListener(spinnerListener);
        backgroundAlphaSeekBar.setOnSeekBarChangeListener(previewListener);
        
        showSecondsCheckbox.setOnCheckedChangeListener(checkboxListener);
        showDateCheckbox.setOnCheckedChangeListener(checkboxListener);
        showDayOfWeekCheckbox.setOnCheckedChangeListener(checkboxListener);
        use12HourCheckbox.setOnCheckedChangeListener(checkboxListener);

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConfiguration();
            }
        });

        // Start preview updates
        startPreviewUpdates();
        updatePreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPreviewUpdates();
    }

    private void startPreviewUpdates() {
        previewUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                updatePreview();
                previewHandler.postDelayed(this, 1000); // Update every second
            }
        };
        previewHandler.post(previewUpdateRunnable);
    }

    private void stopPreviewUpdates() {
        if (previewUpdateRunnable != null) {
            previewHandler.removeCallbacks(previewUpdateRunnable);
        }
    }

    private void updatePreview() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+3"));
        boolean use12Hour = use12HourCheckbox.isChecked();
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

        boolean secondsAsWords = secondsAsWordsCheckbox.isChecked();
        String secondsDisplayMode = (String) secondsDisplayModeSpinner.getSelectedItem();

        String secondText = NumberToWords.convertSecond(second, secondsAsWords);
        if ("Вертикально".equals(secondsDisplayMode)) {
            if (secondsAsWords) {
                String[] secondLines = NumberToWords.convertSecondVertical(second, true);
                secondText = android.text.TextUtils.join("\n", secondLines);
            } else {
                String formatted = String.format("%02d", second);
                secondText = formatted.charAt(0) + "\n" + formatted.charAt(1);
            }
        }

        previewHourText.setText(hourText);
        previewDayNightText.setText(dayNightText);
        previewMinuteText.setText(minuteText);

        if (showDayOfWeekCheckbox.isChecked()) {
            previewDayOfWeekText.setVisibility(View.VISIBLE);
            previewDayOfWeekText.setText(dayOfWeekText);
        } else {
            previewDayOfWeekText.setVisibility(View.GONE);
        }

        if (showDateCheckbox.isChecked()) {
            previewDateText.setVisibility(View.VISIBLE);
            previewDateText.setText(dateText);
        } else {
            previewDateText.setVisibility(View.GONE);
        }

        if (showSecondsCheckbox.isChecked()) {
            previewSecondText.setVisibility(View.VISIBLE);
            previewSecondText.setText(secondText);
            previewSecondText.setTextSize(secondFontSizeSeekBar.getProgress());
        } else {
            previewSecondText.setVisibility(View.GONE);
        }

        int textColor = getColorFromSpinner(colorSpinner);
        int blockBackgroundColor = getColorFromSpinner(blockBackgroundColorSpinner);
        int blockBorderColor = getColorFromSpinner(blockBorderColorSpinner);

        boolean blockModeEnabled = "Блочная система".equals(blockModeSpinner.getSelectedItem());

        applyBlockStyle(previewHourText, blockModeEnabled, blockBackgroundColor, blockBorderColor);
        applyBlockStyle(previewMinuteText, blockModeEnabled, blockBackgroundColor, blockBorderColor);
        applyBlockStyle(previewSecondText, blockModeEnabled, blockBackgroundColor, blockBorderColor);
        applyBlockStyle(previewDayNightText, blockModeEnabled, blockBackgroundColor, blockBorderColor);
        applyBlockStyle(previewDayOfWeekText, blockModeEnabled, blockBackgroundColor, blockBorderColor);
        applyBlockStyle(previewDateText, blockModeEnabled, blockBackgroundColor, blockBorderColor);
        float fontSize = fontSizeSeekBar.getProgress();
        int backgroundColor = getColorFromSpinner(backgroundColorSpinner);
        int backgroundAlpha = backgroundAlphaSeekBar.getProgress();
        int bgColor = Color.argb(backgroundAlpha, Color.red(backgroundColor), Color.green(backgroundColor), Color.blue(backgroundColor));

        previewHourText.setTextColor(textColor);
        previewHourText.setTextSize(fontSize);
        previewMinuteText.setTextColor(textColor);
        previewMinuteText.setTextSize(fontSize);
        previewDayNightText.setTextColor(getColorFromSpinner(borderColorSpinner));
        previewDayOfWeekText.setTextColor(textColor);
        previewDateText.setTextColor(textColor);
        previewSecondText.setTextColor(textColor);

        findViewById(R.id.widget_preview_container).setBackgroundColor(bgColor);
    }

    private void applyBlockStyle(TextView view, boolean enabled, int bgColor, int borderColor) {
        if (view == null) return;
        if (enabled) {
            view.setBackgroundColor(bgColor);
            view.setTextColor(borderColor);
            view.setPadding(12, 8, 12, 8);
        } else {
            view.setBackgroundColor(Color.TRANSPARENT);
            view.setTextColor(getColorFromSpinner(colorSpinner));
            view.setPadding(0, 0, 0, 0);
        }
    }

    private void saveConfiguration() {
        String style = (String) styleSpinner.getSelectedItem();
        WidgetPreferences.saveStyle(this, appWidgetId, style);

        int color = getColorFromSpinner(colorSpinner);
        WidgetPreferences.saveColor(this, appWidgetId, color);

        float fontSize = fontSizeSeekBar.getProgress();
        WidgetPreferences.saveFontSize(this, appWidgetId, fontSize);

        int borderColor = getColorFromSpinner(borderColorSpinner);
        WidgetPreferences.saveBorderColor(this, appWidgetId, borderColor);

        int borderWidth = borderWidthSeekBar.getProgress();
        WidgetPreferences.saveBorderWidth(this, appWidgetId, borderWidth);

        int backgroundColor = getColorFromSpinner(backgroundColorSpinner);
        WidgetPreferences.saveBackgroundColor(this, appWidgetId, backgroundColor);

        int backgroundAlpha = backgroundAlphaSeekBar.getProgress();
        WidgetPreferences.saveBackgroundAlpha(this, appWidgetId, backgroundAlpha);

        String secondsDisplayMode = (String) secondsDisplayModeSpinner.getSelectedItem();
        WidgetPreferences.saveSecondsDisplayMode(this, appWidgetId, secondsDisplayMode);

        String blockMode = (String) blockModeSpinner.getSelectedItem();
        WidgetPreferences.saveBlockMode(this, appWidgetId, blockMode);

        int blockBackgroundColor = getColorFromSpinner(blockBackgroundColorSpinner);
        WidgetPreferences.saveBlockBackgroundColor(this, appWidgetId, blockBackgroundColor);

        int blockBorderColor = getColorFromSpinner(blockBorderColorSpinner);
        WidgetPreferences.saveBlockBorderColor(this, appWidgetId, blockBorderColor);

        // Convert bounded progress values back to real offsets
        int minOffset = WidgetPreferences.getMinOffset();
        
        int hourOffsetX = WidgetPreferences.constrainOffset(hourOffsetXSeekBar.getProgress() + minOffset);
        int hourOffsetY = WidgetPreferences.constrainOffset(hourOffsetYSeekBar.getProgress() + minOffset);
        int minuteOffsetX = WidgetPreferences.constrainOffset(minuteOffsetXSeekBar.getProgress() + minOffset);
        int minuteOffsetY = WidgetPreferences.constrainOffset(minuteOffsetYSeekBar.getProgress() + minOffset);
        int secondOffsetX = WidgetPreferences.constrainOffset(secondOffsetXSeekBar.getProgress() + minOffset);
        int secondOffsetY = WidgetPreferences.constrainOffset(secondOffsetYSeekBar.getProgress() + minOffset);
        int dateOffsetX = WidgetPreferences.constrainOffset(dateOffsetXSeekBar.getProgress() + minOffset);
        int dateOffsetY = WidgetPreferences.constrainOffset(dateOffsetYSeekBar.getProgress() + minOffset);
        int dayOfWeekOffsetX = WidgetPreferences.constrainOffset(dayOfWeekOffsetXSeekBar.getProgress() + minOffset);
        int dayOfWeekOffsetY = WidgetPreferences.constrainOffset(dayOfWeekOffsetYSeekBar.getProgress() + minOffset);
        int dayNightOffsetX = WidgetPreferences.constrainOffset(dayNightOffsetXSeekBar.getProgress() + minOffset);
        int dayNightOffsetY = WidgetPreferences.constrainOffset(dayNightOffsetYSeekBar.getProgress() + minOffset);

        WidgetPreferences.saveOffsetX(this, appWidgetId, "hour", hourOffsetX);
        WidgetPreferences.saveOffsetY(this, appWidgetId, "hour", hourOffsetY);
        WidgetPreferences.saveOffsetX(this, appWidgetId, "minute", minuteOffsetX);
        WidgetPreferences.saveOffsetY(this, appWidgetId, "minute", minuteOffsetY);

        WidgetPreferences.saveShowSeconds(this, appWidgetId, showSecondsCheckbox.isChecked());
        WidgetPreferences.saveShowDate(this, appWidgetId, showDateCheckbox.isChecked());
        WidgetPreferences.saveShowDayOfWeek(this, appWidgetId, showDayOfWeekCheckbox.isChecked());
        WidgetPreferences.saveUse12HourFormat(this, appWidgetId, use12HourCheckbox.isChecked());
        WidgetPreferences.saveSecondsAsWords(this, appWidgetId, secondsAsWordsCheckbox.isChecked());

        WidgetPreferences.saveMinuteFontSize(this, appWidgetId, minuteFontSizeSeekBar.getProgress());
        WidgetPreferences.saveSecondFontSize(this, appWidgetId, secondFontSizeSeekBar.getProgress());

        WidgetPreferences.saveSecondOffsetX(this, appWidgetId, secondOffsetX);
        WidgetPreferences.saveSecondOffsetY(this, appWidgetId, secondOffsetY);
        WidgetPreferences.saveDateOffsetX(this, appWidgetId, dateOffsetX);
        WidgetPreferences.saveDateOffsetY(this, appWidgetId, dateOffsetY);
        WidgetPreferences.saveDayOfWeekOffsetX(this, appWidgetId, dayOfWeekOffsetX);
        WidgetPreferences.saveDayOfWeekOffsetY(this, appWidgetId, dayOfWeekOffsetY);
        WidgetPreferences.saveDayNightOffsetX(this, appWidgetId, dayNightOffsetX);
        WidgetPreferences.saveDayNightOffsetY(this, appWidgetId, dayNightOffsetY);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);

        // Update the widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        Class<?> providerClass = getProviderClass(style);
        Intent intent = new Intent(this, providerClass);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId});
        sendBroadcast(intent);

        finish();
    }

    private int getColorFromSpinner(Spinner spinner) {
        switch (spinner.getSelectedItemPosition()) {
            case 0: return Color.BLACK;
            case 1: return Color.WHITE;
            case 2: return Color.RED;
            case 3: return Color.GREEN;
            case 4: return Color.BLUE;
            case 5: return Color.YELLOW;
            case 6: return Color.rgb(255, 165, 0); // Orange
            case 7: return Color.rgb(128, 0, 128); // Purple
            case 8: return Color.rgb(255, 192, 203); // Pink
            case 9: return Color.GRAY;
            default: return Color.BLACK;
        }
    }

    private int getStylePosition(String style) {
        switch (style) {
            case "Базовый": return 0;
            case "Горизонтальный": return 1;
            case "Расширенный": return 2;
            case "Кислотный": return 3;
            case "Неоновый": return 4;
            case "Маленький": return 5;
            default: return 0;
        }
    }

    private int getPositionForColor(int color) {
        switch (color) {
            case Color.BLACK: return 0;
            case Color.WHITE: return 1;
            case Color.RED: return 2;
            case Color.GREEN: return 3;
            case Color.BLUE: return 4;
            case Color.YELLOW: return 5;
            case 0xFFFFA500: return 6; // orange
            case 0xFF800080: return 7; // purple
            case 0xFFFFC0CB: return 8; // pink
            case Color.GRAY: return 9;
            default: return 0;
        }
    }

    private Class<?> getProviderClass(String style) {
        switch (style) {
            case "Базовый": return WordClockWidgetProvider.class;
            case "Горизонтальный": return HorizontalWordClockWidgetProvider.class;
            case "Расширенный": return ExtendedWordClockWidgetProvider.class;
            case "Кислотный": return AcidWordClockWidgetProvider.class;
            case "Неоновый": return NeonWordClockWidgetProvider.class;
            case "Маленький": return SmallWordClockWidgetProvider.class;
            default: return WordClockWidgetProvider.class;
        }
    }

    private int findExistingWidgetId() {
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        int[] widgetIds;

        widgetIds = manager.getAppWidgetIds(new ComponentName(this, WordClockWidgetProvider.class));
        if (widgetIds.length > 0) return widgetIds[0];

        widgetIds = manager.getAppWidgetIds(new ComponentName(this, HorizontalWordClockWidgetProvider.class));
        if (widgetIds.length > 0) return widgetIds[0];

        widgetIds = manager.getAppWidgetIds(new ComponentName(this, ExtendedWordClockWidgetProvider.class));
        if (widgetIds.length > 0) return widgetIds[0];

        widgetIds = manager.getAppWidgetIds(new ComponentName(this, AcidWordClockWidgetProvider.class));
        if (widgetIds.length > 0) return widgetIds[0];

        widgetIds = manager.getAppWidgetIds(new ComponentName(this, NeonWordClockWidgetProvider.class));
        if (widgetIds.length > 0) return widgetIds[0];

        widgetIds = manager.getAppWidgetIds(new ComponentName(this, SmallWordClockWidgetProvider.class));
        if (widgetIds.length > 0) return widgetIds[0];

        return AppWidgetManager.INVALID_APPWIDGET_ID;
    }
}