package de.jr.smtweaks.widgets.calendar.remoteview;

import android.content.Context;
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
    private final int[][] textIdArray = {
            {R.id.t5l1, R.id.t5r1, R.id.t5b1},
            {R.id.t5l2, R.id.t5r2, R.id.t5b2},
            {R.id.t5l3, R.id.t5r3, R.id.t5b3},
            {R.id.t5l4, R.id.t5r4, R.id.t5b4},
            {R.id.t5l5, R.id.t5r5, R.id.t5b5}
    };
    private TableItem[] items = new TableItem[0];

    public RemoteViewFactory(Context context) {
        this.context = context;
        onDataSetChanged();
    }

    @Override
    public int getCount() {
        if (items == null) {
            return 0;
        }
        int largest = 0;
        for (TableItem t: items) {
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
            rv.setTextViewText(textIdArray[i][0], "");
            rv.setTextViewText(textIdArray[i][1], "");
            rv.setTextViewText(textIdArray[i][2], "");
        }
        for (TableItem item : items) {
            if (item.getRow() == position + 1) {
                int[] text = textIdArray[item.getCol() - 1];
                rv.setTextViewText(text[0], item.getLeftTop());
                rv.setTextColor(text[0], defaultColor);

                if (item.getBottomAlternate() != null) {
                    rv.setTextViewText(text[2], item.getBottomAlternate());
                    rv.setTextColor(text[2], greenColor);
                } else {
                    rv.setTextColor(text[2], defaultColor);
                    rv.setTextViewText(text[2], item.getBottom());
                }

                if (item.getRightTopAlternate() != null) {
                    rv.setTextViewText(text[1], item.getRightTopAlternate());
                    rv.setTextColor(text[1], greenColor);
                } else {
                    rv.setTextColor(text[1], defaultColor);
                    rv.setTextViewText(text[1], item.getRightTop());
                }
                if (item.getIsCancelled()) {
                    rv.setTextColor(text[0], Color.RED);
                    rv.setTextColor(text[1], Color.RED);
                    rv.setTextColor(text[2], Color.RED);
                    for (int i = 0; i < 3; i++) {
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

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        try {
            items = new GsonRepository().jsonToTableItemList(new String(CryptoUtil.readFile(
                    new File(context.getFilesDir(),
                            CryptoUtil.FileNames.PLAIN_CALENDAR_TABLE_DATA_FILE_NAME
                    ))));
        } catch (IOException e) {
            Log.e("Data", "Data file not found", e);
        }
    }

    @Override
    public void onDestroy() {

    }
}
