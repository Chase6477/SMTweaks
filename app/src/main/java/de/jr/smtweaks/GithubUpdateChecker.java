package de.jr.smtweaks;

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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GithubUpdateChecker {

    SharedPreferences prefs;

    public GithubUpdateChecker(Activity activity, onFinishedUpdateRequest listener) {
        prefs = activity.getSharedPreferences("main_preference", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("show_update_alert", true)) {
            listener.onFinishedUpdateRequest(activity);
            return;
        }
        Request request = new Request.Builder()
                .url("https://api.github.com/repos/Chase6477/SMTweaks/releases/latest")
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            String currentVersion = "";

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                listener.onFinishedUpdateRequest(activity);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();

                    String latestVersion = json.replaceAll(".*\"tag_name\"\\s*:\\s*\"([^\"]+)\".*", "$1");
                    try {
                        PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                        currentVersion = pInfo.versionName;
                    } catch (PackageManager.NameNotFoundException e) {
                        listener.onFinishedUpdateRequest(activity);
                    }

                    if (!latestVersion.trim().equals(currentVersion.trim())) {
                        activity.runOnUiThread(() -> {
                            new AlertDialog.Builder(activity)
                                    .setTitle(R.string.update_alert_update_available)
                                    .setMessage(activity.getString(R.string.update_alert_text) + "\n" +
                                            activity.getString(R.string.update_alert_current_version, currentVersion) + "\n" +
                                            activity.getString(R.string.update_alert_latest_version, latestVersion))

                                    .setPositiveButton(activity.getString(R.string.update_alert_option_show_version), (dialog, which) -> {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Chase6477/SMTweaks/releases/latest"));
                                        activity.startActivity(intent);
                                        listener.onFinishedUpdateRequest(activity);
                                    })
                                    .setNeutralButton(activity.getString(R.string.update_alert_option_dont_show_again), (dialog, which) -> {
                                        prefs.edit().putBoolean("show_update_alert", false).apply();
                                        listener.onFinishedUpdateRequest(activity);
                                    })
                                    .setNegativeButton(activity.getString(R.string.update_alert_option_later), (dialog, which) ->
                                            listener.onFinishedUpdateRequest(activity))
                                    .show();
                        });
                    } else
                        listener.onFinishedUpdateRequest(activity);
                } else
                    listener.onFinishedUpdateRequest(activity);
            }
        });
    }

    public interface onFinishedUpdateRequest {
        void onFinishedUpdateRequest(Activity activity);
    }
}
