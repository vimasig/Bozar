package com.vimasig.bozar.obfuscator.utils.model;

import com.vimasig.bozar.obfuscator.utils.BozarUtils;

public enum BozarMessage {

    TITLE("Bozar Java Bytecode Obfuscator"),
    VERSION_TEXT(TITLE.toString() + " v" + BozarUtils.getVersion()),
    WATERMARK("BOZAR" + BozarUtils.getVersion());

    private final String message;
    BozarMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return this.message;
    }
}
