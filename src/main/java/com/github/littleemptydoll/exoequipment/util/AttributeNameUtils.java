package com.github.littleemptydoll.exoequipment.util;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class AttributeNameUtils {
    private AttributeNameUtils() {}

    public static Component getName(ResourceLocation id) {
        return Component.literal(NameUtils.toDisplayName(id.getPath()));
    }
}
