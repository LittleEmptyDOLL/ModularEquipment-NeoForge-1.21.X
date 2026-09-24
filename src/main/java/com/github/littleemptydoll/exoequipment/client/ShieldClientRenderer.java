package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(
        modid = ExoEquipment.MODID,
        value = Dist.CLIENT
)
public final class ShieldClientRenderer {
    private ShieldClientRenderer() {}

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);

            if (renderer != null) {
                renderer.addLayer(
                        new ShieldRenderLayer(
                                renderer,
                                skin == PlayerSkin.Model.SLIM
                        )
                );
            }
        }
    }
}
