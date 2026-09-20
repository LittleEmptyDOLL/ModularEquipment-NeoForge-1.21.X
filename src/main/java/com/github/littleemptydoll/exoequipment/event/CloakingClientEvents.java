package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@EventBusSubscriber(
        modid = ExoEquipment.MODID,
        value = Dist.CLIENT
)
public final class CloakingClientEvents {
    private CloakingClientEvents() {}

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (event.getEntity().isInvisible()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        if (event.getPlayer().isInvisible()) {
            event.setCanceled(true);
        }
    }
}
