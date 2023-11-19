package com.minosapps.aiwp;

import java.util.Arrays;

public class EnumUtils {

    public static String toFormattedString(Enum<?> enumValue) {
        String name = enumValue.name().replace('_', ' ').toLowerCase();
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            result.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }
        return result.toString().trim();
    }

    public static <E extends Enum<E>> String[] getFormattedNames(Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(EnumUtils::toFormattedString)
                .toArray(String[]::new);
    }
}