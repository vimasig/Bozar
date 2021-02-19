package com.vimasig.bozar.obfuscator.utils;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StringUtils {

    public static String getAlphabet() {
        return IntStream.rangeClosed('A', 'z')
                .mapToObj(operand -> (char) operand)
                .filter(Character::isLetter)
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    public static String getConvertedSize(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %cB", value / 1024.0, ci.current());
    }
}
