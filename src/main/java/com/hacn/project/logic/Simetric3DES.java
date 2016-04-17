package com.hacn.project.logic;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Created by familia on 17/04/2016.
 */
public class Simetric3DES implements Crypto {


    final static String DIGEST_KEY_PASS = "HG58YZ3CR9";
    final static String SECRET_VALUE_PASS = "DESede";

    final static String CIPHER = "DESede/CBC/PKCS5Padding";
    private long timeMillies;

    @Override
    public byte[] encrypt(byte[] bytes) throws Exception {

        final MessageDigest md = MessageDigest.getInstance("md5");
        final byte[] digestOfPassword = md.digest(DIGEST_KEY_PASS.getBytes());
        final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
        for (int j = 0, k = 16; j < 8; ) {
            keyBytes[k++] = keyBytes[j++];
        }

        final SecretKey key = new SecretKeySpec(keyBytes, SECRET_VALUE_PASS);
        final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        final Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        long t1 = System.currentTimeMillis();
        final byte[] cipherText = cipher.doFinal(bytes);
        long t2 = System.currentTimeMillis();
        timeMillies = t2 - t1;
        // final String encodedCipherText = new sun.misc.BASE64Encoder()
        // .encode(cipherText);

        return cipherText;
    }

    @Override
    public byte[] decrypt(byte[] message) throws Exception {
        final MessageDigest md = MessageDigest.getInstance("md5");
        final byte[] digestOfPassword = md.digest(DIGEST_KEY_PASS
                .getBytes());
        final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
        for (int j = 0, k = 16; j < 8; ) {
            keyBytes[k++] = keyBytes[j++];
        }

        final SecretKey key = new SecretKeySpec(keyBytes, SECRET_VALUE_PASS);
        final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        final Cipher decipher = Cipher.getInstance(CIPHER);
        decipher.init(Cipher.DECRYPT_MODE, key, iv);

        long t1 = System.currentTimeMillis();
        byte[] bytes1 = decipher.doFinal(message);
        long t2 = System.currentTimeMillis();

        timeMillies = t2 - t1;

        return bytes1;

    }

    @Override
    public long getTime() {
        return timeMillies;
    }
}
