package de.jr.smtweaks.widgets.calendar;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

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

        SwitchCompat showHolidays = findViewById(R.id.calendar_config_show_holidays);
        SwitchCompat saveWeek = findViewById(R.id.calendar_config_save_week);
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        Spinner from = findViewById(R.id.calendar_config_spinner_from);
        Spinner to = findViewById(R.id.calendar_config_spinner_to);

        radioGroup.check(widgetPrefs.getInt("day_list_option", R.id.calendar_config_radio_adaptive));
        reload();

        showHolidays.setChecked(widgetPrefs.getBoolean("show_holidays", true));
        saveWeek.setChecked(widgetPrefs.getBoolean("show_last_week", true));
        from.setSelection(widgetPrefs.getInt("from", 0));
        to.setSelection(widgetPrefs.getInt("to", 4));

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            reload();
        });

        findViewById(R.id.calendar_config_button_apply).setOnClickListener(v -> {
            SharedPreferences.Editor widgetEditor = widgetPrefs.edit();

            widgetEditor.putBoolean("show_holidays", showHolidays.isChecked());
            widgetEditor.putBoolean("show_last_week", saveWeek.isChecked());
            widgetEditor.putInt("day_list_option", radioGroup.getCheckedRadioButtonId());
            widgetEditor.putInt("from", from.getSelectedItemPosition());
            widgetEditor.putInt("to", to.getSelectedItemPosition());
            widgetEditor.apply();


            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
            setResult(RESULT_OK, resultValue);
            finish();
        });

    }

    private void reload() {
        SwitchCompat saveWeek = findViewById(R.id.calendar_config_save_week);
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        Spinner from = findViewById(R.id.calendar_config_spinner_from);
        TextView fromText = findViewById(R.id.calendar_config_text_from);
        Spinner to = findViewById(R.id.calendar_config_spinner_to);
        TextView toText = findViewById(R.id.calendar_config_text_to);

        ArrayAdapter<CharSequence> dayAdapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.weekDay,
                        android.R.layout.simple_spinner_item
                );
        dayAdapter.setDropDownViewResource(
                R.layout.dropdown_item
        );

        ArrayAdapter<CharSequence> maxAdapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.max,
                        android.R.layout.simple_spinner_item
                );
        maxAdapter.setDropDownViewResource(
                R.layout.dropdown_item
        );

        if (radioGroup.getCheckedRadioButtonId() == R.id.calendar_config_radio_updated) {
            to.setVisibility(GONE);
            toText.setVisibility(GONE);
            saveWeek.setVisibility(GONE);
            fromText.setText("max");
            from.setAdapter(maxAdapter);
        } else {
            to.setVisibility(VISIBLE);
            toText.setVisibility(VISIBLE);
            saveWeek.setVisibility(VISIBLE);
            fromText.setText("from");
            from.setAdapter(dayAdapter);
            to.setAdapter(dayAdapter);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
