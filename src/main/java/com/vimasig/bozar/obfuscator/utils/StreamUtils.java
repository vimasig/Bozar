package com.vimasig.bozar.obfuscator.utils;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StreamUtils {

    public static String getHash(InputStream is) throws NoSuchAlgorithmException, IOException {
        MessageDigest digset = MessageDigest.getInstance("SHA-1");

        byte[] byteArray = new byte[4096];
        int bytesCount;
        while ((bytesCount = is.read(byteArray)) != -1) {
            digset.update(byteArray, 0, bytesCount);
        }

        is.close();

        byte[] bytes = digset.digest();
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    public static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[0x1000];
        int read;
        while((read = in.read(buffer)) != -1)
            out.write(buffer, 0, read);
        return out.toByteArray();
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        int read;
        byte[] buffer = new byte[0x1000];
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
