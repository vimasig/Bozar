package io.github.vimasig.bozar.obfuscator.transformer;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
            case IlIlIlIlIl -> getRandomIl(200);
            default -> throw new IllegalStateException("transformClass called while rename is disabled, this shouldn't happen");
        };
        map.put(key, str); index++;
        return str;
    }

    private final List<String> IlList = new ArrayList<>();
    private String getRandomIl(int length) {
        String s;
        do {
            s = IntStream.range(0, length)
                    .mapToObj(i -> (ThreadLocalRandom.current().nextBoolean()) ? "I" : "l")
                    .collect(Collectors.joining());
        } while (IlList.contains(s));
        IlList.add(s);
        return s;
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
