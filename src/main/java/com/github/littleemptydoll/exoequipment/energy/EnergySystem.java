package com.github.littleemptydoll.exoequipment.energy;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

public record EnergySystem(
        ResourceLocation definitionId
) {
    public static final Codec<EnergySystem> CODEC =
            ResourceLocation.CODEC.xmap(
                    EnergySystem::new,
                    EnergySystem::definitionId
            );
}
