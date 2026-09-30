package com.github.littleemptydoll.exoequipment.fabricator;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class FabricatorScreen extends AbstractContainerScreen<FabricatorMenu> {
    private static final int BACKGROUND = 0xFF14212B;
    private static final int SLOT = 0xFF35434D;
    private static final int TEXT = 0xFFDEE6E9;

    public FabricatorScreen(FabricatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 168;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, BACKGROUND);
        graphics.fill(x + 7, y + 27, x + 169, y + 32, SLOT);
        int width = 160 * Math.min(menu.energy(), FabricatorBlockEntity.CAPACITY)
                / FabricatorBlockEntity.CAPACITY;
        graphics.fill(x + 8, y + 28, x + 8 + width, y + 31, 0xFF49B5D6);
        for (int slot = 0; slot < FabricatorUnlocks.UPGRADE_SLOT_COUNT; slot++) {
            int left = x + 42 + slot * 36;
            graphics.fill(left, y + 49, left + 18, y + 67, SLOT);
            graphics.fill(left + 1, y + 50, left + 17, y + 66, 0xFF0D151A);
        }
        graphics.fill(x + 7, y + 82, x + 169, y + 166, SLOT);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                slotBackground(graphics, x + 8 + col * 18, y + 84 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            slotBackground(graphics, x + 8 + col * 18, y + 142);
        }
    }

    private static void slotBackground(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF14212B);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 7, TEXT, false);
        graphics.drawString(font, Component.translatable("gui.exoequipment.fabricator.energy",
                menu.energy(), FabricatorBlockEntity.CAPACITY), 8, 17, TEXT, false);
        graphics.drawString(font, Component.translatable("gui.exoequipment.fabricator.upgrades"),
                8, 37, TEXT, false);
        graphics.drawString(font, playerInventoryTitle, 8, 72, TEXT, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (menu.getCarried().isEmpty()) {
            String[] labels = {"military", "engineering", "experimental"};
            for (int i = 0; i < labels.length; i++) {
                int x = leftPos + 43 + i * 36;
                int y = topPos + 50;
                if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16
                        && menu.getSlot(i).getItem().isEmpty()) {
                    graphics.renderTooltip(font,
                            Component.translatable("gui.exoequipment.fabricator." + labels[i]),
                            mouseX, mouseY);
                    break;
                }
            }
        }
    }
}
