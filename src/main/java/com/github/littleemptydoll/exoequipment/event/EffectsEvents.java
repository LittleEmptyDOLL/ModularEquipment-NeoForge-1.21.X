package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.module.EffectsOperations;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
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

        Map<ResourceLocation, Integer> appliedNow = new HashMap<>();

        ExoskeletonAccess.findContext(player)
                .ifPresent(context ->
                        appliedNow.putAll(
                                EffectsOperations.apply(
                                        player,
                                        context.data(),
                                        context.poweredModules()
                                )
                        )
                );

        Map<ResourceLocation, Integer> previous = APPLIED.computeIfAbsent(
                player,
                ignored -> new HashMap<>()
        );

        synchronized (previous) {
            for (Map.Entry<ResourceLocation, Integer> entry : previous.entrySet()) {
                if (!appliedNow.containsKey(entry.getKey())) {
                    removeEffect(
                            player,
                            entry.getKey(),
                            entry.getValue()
                    );
                }
            }

            previous.clear();
            previous.putAll(appliedNow);
        }
    }

    private static void removeEffect(
            Player player,
            ResourceLocation effectId,
            int amplifier
    ) {
        BuiltInRegistries.MOB_EFFECT.getHolder(effectId).ifPresent(effect -> {
            MobEffectInstance current = player.getEffect(effect);

            if (current != null
                    && current.getAmplifier() == amplifier
                    && current.getDuration() == MobEffectInstance.INFINITE_DURATION) {
                player.removeEffect(effect);
            }
        });
    }
}
