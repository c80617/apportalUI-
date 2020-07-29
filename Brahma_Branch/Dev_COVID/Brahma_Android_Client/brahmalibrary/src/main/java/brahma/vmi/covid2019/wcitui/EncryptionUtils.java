package brahma.vmi.covid2019.wcitui;

import android.util.Base64;
import android.util.Log;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtils {

    private Cipher mCipher;
    private SecretKeySpec mSpecSecretKey;
    private IvParameterSpec mSpecIV;

    /*  private String iv = "4957414F6B524147";//Dummy iv (CHANGE IT!)
        private String secret_key = "764164673344344D55624A6C486E4168";//Dummy secretKey (CHANGE IT!)
    */
    public EncryptionUtils(String secret_key,String iv) {
        //Log.d("test","EncryptionUtils");
        mSpecSecretKey = new SecretKeySpec(secret_key.getBytes(), "AES");
        //mSpecIV = new IvParameterSpec(iv.getBytes());
        try {
            mCipher = Cipher.getInstance("AES/ECB/NoPadding");
            //mCipher = Cipher.getInstance("AES/CBC/NoPadding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public byte[] encrypt(String text) throws Exception {
        if (text == null || text.length() == 0) throw new Exception("Empty string");

        byte[] encrypted = null;

        //mCipher.init(Cipher.ENCRYPT_MODE, mSpecSecretKey, mSpecIV);
        mCipher.init(Cipher.ENCRYPT_MODE, mSpecSecretKey);
        encrypted = mCipher.doFinal(padString(text).getBytes());
        try {
            encrypted = android.util.Base64.encode(encrypted, android.util.Base64.NO_PADDING);
        } catch (NoClassDefFoundError e) {
        }

        return encrypted;
    }

    public byte[] decrypt(String code) throws Exception {
        if (code == null || code.length() == 0) throw new Exception("Empty string");

        byte[] decrypted = null;

        //mCipher.init(Cipher.DECRYPT_MODE, mSpecSecretKey, mSpecIV);
        mCipher.init(Cipher.DECRYPT_MODE, mSpecSecretKey);
        try {
            decrypted = mCipher.doFinal(android.util.Base64.decode(code, Base64.NO_WRAP));
        } catch (NoClassDefFoundError e) {
        }
        return decrypted;
    }



    private static String padString(String source) {
        char paddingChar = ' ';
        int size = 16;// 1 byte = 8 bit, so 16 byte = 128 bit and that is what we need 128bit AES
        int x = source.length() % size;
        int padLength = size - x;

        for (int i = 0; i < padLength; i++) {
            source += paddingChar;
        }

        return source;
    }
}