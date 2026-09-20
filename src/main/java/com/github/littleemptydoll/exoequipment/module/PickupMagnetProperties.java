package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Locale;

public record PickupMagnetProperties(
        double radius,
        Mode mode
) {
    public static final Codec<PickupMagnetProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .fieldOf("radius")
                                    .forGetter(PickupMagnetProperties::radius),
                            Mode.CODEC
                                    .fieldOf("mode")
                                    .forGetter(PickupMagnetProperties::mode)
                    ).apply(instance, PickupMagnetProperties::new)
            );

    public PickupMagnetProperties {
        if (!Double.isFinite(radius) || radius <= 0.0D) {
            throw new IllegalArgumentException("Pickup magnet radius must be greater than 0");
        }
        if (mode == null) {
            throw new IllegalArgumentException("Pickup magnet mode must not be null");
        }
    }

    public enum Mode {
        ITEMS,
        EXPERIENCE,
        BOTH;

        public static final Codec<Mode> CODEC =
                Codec.STRING.xmap(
                        value -> switch (value.toLowerCase(Locale.ROOT)) {
                            case "items" -> ITEMS;
                            case "experience" -> EXPERIENCE;
                            case "both" -> BOTH;
                            default -> throw new IllegalArgumentException(
                                    "Unknown pickup magnet mode: " + value
                            );
                        },
                        mode -> mode.name().toLowerCase(Locale.ROOT)
                );

        public boolean acceptsItems() {
            return this == ITEMS || this == BOTH;
        }

        public boolean acceptsExperience() {
            return this == EXPERIENCE || this == BOTH;
        }
    }
}
