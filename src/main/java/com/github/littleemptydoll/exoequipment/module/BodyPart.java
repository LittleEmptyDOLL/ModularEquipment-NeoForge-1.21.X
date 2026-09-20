package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

public enum BodyPart {
    HEAD,
    RIGHT_ARM,
    LEFT_ARM,
    CHEST,
    RIGHT_LEG,
    RIGHT_FOOT,
    LEFT_LEG,
    LEFT_FOOT;

    public static final Codec<BodyPart> CODEC =
            Codec.STRING.comapFlatMap(
                    value -> {
                        try {
                            return DataResult.success(
                                    BodyPart.valueOf(value.toUpperCase(Locale.ROOT))
                            );
                        } catch (IllegalArgumentException exception) {
                            return DataResult.error(
                                    () -> "Unknown body part: " + value
                            );
                        }
                    },
                    part -> part.name().toLowerCase(Locale.ROOT)
            );

    public static final Codec<Set<BodyPart>> SET_CODEC =
            CODEC.listOf().xmap(Set::copyOf, ArrayList::new);

    /**
     * An empty set means that the property applies to every body part.
     */
    public static boolean applies(Set<BodyPart> bodyParts, BodyPart bodyPart) {
        return bodyParts.isEmpty() || bodyParts.contains(bodyPart);
    }
}
