package com.vimasig.bozar.obfuscator.utils;

import java.io.IOException;
import java.util.Properties;

public class BozarUtils {

    public static String getVersion() {
        try {
            final Properties properties = new Properties();
            properties.load(BozarUtils.class.getResourceAsStream("/bozar.properties"));
            return properties.getProperty("bozar.version");
        } catch (IOException e) {
            e.printStackTrace();
            return "Unknown version";
        }
    }
}
