package com.akfsno.wordclockwidgets;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
    private View previewContainer;
    private TextView previewHour, previewMinute, previewDayNight, previewDate, previewDayOfWeek;
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
    private String widgetProviderClass = "";
    private Map<String, int[]> blockOffsets = new HashMap<>();
    private int currentBackgroundColor = 0xFFFFFFFF;
    private int currentBorderColor = 0xFF000000;

    @Override
    protected void onPause() {
        super.onPause();
        // saveOffsets(); // removed to avoid frequent saves
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constructor);

        appWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
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

        initializeViews();
        setPreviewContainerByProvider();
        setupBlockList();
        loadOffsets();
        updatePreview();
        setupDragAndDrop();
        setupJoystick();
        startPreviewUpdater();
        setupButtons();

        // Set result for widget configuration
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
    }

    private void initializeViews() {
        blockList = findViewById(R.id.block_list);
        previewContainer = findViewById(R.id.preview_container);
        previewHour = findViewById(R.id.preview_hour);
        previewMinute = findViewById(R.id.preview_minute);
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
    }

    private void setPreviewContainerByProvider() {
        if (previewContainer == null) return;

        int widthDp = 330; // default for 3x1
        int heightDp = 72;

        if (widgetProviderClass.endsWith("SmallWordClockWidgetProvider")) {
            widthDp = 220; // 2x1
        } else if (widgetProviderClass.endsWith("WordClockWidgetProvider")) {
            widthDp = 330; // 3x1
        } else if (widgetProviderClass.endsWith("LargeWordClockWidgetProvider")) {
            widthDp = 440; // 4x1
        }

        int newWidth = dpToPx(widthDp);
        int newHeight = dpToPx(heightDp);

        ViewGroup.LayoutParams params = previewContainer.getLayoutParams();
        if (params != null) {
            params.width = newWidth;
            params.height = newHeight;
            previewContainer.setLayoutParams(params);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void setupBlockList() {
        List<String> groups = new ArrayList<>();
        groups.add("Общие настройки");
        groups.add("Часы");
        groups.add("Минуты");
        groups.add("День/Ночь");
        groups.add("Дата");
        groups.add("День недели");

        Map<String, List<String>> children = new HashMap<>();
        List<String> generalChildren = new ArrayList<>();
        generalChildren.add("Цвет фона");
        generalChildren.add("Непрозрачность фона");
        generalChildren.add("Цвет рамки");
        generalChildren.add("Толщина рамки");
        generalChildren.add("12/24-часовой режим");
        children.put("Общие настройки", generalChildren);

        for (String group : groups) {
            if (group.equals("Общие настройки")) continue;
            List<String> childList = new ArrayList<>();
            childList.add("Цвет текста");
            childList.add("Размер шрифта");
            childList.add("Показать элемент");
            if (group.equals("Минуты")) {
                childList.add("+ 0 для цифр до 10");
            }
            children.put(group, childList);
        }

        BlockAdapter adapter = new BlockAdapter(this, groups, children, appWidgetId);
        blockList.setAdapter(adapter);

        blockList.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            saveOffsets();
            selectedBlock = getBlockKey(groupPosition);
            updateCoordinates();
            updatePreviewText();
            return true;
        });
    }

    private String getBlockKey(int position) {
        switch (position) {
            case 0: return "general";
            case 1: return "hour";
            case 2: return "minute";
            case 3: return "dayNight";
            case 4: return "date";
            case 5: return "dayOfWeek";
            default: return "hour";
        }
    }

    private void loadOffsets() {
        blockOffsets.put("hour", new int[]{WidgetPreferences.getOffsetX(this, appWidgetId, "hour", 0), WidgetPreferences.getOffsetY(this, appWidgetId, "hour", 0)});
        blockOffsets.put("minute", new int[]{WidgetPreferences.getOffsetX(this, appWidgetId, "minute", 0), WidgetPreferences.getOffsetY(this, appWidgetId, "minute", 0)});
        blockOffsets.put("dayNight", new int[]{WidgetPreferences.getDayNightOffsetX(this, appWidgetId, 0), WidgetPreferences.getDayNightOffsetY(this, appWidgetId, 0)});
        blockOffsets.put("date", new int[]{WidgetPreferences.getDateOffsetX(this, appWidgetId, 0), WidgetPreferences.getDateOffsetY(this, appWidgetId, 0)});
        blockOffsets.put("dayOfWeek", new int[]{WidgetPreferences.getDayOfWeekOffsetX(this, appWidgetId, 0), WidgetPreferences.getDayOfWeekOffsetY(this, appWidgetId, 0)});
    }

    private void updatePreview() {
        applyTranslationWithBounds("hour", previewHour);
        applyTranslationWithBounds("minute", previewMinute);
        applyTranslationWithBounds("dayNight", previewDayNight);
        applyTranslationWithBounds("date", previewDate);
        applyTranslationWithBounds("dayOfWeek", previewDayOfWeek);
    }

    private void applyTranslationWithBounds(String block, View view) {
        int[] off = blockOffsets.get(block);
        if (off == null) return;

        int[] bounded = constrainOffsetToPreview(view, off[0], off[1]);
        off[0] = bounded[0];
        off[1] = bounded[1];

        view.setTranslationX(off[0]);
        view.setTranslationY(off[1]);
    }

    private int[] constrainOffsetToPreview(View view, int x, int y) {
        if (previewContainer == null || view == null) {
            return new int[]{WidgetPreferences.constrainOffset(x), WidgetPreferences.constrainOffset(y)};
        }

        int containerW = previewContainer.getWidth();
        int containerH = previewContainer.getHeight();
        int viewW = view.getWidth();
        int viewH = view.getHeight();

        if (containerW == 0 || containerH == 0 || viewW == 0 || viewH == 0) {
            return new int[]{WidgetPreferences.constrainOffset(x), WidgetPreferences.constrainOffset(y)};
        }

        int borderPx = (int) (2 * view.getResources().getDisplayMetrics().density);
        int halfW = containerW / 2;
        int halfH = containerH / 2;

        int maxX = halfW - viewW / 2 - borderPx;
        int minX = -maxX;

        int maxY = halfH - viewH / 2 - borderPx;
        int minY = -maxY;

        int boundedX = Math.max(minX, Math.min(maxX, x));
        int boundedY = Math.max(minY, Math.min(maxY, y));

        return new int[]{boundedX, boundedY};
    }

    private void setupDragAndDrop() {
        View.OnTouchListener dragListener = new View.OnTouchListener() {
            private float startRawX, startRawY;
            private int startOffsetX, startOffsetY;
            private String draggedBlock;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startRawX = event.getRawX();
                        startRawY = event.getRawY();
                        draggedBlock = getBlockFromView(view);
                        selectedBlock = draggedBlock;

                        int[] current = blockOffsets.get(draggedBlock);
                        startOffsetX = current[0];
                        startOffsetY = current[1];

                        updateCoordinates();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getRawX() - startRawX;
                        float deltaY = event.getRawY() - startRawY;

                        int newX = (int) (startOffsetX + deltaX);
                        int newY = (int) (startOffsetY + deltaY);
                        int[] bounded = constrainOffsetToPreview(view, newX, newY);

                        blockOffsets.get(draggedBlock)[0] = bounded[0];
                        blockOffsets.get(draggedBlock)[1] = bounded[1];

                        view.setTranslationX(bounded[0]);
                        view.setTranslationY(bounded[1]);
                        updateCoordinates();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int[] off = blockOffsets.get(draggedBlock);
                        off[0] = WidgetPreferences.constrainOffset(off[0]);
                        off[1] = WidgetPreferences.constrainOffset(off[1]);

                        view.setTranslationX(off[0]);
                        view.setTranslationY(off[1]);

                        return true;
                }
                return false;
            }
        };

        previewHour.setOnTouchListener(dragListener);
        previewMinute.setOnTouchListener(dragListener);
        previewDayNight.setOnTouchListener(dragListener);
        previewDate.setOnTouchListener(dragListener);
        previewDayOfWeek.setOnTouchListener(dragListener);
    }

    private String getBlockFromView(View view) {
        if (view == previewHour) return "hour";
        if (view == previewMinute) return "minute";
        if (view == previewDayNight) return "dayNight";
        if (view == previewDate) return "date";
        if (view == previewDayOfWeek) return "dayOfWeek";
        return "hour";
    }

    private void moveBlock(int dx, int dy) {
        int[] off = blockOffsets.get(selectedBlock);
        off[0] += dx;
        off[1] += dy;

        View selectedView = getViewByBlock(selectedBlock);
        int[] bounded = constrainOffsetToPreview(selectedView, off[0], off[1]);
        off[0] = bounded[0];
        off[1] = bounded[1];

        updatePreview();
        updateCoordinates();
    }

    private View getViewByBlock(String block) {
        switch (block) {
            case "hour": return previewHour;
            case "minute": return previewMinute;
            case "dayNight": return previewDayNight;
            case "date": return previewDate;
            case "dayOfWeek": return previewDayOfWeek;
            default: return previewHour;
        }
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

    public void updatePreviewText() {
        Calendar calendar = Calendar.getInstance();
        int hour24 = calendar.get(Calendar.HOUR_OF_DAY);
        int hour12 = calendar.get(Calendar.HOUR);
        if (hour12 == 0) hour12 = 12;

        boolean use12 = WidgetPreferences.getUse12HourFormat(this, appWidgetId, true);
        boolean showHour = WidgetPreferences.getShowHour(this, appWidgetId, true);
        boolean showMinute = WidgetPreferences.getShowMinute(this, appWidgetId, true);
        boolean showDayNight = WidgetPreferences.getShowDayNight(this, appWidgetId, true);
        boolean showDate = WidgetPreferences.getShowDate(this, appWidgetId, false);
        boolean showDayOfWeek = WidgetPreferences.getShowDayOfWeek(this, appWidgetId, false);

        String hourText = use12 ? NumberToWords.convertHour(hour24) : NumberToWords.convertHour24(hour24);
        String minuteText = NumberToWords.convertMinute(calendar.get(Calendar.MINUTE), WidgetPreferences.getAddZeroMinute(this, appWidgetId, false));

        if (!use12 && hour24 == 0 && calendar.get(Calendar.MINUTE) == 0) {
            hourText = "двенадцать";
            minuteText = "ноль-ноль";
        }
        String dayNightText = NumberToWords.getDayNight(hour24);
        String dateText = NumberToWords.convertDate(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
        String dayOfWeekText = NumberToWords.getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK) - 1);

        if (previewHour != null) {
            previewHour.setText(hourText);
            previewHour.setTextSize(WidgetPreferences.getFontSize(this, appWidgetId, 24f));
            previewHour.setTextColor(WidgetPreferences.getHourTextColor(this, appWidgetId, getResources().getColor(android.R.color.black)));
            previewHour.setVisibility(showHour ? View.VISIBLE : View.GONE);
        }

        if (previewMinute != null) {
            previewMinute.setText(minuteText);
            previewMinute.setTextSize(WidgetPreferences.getMinuteFontSize(this, appWidgetId, 24f));
            previewMinute.setTextColor(WidgetPreferences.getMinuteTextColor(this, appWidgetId, getResources().getColor(android.R.color.black)));
            previewMinute.setVisibility(showMinute ? View.VISIBLE : View.GONE);
        }

        if (previewDayNight != null) {
            previewDayNight.setText(dayNightText);
            previewDayNight.setTextSize(WidgetPreferences.getDayNightFontSize(this, appWidgetId, 18f));
            previewDayNight.setTextColor(WidgetPreferences.getDayNightTextColor(this, appWidgetId, getResources().getColor(android.R.color.holo_red_dark)));
            previewDayNight.setVisibility(showDayNight ? View.VISIBLE : View.GONE);
        }

        if (previewDate != null) {
            previewDate.setText(dateText);
            previewDate.setTextSize(WidgetPreferences.getDateFontSize(this, appWidgetId, 18f));
            previewDate.setTextColor(WidgetPreferences.getDateTextColor(this, appWidgetId, getResources().getColor(android.R.color.black)));
            previewDate.setVisibility(showDate ? View.VISIBLE : View.GONE);
        }

        if (previewDayOfWeek != null) {
            previewDayOfWeek.setText(dayOfWeekText);
            previewDayOfWeek.setTextSize(WidgetPreferences.getDayOfWeekFontSize(this, appWidgetId, 18f));
            previewDayOfWeek.setTextColor(WidgetPreferences.getDayOfWeekTextColor(this, appWidgetId, getResources().getColor(android.R.color.black)));
            previewDayOfWeek.setVisibility(showDayOfWeek ? View.VISIBLE : View.GONE);
        }

        // no seconds in preview anymore
        if (previewHour != null) {
            previewHour.setTextColor(WidgetPreferences.getHourTextColor(this, appWidgetId, getResources().getColor(android.R.color.black)));
        }
        if (previewMinute != null) {
            previewMinute.setTextColor(WidgetPreferences.getMinuteTextColor(this, appWidgetId, getResources().getColor(android.R.color.black)));
        }
        if (previewDayNight != null) {
            previewDayNight.setTextColor(WidgetPreferences.getDayNightTextColor(this, appWidgetId, getResources().getColor(android.R.color.holo_red_dark)));
        }
        if (previewDate != null) {
            previewDate.setTextColor(WidgetPreferences.getDateTextColor(this, appWidgetId, getResources().getColor(android.R.color.black)));
        }
        if (previewDayOfWeek != null) {
            previewDayOfWeek.setTextColor(WidgetPreferences.getDayOfWeekTextColor(this, appWidgetId, getResources().getColor(android.R.color.black)));
        }

        if (previewHour != null) previewHour.setVisibility(showHour ? View.VISIBLE : View.GONE);
        if (previewMinute != null) previewMinute.setVisibility(showMinute ? View.VISIBLE : View.GONE);
        if (previewDayNight != null) previewDayNight.setVisibility(showDayNight ? View.VISIBLE : View.GONE);
        if (previewDate != null) previewDate.setVisibility(showDate ? View.VISIBLE : View.GONE);
        if (previewDayOfWeek != null) previewDayOfWeek.setVisibility(showDayOfWeek ? View.VISIBLE : View.GONE);

        int bgColor = WidgetPreferences.getBackgroundColor(this, appWidgetId, 0xFFFFFFFF);
        int alpha = WidgetPreferences.getBackgroundAlpha(this, appWidgetId, 255);
        bgColor = (bgColor & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
        findViewById(R.id.preview_container).setBackgroundColor(bgColor);

        int borderColor = WidgetPreferences.getBorderColor(this, appWidgetId, getResources().getColor(android.R.color.holo_red_dark));
        // Set border color by modifying the drawable
        View container = findViewById(R.id.preview_container);
        android.graphics.drawable.Drawable bg = container.getBackground();
        if (bg instanceof android.graphics.drawable.GradientDrawable) {
            android.graphics.drawable.GradientDrawable drawable = (android.graphics.drawable.GradientDrawable) bg.mutate();
            drawable.setStroke(2, borderColor);
        } else {
            // Fallback if not GradientDrawable
        }
    }

    private void setupButtons() {
        saveButton.setOnClickListener(v -> saveOffsets());
        applyButton.setOnClickListener(v -> saveOffsets());
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
        WidgetPreferences.saveDayNightOffsetX(this, appWidgetId, blockOffsets.get("dayNight")[0]);
        WidgetPreferences.saveDayNightOffsetY(this, appWidgetId, blockOffsets.get("dayNight")[1]);
        WidgetPreferences.saveDateOffsetX(this, appWidgetId, blockOffsets.get("date")[0]);
        WidgetPreferences.saveDateOffsetY(this, appWidgetId, blockOffsets.get("date")[1]);
        WidgetPreferences.saveDayOfWeekOffsetX(this, appWidgetId, blockOffsets.get("dayOfWeek")[0]);
        WidgetPreferences.saveDayOfWeekOffsetY(this, appWidgetId, blockOffsets.get("dayOfWeek")[1]);

        updateWidget();

        Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show();
    }

    private void updateWidget() {
        Intent intent = new Intent(this, WordClockWidgetProvider.class);
        intent.setAction(BaseWordClockWidgetProvider.UPDATE_ACTION);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        sendBroadcast(intent);
    }

    private void resetAll() {
        // Reset all offsets to 0
        for (String key : blockOffsets.keySet()) {
            blockOffsets.get(key)[0] = 0;
            blockOffsets.get(key)[1] = 0;
        }
        WidgetPreferences.saveUse12HourFormat(this, appWidgetId, true);
        WidgetPreferences.saveBackgroundColor(this, appWidgetId, 0xFFFFFFFF);
        WidgetPreferences.saveBackgroundAlpha(this, appWidgetId, 255);
        WidgetPreferences.saveBorderColor(this, appWidgetId, getResources().getColor(android.R.color.holo_red_dark));
        WidgetPreferences.saveBorderWidth(this, appWidgetId, 2);
        // reset visibility
        WidgetPreferences.saveShowHour(this, appWidgetId, true);
        WidgetPreferences.saveShowMinute(this, appWidgetId, true);
        WidgetPreferences.saveShowDayNight(this, appWidgetId, true);
        WidgetPreferences.saveShowDate(this, appWidgetId, false);
        WidgetPreferences.saveShowDayOfWeek(this, appWidgetId, false);
        // reset colors
        WidgetPreferences.saveHourTextColor(this, appWidgetId, getResources().getColor(android.R.color.black));
        WidgetPreferences.saveMinuteTextColor(this, appWidgetId, getResources().getColor(android.R.color.black));
        WidgetPreferences.saveDayNightTextColor(this, appWidgetId, getResources().getColor(android.R.color.holo_red_dark));
        WidgetPreferences.saveDateTextColor(this, appWidgetId, getResources().getColor(android.R.color.black));
        WidgetPreferences.saveDayOfWeekTextColor(this, appWidgetId, getResources().getColor(android.R.color.black));
        // reset font sizes
        WidgetPreferences.saveFontSize(this, appWidgetId, 24f);
        WidgetPreferences.saveMinuteFontSize(this, appWidgetId, 24f);

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