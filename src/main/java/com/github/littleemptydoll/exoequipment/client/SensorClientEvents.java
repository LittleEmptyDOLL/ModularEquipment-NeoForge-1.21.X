package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.SensorOperations;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@EventBusSubscriber(
        modid = ExoEquipment.MODID,
        value = Dist.CLIENT
)
public final class SensorClientEvents {
    private static final Set<Integer> DETECTED_ENTITIES = new HashSet<>();
    private static final Set<Integer> TEMPORARY_GLOWING_ENTITIES = new HashSet<>();

    private SensorClientEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (Minecraft.getInstance().player != player) {
            return;
        }

        for (int entityId : TEMPORARY_GLOWING_ENTITIES) {
            Entity entity = player.level().getEntity(entityId);

            if (entity != null && entity.hasGlowingTag()) {
                entity.setGlowingTag(false);
            }
        }

        TEMPORARY_GLOWING_ENTITIES.clear();
        DETECTED_ENTITIES.clear();

        Optional<ItemStack> stack = findExoskeleton(player);

        if (stack.isEmpty()) {
            return;
        }

        ExoskeletonRuntimeState runtime =
                stack.get().get(ModDataComponents.EXOSKELETON_RUNTIME.get());

        if (runtime == null) {
            runtime = ExoskeletonRuntimeState.empty();
        }

        SensorOperations.detectEntities(
                player,
                ExoskeletonItem.getData(stack.get()),
                runtime.poweredModules()
        ).forEach(detected -> {
            Entity entity = detected.entity();

            DETECTED_ENTITIES.add(entity.getId());

            if (!entity.hasGlowingTag()) {
                entity.setGlowingTag(true);
                TEMPORARY_GLOWING_ENTITIES.add(entity.getId());
            }
        });
    }

    private static Optional<ItemStack> findExoskeleton(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(curios ->
                        curios.findFirstCurio(
                                stack -> stack.getItem() instanceof ExoskeletonItem
                        )
                )
                .map(result -> result.stack());
    }
}
