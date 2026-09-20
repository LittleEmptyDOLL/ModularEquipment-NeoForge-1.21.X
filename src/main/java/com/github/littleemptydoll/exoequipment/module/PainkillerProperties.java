package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;

public record PainkillerProperties() {
    public static final Codec<PainkillerProperties> CODEC =
            Codec.unit(new PainkillerProperties());
}
