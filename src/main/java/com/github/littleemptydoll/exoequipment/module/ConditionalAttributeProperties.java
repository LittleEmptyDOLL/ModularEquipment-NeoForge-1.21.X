package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record ConditionalAttributeProperties(
        List<AttributeCondition> conditions,
        AttributeProperties attributes
) {
    public static final Codec<ConditionalAttributeProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            AttributeCondition.CODEC
                                    .listOf()
                                    .fieldOf("conditions")
                                    .forGetter(ConditionalAttributeProperties::conditions),
                            AttributeProperties.CODEC
                                    .fieldOf("attributes")
                                    .forGetter(ConditionalAttributeProperties::attributes)
                    ).apply(instance, ConditionalAttributeProperties::new)
            );

    public ConditionalAttributeProperties {
        if (conditions == null || conditions.isEmpty()) {
            throw new IllegalArgumentException("Conditional attribute conditions must not be empty");
        }

        conditions = List.copyOf(conditions);

        if (conditions.stream().anyMatch(condition -> condition == null)) {
            throw new IllegalArgumentException("Conditional attribute conditions must not contain null entries");
        }

        if (attributes == null) {
            throw new IllegalArgumentException("Conditional attributes must not be null");
        }
    }
}
