package com.github.littleemptydoll.exoequipment.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class AttributeNameUtils {
    private AttributeNameUtils() {}

    public static Component getName(ResourceLocation id) {
        if (id != null) {
            var holder = BuiltInRegistries.ATTRIBUTE.getHolder(id).orElse(null);

            if (holder != null) {
                return Component.translatable(
                        holder.value().getDescriptionId()
                );
            }
        }

        return Component.literal(fallbackName(id));
    }

    private static String fallbackName(ResourceLocation id) {
        if (id == null) {
            return "Unknown Attribute";
        }

        String name = id.getPath();
        int separator = name.lastIndexOf('.');

        if (separator >= 0) {
            name = name.substring(separator + 1);
        }

        return NameUtils.toDisplayName(name);
    }
}
