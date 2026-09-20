package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import com.github.littleemptydoll.exoequipment.client.CloakingClientState;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;

@EventBusSubscriber(
        modid = ExoEquipment.MODID,
        value = Dist.CLIENT
)
public final class CloakingClientEvents {
    private CloakingClientEvents() {}

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (CloakingClientState.isActive(event.getEntity().getId())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (CloakingClientState.isActive(event.getEntity().getId())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        if (CloakingClientState.isActive(event.getPlayer().getId())) {
            event.setCanceled(true);
        }
    }
}
