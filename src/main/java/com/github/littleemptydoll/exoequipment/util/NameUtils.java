package com.github.littleemptydoll.exoequipment.util;

import net.minecraft.resources.ResourceLocation;

public class NameUtils {
    private NameUtils() {
    }

    public static String toDisplayName(ResourceLocation id) {
        return toDisplayName(id.getPath());
    }

    public static String toDisplayName(String value) {
        String[] words = value.toLowerCase().split("_");
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