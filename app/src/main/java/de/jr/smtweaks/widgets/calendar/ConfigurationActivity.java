package de.jr.smtweaks.widgets.calendar;

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

        SharedPreferences prefs = this.getSharedPreferences("calendar_widget_preferences", Context.MODE_PRIVATE);

        findViewById(R.id.calendar_config_button_apply).setOnClickListener(v -> {
            EditText password = findViewById(R.id.calendar_config_password);
            EditText username = findViewById(R.id.calendar_config_username);
            SharedPreferences.Editor editor = prefs.edit();

            if (username.getText().length() != 0) {
                editor.putString("username", username.getText().toString());
            }
            if (password.getText().length() != 0) {
                CryptoUtil.encrypt(password.getText().toString().getBytes(StandardCharsets.UTF_8), CryptoUtil.getKeyStoreSecretKey("passwordKey"), this, CryptoUtil.FileNames.ENC_USER_DATA_FILE_NAME);
            }
            SwitchCompat switchSaveWeek = findViewById(R.id.calendar_config_save_week);
            editor.putBoolean(getString(R.string.calendar_widget_configuration_show_last_week_switch), switchSaveWeek.isChecked());
            editor.apply();


            int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            WidgetProvider.updateAppWidget(this, appWidgetManager, mAppWidgetId);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        });

    }
}
