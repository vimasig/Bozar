package io.github.vimasig.bozar.obfuscator.utils.model;

import io.github.vimasig.bozar.obfuscator.utils.BozarUtils;

import javax.swing.*;

public enum BozarMessage {

    TITLE("Bozar Java Bytecode Obfuscator"),
    VERSION_TEXT(TITLE + " v" + BozarUtils.getVersion());
    private final String message;
    BozarMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return this.message;
    }
}
