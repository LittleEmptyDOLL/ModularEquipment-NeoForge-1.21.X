package com.github.littleemptydoll.exoequipment.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExoskeletonScreen extends AbstractContainerScreen<ExoskeletonMenu> {

    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 166;

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
        int left = leftPos;
        int top = topPos;

        // Main GUI
        guiGraphics.fill(
                left,
                top,
                left + imageWidth,
                top + imageHeight,
                0xFF2B2B2B
        );

        // Equipment area
        guiGraphics.fill(
                left + 4,
                top + 4,
                left + 172,
                top + 54,
                0xFF3A3A3A
        );

        // Inventory Area
        guiGraphics.fill(
                left + 4,
                top + 78,
                left + 172,
                top + 162,
                0xFF3A3A3A
        );

        // Separating line
        guiGraphics.fill(
                left + 4,
                top + 74,
                left + 172,
                top + 75,
                0xFF1E1E1E
        );
    }

    @Override
    protected void renderLabels(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {
        guiGraphics.drawString(
                font,
                title,
                104,
                8,
                0xFFFFFF,
                false
        );

        guiGraphics.drawString(
                font,
                playerInventoryTitle,
                8,
                67,
                0xFFFFFF,
                false
        );
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        renderTooltip(guiGraphics, mouseX, mouseY);    }
}
