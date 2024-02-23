package io.github.vimasig.bozar.obfuscator.utils;

import com.google.gson.annotations.SerializedName;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarMessage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
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

    public static String getSerializedName(Enum<?> en) {
        try {
            return en.getClass().getField(en.name()).getAnnotation(SerializedName.class).value();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }
}
