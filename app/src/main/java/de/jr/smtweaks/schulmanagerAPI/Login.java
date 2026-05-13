package de.jr.smtweaks.schulmanagerAPI;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;

import de.jr.smtweaks.UserData;
import de.jr.smtweaks.qrCodeScanner.TwoFAConfig;
import de.jr.smtweaks.util.CryptoUtil;
import de.jr.smtweaks.util.GsonRepository;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Login {

    public static String BUNDLE_VERSION = "d621ff8b33";

    public static void loginForReal(Context context, String username, String password, TwoFAConfig twoFAConfig, OnFinishedUpdateRequest listener) {


        if (username == null || password == null) {
            listener.onFinishedUpdateRequest(null);
            return;
        }

        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        String twoFAString = "";

        if (twoFAConfig != null && twoFAConfig.generateCode() != null) {
            twoFAString = "\"twoFactorCode\":\"" + twoFAConfig.generateCode() + "\",";
        }

        RequestBody body = RequestBody.create("{\"emailOrUsername\":\"" + username + "\",\"password\":\"" + password + "\",\"hash\":null,\"mobileApp\":false," + twoFAString + "\"institutionId\":null}", JSON);
        Request request = new Request.Builder()
                .url("https://login.schulmanager-online.de/api/login")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                listener.onFinishedUpdateRequest(null);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    String responeString = response.body().string();

                    UserData userData = CryptoUtil.getUserData(context);
                    if (userData == null) {
                        userData = new UserData();
                    }
                    userData.setUserString(new GsonRepository().getStudent(responeString));
                    CryptoUtil.setUserData(context, userData);

                    listener.onFinishedUpdateRequest(new GsonRepository().getToken(responeString));
                } catch (Exception e) {
                    listener.onFinishedUpdateRequest(null);
                }
            }
        });
    }

    public static void login(Context context, OnFinishedUpdateRequest listener) {
        UserData userData = CryptoUtil.getUserData(context);
        if (userData == null || userData.getEmail() == null || userData.getPassword() == null) {
            listener.onFinishedUpdateRequest(null);
            return;
        }
        loginForReal(context, userData.getEmail(), new String(userData.getPassword()), userData.getTwoFAConfig(), listener);
    }

    public interface OnFinishedUpdateRequest {
        void onFinishedUpdateRequest(String token);
    }
}
