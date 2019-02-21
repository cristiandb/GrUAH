package com.gruas.app;

/**
 * Created by Dani on 27/04/2014.
 */
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1 {
    private MessageDigest md;
    private byte[] buffer, digest;
    private String hash = "";

    private String getHash(String message) throws NoSuchAlgorithmException {
        buffer = message.getBytes();
        md = MessageDigest.getInstance("SHA1");
        md.update(buffer);
        digest = md.digest();

        for(byte aux : digest) {
            int b = aux & 0xff;
            if (Integer.toHexString(b).length() == 1) hash += "0";
            hash += Integer.toHexString(b);
        }
        return hash;
    }

    public String encriptar(String pass){
        String sec="rwr24t5yt25y543td32ty6";
        try {
            return this.getHash(this.getHash(pass + sec)+sec);
        }
        catch(NoSuchAlgorithmException e) {
            Log.d("Error encriptacion","Error",e);
        }

        return "0";}
}
