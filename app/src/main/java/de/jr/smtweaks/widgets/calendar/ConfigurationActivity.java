package de.jr.smtweaks.widgets.calendar;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.nio.charset.StandardCharsets;

import de.jr.smtweaks.R;
import de.jr.smtweaks.util.CryptoUtil;

public class ConfigurationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.calendar_configuration_activity);

        int widgetID = getIntent().getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                -1
        );
        if (widgetID == -1) {
            finish();
        }

        SharedPreferences widgetPrefs = getSharedPreferences(getString(R.string.calendar_widget_preference, widgetID), Context.MODE_PRIVATE);
        SharedPreferences mainPrefs = getSharedPreferences("main_preference", Context.MODE_PRIVATE);

        findViewById(R.id.calendar_config_button_apply).setOnClickListener(v -> {
            EditText password = findViewById(R.id.calendar_config_password);
            EditText username = findViewById(R.id.calendar_config_username);
            SharedPreferences.Editor mainEditor = mainPrefs.edit();
            SharedPreferences.Editor widgetEditor = widgetPrefs.edit();

            if (username.getText().length() != 0) {
                mainEditor.putString("username", username.getText().toString());
            }
            if (password.getText().length() != 0) {
                CryptoUtil.encrypt(password.getText().toString().getBytes(StandardCharsets.UTF_8), CryptoUtil.getKeyStoreSecretKey("passwordKey"), this, CryptoUtil.FileNames.ENC_USER_DATA_FILE_NAME);
            }
            SwitchCompat switchSaveWeek = findViewById(R.id.calendar_config_save_week);
            widgetEditor.putBoolean("show_last_week", switchSaveWeek.isChecked());
            widgetEditor.apply();
            mainEditor.apply();


            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
            setResult(RESULT_OK, resultValue);
            finish();
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
