package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.module.EffectsOperations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class EffectsEvents {
    private static final String PERSISTENT_EFFECTS_KEY =
            ExoEquipment.MODID + ":owned_module_effects";

    private static final Map<Player, Map<ResourceLocation, Integer>> APPLIED =
            Collections.synchronizedMap(new WeakHashMap<>());

    private EffectsEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }

        Map<ResourceLocation, Integer> previous = APPLIED.computeIfAbsent(
                player,
                EffectsEvents::loadOwnedEffects
        );

        Map<ResourceLocation, Integer> previousSnapshot;
        synchronized (previous) {
            previousSnapshot = Map.copyOf(previous);
        }

        var context = ExoskeletonAccess.findContext(player).orElse(null);
        Map<ResourceLocation, Integer> appliedNow;

        if (context == null) {
            EffectsOperations.clear(player, previousSnapshot);
            appliedNow = Map.of();
        } else {
            appliedNow = EffectsOperations.reconcile(
                    player,
                    context.data(),
                    context.poweredModules(),
                    previousSnapshot
            );
        }

        synchronized (previous) {
            previous.clear();
            previous.putAll(appliedNow);
        }

        saveOwnedEffects(player, appliedNow);
    }

    private static Map<ResourceLocation, Integer> loadOwnedEffects(
            Player player
    ) {
        CompoundTag persistentData = player.getPersistentData();

        if (!persistentData.contains(
                PERSISTENT_EFFECTS_KEY,
                Tag.TAG_COMPOUND
        )) {
            return new HashMap<>();
        }

        CompoundTag effectsTag = persistentData.getCompound(
                PERSISTENT_EFFECTS_KEY
        );
        Map<ResourceLocation, Integer> result = new HashMap<>();

        for (String key : effectsTag.getAllKeys()) {
            ResourceLocation effectId = ResourceLocation.tryParse(key);

            if (effectId == null
                    || !effectsTag.contains(key, Tag.TAG_INT)) {
                continue;
            }

            result.put(effectId, effectsTag.getInt(key));
        }

        return result;
    }

    private static void saveOwnedEffects(
            Player player,
            Map<ResourceLocation, Integer> effects
    ) {
        CompoundTag persistentData = player.getPersistentData();

        if (effects.isEmpty()) {
            persistentData.remove(PERSISTENT_EFFECTS_KEY);
            return;
        }

        CompoundTag effectsTag = new CompoundTag();

        effects.forEach((effectId, amplifier) ->
                effectsTag.putInt(
                        effectId.toString(),
                        amplifier
                )
        );

        persistentData.put(PERSISTENT_EFFECTS_KEY, effectsTag);
    }
}
