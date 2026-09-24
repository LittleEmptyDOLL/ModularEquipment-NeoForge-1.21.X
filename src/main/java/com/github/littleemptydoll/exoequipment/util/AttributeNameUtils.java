package com.github.littleemptydoll.exoequipment.util;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class AttributeNameUtils {
    private AttributeNameUtils() {}

    public static Component getName(ResourceLocation id) {
        String name = id.toString();
        int separator = Math.max(name.lastIndexOf('.'), name.lastIndexOf(':'));

        if (separator >= 0) {
            name = name.substring(separator + 1);
        }

        return Component.literal(NameUtils.toDisplayName(name));
    }
}
