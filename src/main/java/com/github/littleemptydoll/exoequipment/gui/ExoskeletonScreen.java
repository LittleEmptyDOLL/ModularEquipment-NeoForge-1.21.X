package com.github.littleemptydoll.exoequipment.gui;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ExoskeletonScreen extends AbstractContainerScreen<ExoskeletonMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "textures/gui/exoskeleton.png"
            );

    private static final int IMAGE_WIDTH = 256;
    private static final int IMAGE_HEIGHT = 256;

    // Status text
    private static final int STATUS_VALUE_X = 170;

    private static final int ENERGY_Y = 23;
    private static final int TEMPERATURE_Y = 40;
    private static final int PROFILE_Y = 57;
    private static final int MATRICES_Y = 74;

    // Maximum width for profile
    private static final int PROFILE_MAX_WIDTH = 54;

    // Player model area
    private static final int PLAYER_MODEL_X1 = 8;
    private static final int PLAYER_MODEL_Y1 = 29;
    private static final int PLAYER_MODEL_X2 = 57;
    private static final int PLAYER_MODEL_Y2 = 99;

    private static final int PLAYER_MODEL_SIZE = 28;

    // Color of dynamic status values
    private static final int STATUS_TEXT_COLOR = 0xFFD8EAF5;

    public ExoskeletonScreen(
            ExoskeletonMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);

        this.imageWidth = IMAGE_WIDTH;
        this.imageHeight = IMAGE_HEIGHT;
    }

    @Override
    protected void renderBg(
            GuiGraphics guiGraphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        // Draw GUI texture
        guiGraphics.blit(
                TEXTURE,
                leftPos,
                topPos,
                0,
                0,
                IMAGE_WIDTH,
                IMAGE_HEIGHT
        );

        // Render player
        if (minecraft != null && minecraft.player != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    guiGraphics,
                    leftPos + PLAYER_MODEL_X1,
                    topPos + PLAYER_MODEL_Y1,
                    leftPos + PLAYER_MODEL_X2,
                    topPos + PLAYER_MODEL_Y2,
                    PLAYER_MODEL_SIZE,
                    0.0F,
                    mouseX,
                    mouseY,
                    minecraft.player
            );
        }
    }

    @Override
    protected void renderLabels(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {
        guiGraphics.drawString(
                font,
                Component.translatable("gui.exoequipment.exoskeleton"),
                9,
                9,
                0xFFD8EAF5,
                false
        );

        guiGraphics.drawString(
                font,
                Component.translatable("gui.exoequipment.components"),
                69,
                32,
                0xFFD8EAF5,
                false
        );

        guiGraphics.drawString(
                font,
                Component.translatable("gui.exoequipment.matrices"),
                69,
                67,
                0xFFD8EAF5,
                false
        );

        guiGraphics.drawString(
                font,
                Component.translatable("gui.exoequipment.system"),
                157,
                7,
                0xFFD8EAF5,
                false
        );

        guiGraphics.drawString(
                font,
                Component.translatable("gui.exoequipment.inventory"),
                42,
                109,
                0xFFD8EAF5,
                false
        );

        ExoskeletonData data = getExoskeletonData();

        // Energy
        drawStatusValue(
                guiGraphics,
                "None",
                ENERGY_Y
        );

        // Temperature
        drawStatusValue(
                guiGraphics,
                "None",
                TEMPERATURE_Y
        );

        // Active profile
        String profileText = getProfileText(data);

        drawStatusValue(
                guiGraphics,
                profileText,
                PROFILE_Y
        );

        // Installed matrices
        int installedMatrices = countInstalledMatrices(data);

        drawStatusValue(
                guiGraphics,
                installedMatrices + " / " + ExoskeletonData.MAX_MATRICES,
                MATRICES_Y
        );
    }

    private ExoskeletonData getExoskeletonData() {
        ItemStack stack = menu.getSlot(ExoskeletonMenu.SOURCE_SLOT).getItem();

        return ExoskeletonItem.getData(stack);
    }

    private int countInstalledMatrices(
            ExoskeletonData data
    ) {
        return (int) data.matrices()
                .stream()
                .filter(matrixSlot ->
                        matrixSlot.matrix().isPresent()
                )
                .count();
    }

    private String getProfileText(
            ExoskeletonData data
    ) {
        int activeProfile = data.activeProfile();

        if (activeProfile < 0) {
            return "None";
        }

        String profileName = "#" + (activeProfile + 1);

        return truncate(
                profileName,
                PROFILE_MAX_WIDTH
        );
    }

    private void drawStatusValue(
            GuiGraphics guiGraphics,
            String text,
            int y
    ) {
        guiGraphics.drawString(
                font,
                text,
                STATUS_VALUE_X,
                y,
                STATUS_TEXT_COLOR,
                false
        );
    }

    private String truncate(
            String text,
            int maxWidth
    ) {
        if (font.width(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";

        if (font.width(ellipsis) > maxWidth) {
            return "";
        }

        String result = text;

        while (!result.isEmpty() && font.width(result + ellipsis) > maxWidth) {
            result = result.substring(
                    0,
                    result.length() - 1
            );
        }

        return result + ellipsis;
    }

    private String formatCompact(long value) {
        if (value < 1000) {
            return Long.toString(value);
        }

        if (value < 1_000_000) {
            return formatUnit(
                    value,
                    1_000,
                    "k"
            );
        }

        if (value < 1_000_000_000) {
            return formatUnit(
                    value,
                    1_000_000,
                    "M"
            );
        }

        return formatUnit(
                value,
                1_000_000_000,
                "G"
        );
    }

    private  String formatUnit(
            long value,
            long unit,
            String suffix
    ) {
        double result = (double) value / unit;

        if (result >= 100) {
            return String.format(
                    "%.0f%s",
                    result,
                    suffix
            );
        }

        if (result >= 10) {
            return String.format(
                    "%.1f%s",
                    result,
                    suffix
            );
        }

        return String.format(
                "%.1f%s",
                result,
                suffix
        );
    }

    @Override
    protected void renderSlot(
            GuiGraphics guiGraphics,
            Slot slot
    ) {
        if (slot.index == ExoskeletonMenu.SOURCE_SLOT) {
            return;
        }

        super.renderSlot(
                guiGraphics,
                slot
        );
    }

    @Override
    protected void renderSlotHighlight(
            GuiGraphics guiGraphics,
            Slot slot,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        if (slot.index == ExoskeletonMenu.SOURCE_SLOT) {
            return;
        }

        super.renderSlotHighlight(
                guiGraphics,
                slot,
                mouseX,
                mouseY,
                partialTick
        );
    }

    @Override
    protected void renderTooltip(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {
        Slot hoveredSlot = getSlotUnderMouse();

        if (hoveredSlot != null && hoveredSlot.index == ExoskeletonMenu.SOURCE_SLOT) {
            return;
        }

        super.renderTooltip(
                guiGraphics,
                mouseX,
                mouseY
        );
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );

        super.render(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );

        renderTooltip(
                guiGraphics,
                mouseX,
                mouseY
        );
    }
}
