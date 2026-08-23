package com.github.littleemptydoll.exoequipment.util;

public class NameUtils {
    private NameUtils() {
    }

    public static String toDisplayName(String name) {
        String[] words = name.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return result.toString().trim();
    }
}