package de.jr.smtweaks.widgets.calendar;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

import de.jr.smtweaks.R;
import de.jr.smtweaks.util.CryptoUtil;


public class CalendarUpdateActivity extends AppCompatActivity {
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);

        webView = new WebView(this);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webView.evaluateJavascript(
                        JavaScripts.outerHtml,
                        html -> {
                            if (Identifier.identify("passwordPage", html, url)) {
                                String username = "";
                                String password = "";
                                webView.evaluateJavascript(
                                        JavaScripts.login + "(\"" + username + "\", \"" + password + "\")",
                                        asd -> {
                                        }
                                );
                            } else if (Identifier.identify("calendarPage", html, url)) {
                                webView.evaluateJavascript(
                                        JavaScripts.tableGetter,
                                        asd -> {
                                            try {
                                                CryptoUtil.writeFile(
                                                        new File(getFilesDir(), CryptoUtil.FileNames.PLAIN_CALENDAR_TABLE_DATA_FILE_NAME),
                                                        asd.substring(1, asd.length() - 1)
                                                                .replace("\\\"", "\"")
                                                                .replace("\\\\", "\\").getBytes(StandardCharsets.UTF_8));
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                            updateWidget();
                                            finish();
                                        }
                                );
                            } else {
                                Handler handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(() -> onPageFinished(webView, webView.getUrl()), 100
                                );
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
        ComponentName widget = new ComponentName(this, CalendarTableWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widget);

        Intent intent = new Intent(this, CalendarTableWidget.class);
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
                "        rightTop = parts[1].split(\") \")[0] + \")\".trim();\n" +
                "      }\n" +
                "      let bottom = parts[2];\n" +
                "      let bottomAlternate = null;\n" +
                "      if (parts[2].includes(\") \")) {\n" +
                "        bottomAlternate = parts[2].split(\") \")[1].trim();\n" +
                "        bottom = parts[2].split(\") \")[0] + \")\".trim();\n" +
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
