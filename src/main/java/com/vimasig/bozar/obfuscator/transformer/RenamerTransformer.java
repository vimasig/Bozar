package com.vimasig.bozar.obfuscator.transformer;

import com.vimasig.bozar.obfuscator.Bozar;

import java.util.HashMap;

public class RenamerTransformer extends ClassTransformer {

    public RenamerTransformer(Bozar bozar) {
        super(bozar);
    }

    public final HashMap<String, String> map = new HashMap<>();
}
