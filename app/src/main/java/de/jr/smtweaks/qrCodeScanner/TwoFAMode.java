package de.jr.smtweaks.qrCodeScanner;

import java.util.Arrays;
import java.util.List;

public class TwoFAMode {
    public static int TOTP = 0;
    public static int HOTP = 1;

    public static List<String> getList() {
        return Arrays.asList("TOTP", "HOTP");
    }
}
