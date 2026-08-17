package com.github.littleemptydoll.exoequipment.module;

import net.minecraft.resources.ResourceLocation;

public record ModuleDefinition(
        ResourceLocation id,
        ModuleSize size,
        ModuleCategory category,
        ModuleBranch branch
) {
}
