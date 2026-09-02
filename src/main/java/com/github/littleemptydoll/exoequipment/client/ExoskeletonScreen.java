package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.gui.ExoskeletonMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExoskeletonScreen extends AbstractContainerScreen<ExoskeletonMenu> {

    public ExoskeletonScreen(
            ExoskeletonMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(
                menu,
                inventory,
                title
        );

        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        graphics.fill(
                leftPos,
                topPos,
                leftPos + imageWidth,
                topPos + imageHeight,
                0xFF202020
        );

        graphics.fill(
                leftPos + 4,
                topPos + 4,
                leftPos + imageWidth - 4,
                topPos + imageHeight - 4,
                0xFF303030
        );
    }

    @Override
    protected void renderLabels(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        graphics.drawString(
                font,
                title,
                titleLabelX,
                titleLabelY,
                0xFFFFFF
        );
    }
}
