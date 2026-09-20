package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.client.CloakingClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.common.util.TriState;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(
        modid = ExoEquipment.MODID,
        value = Dist.CLIENT
)
public final class CloakingClientEvents {
    private static final Map<Integer, Boolean> RESTORE_INVISIBILITY = new HashMap<>();

    private CloakingClientEvents() {}

    @SubscribeEvent
    public static void onRenderFramePre(RenderFrameEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return;
        }

        RESTORE_INVISIBILITY.clear();

        for (int entityId : CloakingClientState.activeEntities()) {
            Entity entity = minecraft.level.getEntity(entityId);

            if (entity != null && !entity.isInvisible()) {
                RESTORE_INVISIBILITY.put(entityId, false);
                entity.setInvisible(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderFramePost(RenderFrameEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level != null) {
            for (int entityId : RESTORE_INVISIBILITY.keySet()) {
                Entity entity = minecraft.level.getEntity(entityId);

                if (entity != null) {
                    entity.setInvisible(false);
                }
            }
        }

        RESTORE_INVISIBILITY.clear();
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (CloakingClientState.isActive(event.getEntity().getId())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (CloakingClientState.isActive(event.getEntity().getId())) {
            event.setCanRender(TriState.FALSE);
        }
    }

    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        if (CloakingClientState.isActive(event.getPlayer().getId())) {
            event.setCanceled(true);
        }
    }
}
