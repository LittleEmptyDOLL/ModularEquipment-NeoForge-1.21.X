package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record AttributeCondition(
        Type type,
        Optional<Double> value
) {
    public static final Codec<AttributeCondition> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Type.CODEC
                                    .fieldOf("condition")
                                    .forGetter(AttributeCondition::type),
                            Codec.DOUBLE
                                    .optionalFieldOf("value")
                                    .forGetter(AttributeCondition::value)
                    ).apply(instance, AttributeCondition::new)
            );

    public AttributeCondition {
        if (type == null) {
            throw new IllegalArgumentException("Attribute condition type must not be null");
        }

        if (value == null) {
            throw new IllegalArgumentException("Attribute condition value must not be null");
        }

        if (value.isPresent() && !Double.isFinite(value.get())) {
            throw new IllegalArgumentException("Attribute condition value must be finite");
        }

        boolean threshold = type == Type.HEALTH_BELOW || type == Type.HEALTH_ABOVE;

        if (threshold) {
            if (value.isEmpty() || value.get() < 0.0D || value.get() > 1.0D) {
                throw new IllegalArgumentException(
                        "Health condition value must be between 0 and 1"
                );
            }
        } else if (value.isPresent()) {
            throw new IllegalArgumentException(
                    "Condition " + type.getSerializedName() + " does not accept a value"
            );
        }
    }

    public enum Type {
        DAY,
        NIGHT,
        IN_WATER,
        UNDER_WATER,
        ON_FIRE,
        ON_GROUND,
        SNEAKING,
        SPRINTING,
        RIDDEN,
        HEALTH_BELOW,
        HEALTH_ABOVE;

        public static final Codec<Type> CODEC =
                Codec.STRING.xmap(
                        Type::fromSerializedName,
                        Type::getSerializedName
                );

        private String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }

        private static Type fromSerializedName(String name) {
            for (Type type : values()) {
                if (type.getSerializedName().equals(name)) {
                    return type;
                }
            }

            throw new IllegalArgumentException("Unknown attribute condition: " + name);
        }
    }
}
