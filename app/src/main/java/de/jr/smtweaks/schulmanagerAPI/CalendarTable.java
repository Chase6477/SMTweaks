package de.jr.smtweaks.schulmanagerAPI;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Calendar;

import javax.crypto.BadPaddingException;

import de.jr.smtweaks.MainActivity;
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
                            else
                                listener.onFinishedUpdateRequest(null);
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

    private void fetchData(String token, String student, OnFinishedFetching listener) {
        Calendar sunday = getMonday();
        sunday.add(Calendar.DAY_OF_WEEK, 7);
        RequestBody body = RequestBody.create("{\"bundleVersion\":\"fb091ba7cd\",\"requests\":[{\"moduleName\":\"schedules\",\"endpointName\":\"get-actual-lessons\",\"parameters\":{\"student\":" + student + ",\"start\":\"" + formatCalendar(getMonday()) + "\",\"end\":\"" + formatCalendar(sunday) + "\"}}]}", MediaType.get("application/json; charset=utf-8"));

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
                if (MainActivity.DEBUG)
                    listener.onFinishedFetching(new GsonRepository().jsonToTableItemList(test));
                else
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

    public final String test2 = "[{\"leftTop\":\"E\",\"rightTopAlternate\":null,\"rightTop\":\"ABC\",\"bottom\":\"1.00\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":1,\"col\":1},{\"leftTop\":\"Ph\",\"rightTopAlternate\":null,\"rightTop\":\"DoM\",\"bottom\":\"1.N1\",\"bottomAlternate\":null,\"isCancelled\":true,\"row\":1,\"col\":2},{\"leftTop\":\"E\",\"rightTopAlternate\":null,\"rightTop\":\"ABC\",\"bottom\":\"1.00\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":2,\"col\":1},{\"leftTop\":\"Ph\",\"rightTopAlternate\":\"EVA\",\"rightTop\":\"DoM\",\"bottom\":\"1.N1\",\"bottomAlternate\":\"2.11\",\"isCancelled\":false,\"row\":2,\"col\":2},{\"leftTop\":\"Geo\",\"rightTopAlternate\":\"EVA\",\"rightTop\":\"GrE\",\"bottom\":\"2.11\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":3,\"col\":1},{\"leftTop\":\"Ku\",\"rightTopAlternate\":null,\"rightTop\":\"ScS\",\"bottom\":\"2.N1\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":3,\"col\":2},{\"leftTop\":\"Geo\",\"rightTopAlternate\":\"EVA\",\"rightTop\":\"GrE\",\"bottom\":\"2.11\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":4,\"col\":1},{\"leftTop\":\"Ku\",\"rightTopAlternate\":null,\"rightTop\":\"ScS\",\"bottom\":\"2.N1\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":4,\"col\":2},{\"leftTop\":\"F\",\"rightTopAlternate\":null,\"rightTop\":\"DeD\",\"bottom\":\"2.N12\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":5,\"col\":1},{\"leftTop\":\"E\",\"rightTopAlternate\":null,\"rightTop\":\"HaR\",\"bottom\":\"2.11\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":5,\"col\":2},{\"leftTop\":\"PuG\",\"rightTopAlternate\":null,\"rightTop\":\"WeS\",\"bottom\":\"2.11\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":6,\"col\":1},{\"leftTop\":\"M\",\"rightTopAlternate\":null,\"rightTop\":\"MüM\",\"bottom\":\"2.11\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":6,\"col\":2},{\"leftTop\":\"M\",\"rightTopAlternate\":null,\"rightTop\":\"MüM\",\"bottom\":\"2.11\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":7,\"col\":2}]\n";
    public final String test = "[{\"leftTop\":\"E\",\"rightTopAlternate\":null,\"rightTop\":\"JoK\",\"bottom\":\"1.00\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":1,\"col\":1},{\"leftTop\":\"E\",\"rightTopAlternate\":\"GaY\",\"rightTop\":\"JoK\",\"bottom\":\"1.00\",\"bottomAlternate\":\"0.05\",\"isCancelled\":false,\"row\":2,\"col\":1},{\"leftTop\":\"Mu\",\"rightTopAlternate\":null,\"rightTop\":\"ScM\",\"bottom\":\"1.61\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":3,\"col\":1},{\"leftTop\":\"Mu\",\"rightTopAlternate\":null,\"rightTop\":\"ScM\",\"bottom\":\"1.61\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":4,\"col\":1},{\"leftTop\":\"De\",\"rightTopAlternate\":null,\"rightTop\":\"HoD\",\"bottom\":\"-1.01\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":5,\"col\":1},{\"leftTop\":\"De\",\"rightTopAlternate\":null,\"rightTop\":\"HoD\",\"bottom\":\"-1.01\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":6,\"col\":1},{\"leftTop\":\"Inf\",\"rightTopAlternate\":null,\"rightTop\":\"ReJ\",\"bottom\":\"2.05\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":1,\"col\":2},{\"leftTop\":\"Inf\",\"rightTopAlternate\":null,\"rightTop\":\"ReJ\",\"bottom\":\"2.05\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":2,\"col\":2},{\"leftTop\":\"Inf\",\"rightTopAlternate\":null,\"rightTop\":\"ReJ\",\"bottom\":\"2.05\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":3,\"col\":2},{\"leftTop\":\"Ch\",\"rightTopAlternate\":null,\"rightTop\":\"HeS\",\"bottom\":\"1.25\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":4,\"col\":2},{\"leftTop\":\"Ch\",\"rightTopAlternate\":null,\"rightTop\":\"HeS\",\"bottom\":\"1.25\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":5,\"col\":2},{\"leftTop\":\"Sp\",\"rightTopAlternate\":null,\"rightTop\":\"SoL\",\"bottom\":\"TH1\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":6,\"col\":2},{\"leftTop\":\"Sp\",\"rightTopAlternate\":null,\"rightTop\":\"SoL\",\"bottom\":\"TH1\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":7,\"col\":2},{\"leftTop\":\"Wr\",\"rightTopAlternate\":null,\"rightTop\":\"DeM\",\"bottom\":\"1.47\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":1,\"col\":3},{\"leftTop\":\"Wr\",\"rightTopAlternate\":null,\"rightTop\":\"DeM\",\"bottom\":\"1.47\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":2,\"col\":3},{\"leftTop\":\"Rk\",\"rightTopAlternate\":null,\"rightTop\":\"KaJ\",\"bottom\":\"2.71\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":3,\"col\":3},{\"leftTop\":\"Rk\",\"rightTopAlternate\":null,\"rightTop\":\"KaJ\",\"bottom\":\"2.71\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":4,\"col\":3},{\"leftTop\":\"Bio\",\"rightTopAlternate\":\"HeS\",\"rightTop\":\"null\",\"bottom\":\"1.11\",\"bottomAlternate\":\"1.11\",\"isCancelled\":false,\"row\":5,\"col\":3},{\"leftTop\":\"Bio\",\"rightTopAlternate\":\"Hes\",\"rightTop\":\"null\",\"bottom\":\"1.11\",\"bottomAlternate\":\"2.22\",\"isCancelled\":false,\"row\":6,\"col\":3},{\"leftTop\":\"FaQ\",\"rightTopAlternate\":null,\"rightTop\":\"EdD\",\"bottom\":\"Atrium\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":8,\"col\":3},{\"leftTop\":\"Fr\",\"rightTopAlternate\":null,\"rightTop\":\"HoD\",\"bottom\":\"0.67\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":2,\"col\":4},{\"leftTop\":\"Fr\",\"rightTopAlternate\":null,\"rightTop\":\"HoD\",\"bottom\":\"0.67\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":3,\"col\":4},{\"leftTop\":\"Geo\",\"rightTopAlternate\":null,\"rightTop\":\"ZwM\",\"bottom\":\"R.34\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":4,\"col\":4},{\"leftTop\":\"Geo\",\"rightTopAlternate\":null,\"rightTop\":\"ZwM\",\"bottom\":\"R.34\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":5,\"col\":4},{\"leftTop\":\"Ku\",\"rightTopAlternate\":null,\"rightTop\":\"ReM\",\"bottom\":\"2.14\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":1,\"col\":5},{\"leftTop\":\"Ku\",\"rightTopAlternate\":null,\"rightTop\":\"ReM\",\"bottom\":\"2.14\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":2,\"col\":5},{\"leftTop\":\"G\",\"rightTopAlternate\":null,\"rightTop\":\"DeM\",\"bottom\":\"1.12\",\"bottomAlternate\":null,\"isCancelled\":true,\"row\":3,\"col\":5},{\"leftTop\":\"G\",\"rightTopAlternate\":null,\"rightTop\":\"DeM\",\"bottom\":\"1.12\",\"bottomAlternate\":null,\"isCancelled\":true,\"row\":4,\"col\":5},{\"leftTop\":\"Ma\",\"rightTopAlternate\":null,\"rightTop\":\"ReJ\",\"bottom\":\"2.09\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":7,\"col\":5},{\"leftTop\":\"Ma\",\"rightTopAlternate\":null,\"rightTop\":\"ReJ\",\"bottom\":\"2.09\",\"bottomAlternate\":null,\"isCancelled\":false,\"row\":8,\"col\":5}]";
}
