package com.github.littleemptydoll.exoequipment.controller;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

public record Controller(
        ResourceLocation definitionId
) {
    public static final Codec<Controller> CODEC =
            ResourceLocation.CODEC.xmap(
                    Controller::new,
                    Controller::definitionId
            );
}
