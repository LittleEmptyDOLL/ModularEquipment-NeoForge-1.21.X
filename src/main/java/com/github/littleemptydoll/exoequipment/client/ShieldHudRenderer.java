package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.EmergencyShieldOperations;
import com.github.littleemptydoll.exoequipment.module.RevivalOperations;
import com.github.littleemptydoll.exoequipment.module.ShieldOperations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

@EventBusSubscriber(
        modid = ExoEquipment.MODID,
        value = Dist.CLIENT
)
public final class ShieldHudRenderer {
    private static final ResourceLocation LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "shield_hud"
            );

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "textures/gui/shield_hud.png"
            );

    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 128;

    private static final int HUD_WIDTH = 128;
    private static final int HUD_HEIGHT = 38;

    private static final int BAR_BACKGROUND_WIDTH = 102;
    private static final int BAR_BACKGROUND_HEIGHT = 7;
    private static final int BAR_WIDTH = 100;
    private static final int BAR_HEIGHT = 5;

    private static final int SCREEN_MARGIN = 6;

    private ShieldHudRenderer() {}

    @SubscribeEvent
    public static void registerGuiLayer(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                LAYER_ID,
                ShieldHudRenderer::render
        );
    }

    private static void render(
            GuiGraphics guiGraphics,
            net.minecraft.client.DeltaTracker deltaTracker
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null
                || minecraft.options.hideGui
                || minecraft.screen != null) {
            return;
        }

        Optional<ItemStack> exoskeletonStack =
                CuriosApi.getCuriosInventory(minecraft.player)
                        .flatMap(curios ->
                                curios.findFirstCurio(
                                        stack ->
                                                stack.getItem()
                                                        instanceof ExoskeletonItem
                                )
                        )
                        .map(result -> result.stack());

        if (exoskeletonStack.isEmpty()) {
            return;
        }

        ExoskeletonData data = ExoskeletonItem.getData(exoskeletonStack.get());
        ShieldOperations.ShieldStatus status = ShieldOperations.getStatus(data);

        if (!status.hasShields()) {
            return;
        }

        int x = SCREEN_MARGIN;
        int y = guiGraphics.guiHeight() - HUD_HEIGHT - SCREEN_MARGIN;

        renderBackground(guiGraphics, x, y);
        renderShieldIcon(guiGraphics, x, y);
        renderShieldBar(guiGraphics, x, y, status);
        renderShieldValue(guiGraphics, minecraft, x, y, status);
        renderReserveCounters(guiGraphics, minecraft, data, x, y);
    }

    private static void renderBackground(
            GuiGraphics graphics,
            int x,
            int y
    ) {
        graphics.blit(
                TEXTURE,
                x,
                y,
                0,
                0,
                HUD_WIDTH,
                HUD_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }

    private static void renderShieldIcon(
            GuiGraphics graphics,
            int x,
            int y
    ) {
        graphics.pose().pushPose();
        graphics.pose().translate(x + 6, y + 11, 0.0F);
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        graphics.blit(
                TEXTURE,
                0,
                0,
                0,
                62,
                18,
                22,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
        graphics.pose().popPose();
    }

    private static void renderShieldBar(
            GuiGraphics graphics,
            int x,
            int y,
            ShieldOperations.ShieldStatus status
    ) {
        graphics.blit(
                TEXTURE,
                x + 17,
                y + 13,
                0,
                50,
                BAR_BACKGROUND_WIDTH,
                BAR_BACKGROUND_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );

        if (status.capacity() <= 0) {
            return;
        }

        double fraction = Math.max(
                0.0D,
                Math.min(1.0D, status.currentEnergy() / status.capacity())
        );
        int filledWidth = (int) Math.round(BAR_WIDTH * fraction);

        if (filledWidth <= 0) {
            return;
        }

        graphics.blit(
                TEXTURE,
                x + 18,
                y + 14,
                0,
                57,
                filledWidth,
                BAR_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }

    private static void renderShieldValue(
            GuiGraphics graphics,
            Minecraft minecraft,
            int x,
            int y,
            ShieldOperations.ShieldStatus status
    ) {
        Component value = Component.literal(
                Math.round(status.currentEnergy())
                        + "/"
                        + status.capacity()
        );

        graphics.drawString(
                minecraft.font,
                value,
                x + 19,
                y + 5,
                0xFFFFFF,
                false
        );
    }

    private static void renderReserveCounters(
            GuiGraphics graphics,
            Minecraft minecraft,
            ExoskeletonData data,
            int x,
            int y
    ) {
        int emergencyCount = EmergencyShieldOperations.readyCount(data);
        int revivalCount = RevivalOperations.readyCount(data);
        int cursorX = x + 17;
        int iconY = y + 22;

        if (emergencyCount > 0) {
            renderEmergencyShieldIcon(graphics, cursorX, iconY);

            String count = "x" + emergencyCount;
            int textX = cursorX + 11;
            graphics.drawString(
                    minecraft.font,
                    count,
                    textX,
                    iconY + 2,
                    0xFFFFFF,
                    false
            );

            cursorX = textX + minecraft.font.width(count) + 4;
        }

        if (revivalCount > 0) {
            renderRevivalIcon(graphics, cursorX, iconY);

            String count = "x" + revivalCount;
            graphics.drawString(
                    minecraft.font,
                    count,
                    cursorX + 13,
                    iconY + 2,
                    0xFFFFFF,
                    false
            );
        }
    }

    private static void renderEmergencyShieldIcon(
            GuiGraphics graphics,
            int x,
            int y
    ) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        graphics.blit(
                TEXTURE,
                0,
                0,
                18,
                62,
                18,
                22,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
        graphics.pose().popPose();
    }

    private static void renderRevivalIcon(
            GuiGraphics graphics,
            int x,
            int y
    ) {
        float scale = 11.0F / 16.0F;

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.renderItem(new ItemStack(Items.TOTEM_OF_UNDYING), 0, 0);
        graphics.pose().popPose();
    }
}
