package de.jr.smtweaks.schulmanagerAPI;

import androidx.annotation.NonNull;

import java.io.IOException;

import de.jr.smtweaks.util.GsonRepository;
import de.jr.smtweaks.widgets.calendar.HolidayItem;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Holiday {

    public static void fetchData(String token, Holiday.OnFinishedFetching listener) {

        RequestBody body = RequestBody.create("{\"bundleVersion\":\"" + Login.BUNDLE_VERSION + "\",\"requests\":[{\"moduleName\":null,\"endpointName\":\"get-all-holidays\"}]}", MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("https://login.schulmanager-online.de/api/calls")
                .post(body)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                listener.onFinishedFetching(null);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseString = response.body().string();
                if (!responseString.contains("\"status\":200")) {
                    listener.onFinishedFetching(null);
                    return;
                }
                listener.onFinishedFetching(new GsonRepository().schulmanagerFormatToHolidayItem(responseString));
            }
        });
    }

    public interface OnFinishedFetching {
        void onFinishedFetching(HolidayItem[] holidayItems);
    }
}
