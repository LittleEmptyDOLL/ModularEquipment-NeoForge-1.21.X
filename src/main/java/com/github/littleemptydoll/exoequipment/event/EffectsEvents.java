package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.EffectsOperations;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class EffectsEvents {
    private static final Map<Player, Set<ResourceLocation>> APPLIED =
            Collections.synchronizedMap(new WeakHashMap<>());

    private EffectsEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        Map<ResourceLocation, Integer> desired = findEffects(player);

        Set<ResourceLocation> previous = APPLIED.computeIfAbsent(
                player,
                ignored -> new HashSet<>()
        );

        Set<ResourceLocation> previousSnapshot;
        synchronized (previous) {
            previousSnapshot = new HashSet<>(previous);
        }

        for (ResourceLocation effectId : previousSnapshot) {
            if (!desired.containsKey(effectId)) {
                removeEffect(player, effectId);
            }
        }

        Optional<ItemStack> exoskeletonStack = findExoskeleton(player);
        if (exoskeletonStack.isPresent()) {
            EffectsOperations.apply(
                    player,
                    ExoskeletonItem.getData(exoskeletonStack.get()),
                    getPoweredModules(exoskeletonStack.get())
            );
        }

        synchronized (previous) {
            previous.clear();
            previous.addAll(desired.keySet());
        }
    }

    private static Map<ResourceLocation, Integer> findEffects(Player player) {
        Optional<ItemStack> stack = findExoskeleton(player);
        if (stack.isEmpty()) {
            return Map.of();
        }

        return EffectsOperations.collectEffects(
                ExoskeletonItem.getData(stack.get()),
                getPoweredModules(stack.get())
        );
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

    private static void removeEffect(Player player, ResourceLocation effectId) {
        BuiltInRegistries.MOB_EFFECT.getHolder(effectId)
                .ifPresent(player::removeEffect);
    }
}
