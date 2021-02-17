package com.vimasig.bozar.obfuscator.utils;

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
}
