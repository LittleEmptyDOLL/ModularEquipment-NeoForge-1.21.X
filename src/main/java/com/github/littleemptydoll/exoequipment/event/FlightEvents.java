package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.module.FlightOperations;
import com.github.littleemptydoll.exoequipment.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class FlightEvents {
    private static final String FLIGHT_MARKER =
            "exoequipment_flight";
    private static final String PREVIOUS_MAYFLY =
            "exoequipment_flight_previous_mayfly";
    private static final String PREVIOUS_FLYING =
            "exoequipment_flight_previous_flying";

    private FlightEvents() {}

    public static void markActive(Player player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        CompoundTag persistentData =
                player.getPersistentData();
        boolean alreadyActive =
                persistentData.getBoolean(FLIGHT_MARKER);

        if (!alreadyActive) {
            persistentData.putBoolean(
                    PREVIOUS_MAYFLY,
                    player.getAbilities().mayfly
            );
            persistentData.putBoolean(
                    PREVIOUS_FLYING,
                    player.getAbilities().flying
            );
        }

        persistentData.putBoolean(
                FLIGHT_MARKER,
                true
        );

        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.onUpdateAbilities();

        if (!alreadyActive && !player.level().isClientSide()) {
            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    ModSounds.FLIGHT_ON.get(),
                    SoundSource.PLAYERS,
                    0.9F,
                    1.0F
            );
        }
    }

    public static boolean isActive(Player player) {
        return player.getPersistentData()
                .getBoolean(FLIGHT_MARKER);
    }

    public static void markInactive(Player player) {
        CompoundTag persistentData =
                player.getPersistentData();

        if (!persistentData.getBoolean(FLIGHT_MARKER)) {
            return;
        }

        boolean previousMayfly =
                persistentData.getBoolean(
                        PREVIOUS_MAYFLY
                );
        boolean previousFlying =
                persistentData.getBoolean(
                        PREVIOUS_FLYING
                );

        clearOwnership(persistentData);

        if (!player.isCreative()
                && !player.isSpectator()) {
            player.getAbilities().mayfly =
                    previousMayfly;
            player.getAbilities().flying =
                    previousFlying;
            player.onUpdateAbilities();
        }

        if (!player.level().isClientSide()) {
            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    ModSounds.FLIGHT_OFF.get(),
                    SoundSource.PLAYERS,
                    0.85F,
                    1.0F
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(
            PlayerTickEvent.Post event
    ) {
        Player player = event.getEntity();

        if (player.level().isClientSide()
                || !isActive(player)) {
            return;
        }

        if (player.isCreative()
                || player.isSpectator()) {
            clearOwnership(
                    player.getPersistentData()
            );
            return;
        }

        var context =
                ExoskeletonAccess.findContext(player)
                        .orElse(null);

        if (context == null) {
            markInactive(player);
            return;
        }

        ExoskeletonData data = context.data();

        if (!FlightOperations.hasActive(data)
                || !ExoskeletonState.canActivate(data)) {
            markInactive(player);
        }
    }

    @SubscribeEvent
    public static void onClone(
            PlayerEvent.Clone event
    ) {
        CompoundTag originalData =
                event.getOriginal()
                        .getPersistentData();

        if (!originalData.getBoolean(FLIGHT_MARKER)) {
            return;
        }

        boolean previousMayfly =
                originalData.getBoolean(
                        PREVIOUS_MAYFLY
                );
        boolean previousFlying =
                originalData.getBoolean(
                        PREVIOUS_FLYING
                );

        Player player = event.getEntity();
        clearOwnership(player.getPersistentData());

        if (!player.isCreative()
                && !player.isSpectator()) {
            player.getAbilities().mayfly =
                    previousMayfly;
            player.getAbilities().flying =
                    previousFlying;
            player.onUpdateAbilities();
        }
    }

    private static void clearOwnership(
            CompoundTag persistentData
    ) {
        persistentData.remove(FLIGHT_MARKER);
        persistentData.remove(PREVIOUS_MAYFLY);
        persistentData.remove(PREVIOUS_FLYING);
    }
}
