package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.module.RegenerationOperations;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class RegenerationEvents {
    private static final int REGENERATION_INTERVAL = 20;

    private RegenerationEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()
                || player.tickCount % REGENERATION_INTERVAL != 0
                || player.isDeadOrDying()
                || player.getHealth() >= player.getMaxHealth()) {
            return;
        }

        ExoskeletonAccess.findContext(player)
                .ifPresent(context -> {
                    double healthPerSecond =
                            RegenerationOperations.calculateHealthPerSecond(
                                    context.data(),
                                    context.poweredModules()
                            );

                    if (healthPerSecond > 0.0D) {
                        player.heal(
                                (float) healthPerSecond
                        );
                    }
                });
    }
}
