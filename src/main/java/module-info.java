module io.github.vimasig.bozar {
    requires com.google.gson;
    requires commons.cli;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.base;
    requires java.desktop;
    requires java.net.http;
    requires java.scripting;
    requires org.objectweb.asm.commons;
    requires org.objectweb.asm.util;
    requires static lombok;

    opens io.github.vimasig.bozar.obfuscator.utils.model;
    opens io.github.vimasig.bozar.ui;
}