package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record AttributeProperties(
        Map<ResourceLocation, AttributeModifierProperties> modifiers
) {
    public static final Codec<AttributeProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.unboundedMap(
                                            ResourceLocation.CODEC,
                                            AttributeModifierProperties.CODEC
                                    )
                                    .optionalFieldOf("attributes", Map.of())
                                    .forGetter(AttributeProperties::modifiers)
                    ).apply(instance, AttributeProperties::new)
            );

    public AttributeProperties {
        modifiers = Map.copyOf(modifiers);

        for (Map.Entry<ResourceLocation, AttributeModifierProperties> entry : modifiers.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new IllegalArgumentException("Attribute definitions must not contain null entries");
            }
        }
    }
}
