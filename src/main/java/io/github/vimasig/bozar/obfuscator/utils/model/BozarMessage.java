package io.github.vimasig.bozar.obfuscator.utils.model;

import io.github.vimasig.bozar.obfuscator.utils.BozarUtils;

import javax.swing.*;

public enum BozarMessage {

    TITLE("Bozar Java Bytecode Obfuscator"),
    VERSION_TEXT(TITLE.toString() + " v" + BozarUtils.getVersion()),

    // Update checker messages
    NEW_UPDATE_AVAILABLE("New update is available: v"),
    CANNOT_CHECK_UPDATE("Cannot check the latest version." + System.lineSeparator() + "Connection failed."),
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
