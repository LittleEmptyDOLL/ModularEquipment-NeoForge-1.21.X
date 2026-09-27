package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.module.ThermalVisionOperations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Keeps the world filter in sync with the powered, active thermal module. */
@EventBusSubscriber(modid = ExoEquipment.MODID, value = Dist.CLIENT)
public final class ThermalVisionClientEffect {
    private static final ResourceLocation EFFECT = ResourceLocation.fromNamespaceAndPath(
            ExoEquipment.MODID, "shaders/post/thermal_vision.json");
    private static PostChain ownedEffect;
    private static double visibleRange;
    private static long ticks;
    private static long lastLoadAttempt = -100;

    private ThermalVisionClientEffect() {}

    public static double visibleRange() {
        return visibleRange;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ticks++;
        visibleRange = minecraft.player == null || minecraft.level == null
                ? 0.0D
                : ExoskeletonAccess.findContext(minecraft.player)
                        .map(context -> ThermalVisionOperations.range(
                                context.data(), context.poweredModules()))
                        .orElse(0.0D);

        PostChain current = minecraft.gameRenderer.currentEffect();
        if (visibleRange <= 0.0D) {
            if (ownedEffect != null && current == ownedEffect) {
                minecraft.gameRenderer.shutdownEffect();
            }
            ownedEffect = null;
            lastLoadAttempt = ticks - 100;
            return;
        }

        if (current == ownedEffect && ownedEffect != null) {
            return;
        }

        // Resource reloads can replace the PostChain while thermal vision stays on.
        if (current != null && EFFECT.toString().equals(current.getName())) {
            ownedEffect = current;
            return;
        }

        // Leave another mod's or spectator's post effect alone.
        ownedEffect = null;
        if (current != null || ticks - lastLoadAttempt < 100) {
            return;
        }

        lastLoadAttempt = ticks;
        minecraft.gameRenderer.loadEffect(EFFECT);
        ownedEffect = minecraft.gameRenderer.currentEffect();
    }

    public static boolean isRendering() {
        return visibleRange > 0.0D
                && ownedEffect != null
                && Minecraft.getInstance().gameRenderer.currentEffect() == ownedEffect;
    }
}
