package de.jr.smtweaks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import de.jr.smtweaks.util.CryptoUtil;
import de.jr.smtweaks.util.GithubUpdateChecker;

public class MainActivity extends AppCompatActivity {

    public static boolean DEBUG = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onNewIntent(getIntent());
    }

    private void start() {
        SharedPreferences mainPrefs = getSharedPreferences("main_preference", Context.MODE_PRIVATE);
        CheckBox checkBox = findViewById(R.id.automaticUpdateCheckBox);
        checkBox.setChecked(mainPrefs.getBoolean("show_update_alert", true));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> mainPrefs.edit().putBoolean("show_update_alert", isChecked).apply());
        if (!new File(getFilesDir(), CryptoUtil.FileNames.PLAIN_CALENDAR_TABLE_DATA_FILE_NAME).exists()) {
            findViewById(R.id.welcomeText).setVisibility(View.VISIBLE);
        }
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            TextView t = findViewById(R.id.versionText);
            t.setText(getString(R.string.activity_version_text, pInfo.versionName));
        } catch (PackageManager.NameNotFoundException ignored) {
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (getIntent().getAction() != null) {
            switch (getIntent().getAction()) {
                case "de.jr.smtweaks.ACTION_UPDATE_ALERT":
                    GithubUpdateChecker.makeAlert(this, getIntent().getStringExtra("currentVersion"), getIntent().getStringExtra("latestVersion"), exitActivity -> {
                        if (exitActivity)
                            finishAndRemoveTask();
                        start();
                    });
                    break;
                default:
                    start();
                    break;
            }
        } else {
            start();
        }
    }
}
