package com.vimasig.bozar.obfuscator.utils;

import java.io.*;

public class StreamUtils {

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
