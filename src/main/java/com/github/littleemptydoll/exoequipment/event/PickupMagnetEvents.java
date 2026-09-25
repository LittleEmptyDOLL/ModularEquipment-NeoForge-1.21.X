package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.module.PickupMagnetOperations;
import com.github.littleemptydoll.exoequipment.module.PickupMagnetProperties;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class PickupMagnetEvents {
    private static final int INTERVAL = 1;

    private PickupMagnetEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.isCrouching()
                || player.tickCount % INTERVAL != 0) {
            return;
        }

        ExoskeletonAccess.findContext(player)
                .flatMap(context ->
                        PickupMagnetOperations.findProperties(
                                context.data(),
                                context.poweredModules()
                        )
                )
                .ifPresent(properties ->
                        pullNearby(
                                player,
                                properties
                        )
                );
    }

    private static void pullNearby(
            ServerPlayer player,
            PickupMagnetProperties properties
    ) {
        double radius = properties.radius();
        var box = player.getBoundingBox().inflate(radius);

        if (properties.mode().acceptsItems()) {
            for (ItemEntity item : player.level()
                    .getEntitiesOfClass(ItemEntity.class, box)) {
                PickupMagnetOperations.pull(
                        item,
                        player,
                        radius
                );
            }
        }

        if (properties.mode().acceptsExperience()) {
            for (ExperienceOrb orb : player.level()
                    .getEntitiesOfClass(ExperienceOrb.class, box)) {
                PickupMagnetOperations.pull(
                        orb,
                        player,
                        radius
                );
            }
        }
    }
}
