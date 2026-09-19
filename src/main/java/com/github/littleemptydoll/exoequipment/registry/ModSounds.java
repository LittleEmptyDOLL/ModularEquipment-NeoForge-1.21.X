package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModSounds {
    private ModSounds() {}

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, ExoEquipment.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> SHIELD_HIT =
            SOUND_EVENTS.register(
                    "shield_hit",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(
                                    ExoEquipment.MODID,
                                    "shield_hit"
                            )
                    )
            );

    public static void register(net.neoforged.bus.api.IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
