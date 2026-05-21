package de.jr.smtweaks;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.animation.ObjectAnimator;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;

import de.jr.smtweaks.qrCodeScanner.LinkParser;
import de.jr.smtweaks.qrCodeScanner.QrCodeActivity;
import de.jr.smtweaks.qrCodeScanner.TwoFAConfig;
import de.jr.smtweaks.schulmanagerAPI.Login;
import de.jr.smtweaks.util.CryptoUtil;
import de.jr.smtweaks.util.GithubUpdateChecker;
import de.jr.smtweaks.widgets.calendar.ConfigurationActivity;

public class MainActivity extends AppCompatActivity {

    public static boolean DEBUG = true;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable progressBarRunnable;
    private boolean showPassword = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_fragment);
        onNewIntent(getIntent());
    }

    private void start() {
        SharedPreferences mainPrefs = getSharedPreferences("main_preference", Context.MODE_PRIVATE);
        MaterialSwitch aSwitch = findViewById(R.id.config_app_automatic_update);
        aSwitch.setChecked(mainPrefs.getBoolean("show_update_alert", true));
        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> mainPrefs.edit().putBoolean("show_update_alert", isChecked).apply());

        Spinner updateIntervalSpinner = findViewById(R.id.config_app_update_interval);
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.updateInterval,
                        android.R.layout.simple_spinner_item
                );
        adapter.setDropDownViewResource(
                R.layout.dropdown_item
        );
        updateIntervalSpinner.setAdapter(adapter);

        Intent i = new Intent(this, ConfigurationActivity.class);
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 9);

        Spinner weekSpinner = findViewById(R.id.config_app_week);
        ArrayAdapter<CharSequence> adapter2 =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.weekDay,
                        android.R.layout.simple_spinner_item
                );
        adapter2.setDropDownViewResource(
                R.layout.dropdown_item
        );
        weekSpinner.setAdapter(adapter2);


        UserData userDataBuilder = CryptoUtil.getUserData(this);
        if (userDataBuilder == null)
            userDataBuilder = new UserData();
        UserData userData = userDataBuilder;
        EditText username = findViewById(R.id.config_app_username);
        EditText password = findViewById(R.id.config_app_password);


        findViewById(R.id.config_app_validate_data).setOnClickListener(v -> {
            getSharedPreferences("API", Context.MODE_PRIVATE).edit().putLong("tokenCreated", Long.MIN_VALUE).apply();
            new Login().fetchData(
                    new UserData(username.getText().toString(), password.getText().toString().toCharArray(), userData.getTwoFAConfig()),
                    null,
                    this,
                    result -> runOnUiThread(() -> {


                        if (result == null) {
                            Snackbar.make(
                                    findViewById(android.R.id.content),
                                    "Unexpected Error",
                                    Snackbar.LENGTH_SHORT
                            ).setAnchorView(R.id.config_app_validate_data).show();
                            return;
                        }

                        switch (result) {
                            case "requireTOTP":
                                Snackbar.make(
                                        findViewById(android.R.id.content),
                                        "Requires 2 Factor Authentification",
                                        Snackbar.LENGTH_SHORT
                                ).setAnchorView(R.id.config_app_validate_data).show();
                                return;
                            case "loginData":
                                Snackbar.make(
                                        findViewById(android.R.id.content),
                                        "Wrong Username or Password",
                                        Snackbar.LENGTH_SHORT
                                ).setAnchorView(R.id.config_app_validate_data).show();
                                return;
                            default:
                                UserData userDataSave = CryptoUtil.getUserData(this);
                                if (userDataSave == null)
                                    userDataSave = new UserData();
                                userDataSave.setEmail(username.getText().toString());
                                userDataSave.setPassword(password.getText().toString().toCharArray());

                                CryptoUtil.setUserData(this, userDataSave);
                                userAuthenticated(userDataSave.getEmail());

                        }
                    }));
        });

        findViewById(R.id.config_app_password_show_button).setOnClickListener(v -> {
            ImageView eyecon = findViewById(R.id.config_app_password_show_image); // Its an Eye Icon -> Eyecon!!!!!
            if (showPassword) {
                eyecon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.eyestroketransparent));
                password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                eyecon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.eye));
                password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            showPassword = !showPassword;
        });

        EditText hour = findViewById(R.id.config_app_hour);
        hour.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                autoUpdateIntervalChanged();
                if (s.toString().isEmpty())
                    return;
                if (Integer.parseInt(s.toString()) > 23)
                    hour.setText(String.valueOf(23));
            }
        });
        EditText minute = findViewById(R.id.config_app_minute);
        minute.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                autoUpdateIntervalChanged();
                if (s.toString().isEmpty())
                    return;
                if (Integer.parseInt(s.toString()) > 59)
                    minute.setText(String.valueOf(59));
            }
        });

        weekSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                autoUpdateIntervalChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        updateIntervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0)
                    findViewById(R.id.config_app_update_interval_container).setVisibility(GONE);
                else
                    findViewById(R.id.config_app_update_interval_container).setVisibility(VISIBLE);
                autoUpdateIntervalChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        findViewById(R.id.config_app_apply_interval).setOnClickListener(v -> {
            SharedPreferences.Editor editor = mainPrefs.edit();
            editor.putInt("AUInterval", updateIntervalSpinner.getSelectedItemPosition());
            int buffer;
            if (hour.getText().toString().isEmpty())
                buffer = 0;
            else
                buffer = Integer.parseInt(hour.getText().toString());
            editor.putInt("AUHour", buffer);
            if (minute.getText().toString().isEmpty())
                buffer = 0;
            else
                buffer = Integer.parseInt(minute.getText().toString());
            editor.putInt("AUMinute", buffer);
            editor.putInt("AUWeek", weekSpinner.getSelectedItemPosition());
            editor.apply();
            findViewById(R.id.config_app_unsaved).setVisibility(GONE);
        });

        CheckBox activate2FA = findViewById(R.id.config_app_qr_activate);
        activate2FA.setOnCheckedChangeListener((buttonView, isChecked) -> {
            UserData userData2FA = CryptoUtil.getUserData(this);
            if (!isChecked) {
                set2FAVisible(GONE);
                mainPrefs.edit().putBoolean("usesOTP", false).apply();
            } else if (!mainPrefs.getBoolean("usesOTP", false) && (userData2FA == null || userData2FA.getTwoFAConfig() == null))
                qrLauncher.launch(new Intent(this, QrCodeActivity.class));
            else if (userData2FA != null && userData2FA.getTwoFAConfig() != null) {
                set2FAVisible(VISIBLE);
                startProgressBar(userData2FA);
                mainPrefs.edit().putBoolean("usesOTP", true).apply();
            }
        });

        findViewById(R.id.config_app_delete_twofa).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setNegativeButton("Cancel", (dialog, which) -> {
                System.out.println("Cancel");
            });
            builder.setPositiveButton("Delete", (dialog, which) -> {
                System.out.println("Delete");
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setTitle("Really?");
                builder2.setNegativeButton("Cancel", (d, w) -> {
                    System.out.println("Cancel fr");
                });
                builder2.setPositiveButton("Delete", (d, w) -> {
                    System.out.println("Delete fr");
                    UserData userData2FA = CryptoUtil.getUserData(this);
                    if (userData2FA == null)
                        return;
                    userData2FA.setTwoFAConfig(null);
                    CryptoUtil.setUserData(this, userData2FA);
                    if (mainPrefs.getBoolean("usesOTP", true))
                        set2FAVisible(GONE);
                    mainPrefs.edit().putBoolean("usesOTP", false).apply();
                    activate2FA.setChecked(false);
                    findViewById(R.id.config_app_delete_twofa).setVisibility(GONE);
                    findViewById(R.id.config_app_delete_twofa_icon).setVisibility(GONE);
                });
                builder2.setMessage("There wont be a way back! Be REALLY sure if you don't need this code anymore!");
                builder2.show();
            });
            builder.setTitle("Delete 2FA");
            builder.setMessage("Deleting your 2FA code cannot be undone. Please check if you can really delete this code, else you could lock yourself out of Schulmanager, even out of the official App an need to contact the support in your school");
            builder.show();
        });


        if (mainPrefs.getBoolean("usesOTP", false)) {
            activate2FA.setChecked(true);
            set2FAVisible(VISIBLE);
            startProgressBar(userData);
        } else {
            activate2FA.setChecked(false);
            set2FAVisible(GONE);
        }

        if (userData.getTwoFAConfig() != null) {
            findViewById(R.id.config_app_delete_twofa).setVisibility(VISIBLE);
            findViewById(R.id.config_app_delete_twofa_icon).setVisibility(VISIBLE);
        }


        ((TextView) findViewById(R.id.versionText)).setText(getString(R.string.activity_version_text, BuildConfig.VERSION_NAME));
        if (userData.getEmail() != null)
            username.setText(userData.getEmail());
        if (getSharedPreferences("API", Context.MODE_PRIVATE).getLong("tokenCreated", Long.MIN_VALUE) + Login.EXPIRATION_TIME > System.currentTimeMillis())
            userAuthenticated(userData.getEmail());
        else if (userData.getEmail() != null)
            new Login().login(this, result -> {
                if (result != null && !result.equals("requireTOTP") && !result.equals("loginData")) {
                    userAuthenticated(userData.getEmail());
                } else {
                    userAuthenticated(null);
                }
            });

        updateIntervalSpinner.setSelection(mainPrefs.getInt("AUInterval", 0));
        if (mainPrefs.getInt("AUHour", 0) != 0)
            hour.setText(String.valueOf(mainPrefs.getInt("AUHour", 0)));
        if (mainPrefs.getInt("AUMinute", 0) != 0)
            minute.setText(String.valueOf(mainPrefs.getInt("AUMinute", 0)));
        weekSpinner.setSelection(mainPrefs.getInt("AUWeek", 0));

        if (updateIntervalSpinner.getSelectedItemPosition() == 0)
            findViewById(R.id.config_app_update_interval_container).setVisibility(GONE);
        else
            findViewById(R.id.config_app_update_interval_container).setVisibility(VISIBLE);


    }

    private void autoUpdateIntervalChanged() {
        SharedPreferences mainPref = getSharedPreferences("main_preference", Context.MODE_PRIVATE);
        EditText hour = findViewById(R.id.config_app_hour);
        EditText minute = findViewById(R.id.config_app_minute);
        Spinner weekSpinner = findViewById(R.id.config_app_week);
        Spinner intervalSpinner = findViewById(R.id.config_app_update_interval);
        TextView unsaved = findViewById(R.id.config_app_unsaved);

        if (String.valueOf(mainPref.getInt("AUHour", 0)).equals(hour.getText().toString())
                || (hour.getText().toString().isEmpty() && mainPref.getInt("AUHour", 0) == 0)
                && String.valueOf(mainPref.getInt("AUMinute", 0)).equals(minute.getText().toString())
                || (minute.getText().toString().isEmpty() && mainPref.getInt("AUMinute", 0) == 0)
                && mainPref.getInt("AUWeek", 0) == weekSpinner.getSelectedItemPosition()
                && mainPref.getInt("AUInterval", 0) == intervalSpinner.getSelectedItemPosition()
        )
            unsaved.setVisibility(GONE);
        else
            unsaved.setVisibility(VISIBLE);
    }

    private void userAuthenticated(String username) {
        runOnUiThread(() -> {
            TextView validateData = findViewById(R.id.config_app_text_validate_data);
            validateData.setVisibility(VISIBLE);
            if (username != null)
                validateData.setText("Logged in as \"" + username + '"');
            else
                validateData.setText("Login problems!");
        });
    }

    private void set2FAVisible(int visible) {
        findViewById(R.id.config_app_progress_bar).setVisibility(visible);
        findViewById(R.id.config_app_progress_bar_text).setVisibility(visible);
        findViewById(R.id.config_app_progress_bar_text).setVisibility(visible);
    }

    private void startProgressBar(UserData userData) {
        if (progressBarRunnable != null)
            return;
        ProgressBar bar = findViewById(R.id.config_app_progress_bar);
        if (userData != null && userData.getTwoFAConfig() != null) {
            progressBarRunnable = new Runnable() {
                @Override
                public void run() {
                    TextView text = findViewById(R.id.config_app_progress_bar_text);
                    text.setText(userData.getTwoFAConfig().generateCode());
                    int timeRemaining = (int) (30000 - System.currentTimeMillis() % 30000);
                    bar.setMax(3000);
                    bar.setProgress(timeRemaining / 10);
                    ObjectAnimator progressBarAnimator = ObjectAnimator.ofInt(bar, "progress", 0);
                    progressBarAnimator.setDuration(timeRemaining);
                    progressBarAnimator.setInterpolator(new
                            LinearInterpolator());
                    progressBarAnimator.start();
                    handler.postDelayed(this, timeRemaining);
                }
            };
        }
        handler.post(progressBarRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressBarRunnable != null)
            handler.removeCallbacks(progressBarRunnable);
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
    }    private final ActivityResultLauncher<Intent> qrLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() != RESULT_OK) {
                            start();
                            return;
                        }

                        Intent data = result.getData();
                        if (data == null) {
                            start();
                            return;
                        }


                        String qrCode = data.getStringExtra(QrCodeActivity.EXTRA_RESULT);

                        TwoFAConfig twoFA = LinkParser.otpAuthParser(qrCode);

                        if (twoFA != null) {
                            UserData userData = CryptoUtil.getUserData(this);
                            if (userData == null)
                                userData = new UserData();
                            userData.setTwoFAConfig(twoFA);

                            CryptoUtil.setUserData(this, userData);

                            getSharedPreferences("main_preference", Context.MODE_PRIVATE).edit().putBoolean("usesOTP", true).apply();
                            findViewById(R.id.config_app_delete_twofa).setVisibility(VISIBLE);
                            findViewById(R.id.config_app_delete_twofa_icon).setVisibility(VISIBLE);
                            set2FAVisible(VISIBLE);
                            startProgressBar(userData);
                        }
                    });




}
