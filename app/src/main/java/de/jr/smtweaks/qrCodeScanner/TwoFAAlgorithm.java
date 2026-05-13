package de.jr.smtweaks.qrCodeScanner;

import java.util.Arrays;
import java.util.List;

public class TwoFAAlgorithm {
    public static final String HMACSHA1 = "HmacSHA1";
    public static final String HMACSHA256 = "HmacSHA256";
    public static final String HMACSHA512 = "HmacSHA512";

    public static List<String> getList() {
        return Arrays.asList(HMACSHA1, HMACSHA256, HMACSHA512);
    }
}
