package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record EffectsProperties(
        Map<ResourceLocation, Integer> effects
) {
    public static final Codec<EffectsProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.unboundedMap(
                                    ResourceLocation.CODEC,
                                    Codec.INT
                            )
                            .fieldOf("effects")
                            .forGetter(EffectsProperties::effects)
                    ).apply(instance, EffectsProperties::new)
            );

    public EffectsProperties {
        effects = Map.copyOf(effects);
        for (Map.Entry<ResourceLocation, Integer> entry : effects.entrySet()) {
            if (entry.getValue() < 0) {
                throw new IllegalArgumentException(
                        "Effect amplifier must be non-negative: " + entry.getKey()
                );
            }
        }
    }
}
