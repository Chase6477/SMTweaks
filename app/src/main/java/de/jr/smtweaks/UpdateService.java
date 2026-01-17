package de.jr.smtweaks;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.jr.smtweaks.schulmanagerAPI.CalendarTable;
import de.jr.smtweaks.util.CryptoUtil;
import de.jr.smtweaks.util.GithubUpdateChecker;
import de.jr.smtweaks.util.GsonRepository;
import de.jr.smtweaks.util.HolidayRequest;
import de.jr.smtweaks.widgets.calendar.HolidayItem;
import de.jr.smtweaks.widgets.calendar.TableItem;
import de.jr.smtweaks.widgets.calendar.WidgetProvider;

public class UpdateService extends Service {

    private Intent intent;
    private int widgetID;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (this.intent != null)
            return START_NOT_STICKY;
        this.intent = intent;
        if (Build.VERSION.SDK_INT > 28)
            startForeground(1, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        else
            startForeground(1, createNotification());

        widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        HolidayRequest.getHolidays(this, new HolidayRequest.OnFinishedHolidayRequest() {
            @Override
            public void onFinishedHolidayRequest(HolidayItem[] items) {
                if (items == null) {
                    return;
                }
                try {
                    CryptoUtil.writeFile(
                            new File(getFilesDir(), CryptoUtil.FileNames.PLAIN_HOLIDAY_DATES_FILE_NAME),
                            new GsonRepository().holidayItemListToJson(items).getBytes(StandardCharsets.UTF_8));
                } catch (IOException ignore) {
                }
            }
        });

        GithubUpdateChecker.checkForUpdate(this);

        Context context = this;
        Intent buttonIntent = new Intent("de.jr.smtweaks.ACTION_CALENDAR_WIDGET_BUTTON_LOADING");
        buttonIntent.setComponent(new ComponentName(getApplicationContext(), WidgetProvider.class));
        buttonIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        sendBroadcast(buttonIntent);

        if (widgetID == -1)
            stop();

        new CalendarTable().getCalendarTable(this, new CalendarTable.OnFinishedUpdateRequest() {

            @Override
            public void onFinishedUpdateRequest(TableItem[] tableItemList) {
                if (tableItemList == null) {
                    stop();
                    return;
                }
                try {
                    TableItem[] merged = getFullWeekTableItems(tableItemList);
                    CryptoUtil.writeFile(new File(context.getFilesDir(), CryptoUtil.FileNames.PLAIN_CALENDAR_TABLE_DATA_FILE_NAME), new GsonRepository().tableItemListToJson(merged).getBytes(StandardCharsets.UTF_8));
                    CryptoUtil.writeFile(new File(context.getFilesDir(), CryptoUtil.FileNames.PLAIN_CALENDAR_TABLE_DATA_FILE_NAME_SMALL), new GsonRepository().tableItemListToJson(tableItemList).getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    Log.e("File", "Could not write files", e);
                }
                stop();
            }
        }, 0);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void stop() {
        Intent intent = new Intent("de.jr.smtweaks.ACTION_CALENDAR_WIDGET_BUTTON_READY");
        intent.setComponent(new ComponentName(getApplicationContext(), WidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        sendBroadcast(intent);
        stopForeground(true);
        updateWidget();
        stopSelf();
    }

    private void updateWidget() {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.setComponent(new ComponentName(this, WidgetProvider.class));
        sendBroadcast(intent);
    }

    private TableItem[] getFullWeekTableItems(TableItem[] newItems) throws IOException {
        TableItem[] oldItems;
        if (new File(this.getFilesDir(), CryptoUtil.FileNames.PLAIN_CALENDAR_TABLE_DATA_FILE_NAME).exists()) {
            oldItems = new GsonRepository().jsonToTableItemList(
                    new String(CryptoUtil.readFile(new File(this.getFilesDir(), CryptoUtil.FileNames.PLAIN_CALENDAR_TABLE_DATA_FILE_NAME)))
            );
        } else
            oldItems = new TableItem[0];

        boolean[] cols = new boolean[5];
        for (TableItem t : newItems) {
            cols[t.getCol() - 1] = true;
        }

        List<TableItem> merged = new ArrayList<>();
        for (TableItem t : oldItems) {
            if (cols[t.getCol() - 1])
                continue;

            t.setBottomAlternate(null);
            t.setRightTopAlternate(null);
            t.setIsCancelled(false);
            merged.add(t);
        }

        Collections.addAll(merged, newItems);

        return merged.toArray(new TableItem[0]);

    }

    private Notification createNotification() {
        String channelId = "smt_update_service";

        NotificationChannel channel =
                new NotificationChannel(
                        channelId,
                        "DOM Service",
                        NotificationManager.IMPORTANCE_HIGH
                );
        getSystemService(NotificationManager.class)
                .createNotificationChannel(channel);

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Updating data")
                .setSmallIcon(R.drawable.smt)
                .setOngoing(true)
                .build();
    }
}
