package io.github.vimasig.bozar.obfuscator.utils.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

public enum BozarCategory {
    @SerializedName("Stable") STABLE("Stable obfuscation options. Most options are irreversible.\nA good way to protect & speed up your application."),
    @SerializedName("Advanced") ADVANCED("Advanced obfuscation options. Reversible.\nPowerful protection against newbies."),
    @SerializedName("Watermark") WATERMARK("Different ways to implement watermark to your application.");

    @Getter
    private final String description;

    BozarCategory(String description) {
        this.description = description;
    }
}
