package de.jr.smtweaks;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import de.jr.smtweaks.util.CryptoUtil;
import de.jr.smtweaks.util.GithubUpdateChecker;
import de.jr.smtweaks.util.GsonRepository;
import de.jr.smtweaks.util.HolidayRequest;
import de.jr.smtweaks.widgets.calendar.HolidayItem;
import de.jr.smtweaks.widgets.calendar.TableItem;
import de.jr.smtweaks.widgets.calendar.WidgetProvider;

public class UpdateService extends Service {
    private static final int TIMEOUT_DURATION = 30000;

    private Handler mainHandler;
    private WebView webView;
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
                    //"[{\"endDate\":\"2026-07-01\",\"startDate\":\"2025-01-01\"}]".getBytes(StandardCharsets.UTF_8));
                } catch (IOException ignore) {
                }
            }
        });
        GithubUpdateChecker.checkForUpdate(this);
        mainHandler.post(this::update);

        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void update() {
        webView = new WebView(this);
        widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        SharedPreferences mainPrefs = getSharedPreferences("main_preference", Context.MODE_PRIVATE);

        if (widgetID == -1)
            stop();
        Context context = this;
        Intent intent = new Intent("de.jr.smtweaks.ACTION_CALENDAR_WIDGET_BUTTON_LOADING");
        intent.setComponent(new ComponentName(getApplicationContext(), WidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        sendBroadcast(intent);

        webView.setWebViewClient(new WebViewClient() {
            int timeout = 0;

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webView.evaluateJavascript(
                        JavaScripts.outerHtml,
                        html -> {
                            if (Identifier.identify("passwordPage", html, url)) {
                                String password;

                                if (mainPrefs.getString("username", null) == null) {
                                    stop();
                                    return;
                                }
                                try {
                                    byte[] passwordBytes = CryptoUtil.decrypt(CryptoUtil.getKeyStoreSecretKey("passwordKey"), context, CryptoUtil.FileNames.ENC_USER_DATA_FILE_NAME);
                                    if (passwordBytes == null) {
                                        stop();
                                        return;
                                    }
                                    password = new String(passwordBytes);
                                } catch (Exception e) {
                                    stop();
                                    return;
                                }

                                webView.evaluateJavascript(
                                        JavaScripts.login + "(\"" + mainPrefs.getString("username", null) + "\", \"" + password + "\")",
                                        login -> {
                                            Handler handler = new Handler(Looper.getMainLooper());

                                            handler.postDelayed(() -> {
                                                if (webView == null)
                                                    return;

                                                webView.evaluateJavascript(JavaScripts.outerHtml,
                                                        loginCheck -> {

                                                            if (Identifier.identify("wrongLogin", loginCheck, "")) {
                                                                Toast.makeText(context, getString(R.string.wron_login_toast), Toast.LENGTH_LONG).show();
                                                                stop();
                                                            }
                                                        });
                                            }
                                                    , 5000);
                                        }
                                );
                            } else if (Identifier.identify("calendarPage", html, url)) {
                                webView.evaluateJavascript(
                                        JavaScripts.tableGetter,
                                        calendarOutput -> {
                                            String tableData = calendarOutput.substring(1, calendarOutput.length() - 1)
                                                    .replace("\\\"", "\"")
                                                    .replace("\\\\", "\\");
                                            try {
                                                TableItem[] merged = getFullWeekTableItems(new GsonRepository().jsonToTableItemList(tableData));
                                                CryptoUtil.writeFile(new File(context.getFilesDir(), CryptoUtil.FileNames.PLAIN_CALENDAR_TABLE_DATA_FILE_NAME), new GsonRepository().tableItemListToJson(merged).getBytes(StandardCharsets.UTF_8));
                                                CryptoUtil.writeFile(new File(context.getFilesDir(), CryptoUtil.FileNames.PLAIN_CALENDAR_TABLE_DATA_FILE_NAME_SMALL), tableData.getBytes(StandardCharsets.UTF_8));
                                            } catch (IOException e) {
                                                Log.e("File", "File was not found", e);
                                            }
                                            updateWidget();
                                            stop();
                                        }
                                );
                            } else {
                                Handler handler = new Handler(Looper.getMainLooper());
                                if (timeout >= TIMEOUT_DURATION && timeout - 100 < TIMEOUT_DURATION) {
                                    Toast.makeText(context, "Timeout", Toast.LENGTH_LONG).show();
                                    stop();
                                } else {
                                    timeout += 100;
                                    handler.postDelayed(() -> onPageFinished(webView, webView.getUrl()), 100);
                                }
                            }
                        });
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });

        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://login.schulmanager-online.de/#/modules/schedules/view//" + getMonday());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

    private void stop() {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.stopLoading();
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
        mainHandler.removeCallbacks(this::update);
        Intent intent = new Intent("de.jr.smtweaks.ACTION_CALENDAR_WIDGET_BUTTON_READY");
        intent.setComponent(new ComponentName(getApplicationContext(), WidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        sendBroadcast(intent);
        stopForeground(true);
        stopSelf();
    }


    private void updateWidget() {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.setComponent(new ComponentName(this, WidgetProvider.class));
        sendBroadcast(intent);
    }


    private String getMonday() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        if (day != Calendar.SATURDAY && day != Calendar.SUNDAY)
            return "";
        calendar.add(Calendar.DAY_OF_MONTH, (Calendar.MONDAY - day + 7) % 7);
        return "?start=" + calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
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

    /**
     * This class is for unique identifiers for each Page,
     * because I couldn't find a simpler method and url doesn't always work
     */
    private static class Identifier {
        public static boolean identify(String name, String html, String url) {
            switch (name) {
                case "passwordPage":
                    return html.contains("current-password"); //autocomplete field
                case "calendarPage":
                    return html.contains("calendar-table"); //calendar map name
                case "homePage":
                    return url.contains("https://login.schulmanager-online.de/#/dashboard/");
                case "wrongLogin":
                    return html.contains("alert alert-danger"); //false password field
            }
            return false;
        }
    }

    /**
     * Ugly, but simpler and safer than using a InputStream reading .js files...
     * (The files are still in the package lol)
     */
    private static class JavaScripts {
        public static final String outerHtml = "(function() {return document.documentElement.outerHTML; })();";
        public static final String login = "(function(username, password) {\n" +
                "\n" +
                "document.querySelector('input[autocomplete=\"username\"]').value = username;\n" +
                "document.querySelector('input[autocomplete=\"username\"]').dispatchEvent(new Event('input', { bubbles: true }));\n" +
                "document.querySelector('input[autocomplete=\"current-password\"]').value = password;\n" +
                "document.querySelector('input[autocomplete=\"current-password\"]').dispatchEvent(new Event('input', { bubbles: true }));\n" +
                "document.querySelector('button.btn.btn-primary.float-right').click();\n" +
                "})";
        public static final String tableGetter = "(function() {\n" +
                "\n" +
                "class Item {\n" +
                "  constructor(leftTop, rightTop, rightTopAlternate, bottom, bottomAlternate, isCancelled, row, col) {\n" +
                "    this.leftTop = leftTop;\n" +
                "    this.rightTopAlternate = rightTopAlternate;\n" +
                "    this.rightTop = rightTop;\n" +
                "    this.bottom = bottom;\n" +
                "    this.bottomAlternate = bottomAlternate;\n" +
                "    this.isCancelled = isCancelled;\n" +
                "    this.row = row;\n" +
                "    this.col = col;\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "\n" +
                "function main() {\n" +
                "let array = [];\n" +
                "  const table = document.querySelector(\"table.calendar-table\");\n" +
                "  for (let row = 1; row < table.rows.length; row++) {\n" +
                "    for (let col = 1; col < table.rows[row].cells.length; col++) {\n" +
                "      const selection = table.rows[row].cells[col];\n" +
                "      let isCancelled = false;\n" +
                "      if (selection.innerHTML.includes(\"lesson-cell cancelled\")) {\n" +
                "        isCancelled = true;\n" +
                "      }\n" +
                "      let parts = selection.innerText.trim().split(\"\\n\");\n" +
                "      if (parts.length < 3) {\n" +
                "        continue;\n" +
                "      }\n" +
                "      let leftTop = parts[0];\n" +
                "      let rightTop = parts[1];\n" +
                "      let rightTopAlternate = null;\n" +
                "      if (parts[1].includes(\") \")) {\n" +
                "        rightTopAlternate = parts[1].split(\") \")[1].trim();\n" +
                "        rightTop = parts[1].split(\") \")[0].trim().replace(/[()]/g, \"\");\n" +
                "      }\n" +
                "      let bottom = parts[2];\n" +
                "      let bottomAlternate = null;\n" +
                "      if (parts[2].includes(\") \")) {\n" +
                "        bottomAlternate = parts[2].split(\") \")[1].trim();\n" +
                "        bottom = parts[2].split(\") \")[0].trim().replace(/[()]/g, \"\");\n" +
                "      }\n" +
                "      array.push(new Item(leftTop, rightTop, rightTopAlternate, bottom, bottomAlternate, isCancelled, row, col))\n" +
                "    }\n" +
                "  }\n" +
                "  return array;\n" +
                "}\n" +
                "\n" +
                "return JSON.stringify(main())})()";
    }
}
