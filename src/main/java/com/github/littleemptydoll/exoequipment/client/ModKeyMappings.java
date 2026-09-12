package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.network.OpenExoskeletonPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = ExoEquipment.MODID, value = Dist.CLIENT)
public final class ModKeyMappings {

    public static final KeyMapping OPEN_EXOSKELETON = new KeyMapping(
            "key.exoequipment.open_exoskeleton",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.exoequipment"
    );

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_EXOSKELETON);
    }

    private ModKeyMappings() {}
}

@EventBusSubscriber(modid = ExoEquipment.MODID, value = Dist.CLIENT)
final class ModKeyMappingHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (ModKeyMappings.OPEN_EXOSKELETON.consumeClick()) {
            PacketDistributor.sendToServer(new OpenExoskeletonPayload());
        }
    }

    private ModKeyMappingHandler() {}
}
