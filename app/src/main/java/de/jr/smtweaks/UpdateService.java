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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.jr.smtweaks.schulmanagerAPI.CalendarTable;
import de.jr.smtweaks.schulmanagerAPI.Holiday;
import de.jr.smtweaks.schulmanagerAPI.Login;
import de.jr.smtweaks.util.CryptoUtil;
import de.jr.smtweaks.util.GithubUpdateChecker;
import de.jr.smtweaks.util.GsonRepository;
import de.jr.smtweaks.widgets.calendar.HolidayItem;
import de.jr.smtweaks.widgets.calendar.TableItem;
import de.jr.smtweaks.widgets.calendar.WidgetProvider;

public class UpdateService extends Service {

    public final String test = "[{\"leftTop\":\"E\",\"rightTopAlternate\":null,\"rightTop\":\"JoK\",\"bottom\":\"1.00\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":1,\"date\":{\"year\":2026,\"month\":5,\"day\":18}},{\"leftTop\":\"E\",\"rightTopAlternate\":\"GaY\",\"rightTop\":\"JoK\",\"bottom\":\"1.00\",\"bottomAlternate\":\"0.05\",\"isCancelled\":false,\"row\":2,\"date\":{\"year\":2026,\"month\":5,\"day\":18}},{\"leftTop\":\"Mu\",\"rightTopAlternate\":null,\"rightTop\":\"ScM\",\"bottom\":\"1.61\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":3,\"date\":{\"year\":2026,\"month\":5,\"day\":18}},{\"leftTop\":\"Mu\",\"rightTopAlternate\":null,\"rightTop\":\"ScM\",\"bottom\":\"1.61\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":4,\"date\":{\"year\":2026,\"month\":5,\"day\":18}},{\"leftTop\":\"De\",\"rightTopAlternate\":null,\"rightTop\":\"HoD\",\"bottom\":\"-1.01\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":5,\"date\":{\"year\":2026,\"month\":5,\"day\":18}},{\"leftTop\":\"De\",\"rightTopAlternate\":null,\"rightTop\":\"HoD\",\"bottom\":\"-1.01\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":6,\"date\":{\"year\":2026,\"month\":5,\"day\":18}},{\"leftTop\":\"Inf\",\"rightTopAlternate\":null,\"rightTop\":\"ReJ\",\"bottom\":\"2.05\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":1,\"date\":{\"year\":2026,\"month\":5,\"day\":19}},{\"leftTop\":\"Inf\",\"rightTopAlternate\":null,\"rightTop\":\"ReJ\",\"bottom\":\"2.05\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":2,\"date\":{\"year\":2026,\"month\":5,\"day\":19}},{\"leftTop\":\"Inf\",\"rightTopAlternate\":null,\"rightTop\":\"ReJ\",\"bottom\":\"2.05\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":3,\"date\":{\"year\":2026,\"month\":5,\"day\":19}},{\"leftTop\":\"Ch\",\"rightTopAlternate\":null,\"rightTop\":\"HeS\",\"bottom\":\"1.25\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":4,\"date\":{\"year\":2026,\"month\":5,\"day\":19}},{\"leftTop\":\"Ch\",\"rightTopAlternate\":null,\"rightTop\":\"HeS\",\"bottom\":\"1.25\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":5,\"date\":{\"year\":2026,\"month\":5,\"day\":19}},{\"leftTop\":\"Sp\",\"rightTopAlternate\":null,\"rightTop\":\"SoL\",\"bottom\":\"TH1\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":6,\"date\":{\"year\":2026,\"month\":5,\"day\":19}},{\"leftTop\":\"Sp\",\"rightTopAlternate\":null,\"rightTop\":\"SoL\",\"bottom\":\"TH1\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":7,\"date\":{\"year\":2026,\"month\":5,\"day\":19}},{\"leftTop\":\"Wr\",\"rightTopAlternate\":null,\"rightTop\":\"DeM\",\"bottom\":\"1.47\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":1,\"date\":{\"year\":2026,\"month\":5,\"day\":20}},{\"leftTop\":\"Wr\",\"rightTopAlternate\":null,\"rightTop\":\"DeM\",\"bottom\":\"1.47\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":2,\"date\":{\"year\":2026,\"month\":5,\"day\":20}},{\"leftTop\":\"Rk\",\"rightTopAlternate\":null,\"rightTop\":\"KaJ\",\"bottom\":\"2.71\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":3,\"date\":{\"year\":2026,\"month\":5,\"day\":20}},{\"leftTop\":\"Rk\",\"rightTopAlternate\":null,\"rightTop\":\"KaJ\",\"bottom\":\"2.71\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":4,\"date\":{\"year\":2026,\"month\":5,\"day\":20}},{\"leftTop\":\"Bio\",\"rightTopAlternate\":\"HeS\",\"rightTop\":\"null\",\"bottom\":\"1.11\",\"bottomAlternate\":\"1.11\",\"isCancelled\":false,\"row\":5,\"date\":{\"year\":2026,\"month\":5,\"day\":20}},{\"leftTop\":\"Bio\",\"rightTopAlternate\":\"Hes\",\"rightTop\":\"null\",\"bottom\":\"1.11\",\"bottomAlternate\":\"2.22\",\"isCancelled\":false,\"row\":6,\"date\":{\"year\":2026,\"month\":5,\"day\":20}},{\"leftTop\":\"FaQ\",\"rightTopAlternate\":null,\"rightTop\":\"EdD\",\"bottom\":\"Atrium\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":8,\"date\":{\"year\":2026,\"month\":5,\"day\":20}},{\"leftTop\":\"Fr\",\"rightTopAlternate\":null,\"rightTop\":\"HoD\",\"bottom\":\"0.67\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":2,\"date\":{\"year\":2026,\"month\":5,\"day\":21}},{\"leftTop\":\"Fr\",\"rightTopAlternate\":null,\"rightTop\":\"HoD\",\"bottom\":\"0.67\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":3,\"date\":{\"year\":2026,\"month\":5,\"day\":21}},{\"leftTop\":\"Geo\",\"rightTopAlternate\":null,\"rightTop\":\"ZwM\",\"bottom\":\"R.34\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":4,\"date\":{\"year\":2026,\"month\":5,\"day\":21}},{\"leftTop\":\"Geo\",\"rightTopAlternate\":null,\"rightTop\":\"ZwM\",\"bottom\":\"R.34\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":5,\"date\":{\"year\":2026,\"month\":5,\"day\":21}},{\"leftTop\":\"Ku\",\"rightTopAlternate\":null,\"rightTop\":\"ReM\",\"bottom\":\"2.14\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":1,\"date\":{\"year\":2026,\"month\":5,\"day\":22}},{\"leftTop\":\"Ku\",\"rightTopAlternate\":null,\"rightTop\":\"ReM\",\"bottom\":\"2.14\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":2,\"date\":{\"year\":2026,\"month\":5,\"day\":22}},{\"leftTop\":\"G\",\"rightTopAlternate\":null,\"rightTop\":\"DeM\",\"bottom\":\"1.12\",\"bottomAlternate\":null,\"isCancelled\":true,\"row\":3,\"date\":{\"year\":2026,\"month\":5,\"day\":22}},{\"leftTop\":\"G\",\"rightTopAlternate\":null,\"rightTop\":\"DeM\",\"bottom\":\"1.12\",\"bottomAlternate\":null,\"isCancelled\":true,\"row\":4,\"date\":{\"year\":2026,\"month\":5,\"day\":22}},{\"leftTop\":\"Ma\",\"rightTopAlternate\":null,\"rightTop\":\"ReJ\",\"bottom\":\"2.09\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":7,\"date\":{\"year\":2026,\"month\":5,\"day\":22}},{\"leftTop\":\"Ma\",\"rightTopAlternate\":null,\"rightTop\":\"ReJ\",\"bottom\":\"2.09\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":8,\"date\":{\"year\":2026,\"month\":5,\"day\":22}}]";
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

        GithubUpdateChecker.checkForUpdate(this);

        Context context = this;
        Intent buttonIntent = new Intent("de.jr.smtweaks.ACTION_CALENDAR_WIDGET_BUTTON_LOADING");
        buttonIntent.setComponent(new ComponentName(getApplicationContext(), WidgetProvider.class));
        buttonIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        sendBroadcast(buttonIntent);

        if (widgetID == -1)
            stop();

        if (context.getSharedPreferences("main_preference", Context.MODE_PRIVATE).getBoolean("KillSwitchActive", false)) {
            Toast.makeText(context, "Der Dienst wurde bis auf weiteres eingestellt", Toast.LENGTH_LONG).show();
            stop();
            return Service.START_NOT_STICKY;
        }


        new Login().login(context, token -> {

            UserData userData = CryptoUtil.getUserData(context);

            if (userData == null) {
                stop();
                return;
            }

            new Holiday().fetchData(userData, token, context, result -> {

                if (result == null) return;

                HolidayItem[] holidayItems = new GsonRepository().schulmanagerFormatToHolidayItem(result);

                try {
                    CryptoUtil.writeFile(
                            new File(getFilesDir(),
                                    CryptoUtil.FileNames.HOLIDAY_DATES_FILE_NAME),
                            new GsonRepository().holidayItemToJson(holidayItems).getBytes(StandardCharsets.UTF_8)
                    );
                } catch (IOException e) {
                    Log.e("File", "Could not write file", e);
                }
            });
            new CalendarTable().fetchData(userData, token, context, result -> {

                if (result == null) {
                    stop();
                    return;
                }

//TableItem[] tableItemList = new GsonRepository().schulmanagerFormatToTableItemList(result);
                TableItem[] tableItemList = new GsonRepository().jsonToTableItemList(test);

                try {
                    TableItem[] merged = getFullWeekTableItems(tableItemList);
                    CryptoUtil.writeFile(
                            new File(context.getFilesDir(),
                                    CryptoUtil.FileNames.PLAIN_CALENDAR_TABLE_DATA_FILE_NAME),
                            new GsonRepository().tableItemListToJson(merged).getBytes(StandardCharsets.UTF_8)
                    );
                    CryptoUtil.writeFile(
                            new File(context.getFilesDir(),
                                    CryptoUtil.FileNames.PLAIN_CALENDAR_TABLE_DATA_FILE_NAME_SMALL),
                            new GsonRepository().tableItemListToJson(tableItemList).getBytes(StandardCharsets.UTF_8)
                    );
                } catch (IOException e) {
                    Log.e("File", "Could not write files", e);
                }
                stop();
            });
        });
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
        new Handler(Looper.getMainLooper()).postDelayed(() -> sendBroadcast(intent), 1000);
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

        List<TableItem> merge = new ArrayList<>();

        merge.addAll(Arrays.asList(oldItems));
        merge.addAll(Arrays.asList(newItems));
        merge.removeIf(item -> ChronoUnit.DAYS.between(item.getDate(), LocalDate.now()) > 7);

        return merge.toArray(new TableItem[0]);

    }

    private Notification createNotification() {
        String channelId = "smt_update_service";

        NotificationChannel channel =
                new NotificationChannel(
                        channelId,
                        "Update Service",
                        NotificationManager.IMPORTANCE_MIN
                );
        channel.setSound(null, null);
        channel.enableVibration(false);
        channel.setShowBadge(false);

        getSystemService(NotificationManager.class)
                .createNotificationChannel(channel);

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle(getString(R.string.notification_updating))
                .setSmallIcon(R.drawable.smt)
                .setOngoing(true)
                .build();
    }

}
