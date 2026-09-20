package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record AttributeModifierProperties(
        double amount,
        AttributeModifier.Operation operation
) {
    public static final Codec<AttributeModifierProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .fieldOf("amount")
                                    .forGetter(AttributeModifierProperties::amount),
                            AttributeModifier.Operation.CODEC
                                    .fieldOf("operation")
                                    .forGetter(AttributeModifierProperties::operation)
                    ).apply(instance, AttributeModifierProperties::new)
            );

    public AttributeModifierProperties {
        if (!Double.isFinite(amount)) {
            throw new IllegalArgumentException("Attribute modifier amount must be finite");
        }
        if (operation == null) {
            throw new IllegalArgumentException("Attribute modifier operation must not be null");
        }
    }
}
