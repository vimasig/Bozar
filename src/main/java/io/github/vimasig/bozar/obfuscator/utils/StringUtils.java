package io.github.vimasig.bozar.obfuscator.utils;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StringUtils {

    private static final StringBuilder sb = new StringBuilder();
    private static List<String> strings = new ArrayList<>();
    private static void generateCombinations(String dictionary, int maxIndex, int index)
    {
        if(index == maxIndex)
            strings.add(sb.toString());
        else dictionary.chars().forEach(value -> {
            sb.append((char)value);
            generateCombinations(dictionary, maxIndex, index + 1);
            sb.deleteCharAt(sb.length() - 1);
        });
    }

    private static List<String> alphabetCombinations = null;
    public static List<String> getAlphabetCombinations() {
        if(alphabetCombinations == null) {
            strings = new ArrayList<>();
            for (int i = 1; i <= 3; i++)
                generateCombinations(getAlphabet(), i, 0);
            alphabetCombinations = Collections.unmodifiableList(strings);
        } return alphabetCombinations;
    }

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
