package de.jr.smtweaks.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.time.LocalDate;

import de.jr.smtweaks.widgets.calendar.HolidayItem;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HolidayRequest {

    public static void getHolidays(Context context, OnFinishedHolidayRequest listener) {
        SharedPreferences mainPref = context.getSharedPreferences("main_preference", Context.MODE_PRIVATE);
        if (!mainPref.getBoolean("show_holidays", true))
            listener.onFinishedHolidayRequest(null);

        Request request = new Request.Builder()
                .url("https://www.mehr-schulferien.de/api/v2.1/federal-states/" + mainPref.getString("state", "bayern") + "/periods?start_date=" + (LocalDate.now().getYear() - 1) + "-11-01&end_date=" + (LocalDate.now().getYear() + 1) + "-02-01")
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                listener.onFinishedHolidayRequest(null);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if (!response.isSuccessful())
                    return;
                HolidayItem[] items = new GsonRepository().getCroppedHolidayList(response.body().string());
                listener.onFinishedHolidayRequest(items);
            }
        });
    }

    public interface OnFinishedHolidayRequest {
        void onFinishedHolidayRequest(HolidayItem[] items);
    }
}
