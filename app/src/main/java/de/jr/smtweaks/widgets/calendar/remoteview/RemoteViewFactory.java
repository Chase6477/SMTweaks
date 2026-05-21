package de.jr.smtweaks.widgets.calendar.remoteview;

import static android.view.View.GONE;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import de.jr.smtweaks.R;
import de.jr.smtweaks.util.CryptoUtil;
import de.jr.smtweaks.util.GsonRepository;
import de.jr.smtweaks.widgets.calendar.HolidayItem;
import de.jr.smtweaks.widgets.calendar.TableItem;

public class RemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context context;
    private final int widgetID;
    private final SharedPreferences widgetPrefs;
    private final int[][] textIdArray = {
            {R.id.tl1, R.id.tr1, R.id.tb1, R.id.bg1, R.id.im1},
            {R.id.tl2, R.id.tr2, R.id.tb2, R.id.bg2, R.id.im2},
            {R.id.tl3, R.id.tr3, R.id.tb3, R.id.bg3, R.id.im3},
            {R.id.tl4, R.id.tr4, R.id.tb4, R.id.bg4, R.id.im4},
            {R.id.tl5, R.id.tr5, R.id.tb5, R.id.bg5, R.id.im5},
            {R.id.tl6, R.id.tr6, R.id.tb6, R.id.bg6, R.id.im6},
            {R.id.tl7, R.id.tr7, R.id.tb7, R.id.bg7, R.id.im7}
    };
    private int columns;
    private int mode;
    private int from;
    private int to;
    private LocalDate firstDay;
    private HolidayItem[] holidayItems;
    private TableItem[] items = new TableItem[0];

    public RemoteViewFactory(Context context, Intent intent) {
        this.context = context;
        this.widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        this.widgetPrefs = context.getSharedPreferences(context.getString(R.string.calendar_widget_preference, widgetID), Context.MODE_PRIVATE);

        if (widgetID == -1)
            return;
        onDataSetChanged();
    }


    @Override
    public int getCount() {
        if (items == null) {
            return 0;
        }
        int largest = 0;
        for (TableItem t : items) {
            largest = Math.max(largest, t.getRow());
        }
        return largest;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(context.getPackageName(), R.layout.calendar_widget_items);
    }


    @Override
    public RemoteViews getViewAt(int position) {
        int defaultColor = ContextCompat.getColor(context, R.color.widget_default_text);
        int greenColor = ContextCompat.getColor(context, R.color.widget_green);
        if (items == null) {
            return null;
        }
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.calendar_widget_items);

        rv.setTextViewText(R.id.cell0, String.valueOf(position + 1));
        for (int i = 0; i < columns; i++) {
            rv.setInt(textIdArray[i][3], "setBackgroundColor", Color.TRANSPARENT);
            for (int j = 0; j < 3; j++) {
                rv.setTextViewText(textIdArray[i][j], "");
            }
        }

        for (int i = columns + 1; i < 7; i++) {
            rv.setViewVisibility(textIdArray[i][3], GONE);
            rv.setViewVisibility(textIdArray[i][4], GONE);
        }

        if (holidayItems != null && widgetPrefs.getBoolean("show_holidays", true)) {
            LocalDate localDate = firstDay;
            for (int i = 0; i < columns; i++) {
                for (HolidayItem holidayItem : holidayItems) {
                    if (holidayItem.containsDate(localDate)) {
                        rv.setInt(textIdArray[i][3], "setBackgroundColor", ContextCompat.getColor(context, R.color.widget_alert_red));
                        break;
                    }
                }
                localDate.plusDays(1);
            }
        }

        for (TableItem item : items) {
            long daysBetween = ChronoUnit.DAYS.between(firstDay, item.getDate());
            if (item.getRow() == position + 1 && item.getDate() != null && daysBetween >= 0L && daysBetween <= columns) {
                int[] text = textIdArray[(int) daysBetween];
                setText(rv, text[0], item.getLeftTop(), defaultColor);

                if (item.getRightTopAlternate() != null && !item.getRightTopAlternate().equals(item.getRightTop())) {
                    setText(rv, text[1], item.getRightTopAlternate(), greenColor);
                } else {
                    setText(rv, text[1], item.getRightTop(), defaultColor);
                }

                if (item.getBottomAlternate() != null && !item.getBottomAlternate().equals(item.getBottom())) {
                    setText(rv, text[2], item.getBottomAlternate(), greenColor);
                } else {
                    setText(rv, text[2], item.getBottom(), defaultColor);
                }

                if (item.getIsCancelled()) {
                    for (int i = 0; i < 3; i++) {
                        rv.setTextColor(text[i], Color.RED);
                        rv.setInt(
                                text[i],
                                "setPaintFlags",
                                Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG
                        );
                    }
                } else {
                    for (int i = 0; i < 3; i++) {
                        rv.setInt(
                                text[i],
                                "setPaintFlags",
                                Paint.ANTI_ALIAS_FLAG
                        );
                    }
                }
            }
        }
        return rv;
    }

    private void setText(RemoteViews rv, int textID, String text, int color) {
        rv.setTextColor(textID, color);
        rv.setTextViewText(textID, text);
    }

    private LocalDate getFirstDay() {
        LocalDate date = LocalDate.now();
        int currentDay = date.getDayOfWeek().getValue() - 1;

        boolean inRange;

        if (from <= to) {
            inRange = (currentDay >= from && currentDay <= to);
        } else {
            inRange = (currentDay >= from || currentDay <= to);
        }

        if (inRange) {
            return date.plusDays(-((currentDay - from + 7) % 7));
        }

        int diff = (from - currentDay + 7) % 7;
        if (diff == 0) diff = 7;

        return date.plusDays(diff);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public void onDataSetChanged() {
        mode = widgetPrefs.getInt("day_list_option", R.id.calendar_config_radio_adaptive);
        from = widgetPrefs.getInt("from", 0);
        to = widgetPrefs.getInt("to", 4);
        if (mode == R.id.calendar_config_radio_adaptive)
            firstDay = getFirstDay();
        else
            firstDay = LocalDate.now();

        columns = from;
        if (mode == R.id.calendar_config_radio_adaptive) {
            if (from - to > 0)
                columns = 7 - (from - to);
            else
                columns = Math.abs(from - to);
        }

        try {
            String fileName;
            if (widgetPrefs.getBoolean("show_last_week", true)
                    && mode == R.id.calendar_config_radio_adaptive)
                fileName = CryptoUtil.FileNames.PLAIN_CALENDAR_TABLE_DATA_FILE_NAME;
            else
                fileName = CryptoUtil.FileNames.PLAIN_CALENDAR_TABLE_DATA_FILE_NAME_SMALL;

            byte[] bytes = CryptoUtil.readFile(new File(context.getFilesDir(), fileName));

            if (bytes == null)
                items = new TableItem[0];
            else
                items = new GsonRepository().jsonToTableItemList(new String(bytes));
        } catch (IOException e) {
            Log.e("Data", "Data file not found", e);
        }

        try {
            if (widgetPrefs.getBoolean("show_holidays", true)) {
                byte[] bytes = CryptoUtil.readFile(
                        new File(context.getFilesDir(), CryptoUtil.FileNames.HOLIDAY_DATES_FILE_NAME)
                );
                if (bytes == null)
                    bytes = new byte[0];

                holidayItems = new GsonRepository().jsonToHolidayItem(new String(bytes));
                if (holidayItems == null)
                    holidayItems = new HolidayItem[0];
            }
        } catch (IOException e) {
            Log.e("Holiday", "Holiday file not found", e);
        }
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
    }
}
