package de.jr.smtweaks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import de.jr.smtweaks.util.GithubUpdateChecker;

public class MainActivity extends AppCompatActivity {

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
