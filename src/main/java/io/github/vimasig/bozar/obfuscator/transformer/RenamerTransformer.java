package io.github.vimasig.bozar.obfuscator.transformer;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.utils.ASMUtils;
import io.github.vimasig.bozar.obfuscator.utils.StringUtils;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RenamerTransformer extends ClassTransformer {

    public RenamerTransformer(Bozar bozar, boolean enabled) {
        super(bozar, enabled);
    }

    protected final HashMap<String, String> map = new HashMap<>();
    protected int index = 0;

    protected String registerMap(String key) {
        var str = switch (this.getBozar().getConfig().getOptions().getRename()) {
            case ALPHABET -> StringUtils.getAlphabetCombinations().get(index);
            case INVISIBLE -> String.valueOf((char)(index + '\u3050'));
            default -> throw new IllegalStateException("transformClass called while rename is disabled, this shouldn't happen");
        };
        map.put(key, str); index++;
        return str;
    }

    protected boolean isMapRegistered(String key) {
        return map.get(key) != null;
    }

    protected void registerMap(String key, String value) {
        map.put(key, value);
    }

    public HashMap<String, String> getMap() {
        return map;
    }
}
