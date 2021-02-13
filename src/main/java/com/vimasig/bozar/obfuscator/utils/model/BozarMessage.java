package com.vimasig.bozar.obfuscator.utils.model;

import com.vimasig.bozar.obfuscator.utils.BozarUtils;

import javax.swing.*;

public enum BozarMessage {

    TITLE("Bozar Java Bytecode Obfuscator"),
    VERSION_TEXT(TITLE.toString() + " v" + BozarUtils.getVersion()),
    WATERMARK("BOZAR" + BozarUtils.getVersion()),

    CANNOT_OPEN_URL("Cannot open URL. %s, is not supported in your platform.");

    private final String message;
    BozarMessage(String message) {
        this.message = message;
    }

    public void showError(Object... args) {
        JOptionPane.showMessageDialog(null, String.format(this.message, args), BozarMessage.VERSION_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public String toString() {
        return this.message;
    }
}
