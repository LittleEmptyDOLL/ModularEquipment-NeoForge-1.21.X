package com.github.littleemptydoll.exoequipment.compat.apothicattributes;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public final class ApothicAttributesIntegration {
    private ApothicAttributesIntegration() {}

    public static void register(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.register(ApothicCombatEvents.class);
        NeoForge.EVENT_BUS.register(ApothicUtilityEvents.class);
    }
}
