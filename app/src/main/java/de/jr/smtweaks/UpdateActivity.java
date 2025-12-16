package de.jr.smtweaks;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import de.jr.smtweaks.util.CryptoUtil;
import de.jr.smtweaks.util.GsonRepository;
import de.jr.smtweaks.widgets.calendar.TableItem;
import de.jr.smtweaks.widgets.calendar.WidgetProvider;


public class UpdateActivity extends AppCompatActivity {
    private static final int TIMEOUT_DURATION = 30000;
    private WebView webView;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        update();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GithubUpdateChecker(this, activity -> activity.runOnUiThread(this::update));
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void update() {
        setContentView(R.layout.activity_webview);
        webView = new WebView(this);
        int widgetID = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        SharedPreferences widgetPrefs = getSharedPreferences(getString(R.string.calendar_widget_preference, widgetID), Context.MODE_PRIVATE);
        SharedPreferences mainPrefs = getSharedPreferences("main_preference", Context.MODE_PRIVATE);

        CheckBox checkForUpdatesChecker = findViewById(R.id.checkBox);
        if (mainPrefs.getBoolean("show_update_alert", true)) {
            checkForUpdatesChecker.setVisibility(CheckBox.GONE);
        } else {
            checkForUpdatesChecker.setOnCheckedChangeListener(
                    (buttonView, isChecked) ->
                            mainPrefs.edit().putBoolean("show_update_alert", true).apply()
            );
        }

        if (widgetID == -1)
            finishAndRemoveTask();
        Context context = this;

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
                                    finishAndRemoveTask();
                                    return;
                                }
                                try {
                                    byte[] passwordBytes = CryptoUtil.decrypt(CryptoUtil.getKeyStoreSecretKey("passwordKey"), context, CryptoUtil.FileNames.ENC_USER_DATA_FILE_NAME);
                                    if (passwordBytes == null) {
                                        finishAndRemoveTask();
                                        return;
                                    }
                                    password = new String(passwordBytes);
                                } catch (Exception e) {
                                    finishAndRemoveTask();
                                    return;
                                }

                                webView.evaluateJavascript(
                                        JavaScripts.login + "(\"" + mainPrefs.getString("username", null) + "\", \"" + password + "\")",
                                        login -> {
                                            Handler handler = new Handler(Looper.getMainLooper());
                                            handler.postDelayed(() -> webView.evaluateJavascript(JavaScripts.outerHtml,
                                                    loginCheck -> {

                                                        if (Identifier.identify("wrongLogin", loginCheck, "")) {
                                                            Toast.makeText(context, getString(R.string.wron_login_toast), Toast.LENGTH_LONG).show();
                                                            finishAndRemoveTask();
                                                        }
                                                    }), 5000);
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
                                            finishAndRemoveTask();
                                        }
                                );
                            } else {
                                Handler handler = new Handler(Looper.getMainLooper());
                                if (timeout >= TIMEOUT_DURATION) {
                                    Toast.makeText(context, "Timeout", Toast.LENGTH_LONG).show();
                                    finishAndRemoveTask();
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

    private void updateWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName widget = new ComponentName(this, WidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widget);

        Intent intent = new Intent(this, WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

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

    @Override
    protected void onStop() {
        super.onStop();
        finishAndRemoveTask();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.destroy();
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
     * Ugly, but simpler an saver than using a InputStream for reading .js files...
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
