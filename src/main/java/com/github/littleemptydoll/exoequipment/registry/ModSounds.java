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
    public static final DeferredHolder<SoundEvent, SoundEvent> SHIELD_BREAK =
            register("shield_break");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHIELD_REACTIVATE =
            register("shield_reactivate");

    public static final DeferredHolder<SoundEvent, SoundEvent> VISOR_ON =
            register("visor_on");
    public static final DeferredHolder<SoundEvent, SoundEvent> VISOR_OFF =
            register("visor_off");
    public static final DeferredHolder<SoundEvent, SoundEvent> JETPACK_LOOP =
            register("jetpack_loop");

    public static final DeferredHolder<SoundEvent, SoundEvent> BLINK_ACTIVATE =
            register("blink_activate");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLOAKING_ON =
            register("cloaking_on");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLOAKING_OFF =
            register("cloaking_off");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLIGHT_ON =
            register("flight_on");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLIGHT_OFF =
            register("flight_off");
    public static final DeferredHolder<SoundEvent, SoundEvent> LASER_DEFENSE_ON =
            register("laser_defense_on");
    public static final DeferredHolder<SoundEvent, SoundEvent> LASER_DEFENSE_OFF =
            register("laser_defense_off");
    public static final DeferredHolder<SoundEvent, SoundEvent> DISCHARGE_DEFENSE_ON =
            register("discharge_defense_on");
    public static final DeferredHolder<SoundEvent, SoundEvent> DISCHARGE_DEFENSE_OFF =
            register("discharge_defense_off");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, name)
        ));
    }

    public static void register(net.neoforged.bus.api.IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
