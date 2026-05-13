package de.jr.smtweaks.qrCodeScanner;

import android.util.Log;

import org.apache.commons.codec.binary.Base32;

import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TwoFAConfig {

    private final String issuer;
    private final String label;
    private final int mode;
    private final String algorithm;
    private final int digits;
    private final byte[] secret;
    private final long timeStep; //also counter


    public TwoFAConfig(String issuer, String label, int mode, String algorithm, long timeStep, int digits, byte[] secret) {
        this.issuer = issuer;
        this.label = label;
        this.mode = mode;
        this.algorithm = algorithm;
        this.timeStep = timeStep;
        this.digits = digits;
        this.secret = secret;
    }

    public byte[] getSecret() {
        return secret;
    }

    public int getDigits() {
        return digits;
    }

    public long getTimeStep() {
        return timeStep;
    }

    public String getAlgorithm() {
        if (algorithm == null)
            return TwoFAAlgorithm.HMACSHA1;
        return algorithm;
    }


    public String generateCode() {

        try {
            byte[] key = new Base32().decode(getSecret());
            byte[] data = ByteBuffer.allocate(8).putLong(System.currentTimeMillis() / (getTimeStep() * 1000L)).array();

            Mac mac = Mac.getInstance(getAlgorithm());
            SecretKeySpec keySpec = new SecretKeySpec(key, getAlgorithm());
            mac.init(keySpec);
            Arrays.fill(key, (byte) 0);

            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0xF;
            int binary = ((hash[offset] & 0x7F) << 24) |
                    ((hash[offset + 1] & 0xFF) << 16) |
                    ((hash[offset + 2] & 0xFF) << 8) |
                    (hash[offset + 3] & 0xFF);

            int otp = binary % (int) Math.pow(10, getDigits());

            Arrays.fill(hash, (byte) 0);

            return String.format("%0" + getDigits() + "d", otp);

        } catch (Exception e) {
            Log.e("TAG", "", e);
            return null;
        }
    }
}
