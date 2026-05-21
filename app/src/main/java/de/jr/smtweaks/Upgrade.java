package de.jr.smtweaks;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import de.jr.smtweaks.util.CryptoUtil;
import de.jr.smtweaks.util.GsonRepository;

public class Upgrade {
    // I hate this, but I have to do it... upgrading from every version will now preserve data

    private static final String V_UNKNOWN = "unknown";
    private static final String V_1_5 = "1.5";

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void v1_4TOv1_5(Context context) throws Exception {
        File holidayOld = new File(context.getFilesDir(),"holidayDates.txt");
        File calendarTableOld = new File(context.getFilesDir(),"calendarTableData.enc");
        File calendarTableSmallOld = new File(context.getFilesDir(),"CalendarTableDataSmall.txt");
        File userDataOld = new File(context.getFilesDir(),"userData.enc");
        File tokenOld = new File(context.getFilesDir(),"token.enc");
        File studentOld = new File(context.getFilesDir(),"student.enc");

        String password = null;
        String calendarTable = null;
        String userName;
        boolean showUpdateAlert;

        if (tokenOld.exists())
            tokenOld.delete();
        if (studentOld.exists())
            studentOld.delete();
        if (holidayOld.exists())
            holidayOld.delete();
        if (calendarTableSmallOld.exists())
            calendarTableSmallOld.delete();
        if (calendarTableOld.exists()) {
            calendarTable = new String(CryptoUtil.readFile(calendarTableOld));
            calendarTableOld.delete();
        }
        if (userDataOld.exists()) {
            password = new String(CryptoUtil.decrypt(CryptoUtil.getKeyStoreSecretKey("passwordKey"), context, "userData.enc"));
            userDataOld.delete();
        }

        SharedPreferences mainPreference = context.getSharedPreferences("main_preference", Context.MODE_PRIVATE);
        userName = mainPreference.getString("username", null);
        showUpdateAlert = mainPreference.getBoolean("show_update_alert", true);

        context.deleteSharedPreferences("main_preference");

        UserData userData;

        if (password != null && userName != null) {
            userData = new UserData(userName, password.toCharArray(), null);
            CryptoUtil.encrypt(
                    new GsonRepository().userDataToJson(userData),
                    CryptoUtil.getKeyStoreSecretKey("UserData"),
                    context,
                    "UserData.enc");
        }
        context.getSharedPreferences("main_preference", Context.MODE_PRIVATE)
                .edit().putBoolean("show_update_alert", showUpdateAlert).apply();

        if (calendarTable != null) {
            LocalDate monday = LocalDate.now().getDayOfWeek().getValue() >= 6
                    ? LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                    : LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            for (int i = 0; i < 5; i++) {
                calendarTable = calendarTable.replace("\"col\":" + (i + 1), "\"date\":{\"year\":" + monday.getYear() + ",\"month\":" + monday.getMonthValue() + ",\"day\":" + monday.getDayOfMonth() + "}");
                monday = monday.plusDays(1);
            }
            CryptoUtil.writeFile(new File(context.getFilesDir(), "CalendarTableData.txt"), calendarTable.getBytes());
        }
        context.getSharedPreferences("VERSION", Context.MODE_PRIVATE).edit().putString("Version", V_1_5).apply();
    }

    public static void upgrade(Context context) {
        String oldVersion = context.getSharedPreferences("VERSION", Context.MODE_PRIVATE).getString("Version", V_UNKNOWN);
        try {
            while (!oldVersion.equals(V_1_5))
                switch (oldVersion) {
                    case V_UNKNOWN:
                        v1_4TOv1_5(context);
                        oldVersion = V_1_5;
                        break;
                }
        } catch (Exception e) {
            Log.e("UPGRADE", "failed to upgrade: " + oldVersion, e);
            Toast.makeText(context, "Could not preserve Data to new Version", Toast.LENGTH_LONG).show();
        }
    }
}
