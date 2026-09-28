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

    public static final DeferredHolder<SoundEvent, SoundEvent> VISOR_ON =
            register("visor_on");
    public static final DeferredHolder<SoundEvent, SoundEvent> VISOR_OFF =
            register("visor_off");
    public static final DeferredHolder<SoundEvent, SoundEvent> JETPACK_LOOP =
            register("jetpack_loop");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, name)
        ));
    }

    public static void register(net.neoforged.bus.api.IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
