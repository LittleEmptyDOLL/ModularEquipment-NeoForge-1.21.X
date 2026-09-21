package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.network.BlinkPayload;
import com.github.littleemptydoll.exoequipment.network.FlightPayload;
import com.github.littleemptydoll.exoequipment.network.JetpackInputPayload;
import com.github.littleemptydoll.exoequipment.network.CloakingPayload;
import com.github.littleemptydoll.exoequipment.network.OpenExoskeletonPayload;
import com.github.littleemptydoll.exoequipment.module.JetpackInputState;
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
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() == null || minecraft.player == null) {
            return;
        }

        JetpackInputState.set(minecraft.player, readJetpackInput());
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().getConnection() == null) {
            return;
        }

        while (ModKeyMappings.ACTIVATE_FLIGHT.consumeClick()) {
            PacketDistributor.sendToServer(new FlightPayload());
        }

        byte jetpackInput = readJetpackInput();
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

    private static byte readJetpackInput() {
        Minecraft minecraft = Minecraft.getInstance();
        byte input = 0;

        if (minecraft.options.keyUp.isDown()) {
            input |= JetpackInputState.FORWARD;
        }
        if (minecraft.options.keyDown.isDown()) {
            input |= JetpackInputState.BACK;
        }
        if (minecraft.options.keyLeft.isDown()) {
            input |= JetpackInputState.LEFT;
        }
        if (minecraft.options.keyRight.isDown()) {
            input |= JetpackInputState.RIGHT;
        }
        if (minecraft.options.keyJump.isDown()) {
            input |= JetpackInputState.UP;
        }
        if (minecraft.options.keyShift.isDown()) {
            input |= JetpackInputState.DOWN;
        }

        return input;
    }

    private ModKeyMappingHandler() {}
}
