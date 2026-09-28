package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = ExoEquipment.MODID, value = Dist.CLIENT)
public final class JetpackClientSound {
    private static JetpackSoundInstance loop;

    private JetpackClientSound() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!JetpackSoundInstance.isActive(minecraft.player)) {
            if (loop != null) {
                loop.stopSound();
                loop = null;
            }
            return;
        }

        if (loop == null || loop.isStopped()) {
            loop = new JetpackSoundInstance(minecraft.player);
            minecraft.getSoundManager().play(loop);
        }
    }
}
