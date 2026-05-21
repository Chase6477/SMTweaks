package de.jr.smtweaks.schulmanagerAPI;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.jr.smtweaks.MainActivity;
import de.jr.smtweaks.UserData;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Holiday implements API {

    private static final long EXPIRATION_TIME = 604800000; // 7 Days

    @Override
    public void fetchData(UserData userData, String token, Context context, OnFinishedUpdateRequest listener) {

        long holidayCreated = context.getSharedPreferences("API", Context.MODE_PRIVATE)
                .getLong("holidayCreated", Long.MIN_VALUE);

        if (holidayCreated + EXPIRATION_TIME > System.currentTimeMillis()) {
            listener.onFinishedUpdateRequest(null);
            return;
        }

        if (MainActivity.DEBUG) {
            Log.i("APICALL", "Pseudo-Holiday");
            listener.onFinishedUpdateRequest(null);
            return;
        }
        Log.i("APICALL", "Holiday");


        RequestBody body = RequestBody.create("{\"bundleVersion\":\"" + Login.BUNDLE_VERSION + "\",\"requests\":[{\"moduleName\":null,\"endpointName\":\"get-all-holidays\"}]}", MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("https://login.schulmanager-online.de/api/calls")
                .post(body)
                .header("User-Agent", Login.USER_AGENT)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        new OkHttpClient.Builder().callTimeout(Login.TIMEOUT_SECONDS, TimeUnit.SECONDS).build()
                .newCall(request).enqueue(new Callback() {

                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        listener.onFinishedUpdateRequest(null);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String responseString = response.body().string();
                        if (!responseString.contains("\"status\":200")) {
                            listener.onFinishedUpdateRequest(null);
                            return;
                        }

                        context.getSharedPreferences("API", Context.MODE_PRIVATE).edit()
                                .putLong("holidayCreated", System.currentTimeMillis())
                                .apply();

                        listener.onFinishedUpdateRequest(responseString);
                    }
                });
    }
}
