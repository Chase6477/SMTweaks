package de.jr.smtweaks.widgets.calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import de.jr.smtweaks.MainActivity;
import de.jr.smtweaks.R;
import de.jr.smtweaks.UpdateService;
import de.jr.smtweaks.util.CryptoUtil;
import de.jr.smtweaks.widgets.calendar.remoteview.RemoteViewService;

public class WidgetProvider extends AppWidgetProvider {
    private static final int[] headerIDs = {R.id.header1, R.id.header2, R.id.header3, R.id.header4, R.id.header5};

    public static void updateButtonText(Context context, int appwidgetId, String text) {
        RemoteViews views = generateRemoteView(context);
        views.setTextViewText(R.id.calendar_widget_update, text);
        updateRemoteView(views, context, appwidgetId);
    }

    public static RemoteViews generateRemoteView(Context context) {
        return new RemoteViews(context.getPackageName(), R.layout.calendar_table_widget);
    }

    public static void updateRemoteView(RemoteViews views, Context context, int appWidgetId) {
        updateRemoteViewFormats(views, context);

        Intent serviceIntent = new Intent(context, RemoteViewService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.calendar_widget_list, serviceIntent);

        views.setOnClickPendingIntent(R.id.calendar_widget_update, generatePendingIntent(context, appWidgetId));

        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);

    }

    public static PendingIntent generatePendingIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction("de.jr.smtweaks.ACTION_UPDATE_CALENDAR");
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pending = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pending != null) {
            return pending;
        }

        return PendingIntent.getBroadcast(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

    }

    private static void updateRemoteViewFormats(RemoteViews views, Context context) {
        int dayOfWeek = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7;
        for (int i = 0; i < 5; i++) {
            views.setTextColor(headerIDs[i], ContextCompat.getColor(context, R.color.widget_default_text));
            views.setInt(headerIDs[i], "setBackgroundColor", Color.TRANSPARENT);
        }
        if (dayOfWeek <= 4) {
            views.setTextColor(headerIDs[dayOfWeek], ContextCompat.getColor(context, R.color.widget_fat_text));
            views.setInt(
                    headerIDs[dayOfWeek],
                    "setPaintFlags",
                    Paint.FAKE_BOLD_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG
            );
            views.setTextViewTextSize(headerIDs[dayOfWeek], TypedValue.COMPLEX_UNIT_SP, 16);
        }
        File file = new File(context.getFilesDir(), CryptoUtil.FileNames.PLAIN_CALENDAR_TABLE_DATA_FILE_NAME);
        if (MainActivity.DEBUG) {
            views.setTextViewText(R.id.calendar_widget_last_update, context.getString(R.string.calendar_table_widget_last_update, "1.1.1970 00:00"));
            return;
        }
        if (!file.exists())
            views.setTextViewText(R.id.calendar_widget_last_update, context.getString(R.string.calendar_table_widget_last_update, context.getString(R.string.calendar_table_widget_last_update_never)));
        else {
            Date fileDate = new Date(file.lastModified());
            String date = android.text.format.DateFormat.getDateFormat(context).format(fileDate);
            String time = android.text.format.DateFormat.getTimeFormat(context).format(fileDate);
            views.setTextViewText(R.id.calendar_widget_last_update, context.getString(R.string.calendar_table_widget_last_update, date + " " + time));
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateRemoteView(generateRemoteView(context), context, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if ("de.jr.smtweaks.ACTION_UPDATE_CALENDAR".equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, UpdateService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1));
            ContextCompat.startForegroundService(context, serviceIntent);
            return;
        }
        if ("de.jr.smtweaks.ACTION_CALENDAR_WIDGET_BUTTON_LOADING".equals(intent.getAction())) {
            int id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (id != -1) {
                updateButtonText(
                        context,
                        id,
                        context.getString(R.string.calendar_widget_is_loading)
                );
            }
        }
        if ("de.jr.smtweaks.ACTION_CALENDAR_WIDGET_BUTTON_READY".equals(intent.getAction())) {
            int id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (id != -1) {
                updateButtonText(
                        context,
                        id,
                        context.getString(R.string.calendar_table_widget_update)
                );
            }
        }


        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.calendar_widget_list);
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }


    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        updateRemoteView(generateRemoteView(context), context, appWidgetId);
    }
}
