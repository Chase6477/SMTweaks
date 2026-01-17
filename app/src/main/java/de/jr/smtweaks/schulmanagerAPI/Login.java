package de.jr.smtweaks.schulmanagerAPI;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.crypto.BadPaddingException;

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

    public static SharedPreferences getMainPreference(Context context) {
        return context.getSharedPreferences("main_preference", Context.MODE_PRIVATE);
    }

    public static void login(Context context, OnFinishedUpdateRequest listener) {

        System.out.println("Login");

        String password;
        String username = getMainPreference(context).getString("username", null);

        if (username == null) {
            listener.onFinishedUpdateRequest(false);
            return;
        }
        try {
            byte[] passwordBytes = CryptoUtil.decrypt(CryptoUtil.getKeyStoreSecretKey("passwordKey"), context, CryptoUtil.FileNames.ENC_USER_DATA_FILE_NAME);
            if (passwordBytes == null) {
                listener.onFinishedUpdateRequest(false);
                return;
            }
            password = new String(passwordBytes);
        } catch (Exception e) {
            listener.onFinishedUpdateRequest(false);
            return;
        }

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create("{\"emailOrUsername\":\"" + username + "\",\"password\":\"" + password + "\",\"hash\":null,\"mobileApp\":false,\"institutionId\":null}", JSON);
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
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String responeString = response.body().string();

                String token = new GsonRepository().getToken(responeString);
                CryptoUtil.encrypt(token.getBytes(StandardCharsets.UTF_8), CryptoUtil.getKeyStoreSecretKey("tokenKey"), context, CryptoUtil.FileNames.ENC_TOKEN_FILE_NAME);
                String student = new GsonRepository().getStudent(responeString);
                CryptoUtil.encrypt(student.getBytes(StandardCharsets.UTF_8), CryptoUtil.getKeyStoreSecretKey("studentKey"), context, CryptoUtil.FileNames.ENC_STUDENT_FILE_NAME);
                listener.onFinishedUpdateRequest(true);
            }
        });
    }


    public static String getToken(Context context) throws IOException, BadPaddingException {
        if (!new File(context.getFilesDir(), CryptoUtil.FileNames.ENC_TOKEN_FILE_NAME).exists())
            throw new IOException();
        byte[] bytes = CryptoUtil.decrypt(CryptoUtil.getKeyStoreSecretKey("tokenKey"), context, CryptoUtil.FileNames.ENC_TOKEN_FILE_NAME);
        return new String(bytes);
    }

    public static String getStudent(Context context) throws IOException, BadPaddingException {
        if (!new File(context.getFilesDir(), CryptoUtil.FileNames.ENC_STUDENT_FILE_NAME).exists())
            throw new IOException();
        byte[] bytes = CryptoUtil.decrypt(CryptoUtil.getKeyStoreSecretKey("studentKey"), context, CryptoUtil.FileNames.ENC_STUDENT_FILE_NAME);
        return new String(bytes);
    }

    public interface OnFinishedUpdateRequest {
        void onFinishedUpdateRequest(boolean successful);
    }
}
