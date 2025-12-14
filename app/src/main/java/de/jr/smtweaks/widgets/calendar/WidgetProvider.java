package de.jr.smtweaks.widgets.calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.widget.RemoteViews;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import de.jr.smtweaks.R;
import de.jr.smtweaks.util.CryptoUtil;
import de.jr.smtweaks.util.UpdateActivity;
import de.jr.smtweaks.widgets.calendar.remoteview.RemoteViewService;

public class WidgetProvider extends AppWidgetProvider {
    private static final int[] headerIDs = {R.id.header1, R.id.header2, R.id.header3, R.id.header4, R.id.header5};

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.calendar_table_widget);
            updateRemoteViewFormats(views, context);

            Intent serviceIntent = new Intent(context, RemoteViewService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
            views.setRemoteAdapter(R.id.listView, serviceIntent);

            Intent activityIntent = new Intent(context, UpdateActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            views.setOnClickPendingIntent(R.id.button, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView);
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.calendar_table_widget);
        updateRemoteViewFormats(views, context);
        Intent serviceIntent = new Intent(context, RemoteViewService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.listView, serviceIntent);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


    private static void updateRemoteViewFormats(RemoteViews views, Context context) {
        int dayOfWeek = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7;
        if (dayOfWeek <= 4)
            views.setTextColor(headerIDs[dayOfWeek], Color.WHITE);
        File file = new File(context.getFilesDir(), CryptoUtil.FileNames.PLAIN_CALENDAR_TABLE_DATA_FILE_NAME);
        if (!file.exists())
            views.setTextViewText(R.id.textView, context.getString(R.string.calendar_table_widget_last_update, context.getString(R.string.calendar_table_widget_last_update_never)));
        else {
            Date fileDate = new Date(file.lastModified());
            String date = android.text.format.DateFormat.getDateFormat(context).format(fileDate);
            String time = android.text.format.DateFormat.getTimeFormat(context).format(fileDate);
            views.setTextViewText(R.id.textView, context.getString(R.string.calendar_table_widget_last_update, date + " " + time));
        }
    }
}
