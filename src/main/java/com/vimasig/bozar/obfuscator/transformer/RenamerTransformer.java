package com.vimasig.bozar.obfuscator.transformer;

import com.vimasig.bozar.obfuscator.Bozar;
import com.vimasig.bozar.obfuscator.utils.StringUtils;

import java.util.HashMap;

public class RenamerTransformer extends ClassTransformer {

    public RenamerTransformer(Bozar bozar, boolean enabled) {
        super(bozar, enabled);
    }

    protected final HashMap<String, String> map = new HashMap<>();
    protected int index = 0;

    protected void registerMap(String key) {
        map.put(key, switch (this.getBozar().getConfig().getOptions().getRename()) {
            case ALPHABET -> StringUtils.getAlphabetCombinations().get(index);
            case INVISIBLE -> String.valueOf((char)(index + '\u3050'));
            default -> throw new IllegalStateException("transformClass called while rename is disabled, this shouldn't happen");
        }); index++;
    }
}
