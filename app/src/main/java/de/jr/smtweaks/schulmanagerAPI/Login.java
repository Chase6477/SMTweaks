package de.jr.smtweaks.schulmanagerAPI;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;

import de.jr.smtweaks.BuildConfig;
import de.jr.smtweaks.MainActivity;
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

public class Login implements API {

    public static final long TIMEOUT_SECONDS = 30L;
    public static final String BUNDLE_VERSION = "d621ff8b33";
    public static final long EXPIRATION_TIME = 19 * 60 * 1000;
    public static String USER_AGENT = "SMTweaks/" + BuildConfig.VERSION_NAME + " (Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + "; contact: justusreiterdevelopment@gmail.com)";

    public void login(Context context, OnFinishedUpdateRequest listener) {
        UserData userData = CryptoUtil.getUserData(context);
        if (userData == null || userData.getEmail() == null || userData.getPassword() == null) {
            listener.onFinishedUpdateRequest(null);
            return;
        }
        fetchData(userData, null, context, listener);
    }

    @Override
    public void fetchData(UserData userData, String token, Context context, OnFinishedUpdateRequest listener) {

        if (userData == null) {
            listener.onFinishedUpdateRequest(null);
            return;
        }

        long tokenCreated = context.getSharedPreferences("API", Context.MODE_PRIVATE)
                .getLong("tokenCreated", Long.MIN_VALUE);

        if (tokenCreated + EXPIRATION_TIME > System.currentTimeMillis()) {
            try {
                byte[] bytes = CryptoUtil.decrypt(
                        CryptoUtil.getKeyStoreSecretKey("Token"),
                        context,
                        CryptoUtil.FileNames.ENC_TOKEN_DATA_FILE_NAME);
                if (bytes != null && bytes.length != 0) {
                    Log.i("APICALL", "Reuse Token");
                    listener.onFinishedUpdateRequest(new String(bytes));
                    return;
                }
            } catch (BadPaddingException e) {
                Log.e("Token", "Could not get Token");
            }
        }

        String username = userData.getEmail();
        char[] password = userData.getPassword();
        TwoFAConfig twoFAConfig = userData.getTwoFAConfig();
        if (username == null || password == null) {
            listener.onFinishedUpdateRequest(null);
            return;
        }

        String twoFAString = "";
        if (twoFAConfig != null && twoFAConfig.generateCode() != null) {
            twoFAString = "\"twoFactorCode\":\"" + twoFAConfig.generateCode() + "\",";
        }

        if (MainActivity.DEBUG) {
            Log.i("APICALL", "Pseudo-Login");
            listener.onFinishedUpdateRequest("Pseudo-Token");
            return;
        }

        Log.i("APICALL", "Login");
        RequestBody body = RequestBody.create("{\"emailOrUsername\":\"" + username + "\",\"password\":\"" + new String(password) + "\",\"hash\":null,\"mobileApp\":false," + twoFAString + "\"institutionId\":null}", MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://login.schulmanager-online.de/api/login")
                .post(body)
                .header("User-Agent", Login.USER_AGENT)
                .addHeader("Content-Type", "application/json")
                .build();

        new OkHttpClient.Builder().callTimeout(Login.TIMEOUT_SECONDS, TimeUnit.SECONDS).build()
                .newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                listener.onFinishedUpdateRequest(null);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    String responeString = response.body().string();

                    if (responeString.equals("{\"requireTOTP\":true}")) {
                        listener.onFinishedUpdateRequest("requireTOTP");
                        return;
                    }
                        UserData userData = CryptoUtil.getUserData(context);
                    if (userData == null) {
                        userData = new UserData();
                    }
                    userData.setUserString(new GsonRepository().getStudent(responeString));
                    CryptoUtil.setUserData(context, userData);

                    CryptoUtil.encrypt(
                            new GsonRepository().getToken(responeString).getBytes(),
                            CryptoUtil.getKeyStoreSecretKey("Token"),
                            context,
                            CryptoUtil.FileNames.ENC_TOKEN_DATA_FILE_NAME);

                    context.getSharedPreferences("API", Context.MODE_PRIVATE).edit()
                            .putLong("tokenCreated", System.currentTimeMillis())
                            .apply();

                    listener.onFinishedUpdateRequest(new GsonRepository().getToken(responeString));
                } catch (Exception e) {
                    listener.onFinishedUpdateRequest("loginData");
                }
            }
        });
    }
}
