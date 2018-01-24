package com.media.interactive.cs3.hdm.interactivemedia.data;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;



/**
 * Created by benny on 31.10.17.
 */

public class Hash {

    /**
     * Hash string sha 256.
     *
     * @param toHash the to hash
     * @return the string
     */
    public static String hashStringSha256(String toHash) {
        String result = null;
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");

            md.update(toHash.getBytes());

            byte[] byteData = md.digest();

            //convert the byte to hex format method 1
            final StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            result = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }
}
