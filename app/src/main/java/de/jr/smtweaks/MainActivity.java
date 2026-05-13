package de.jr.smtweaks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
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

public class MainActivity extends AppCompatActivity {

    public static boolean DEBUG = false;
    private final ActivityResultLauncher<Intent> qrLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() != RESULT_OK) return;
                        Intent data = result.getData();
                        if (data == null) return;

                        String qrCode = data.getStringExtra(QrCodeActivity.EXTRA_RESULT);

                        TwoFAConfig twoFA = LinkParser.otpAuthParser(qrCode);

                        if (twoFA != null) {
                            UserData userData = CryptoUtil.getUserData(this);
                            if (userData == null)
                                userData = new UserData();
                            userData.setTwoFAConfig(twoFA);

                            CryptoUtil.setUserData(this, userData);
                        }
                    });
    private boolean showPassword = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_fragment);
        onNewIntent(getIntent());

        EditText username = findViewById(R.id.config_app_username);
        EditText password = findViewById(R.id.config_app_password);

        UserData twoFaCheck = CryptoUtil.getUserData(this);
        if (twoFaCheck == null)
            twoFaCheck = new UserData();
        TwoFAConfig twoFAConfig = twoFaCheck.getTwoFAConfig();
        findViewById(R.id.config_app_validate_data).setOnClickListener(v -> Login.loginForReal(
                this,
                username.getText().toString(),
                password.getText().toString(),
                twoFAConfig,
                successful -> runOnUiThread(() -> {
                    TextView validateData = findViewById(R.id.config_app_text_validate_data);
                    if (successful) {
                        UserData userData = CryptoUtil.getUserData(this);
                        if (userData == null)
                            userData = new UserData();
                        userData.setEmail(username.getText().toString());
                        userData.setPassword(password.getText().toString().toCharArray());

                        CryptoUtil.setUserData(this, userData);

                        validateData.setVisibility(View.VISIBLE);
                        validateData.setText("Logged in as \"" + username.getText().toString() + '"');
                    } else {
                        Snackbar.make(
                                findViewById(android.R.id.content),
                                "Wrong username or password",
                                Snackbar.LENGTH_SHORT
                        ).setAnchorView(R.id.config_app_validate_data).show();
                    }
                })));

        findViewById(R.id.config_app_password_show_button).setOnClickListener(v -> {
            ImageView eyecon = findViewById(R.id.config_app_password_show_image);
            if (showPassword) {
                eyecon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.eyestroketransparent));
                password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                eyecon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.eye));
                password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }

            showPassword = !showPassword;
        });


        findViewById(R.id.config_app_password_show_button2).setOnClickListener(v ->
                qrLauncher.launch(new Intent(this, QrCodeActivity.class)));
    }

    private void start() {
        SharedPreferences mainPrefs = getSharedPreferences("main_preference", Context.MODE_PRIVATE);
        MaterialSwitch aSwitch = findViewById(R.id.config_app_automatic_update);
        aSwitch.setChecked(mainPrefs.getBoolean("show_update_alert", true));
        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> mainPrefs.edit().putBoolean("show_update_alert", isChecked).apply());
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            TextView t = findViewById(R.id.versionText);
            t.setText(getString(R.string.activity_version_text, pInfo.versionName));
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        if (mainPrefs.getString("username", null) != null) {
            TextView validateData = findViewById(R.id.config_app_text_validate_data);
            validateData.setVisibility(View.VISIBLE);
            validateData.setText("Logged in as \"" + mainPrefs.getString("username", null) + '"');
        }

        Spinner states = findViewById(R.id.config_app_update_interval);

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.updateInterval,
                        android.R.layout.simple_spinner_item
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        states.setAdapter(adapter);
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
