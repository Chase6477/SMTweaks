package de.jr.smtweaks.schulmanagerAPI;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Calendar;
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

public class CalendarTable implements API{

    private static final long EXPIRATION_TIME = 120 * 1000;

    @Override
    public void fetchData(UserData userData, String token, Context context, OnFinishedUpdateRequest listener) {

        long calendarCreated = context.getSharedPreferences("API", Context.MODE_PRIVATE)
                .getLong("calendarCreated", Long.MIN_VALUE);

        if (calendarCreated + EXPIRATION_TIME > System.currentTimeMillis()) {
            listener.onFinishedUpdateRequest(null);
            return;
        }

        if (MainActivity.DEBUG) {
            Log.i("APICALL", "Pseudo-Calendar");
            listener.onFinishedUpdateRequest(null);
            return;
        }
        Log.i("APICALL", "Calendar");

        Calendar sunday = getMonday();
        sunday.add(Calendar.DAY_OF_WEEK, 7);
        RequestBody body = RequestBody.create("{\"bundleVersion\":\"" + Login.BUNDLE_VERSION + "\",\"requests\":[{\"moduleName\":\"schedules\",\"endpointName\":\"get-actual-lessons\",\"parameters\":{\"student\":" + userData.getUserString() + ",\"start\":\"" + formatCalendar(getMonday()) + "\",\"end\":\"" + formatCalendar(sunday) + "\"}}]}", MediaType.get("application/json; charset=utf-8"));


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
                        .putLong("calendarCreated", System.currentTimeMillis())
                        .apply();

                listener.onFinishedUpdateRequest(responseString);
            }
        });
    }

    private Calendar getMonday() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        if (day != Calendar.SATURDAY && day != Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, Calendar.MONDAY - day);
            return calendar;
        }
        calendar.add(Calendar.DAY_OF_MONTH, (Calendar.MONDAY - day + 7) % 7);
        return calendar;
    }

    private String formatCalendar(Calendar calendar) {
        return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
    }
}
