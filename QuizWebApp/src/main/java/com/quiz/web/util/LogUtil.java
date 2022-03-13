package com.quiz.web.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class LogUtil {

    private static Cipher sEncoder = null;
    private static Cipher sDecoder = null;
    private static String TAG = "LogUtil-AESKey16";

    // Private Constructor
    private LogUtil() {
    }

    // print line
    public static void log(String tag, String log) {
        System.out.println(tag + " : " + log);
    }

    // AES encrypt
    public static String encrypt(String msg) {
        String result = null;
        try {
            byte[] bytes = msg.getBytes(WebUtil.UTF8);
            bytes = getEncoder().doFinal(bytes);
            result = Base64.getUrlEncoder().encodeToString(bytes);
        } catch (GeneralSecurityException e) {
            log(TAG, "encrypt : " + e);
        } catch (IOException e) {
            log(TAG, "encrypt : " + e);
        }
        return result;
    }

    // AES decrypt
    public static String decrypt(String msg) {
        String result = null;
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(msg);;
            bytes = getDecoder().doFinal(bytes);
            result = new String(bytes, WebUtil.UTF8);
        } catch (GeneralSecurityException e) {
            log(TAG, "decrypt : " + e);
        } catch (IOException e) {
            log(TAG, "decrypt : " + e);
        }
        return result;
    }

    // Get encoder
    private static Cipher getEncoder() throws GeneralSecurityException {
        if (sEncoder == null) {
            sEncoder = Cipher.getInstance("AES/ECB/PKCS5Padding");
            byte[] bytes = TAG.getBytes();
            SecretKey key = new SecretKeySpec(bytes, "AES");
            sEncoder.init(Cipher.ENCRYPT_MODE, key);
        }
        return sEncoder;
    }

    // Get decoder
    private static Cipher getDecoder() throws GeneralSecurityException {
        if (sDecoder == null) {
            sDecoder = Cipher.getInstance("AES/ECB/PKCS5Padding");
            byte[] bytes = TAG.getBytes();
            SecretKey key = new SecretKeySpec(bytes, "AES");
            sDecoder.init(Cipher.DECRYPT_MODE, key);
        }
        return sDecoder;
    }

}
