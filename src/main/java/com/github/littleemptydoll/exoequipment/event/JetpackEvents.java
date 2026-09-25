package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.module.JetpackOperations;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class JetpackEvents {

    private JetpackEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        var context = ExoskeletonAccess.findContext(player).orElse(null);
        if (context == null || context.poweredModules().isEmpty()) {
            return;
        }

        var data = context.data();
        var updatedData = JetpackOperations.tick(
                player,
                data,
                context.poweredModules()
        );

        if (!updatedData.equals(data)) {
            context.stack().set(
                    ModDataComponents.EXOSKELETON_DATA.get(),
                    updatedData
            );
        }

        if (!player.level().isClientSide()
                && player instanceof ServerPlayer serverPlayer
                && !updatedData.equals(data)) {
            serverPlayer.connection.send(
                    new ClientboundSetEntityMotionPacket(serverPlayer)
            );
        }
    }
}
