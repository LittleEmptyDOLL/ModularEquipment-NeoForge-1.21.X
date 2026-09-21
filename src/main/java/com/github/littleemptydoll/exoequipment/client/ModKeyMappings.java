package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.network.BlinkPayload;
import com.github.littleemptydoll.exoequipment.network.FlightPayload;
import com.github.littleemptydoll.exoequipment.network.JetpackInputPayload;
import com.github.littleemptydoll.exoequipment.network.CloakingPayload;
import com.github.littleemptydoll.exoequipment.network.OpenExoskeletonPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = ExoEquipment.MODID, value = Dist.CLIENT)
public final class ModKeyMappings {

    public static final KeyMapping ACTIVATE_CLOAKING = new KeyMapping(
            "key.exoequipment.activate_cloaking",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.exoequipment"
    );

    public static final KeyMapping ACTIVATE_BLINK = new KeyMapping(
            "key.exoequipment.activate_blink",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories.exoequipment"
    );

    public static final KeyMapping ACTIVATE_FLIGHT = new KeyMapping(
            "key.exoequipment.activate_flight",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            "key.categories.exoequipment"
    );

    public static final KeyMapping OPEN_EXOSKELETON = new KeyMapping(
            "key.exoequipment.open_exoskeleton",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.exoequipment"
    );

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_EXOSKELETON);
        event.register(ACTIVATE_FLIGHT);
        event.register(ACTIVATE_CLOAKING);
        event.register(ACTIVATE_BLINK);
    }

    private ModKeyMappings() {}
}

@EventBusSubscriber(modid = ExoEquipment.MODID, value = Dist.CLIENT)
final class ModKeyMappingHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (ModKeyMappings.ACTIVATE_FLIGHT.consumeClick()) {
            PacketDistributor.sendToServer(new FlightPayload());
        }

        byte jetpackInput = 0;
        if (Minecraft.getInstance().options.keyUp.isDown()) {
            jetpackInput |= com.github.littleemptydoll.exoequipment.module.JetpackInputState.FORWARD;
        }
        if (Minecraft.getInstance().options.keyDown.isDown()) {
            jetpackInput |= com.github.littleemptydoll.exoequipment.module.JetpackInputState.BACK;
        }
        if (Minecraft.getInstance().options.keyLeft.isDown()) {
            jetpackInput |= com.github.littleemptydoll.exoequipment.module.JetpackInputState.LEFT;
        }
        if (Minecraft.getInstance().options.keyRight.isDown()) {
            jetpackInput |= com.github.littleemptydoll.exoequipment.module.JetpackInputState.RIGHT;
        }
        if (Minecraft.getInstance().options.keyJump.isDown()) {
            jetpackInput |= com.github.littleemptydoll.exoequipment.module.JetpackInputState.UP;
        }
        if (Minecraft.getInstance().options.keyShift.isDown()) {
            jetpackInput |= com.github.littleemptydoll.exoequipment.module.JetpackInputState.DOWN;
        }
        PacketDistributor.sendToServer(new JetpackInputPayload(jetpackInput));

        while (ModKeyMappings.OPEN_EXOSKELETON.consumeClick()) {
            PacketDistributor.sendToServer(new OpenExoskeletonPayload());
        }

        while (ModKeyMappings.ACTIVATE_CLOAKING.consumeClick()) {
            PacketDistributor.sendToServer(new CloakingPayload());
        }

        while (ModKeyMappings.ACTIVATE_BLINK.consumeClick()) {
            PacketDistributor.sendToServer(new BlinkPayload());
        }
    }

    private ModKeyMappingHandler() {}
}
