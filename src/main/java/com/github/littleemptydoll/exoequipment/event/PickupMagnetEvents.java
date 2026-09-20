package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.PickupMagnetOperations;
import com.github.littleemptydoll.exoequipment.module.PickupMagnetProperties;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class PickupMagnetEvents {
    private static final int INTERVAL = 1;

    private PickupMagnetEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.tickCount % INTERVAL != 0) {
            return;
        }

        Optional<ItemStack> stack = findExoskeleton(player);
        if (stack.isEmpty()) {
            return;
        }

        ItemStack exoskeleton = stack.get();
        ExoskeletonRuntimeState runtime =
                exoskeleton.getOrDefault(
                        ModDataComponents.EXOSKELETON_RUNTIME.get(),
                        ExoskeletonRuntimeState.empty()
                );

        PickupMagnetOperations.findProperties(
                ExoskeletonItem.getData(exoskeleton),
                runtime.poweredModules()
        ).ifPresent(properties -> pullNearby(player, properties));
    }

    private static void pullNearby(
            ServerPlayer player,
            PickupMagnetProperties properties
    ) {
        double radius = properties.radius();
        var box = player.getBoundingBox().inflate(radius);

        if (properties.mode().acceptsItems()) {
            for (ItemEntity item : player.level().getEntitiesOfClass(ItemEntity.class, box)) {
                PickupMagnetOperations.pull(item, player, radius);
            }
        }

        if (properties.mode().acceptsExperience()) {
            for (ExperienceOrb orb : player.level().getEntitiesOfClass(ExperienceOrb.class, box)) {
                PickupMagnetOperations.pull(orb, player, radius);
            }
        }
    }

    private static Optional<ItemStack> findExoskeleton(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(curios ->
                        curios.findFirstCurio(
                                stack -> stack.getItem() instanceof ExoskeletonItem
                        )
                )
                .map(result -> result.stack());
    }
}
