package de.jr.smtweaks.util;

import android.util.Log;

import org.apache.commons.codec.binary.Base32;

import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TOTPUtil {

    public static String generateOTP(byte[] base32Secret, long counter, int digits, String algorithm) {
        try {
            byte[] key = new Base32().decode(base32Secret);
            Arrays.fill(base32Secret, (byte) 0);
            byte[] data = ByteBuffer.allocate(8).putLong(counter).array();

            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec keySpec = new SecretKeySpec(key, algorithm);
            mac.init(keySpec);
            Arrays.fill(key, (byte) 0);

            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0xF;
            int binary = ((hash[offset] & 0x7F) << 24) |
                    ((hash[offset + 1] & 0xFF) << 16) |
                    ((hash[offset + 2] & 0xFF) << 8) |
                    (hash[offset + 3] & 0xFF);

            int otp = binary % (int) Math.pow(10, digits);

            Arrays.fill(hash, (byte) 0);

            return String.format("%0" + digits + "d", otp);

        } catch (Exception e) {
            Log.e("TAG", "", e);
            return null;
        }
    }
}
