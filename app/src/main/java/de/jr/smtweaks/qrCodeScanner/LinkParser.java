package de.jr.smtweaks.qrCodeScanner;

import android.net.Uri;
import android.util.Log;

import java.net.URI;
import java.nio.charset.StandardCharsets;


public class LinkParser {

    public static TwoFAConfig otpAuthParser(String link) {

        URI uri;
        try {
            uri = URI.create(link);

            if (uri.getScheme() != null && !uri.getScheme().equals("otpauth"))
                return null;

            if (uri.getAuthority() == null)
                return null;

            int mode = TwoFAMode.getList().indexOf(uri.getAuthority().toUpperCase());

            String attributes = Uri.decode(uri.getQuery());
            String userInfo = Uri.decode(link).substring(15, Uri.decode(link).length() - attributes.length());
            if (userInfo.endsWith("?")) {
                userInfo = userInfo.substring(0, userInfo.length() - 1);
            }

            String issuer = null;
            String label;
            if (userInfo.contains(":")) {

                issuer = userInfo.split(":")[0];
                label = userInfo.split(":")[1];
            } else
                label = userInfo;

            byte[] secret = null;
            String algorithm = "HMACSHA1";
            long period = -1;
            int digits = -1;
            String[] attr;
            if (attributes.contains("&"))
                attr = attributes.split("&");
            else
                attr = new String[]{attributes};

            for (String attribute : attr) {
                if (!attribute.contains("="))
                    continue;
                String[] split = attribute.split("=");
                switch (split[0].toLowerCase()) {
                    case "secret":
                        secret = split[1].getBytes(StandardCharsets.UTF_8);
                        break;
                    case "algorithm":
                        algorithm = "Hmac" + split[1];
                        break;
                    case "digits":
                        digits = Integer.parseInt(split[1]);
                        break;
                    case "period":
                    case "counter":
                        period = Long.parseLong(split[1]);
                        break;
                    case "issuer":
                        issuer = split[1];
                        break;
                }
            }

            if (secret == null) {
                return null;
            }
            if (!TwoFAAlgorithm.getList().contains(algorithm)) {
                algorithm = TwoFAAlgorithm.HMACSHA1;
            }
            if (period < 0) {
                period = 0;
            }
            if (mode == TwoFAMode.TOTP && period == 0) {
                period = 30;
            }
            if (digits <= 0) {
                digits = 6;
            }

            return new TwoFAConfig(issuer, label, mode, algorithm, period, digits, secret);

        } catch (Exception e) {
            Log.e("Parsing", "error in parsing link", e);
            return null;
        }
    }
}
