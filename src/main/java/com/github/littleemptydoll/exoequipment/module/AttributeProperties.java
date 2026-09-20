package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record AttributeProperties(
        Map<ResourceLocation, AttributeModifierProperties> attributes
) {
    public static final Codec<AttributeProperties> CODEC =
            Codec.unboundedMap(
                    ResourceLocation.CODEC,
                    AttributeModifierProperties.CODEC
            ).xmap(AttributeProperties::new, AttributeProperties::attributes);

    public AttributeProperties {
        attributes = Map.copyOf(attributes);

        for (Map.Entry<ResourceLocation, AttributeModifierProperties> entry : attributes.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new IllegalArgumentException("Attribute definitions must not contain null entries");
            }
        }
    }
}
