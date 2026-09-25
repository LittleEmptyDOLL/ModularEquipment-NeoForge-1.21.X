package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.module.EmergencyShieldOperations;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class EmergencyShieldEvents {
    private EmergencyShieldEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }

        var context = ExoskeletonAccess.findContext(player).orElse(null);
        if (context == null) {
            return;
        }

        var updatedData =
                EmergencyShieldOperations.tickCooldowns(context.data());

        if (updatedData != context.data()) {
            context.stack().set(
                    ModDataComponents.EXOSKELETON_DATA.get(),
                    updatedData
            );
        }
    }
}
