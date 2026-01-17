package de.jr.smtweaks.schulmanagerAPI;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;

import javax.crypto.BadPaddingException;

import de.jr.smtweaks.util.GsonRepository;
import de.jr.smtweaks.widgets.calendar.TableItem;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CalendarTable {

    private static final int MAX_LOGIN_TRIES_COUNT = 3;

    public void getCalendarTable(Context context, OnFinishedUpdateRequest listener, int count) {
        System.out.println("Calendar Fetch");
        if (count >= MAX_LOGIN_TRIES_COUNT)
            listener.onFinishedUpdateRequest(null);
        try {
            fetchData(Login.getToken(context), Login.getStudent(context), new OnFinishedFetching() {
                @Override
                public void onFinishedFetching(TableItem[] tableItemList) {
                    if (tableItemList != null) {
                        listener.onFinishedUpdateRequest(tableItemList);
                        return;
                    }
                    Login.login(context, new Login.OnFinishedUpdateRequest() {
                        @Override
                        public void onFinishedUpdateRequest(boolean successful) {
                            if (successful)
                                getCalendarTable(context, listener, count + 1);
                        }
                    });
                }
            });
        } catch (IOException | BadPaddingException e) {
            Login.login(context, new Login.OnFinishedUpdateRequest() {
                @Override
                public void onFinishedUpdateRequest(boolean successful) {
                    if (successful)
                        getCalendarTable(context, listener, count + 1);
                    else
                        listener.onFinishedUpdateRequest(null);
                }
            });
        }
    }

    private void fetchData(String token, String student, OnFinishedFetching listener) {
        RequestBody body = RequestBody.create("{\"bundleVersion\":\"fb091ba7cd\",\"requests\":[{\"moduleName\":\"schedules\",\"endpointName\":\"get-actual-lessons\",\"parameters\":{\"student\":" + student + ",\"start\":\"2026-01-12\",\"end\":\"2026-02-18\"}}]}", MediaType.get("application/json; charset=utf-8"));

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
                listener.onFinishedFetching(new GsonRepository().schulmanagerFormatToTableItemList(responseString));
            }
        });
    }

    public interface OnFinishedUpdateRequest {
        void onFinishedUpdateRequest(TableItem[] tableItemList);
    }


    private interface OnFinishedFetching {
        void onFinishedFetching(TableItem[] tableItemList);
    }
}
