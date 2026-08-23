package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

public record Module(
        ResourceLocation definitionId
) {
    public static final Codec<Module> CODEC =
            ResourceLocation.CODEC.xmap(
                    Module::new,
                    Module::definitionId
            );
}
