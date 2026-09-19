package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;

public record NightVisionProperties() {
    public static final Codec<NightVisionProperties> CODEC =
            Codec.unit(new NightVisionProperties());
}
