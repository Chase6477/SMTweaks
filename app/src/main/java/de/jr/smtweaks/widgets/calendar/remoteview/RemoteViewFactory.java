package de.jr.smtweaks.widgets.calendar.remoteview;

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

import de.jr.smtweaks.R;
import de.jr.smtweaks.util.CryptoUtil;
import de.jr.smtweaks.util.GsonRepository;
import de.jr.smtweaks.widgets.calendar.TableItem;

public class RemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {
    private final Context context;
    private final int widgetID;
    private final SharedPreferences prefs;
    private final int[][] textIdArray = {
            {R.id.t5l1, R.id.t5r1, R.id.t5b1},
            {R.id.t5l2, R.id.t5r2, R.id.t5b2},
            {R.id.t5l3, R.id.t5r3, R.id.t5b3},
            {R.id.t5l4, R.id.t5r4, R.id.t5b4},
            {R.id.t5l5, R.id.t5r5, R.id.t5b5}
    };
    private TableItem[] items = new TableItem[0];

    public RemoteViewFactory(Context context, Intent intent) {
        this.context = context;
        this.widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        this.prefs = context.getSharedPreferences(context.getString(R.string.calendar_widget_preference, widgetID), Context.MODE_PRIVATE);
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
        return new RemoteViews(context.getPackageName(), R.layout.calendar_widget_five_items);
    }


    @Override
    public RemoteViews getViewAt(int position) {
        int defaultColor = ContextCompat.getColor(context, R.color.widget_default_text);
        int greenColor = ContextCompat.getColor(context, R.color.widget_green);
        if (items == null) {
            return null;
        }
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.calendar_widget_five_items);
        rv.setTextViewText(R.id.cell0, String.valueOf(position + 1));
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 3; j++) {
                rv.setTextViewText(textIdArray[i][j], "");
            }
        }
        for (TableItem item : items) {
            if (item.getRow() == position + 1) {
                int[] text = textIdArray[item.getCol() - 1];
                setText(rv, text[0], item.getLeftTop(), defaultColor);

                if (item.getRightTopAlternate() != null) {
                    setText(rv, text[1], item.getRightTopAlternate(), greenColor);
                } else {
                    setText(rv, text[1], item.getRightTop(), defaultColor);
                }

                if (item.getBottomAlternate() != null) {
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
        try {
            String fileName;
            if (prefs.getBoolean("show_last_week", true))
                fileName = CryptoUtil.FileNames.PLAIN_CALENDAR_TABLE_DATA_FILE_NAME;
            else
                fileName = CryptoUtil.FileNames.PLAIN_CALENDAR_TABLE_DATA_FILE_NAME_SMALL;

            items = new GsonRepository().jsonToTableItemList(new String(CryptoUtil.readFile(
                    new File(context.getFilesDir(), fileName)
            )));
        } catch (IOException e) {
            Log.e("Data", "Data file not found", e);
        }
    }

    @Override
    public void onCreate() {}

    @Override
    public void onDestroy() {}
}
