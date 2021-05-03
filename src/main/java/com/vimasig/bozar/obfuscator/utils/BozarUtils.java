package com.vimasig.bozar.obfuscator.utils;

import com.google.gson.annotations.SerializedName;
import com.vimasig.bozar.obfuscator.utils.model.BozarMessage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

public class BozarUtils {

    private static final String RELEASES_URL = "https://github.com/vimasig/Bozar/releases";
    private static final String POM_URL = "https://raw.githubusercontent.com/vimasig/Bozar/master/pom.xml";

    public static String getLatestVersion() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(POM_URL))
                .build();
        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body().lines()
                .map(String::strip)
                .filter(s -> s.startsWith("<version>") && s.endsWith("</version>"))
                .map(s -> s.substring("<version>".length(), s.indexOf("</version>")))
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Cannot find Bozar version from pom.xml"));
    }

    public static void openDownloadURL() {
        String err = Desktop.isDesktopSupported() ? (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE) ? null : "Browse") : "Desktop";
        if(err != null)
            BozarMessage.CANNOT_OPEN_URL.showError(err);
        else
            try {
                Desktop.getDesktop().browse(URI.create(RELEASES_URL));
            } catch (IOException e) {
                e.printStackTrace();
            }
        System.exit(0);
    }

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
