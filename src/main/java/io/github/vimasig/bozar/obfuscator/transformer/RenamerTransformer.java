package io.github.vimasig.bozar.obfuscator.transformer;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.utils.StringUtils;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class RenamerTransformer extends ClassTransformer {

    protected final HashMap<String, String> map = new HashMap<>();
    protected int index = 0;

    public RenamerTransformer(Bozar bozar, String text, BozarCategory category) {
        super(bozar, text, category);
    }

    protected String registerMap(String key) {
        var str = switch (this.getBozar().getConfig().getOptions().getRename()) {
            case ALPHABET -> StringUtils.getAlphabetCombinations().get(index);
            case INVISIBLE -> String.valueOf((char)(index + '\u3050'));
            case IlIlIlIlIl -> getRandomUniqueIl(400);
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
