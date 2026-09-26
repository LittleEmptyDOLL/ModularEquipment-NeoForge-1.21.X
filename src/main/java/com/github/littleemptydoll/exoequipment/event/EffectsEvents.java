package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.module.EffectsOperations;
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
                ignored -> new HashMap<>()
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
    }
}
