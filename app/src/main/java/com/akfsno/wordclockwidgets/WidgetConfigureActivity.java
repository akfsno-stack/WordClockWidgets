package com.akfsno.wordclockwidgets;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WidgetConfigureActivity extends Activity {

    private static final int REAL_WIDGET_DP_WIDTH = 210;
    private static final int REAL_WIDGET_DP_HEIGHT = 70;
    private static final int CONSTRUCTOR_PREVIEW_DP_WIDTH = 300;
    private static final int CONSTRUCTOR_PREVIEW_DP_HEIGHT = 150;

    private static final int GRID_COLUMNS = 6;
    private static final int GRID_ROWS = 2;

    private int previewPixelWidth;
    private int previewPixelHeight;

    private int appWidgetId;
    private ExpandableListView blockList;
    private View previewContainer;
    private View hourWrapper, minuteWrapper, dayNightWrapper, dateWrapper, dayOfWeekWrapper;
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
    private boolean previewInitialized = false;

    @Override
    protected void onPause() {
        super.onPause();
        // auto-save constructor values to avoid loss when user closes without explicit Save
        saveOffsets();
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
        // Delay updatePreview until view is laid out and getWidth/getHeight work properly
        if (previewContainer != null) {
            previewContainer.getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (!previewInitialized) {
                        previewInitialized = true;
                        previewContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        updatePreview();
                    }
                }
            });
        } else {
            Log.w("WidgetConfigureActivity", "previewContainer is null in onCreate, calling updatePreview directly");
            updatePreview();
        }
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
        previewContainer = findViewById(R.id.widget_preview_container);
        hourWrapper = findViewById(R.id.hour_wrapper);
        minuteWrapper = findViewById(R.id.minute_wrapper);
        dayNightWrapper = findViewById(R.id.day_night_wrapper);
        dateWrapper = findViewById(R.id.date_wrapper);
        dayOfWeekWrapper = findViewById(R.id.day_of_week_wrapper);

        previewHour = findViewById(R.id.hour_text);
        previewMinute = findViewById(R.id.minute_text);
        previewDayNight = findViewById(R.id.day_night_text);
        previewDate = findViewById(R.id.date_text);
        previewDayOfWeek = findViewById(R.id.day_of_week_text);
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

        android.util.DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int requiredWidthPx = dpToPx(CONSTRUCTOR_PREVIEW_DP_WIDTH);
        int previewWidthPx = Math.min(screenWidth, requiredWidthPx);
        int previewHeightPx = dpToPx(CONSTRUCTOR_PREVIEW_DP_HEIGHT);

        previewPixelWidth = previewWidthPx;
        previewPixelHeight = previewHeightPx;

        addGridOverlay();

        ViewGroup.LayoutParams params = previewContainer.getLayoutParams();
        if (params != null) {
            params.width = previewWidthPx;
            params.height = previewHeightPx;
            previewContainer.setLayoutParams(params);
        }

        // Set margins to zero
        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) previewContainer.getLayoutParams();
        if (marginParams != null) {
            marginParams.setMargins(0, 0, 0, 0);
            previewContainer.setLayoutParams(marginParams);
        }
    }

    private void addGridOverlay() {
        if (!(previewContainer instanceof FrameLayout)) return;

        FrameLayout container = (FrameLayout) previewContainer;
        // Remove existing grid lines
        for (int i = container.getChildCount() - 1; i >= 0; i--) {
            View child = container.getChildAt(i);
            Object tag = child.getTag();
            if (tag != null && "grid_line".equals(tag)) {
                container.removeViewAt(i);
            }
        }

        int cellWidth = previewPixelWidth / GRID_COLUMNS;
        int cellHeight = previewPixelHeight / GRID_ROWS;

        int lineColor = 0x55FF0000; // semi-transparent red

        for (int x = 1; x < GRID_COLUMNS; x++) {
            View line = new View(this);
            line.setBackgroundColor(lineColor);
            line.setTag("grid_line");
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    Math.max(1, dpToPx(1)),
                    previewPixelHeight
            );
            lp.leftMargin = x * cellWidth;
            lp.topMargin = 0;
            line.setLayoutParams(lp);
            container.addView(line);
        }

        for (int y = 1; y < GRID_ROWS; y++) {
            View line = new View(this);
            line.setBackgroundColor(lineColor);
            line.setTag("grid_line");
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    previewPixelWidth,
                    Math.max(1, dpToPx(1))
            );
            lp.leftMargin = 0;
            lp.topMargin = y * cellHeight;
            line.setLayoutParams(lp);
            container.addView(line);
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
        blockOffsets.put("hour", new int[]{
                widgetToPreviewX(WidgetPreferences.getOffsetX(this, appWidgetId, "hour", 0)),
                widgetToPreviewY(WidgetPreferences.getOffsetY(this, appWidgetId, "hour", 0))});
        blockOffsets.put("minute", new int[]{
                widgetToPreviewX(WidgetPreferences.getOffsetX(this, appWidgetId, "minute", 0)),
                widgetToPreviewY(WidgetPreferences.getOffsetY(this, appWidgetId, "minute", 0))});
        blockOffsets.put("dayNight", new int[]{
                widgetToPreviewX(WidgetPreferences.getDayNightOffsetX(this, appWidgetId, 0)),
                widgetToPreviewY(WidgetPreferences.getDayNightOffsetY(this, appWidgetId, 0))});
        blockOffsets.put("date", new int[]{
                widgetToPreviewX(WidgetPreferences.getDateOffsetX(this, appWidgetId, 0)),
                widgetToPreviewY(WidgetPreferences.getDateOffsetY(this, appWidgetId, 0))});
        blockOffsets.put("dayOfWeek", new int[]{
                widgetToPreviewX(WidgetPreferences.getDayOfWeekOffsetX(this, appWidgetId, 0)),
                widgetToPreviewY(WidgetPreferences.getDayOfWeekOffsetY(this, appWidgetId, 0))});
    }

    private void updatePreview() {
        View rootView = findViewById(R.id.widget_preview_container);
        BaseWordClockWidgetProvider.updateLocalWidgetView(this, rootView, appWidgetId);

        // Apply current constructor offsets to wrappers (interactive drag)
        applyMarginWithBounds("hour", hourWrapper);
        applyMarginWithBounds("minute", minuteWrapper);
        applyMarginWithBounds("dayNight", dayNightWrapper);
        applyMarginWithBounds("date", dateWrapper);
        applyMarginWithBounds("dayOfWeek", dayOfWeekWrapper);

        updateCoordinates();
    }

    private void applyMarginWithBounds(String block, View view) {
        if (view == null || previewContainer == null) {
            Log.w("WidgetConfigureActivity", "applyMarginWithBounds: view or previewContainer is null for block " + block);
            return;
        }

        int[] off = blockOffsets.get(block);
        if (off == null) return;

        int[] bounded = constrainOffsetToPreview(view, off[0], off[1]);
        off[0] = bounded[0];
        off[1] = bounded[1];

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.TOP | Gravity.START;

        int centerX = (previewContainer.getWidth() - view.getWidth()) / 2;
        int centerY = (previewContainer.getHeight() - view.getHeight()) / 2;

        params.leftMargin = centerX + bounded[0];
        params.topMargin = centerY + bounded[1];
        view.setLayoutParams(params);
    }

    private int[] constrainOffsetToPreview(View view, int x, int y) {
        if (previewContainer == null || view == null) {
            Log.w("WidgetConfigureActivity", "constrainOffsetToPreview: previewContainer or view is null, using default bounds");
            int maxX = WidgetPreferences.getMaxOffsetX();
            int minX = WidgetPreferences.getMinOffsetX();
            int maxY = WidgetPreferences.getMaxOffsetY();
            int minY = WidgetPreferences.getMinOffsetY();
            int boundedX = Math.max(minX, Math.min(maxX, x));
            int boundedY = Math.max(minY, Math.min(maxY, y));
            return new int[]{boundedX, boundedY};
        }

        int containerW = previewContainer.getWidth();
        int containerH = previewContainer.getHeight();
        int viewW = view.getWidth();
        int viewH = view.getHeight();

        if (containerW <= 0 || containerH <= 0 || viewW <= 0 || viewH <= 0) {
            int maxX = WidgetPreferences.getMaxOffsetX();
            int minX = WidgetPreferences.getMinOffsetX();
            int maxY = WidgetPreferences.getMaxOffsetY();
            int minY = WidgetPreferences.getMinOffsetY();
            int boundedX = Math.max(minX, Math.min(maxX, x));
            int boundedY = Math.max(minY, Math.min(maxY, y));
            return new int[]{boundedX, boundedY};
        }

        int maxX = (containerW - viewW) / 2;
        int minX = -maxX;

        int maxY = (containerH - viewH) / 2;
        int minY = -maxY;

        int boundedX = Math.max(minX, Math.min(maxX, x));
        int boundedY = Math.max(minY, Math.min(maxY, y));

        return new int[]{boundedX, boundedY};
    }

    private float getScaleX() {
        if (previewPixelWidth <= 0) return 1f;
        return (float) dpToPx(REAL_WIDGET_DP_WIDTH) / previewPixelWidth;
    }

    private float getScaleY() {
        if (previewPixelHeight <= 0) return 1f;
        return (float) dpToPx(REAL_WIDGET_DP_HEIGHT) / previewPixelHeight;
    }

    private int previewToWidgetX(int previewX) {
        return Math.round(previewX * getScaleX());
    }

    private int previewToWidgetY(int previewY) {
        return Math.round(previewY * getScaleY());
    }

    private int widgetToPreviewX(int widgetX) {
        return Math.round(widgetX / getScaleX());
    }

    private int widgetToPreviewY(int widgetY) {
        return Math.round(widgetY / getScaleY());
    }

    private void setupDragAndDrop() {
        View.OnTouchListener dragListener = new View.OnTouchListener() {
            private float startRawX, startRawY;
            private int startMarginX, startMarginY;
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
                        startMarginX = current[0];
                        startMarginY = current[1];

                        updateCoordinates();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getRawX() - startRawX;
                        float deltaY = event.getRawY() - startRawY;

                        int newX = (int) (startMarginX + deltaX);
                        int newY = (int) (startMarginY + deltaY);
                        int[] bounded = constrainOffsetToPreview(view, newX, newY);

                        blockOffsets.get(draggedBlock)[0] = bounded[0];
                        blockOffsets.get(draggedBlock)[1] = bounded[1];

                        updatePreview();
                        updateCoordinates();
                        return true;
                    case MotionEvent.ACTION_UP:
                        updatePreview();
                        updateCoordinates();
                        return true;
                }
                return false;
            }
        };

        if (hourWrapper != null) hourWrapper.setOnTouchListener(dragListener);
        if (minuteWrapper != null) minuteWrapper.setOnTouchListener(dragListener);
        if (dayNightWrapper != null) dayNightWrapper.setOnTouchListener(dragListener);
        if (dateWrapper != null) dateWrapper.setOnTouchListener(dragListener);
        if (dayOfWeekWrapper != null) dayOfWeekWrapper.setOnTouchListener(dragListener);
    }

    private String getBlockFromView(View view) {
        if (view == hourWrapper) return "hour";
        if (view == minuteWrapper) return "minute";
        if (view == dayNightWrapper) return "dayNight";
        if (view == dateWrapper) return "date";
        if (view == dayOfWeekWrapper) return "dayOfWeek";
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
            case "hour": return hourWrapper;
            case "minute": return minuteWrapper;
            case "dayNight": return dayNightWrapper;
            case "date": return dateWrapper;
            case "dayOfWeek": return dayOfWeekWrapper;
            default: return hourWrapper;
        }
    }

    private void updateCoordinates() {
        int[] off = blockOffsets.get(selectedBlock);
        int realX = previewToWidgetX(off[0]);
        int realY = previewToWidgetY(off[1]);
        
        // Calculate grid cell (6 columns x 2 rows) from edge-based coordinates
        float cellWidth = previewPixelWidth / 6f;
        float cellHeight = previewPixelHeight / 2f;
        float xForGrid = off[0] >= 0 ? off[0] : previewPixelWidth + off[0];
        float yForGrid = off[1] >= 0 ? off[1] : previewPixelHeight + off[1];
        int col = (int) (xForGrid / cellWidth);
        int row = (int) (yForGrid / cellHeight);

        // Clamp to valid grid range
        col = Math.max(0, Math.min(5, col));
        row = Math.max(0, Math.min(1, row));

        String horizontalEdgeDesc = off[0] >= 0 ? String.format("left=%d", off[0]) : String.format("right=%d", -off[0]);
        String verticalEdgeDesc = off[1] >= 0 ? String.format("top=%d", off[1]) : String.format("bottom=%d", -off[1]);

        coordinates.setText(String.format("Grid[%d,%d] | Preview(%d,%d) | Widget(%d,%d) | %s | %s",
            col, row, off[0], off[1], realX, realY, horizontalEdgeDesc, verticalEdgeDesc));
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
        updatePreview();
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
        WidgetPreferences.saveUseConstructorLayout(this, appWidgetId, true);

        WidgetPreferences.saveOffsetX(this, appWidgetId, "hour", previewToWidgetX(blockOffsets.get("hour")[0]));
        WidgetPreferences.saveOffsetY(this, appWidgetId, "hour", previewToWidgetY(blockOffsets.get("hour")[1]));
        WidgetPreferences.saveOffsetX(this, appWidgetId, "minute", previewToWidgetX(blockOffsets.get("minute")[0]));
        WidgetPreferences.saveOffsetY(this, appWidgetId, "minute", previewToWidgetY(blockOffsets.get("minute")[1]));
        WidgetPreferences.saveDayNightOffsetX(this, appWidgetId, previewToWidgetX(blockOffsets.get("dayNight")[0]));
        WidgetPreferences.saveDayNightOffsetY(this, appWidgetId, previewToWidgetY(blockOffsets.get("dayNight")[1]));
        WidgetPreferences.saveDateOffsetX(this, appWidgetId, previewToWidgetX(blockOffsets.get("date")[0]));
        WidgetPreferences.saveDateOffsetY(this, appWidgetId, previewToWidgetY(blockOffsets.get("date")[1]));
        WidgetPreferences.saveDayOfWeekOffsetX(this, appWidgetId, previewToWidgetX(blockOffsets.get("dayOfWeek")[0]));
        WidgetPreferences.saveDayOfWeekOffsetY(this, appWidgetId, previewToWidgetY(blockOffsets.get("dayOfWeek")[1]));

        updateWidget();

        Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show();
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