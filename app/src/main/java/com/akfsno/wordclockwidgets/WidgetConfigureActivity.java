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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Calendar;
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
    private Button backgroundColorButton, borderColorButton;
    private SeekBar backgroundAlphaSeekBar, borderWidthSeekBar;
    private CheckBox use12hCheckbox;
    private Handler handler = new Handler();
    private Runnable moveRunnable;
    private Runnable previewUpdateRunnable;

    private String selectedBlock = "hour";
    private Map<String, int[]> blockOffsets = new HashMap<>();
    private int currentBackgroundColor = 0xFFFFFFFF;
    private int currentBorderColor = 0xFF000000;

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
        setupGeneralControls();
        startPreviewUpdater();
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
        backgroundColorButton = findViewById(R.id.background_color_button);
        borderColorButton = findViewById(R.id.border_color_button);
        backgroundAlphaSeekBar = findViewById(R.id.background_alpha_seekbar);
        borderWidthSeekBar = findViewById(R.id.border_width_seekbar);
        use12hCheckbox = findViewById(R.id.use_12h_checkbox);
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
            if (group.equals("Минуты") || group.equals("Секунды")) {
                childList.add("+ 0 для цифр до 10");
            }
            childList.add("Показать элемент");
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

    private void setupGeneralControls() {
        int bgAlpha = WidgetPreferences.getBackgroundAlpha(this, appWidgetId, 255);
        backgroundAlphaSeekBar.setProgress(bgAlpha);

        int borderWidth = WidgetPreferences.getBorderWidth(this, appWidgetId, 2);
        borderWidthSeekBar.setProgress(borderWidth);

        boolean use12h = WidgetPreferences.getUse12HourFormat(this, appWidgetId, true);
        use12hCheckbox.setChecked(use12h);
        use12hCheckbox.setText(use12h ? "12-часовой режим" : "24-часовой режим");

        use12hCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            WidgetPreferences.saveUse12HourFormat(this, appWidgetId, isChecked);
            use12hCheckbox.setText(isChecked ? "12-часовой режим" : "24-часовой режим");
            updatePreviewText();
        });

        currentBackgroundColor = WidgetPreferences.getBackgroundColor(this, appWidgetId, 0xFFFFFFFF);
        backgroundColorButton.setOnClickListener(v -> {
            currentBackgroundColor = (currentBackgroundColor == 0xFFFFFFFF) ? 0xFF000000 : 0xFFFFFFFF;
            WidgetPreferences.saveBackgroundColor(this, appWidgetId, currentBackgroundColor);
            updatePreviewText();
        });

        currentBorderColor = WidgetPreferences.getBorderColor(this, appWidgetId, getResources().getColor(android.R.color.holo_red_dark));
        borderColorButton.setOnClickListener(v -> {
            currentBorderColor = (currentBorderColor == 0xFF000000) ? 0xFFFF0000 : 0xFF000000;
            WidgetPreferences.saveBorderColor(this, appWidgetId, currentBorderColor);
            updatePreviewText();
        });

        backgroundAlphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                WidgetPreferences.saveBackgroundAlpha(WidgetConfigureActivity.this, appWidgetId, progress);
                updatePreviewText();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        borderWidthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                WidgetPreferences.saveBorderWidth(WidgetConfigureActivity.this, appWidgetId, progress);
                // preview might not reflect border width because old drawable; no direct method in RemoteViews
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void startPreviewUpdater() {
        if (previewUpdateRunnable != null) {
            handler.removeCallbacks(previewUpdateRunnable);
        }
        previewUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                updatePreviewText();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(previewUpdateRunnable);
    }

    private void updatePreviewText() {
        Calendar calendar = Calendar.getInstance();
        int hour24 = calendar.get(Calendar.HOUR_OF_DAY);
        int hour12 = calendar.get(Calendar.HOUR);
        if (hour12 == 0) hour12 = 12;

        boolean use12 = WidgetPreferences.getUse12HourFormat(this, appWidgetId, true);
        boolean showHour = WidgetPreferences.getShowHour(this, appWidgetId, true);
        boolean showMinute = WidgetPreferences.getShowMinute(this, appWidgetId, true);
        boolean showSecond = WidgetPreferences.getShowSeconds(this, appWidgetId, false);
        boolean showDayNight = WidgetPreferences.getShowDayNight(this, appWidgetId, true);
        boolean showDate = WidgetPreferences.getShowDate(this, appWidgetId, true);
        boolean showDayOfWeek = WidgetPreferences.getShowDayOfWeek(this, appWidgetId, true);

        String hourText = use12 ? NumberToWords.convertHour(hour24) : NumberToWords.convertHour24(hour24);
        String minuteText = NumberToWords.convertMinute(calendar.get(Calendar.MINUTE), WidgetPreferences.getAddZeroMinute(this, appWidgetId, false));
        String secondText = NumberToWords.convertSecond(calendar.get(Calendar.SECOND), true, WidgetPreferences.getAddZeroSecond(this, appWidgetId, false));
        String dayNightText = NumberToWords.getDayNight(hour24);
        String dateText = NumberToWords.convertDate(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
        String dayOfWeekText = NumberToWords.getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK) - 1);

        previewHour.setText(hourText);
        previewMinute.setText(minuteText);
        previewSecond.setText(secondText);
        previewDayNight.setText(dayNightText);
        previewDate.setText(dateText);
        previewDayOfWeek.setText(dayOfWeekText);

        previewHour.setTextSize(WidgetPreferences.getFontSize(this, appWidgetId, 24f));
        previewMinute.setTextSize(WidgetPreferences.getMinuteFontSize(this, appWidgetId, 24f));
        previewSecond.setTextSize(WidgetPreferences.getSecondFontSize(this, appWidgetId, 18f));

        previewHour.setTextColor(WidgetPreferences.getHourTextColor(this, appWidgetId, getResources().getColor(android.R.color.black)));
        previewMinute.setTextColor(WidgetPreferences.getMinuteTextColor(this, appWidgetId, getResources().getColor(android.R.color.black)));
        previewSecond.setTextColor(WidgetPreferences.getSecondTextColor(this, appWidgetId, getResources().getColor(android.R.color.black)));
        previewDayNight.setTextColor(WidgetPreferences.getDayNightTextColor(this, appWidgetId, getResources().getColor(android.R.color.holo_red_dark)));
        previewDate.setTextColor(WidgetPreferences.getDateTextColor(this, appWidgetId, getResources().getColor(android.R.color.black)));
        previewDayOfWeek.setTextColor(WidgetPreferences.getDayOfWeekTextColor(this, appWidgetId, getResources().getColor(android.R.color.black)));

        previewHour.setVisibility(showHour ? View.VISIBLE : View.GONE);
        previewMinute.setVisibility(showMinute ? View.VISIBLE : View.GONE);
        previewSecond.setVisibility(showSecond ? View.VISIBLE : View.GONE);
        previewDayNight.setVisibility(showDayNight ? View.VISIBLE : View.GONE);
        previewDate.setVisibility(showDate ? View.VISIBLE : View.GONE);
        previewDayOfWeek.setVisibility(showDayOfWeek ? View.VISIBLE : View.GONE);

        int bgColor = WidgetPreferences.getBackgroundColor(this, appWidgetId, 0xFFFFFFFF);
        int alpha = WidgetPreferences.getBackgroundAlpha(this, appWidgetId, 255);
        bgColor = (bgColor & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
        findViewById(R.id.preview_container).setBackgroundColor(bgColor);

        int borderColor = WidgetPreferences.getBorderColor(this, appWidgetId, getResources().getColor(android.R.color.holo_red_dark));
        findViewById(R.id.preview_container).getBackground().setTint(borderColor);
    }

    private void setupButtons() {
        saveButton.setOnClickListener(v -> saveOffsets());
        applyButton.setOnClickListener(v -> applyOffsets());
        resetAllButton.setOnClickListener(v -> resetAll());
    }

    private void setupJoystick() {
        View.OnClickListener clickListener = v -> {
            if (v == joystickUp) moveBlock(0, -10);
            else if (v == joystickDown) moveBlock(0, 10);
            else if (v == joystickLeft) moveBlock(-10, 0);
            else if (v == joystickRight) moveBlock(10, 0);
        };

        View.OnLongClickListener longClickListener = v -> {
            int deltaX = 0, deltaY = 0;
            if (v == joystickUp) deltaY = -5;
            else if (v == joystickDown) deltaY = 5;
            else if (v == joystickLeft) deltaX = -5;
            else if (v == joystickRight) deltaX = 5;

            final int fx = deltaX;
            final int fy = deltaY;

            moveRunnable = new Runnable() {
                @Override
                public void run() {
                    moveBlock(fx, fy);
                    handler.postDelayed(this, 100);
                }
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
        WidgetPreferences.saveOffsetX(this, appWidgetId, "hour", blockOffsets.get("hour")[0]);
        WidgetPreferences.saveOffsetY(this, appWidgetId, "hour", blockOffsets.get("hour")[1]);
        WidgetPreferences.saveOffsetX(this, appWidgetId, "minute", blockOffsets.get("minute")[0]);
        WidgetPreferences.saveOffsetY(this, appWidgetId, "minute", blockOffsets.get("minute")[1]);
        WidgetPreferences.saveSecondOffsetX(this, appWidgetId, blockOffsets.get("second")[0]);
        WidgetPreferences.saveSecondOffsetY(this, appWidgetId, blockOffsets.get("second")[1]);
        WidgetPreferences.saveDayNightOffsetX(this, appWidgetId, blockOffsets.get("dayNight")[0]);
        WidgetPreferences.saveDayNightOffsetY(this, appWidgetId, blockOffsets.get("dayNight")[1]);
        WidgetPreferences.saveDateOffsetX(this, appWidgetId, blockOffsets.get("date")[0]);
        WidgetPreferences.saveDateOffsetY(this, appWidgetId, blockOffsets.get("date")[1]);
        WidgetPreferences.saveDayOfWeekOffsetX(this, appWidgetId, blockOffsets.get("dayOfWeek")[0]);
        WidgetPreferences.saveDayOfWeekOffsetY(this, appWidgetId, blockOffsets.get("dayOfWeek")[1]);

        WidgetPreferences.saveUse12HourFormat(this, appWidgetId, use12hCheckbox.isChecked());
        WidgetPreferences.saveBackgroundAlpha(this, appWidgetId, backgroundAlphaSeekBar.getProgress());
        WidgetPreferences.saveBorderWidth(this, appWidgetId, borderWidthSeekBar.getProgress());

        Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show();
    }

    private void applyOffsets() {
        saveOffsets();
        // Update widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        new WordClockWidgetProvider().onUpdate(this, appWidgetManager, new int[]{appWidgetId});
        Toast.makeText(this, "Применено", Toast.LENGTH_SHORT).show();
    }

    private void resetAll() {
        // Reset all offsets to 0
        for (String key : blockOffsets.keySet()) {
            blockOffsets.get(key)[0] = 0;
            blockOffsets.get(key)[1] = 0;
        }
        use12hCheckbox.setChecked(true);
        WidgetPreferences.saveUse12HourFormat(this, appWidgetId, true);
        WidgetPreferences.saveBackgroundColor(this, appWidgetId, 0xFFFFFFFF);
        WidgetPreferences.saveBackgroundAlpha(this, appWidgetId, 255);
        WidgetPreferences.saveBorderColor(this, appWidgetId, getResources().getColor(android.R.color.holo_red_dark));
        WidgetPreferences.saveBorderWidth(this, appWidgetId, 2);
        // reset visibility
        WidgetPreferences.saveShowHour(this, appWidgetId, true);
        WidgetPreferences.saveShowMinute(this, appWidgetId, true);
        WidgetPreferences.saveShowSeconds(this, appWidgetId, false);
        WidgetPreferences.saveShowDayNight(this, appWidgetId, true);
        WidgetPreferences.saveShowDate(this, appWidgetId, true);
        WidgetPreferences.saveShowDayOfWeek(this, appWidgetId, true);

        updatePreview();
        updateCoordinates();
        saveOffsets();
        Toast.makeText(this, "Сброшено", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (moveRunnable != null) {
            handler.removeCallbacks(moveRunnable);
        }
        if (previewUpdateRunnable != null) {
            handler.removeCallbacks(previewUpdateRunnable);
        }
    }
}