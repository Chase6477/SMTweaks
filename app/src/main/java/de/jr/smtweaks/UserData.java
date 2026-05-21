package de.jr.smtweaks;

import de.jr.smtweaks.qrCodeScanner.TwoFAConfig;

public class UserData {

    private char[] password;
    private String email;
    private String userString;
    private TwoFAConfig twoFAConfig;


    public UserData() {}

    public UserData(String email, char[] password, TwoFAConfig twoFAConfig) {
        this.email = email;
        this.password = password;
        this.twoFAConfig = twoFAConfig;
    }


    public TwoFAConfig getTwoFAConfig() {
        return twoFAConfig;
    }

    public void setTwoFAConfig(TwoFAConfig twoFAConfig) {
        this.twoFAConfig = twoFAConfig;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public String getUserString() {
        return userString;
    }

    public void setUserString(String userString) {
        this.userString = userString;
    }
}
