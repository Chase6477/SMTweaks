package de.jr.smtweaks.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.io.IOException;

import de.jr.smtweaks.MainActivity;
import de.jr.smtweaks.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GithubUpdateChecker {

    public static void makeAlert(Activity activity, String currentVersion, String latestVersion, OnFinishedUpdateRequest listener) {
        activity.runOnUiThread(() -> {
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.update_alert_update_available)
                    .setMessage(activity.getString(R.string.update_alert_text) + "\n" +
                            activity.getString(R.string.update_alert_current_version, currentVersion) + "\n" +
                            activity.getString(R.string.update_alert_latest_version, latestVersion))

                    .setPositiveButton(activity.getString(R.string.update_alert_option_show_version), (dialog, which) -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Chase6477/SMTweaks/releases/latest"));
                        activity.startActivity(intent);
                        listener.onFinishedUpdateRequest(true);
                    })
                    .setNeutralButton(activity.getString(R.string.update_alert_option_dont_show_again), (dialog, which) -> {
                        getMainPreference(activity).edit().putBoolean("show_update_alert", false).apply();
                        listener.onFinishedUpdateRequest(false);
                    })
                    .setNegativeButton(activity.getString(R.string.update_alert_option_later), (dialog, which) ->
                            listener.onFinishedUpdateRequest(true))
                    .show();
        });
    }

    public static SharedPreferences getMainPreference(Context context) {
        return context.getSharedPreferences("main_preference", Context.MODE_PRIVATE);
    }

    public static void checkForUpdate(Context context) {

        if (!getMainPreference(context).getBoolean("show_update_alert", true))
            return;

        Request request = new Request.Builder()
                .url("https://api.github.com/repos/Chase6477/SMTweaks/releases/latest")
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            String currentVersion = "";

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();

                    String latestVersion = json.replaceAll(".*\"tag_name\"\\s*:\\s*\"([^\"]+)\".*", "$1");
                    try {
                        PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                        currentVersion = pInfo.versionName;
                    } catch (PackageManager.NameNotFoundException ignored) {
                    }

                    if (!latestVersion.trim().equals(currentVersion.trim())) {
                        Intent intent = new Intent(context, MainActivity.class);
                        intent.setAction("de.jr.smtweaks.ACTION_UPDATE_ALERT");
                        intent.putExtra("currentVersion", currentVersion);
                        intent.putExtra("latestVersion", latestVersion);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);

                    }
                }
            }
        });
    }

    public interface OnFinishedUpdateRequest {
        void onFinishedUpdateRequest(boolean exitActivity);
    }
}
