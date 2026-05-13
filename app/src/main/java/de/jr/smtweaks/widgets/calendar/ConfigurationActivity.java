package de.jr.smtweaks.widgets.calendar;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import de.jr.smtweaks.R;

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
            SharedPreferences.Editor mainEditor = mainPrefs.edit();
            SharedPreferences.Editor widgetEditor = widgetPrefs.edit();

            SwitchCompat showHolidays = findViewById(R.id.calendar_config_show_holidays);
            widgetEditor.putBoolean("show_holidays", showHolidays.isChecked());
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
