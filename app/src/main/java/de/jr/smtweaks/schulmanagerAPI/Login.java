package de.jr.smtweaks.schulmanagerAPI;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.crypto.BadPaddingException;

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

    public static void loginForReal(Context context, String username, String password, TwoFAConfig twoFAConfig, OnFinishedUpdateRequest listener) {


        if (username == null || password == null) {
            listener.onFinishedUpdateRequest(false);
            return;
        }

        System.out.println(username + " | " + password);


        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        String twoFAString = "";

        if (twoFAConfig != null && twoFAConfig.generateCode() != null) {
            twoFAString = "\"twoFactorCode\":\"" + twoFAConfig.generateCode() + "\",";
        }

        System.out.println("{\"emailOrUsername\":\"" + username + "\",\"password\":\"" + password + "\",\"hash\":null,\"mobileApp\":false," + twoFAString + "\"institutionId\":null}");

        RequestBody body = RequestBody.create("{\"emailOrUsername\":\"" + username + "\",\"password\":\"" + password + "\",\"hash\":null,\"mobileApp\":false," + twoFAString + "\"institutionId\":null}", JSON);
        Request request = new Request.Builder()
                .url("https://login.schulmanager-online.de/api/login")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                listener.onFinishedUpdateRequest(false);
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

                    CryptoUtil.encrypt(
                            new GsonRepository().getToken(responeString).getBytes(StandardCharsets.UTF_8),
                            CryptoUtil.getKeyStoreSecretKey("tokenKey"),
                            context,
                            CryptoUtil.FileNames.ENC_TOKEN_FILE_NAME);
                    listener.onFinishedUpdateRequest(true);
                } catch (Exception e) {
                    listener.onFinishedUpdateRequest(false);
                }
            }
        });
    }

    public static void login(Context context, OnFinishedUpdateRequest listener) {
        UserData userData = CryptoUtil.getUserData(context);
        if (userData == null || userData.getEmail() == null || userData.getPassword() == null) {
            listener.onFinishedUpdateRequest(false);
            return;
        }
        loginForReal(context, userData.getEmail(), new String(userData.getPassword()), userData.getTwoFAConfig(), listener);
    }


    public static String getToken(Context context) throws IOException, BadPaddingException {
        if (!new File(context.getFilesDir(), CryptoUtil.FileNames.ENC_TOKEN_FILE_NAME).exists())
            throw new IOException();
        byte[] bytes = CryptoUtil.decrypt(CryptoUtil.getKeyStoreSecretKey("tokenKey"), context, CryptoUtil.FileNames.ENC_TOKEN_FILE_NAME);
        return new String(bytes);
    }

    public interface OnFinishedUpdateRequest {
        void onFinishedUpdateRequest(boolean successful);
    }
}
