package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.EffectsOperations;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

        Optional<ItemStack> exoskeletonStack = findExoskeleton(player);
        if (exoskeletonStack.isPresent()) {
            appliedNow.putAll(EffectsOperations.apply(
                    player,
                    ExoskeletonItem.getData(exoskeletonStack.get()),
                    getPoweredModules(exoskeletonStack.get())
            ));
        }

        Map<ResourceLocation, Integer> previous = APPLIED.computeIfAbsent(
                player,
                ignored -> new HashMap<>()
        );

        synchronized (previous) {
            for (Map.Entry<ResourceLocation, Integer> entry : previous.entrySet()) {
                if (!appliedNow.containsKey(entry.getKey())) {
                    removeEffect(player, entry.getKey(), entry.getValue());
                }
            }

            previous.clear();
            previous.putAll(appliedNow);
        }
    }

    private static Set<InstalledModuleReference> getPoweredModules(ItemStack stack) {
        ExoskeletonRuntimeState runtime = stack.get(
                ModDataComponents.EXOSKELETON_RUNTIME.get()
        );

        return runtime == null
                ? Set.of()
                : runtime.poweredModules();
    }

    private static Optional<ItemStack> findExoskeleton(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(curios -> curios.findFirstCurio(
                        stack -> stack.getItem() instanceof ExoskeletonItem
                ))
                .map(result -> result.stack());
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
