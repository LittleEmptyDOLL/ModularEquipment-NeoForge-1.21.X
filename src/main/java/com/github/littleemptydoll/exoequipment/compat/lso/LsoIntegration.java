package com.github.littleemptydoll.exoequipment.compat.lso;

import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public final class LsoIntegration {
    private LsoIntegration() {
    }

    public static void register(IEventBus modEventBus) {
        ModModules.registerLsoTestThirst();
        NeoForge.EVENT_BUS.register(LsoThirstEvents.class);
    }
}
