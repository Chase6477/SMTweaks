package de.jr.smtweaks.util;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * This Util class is from one of my other work-in-progress apps (hopefully cuming soon)
 */

public class CryptoUtil {

    public static final int GCM_TAG_SIZE = 128;
    public static final int GCM_IV_SIZE = 12;
    private static final int BUFFER_SIZE = 1024;


    public static void encrypt(byte[] plainData, SecretKey key, Context fileContext, String fileName) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            final byte[][] cipherText = new byte[1][0];
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText[0] = cipher.doFinal(plainData);
            Arrays.fill(plainData, (byte) 0);
            byte[] fileBytes = new byte[cipherText[0].length + cipher.getIV().length];
            System.arraycopy(cipher.getIV(), 0, fileBytes, 0, cipher.getIV().length);
            System.arraycopy(cipherText[0], 0, fileBytes, cipher.getIV().length, cipherText[0].length);
            writeFile(new File(fileContext.getFilesDir(), fileName), fileBytes);
        } catch (IOException e) {
            Log.e("Crypto", "Error in writing file", e);
        } catch (Exception e) {
            Log.e("Crypto", "Error in encryption", e);
        }
    }

    public static byte[] decrypt(SecretKey key, Context fileContext, String fileName) throws BadPaddingException {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] fileBytes = readFile(new File(fileContext.getFilesDir(), fileName));
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_SIZE, Arrays.copyOfRange(fileBytes, 0, GCM_IV_SIZE));
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            return cipher.doFinal(Arrays.copyOfRange(fileBytes, GCM_IV_SIZE, fileBytes.length));
        } catch (BadPaddingException e) {
            throw e;
        } catch (IOException e) {
            Log.e("Crypto", "Error in writing file", e);
            return null;
        } catch (Exception e) {
            Log.e("Crypto", "Error in decryption");
            return null;
        }
    }

    public static byte[] readFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }

    public static void writeFile(File file, byte[] bytes) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
        }
    }

    public static SecretKey getKeyStoreSecretKey(String alias) {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);

            SecretKey secretKey = (SecretKey) ks.getKey(alias, null);

            if (secretKey == null) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

                KeyGenParameterSpec.Builder keyGen = getBuilder(alias);
                keyGenerator.init(keyGen.build());
                secretKey = keyGenerator.generateKey();
            }
            return secretKey;
        } catch (Exception e) {
            Log.e("Key", "Could not load key from keystore", e);
            return null;
        }
    }

    private static KeyGenParameterSpec.Builder getBuilder(String alias) {

        return new KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE);
    }

    public static class FileNames {
        public static final String ENC_USER_DATA_FILE_NAME = "userData.enc";
        public static final String PLAIN_CALENDAR_TABLE_DATA_FILE_NAME = "calendarTableData.enc";
    }
}
