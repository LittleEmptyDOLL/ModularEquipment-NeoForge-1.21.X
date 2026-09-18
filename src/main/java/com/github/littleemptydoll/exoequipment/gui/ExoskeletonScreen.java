package com.github.littleemptydoll.exoequipment.gui;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.SystemStatus;
import com.github.littleemptydoll.exoequipment.network.OpenMatrixPayload;
import com.github.littleemptydoll.exoequipment.network.OpenProfilePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.neoforge.network.PacketDistributor;

public class ExoskeletonScreen extends AbstractContainerScreen<ExoskeletonMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "textures/gui/exoskeleton.png"
            );

    private static final ResourceLocation CONTROLS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "textures/gui/controls.png"
            );

    private static final int IMAGE_WIDTH = 256;
    private static final int IMAGE_HEIGHT = 256;

    private static final int STATUS_VALUE_X = 170;
    private static final int ENERGY_Y = 23;
    private static final int TEMPERATURE_Y = 40;
    private static final int PROFILE_Y = 57;
    private static final int MATRICES_Y = 73;
    private static final int PROFILE_MAX_WIDTH = 63;

    private static final int PLAYER_MODEL_X1 = 8;
    private static final int PLAYER_MODEL_Y1 = 29;
    private static final int PLAYER_MODEL_X2 = 57;
    private static final int PLAYER_MODEL_Y2 = 99;
    private static final int PLAYER_MODEL_SIZE = 28;
    private static final int STATUS_TEXT_COLOR = 0xFFD8EAF5;

    private static final int MATRIX_INDICATOR_SIZE = 8;
    private static final int MATRIX_INDICATOR_GAP = 4;
    private static final int MATRIX_INDICATOR_TEXTURE_Y = 0;
    private static final int MATRIX_ACTIVE_TEXTURE_X = 0;
    private static final int MATRIX_INSTALLED_TEXTURE_X = 8;
    private static final int MATRIX_EMPTY_TEXTURE_X = 16;

    private static final int ENERGY_STATUS_INDICATOR_X = 93;
    private static final int ENERGY_STATUS_INDICATOR_Y = 8;
    private static final int ENERGY_STATUS_INDICATOR_GAP = 10;
    private static final int ENERGY_STATUS_INDICATOR_WIDTH = 13;
    private static final int ENERGY_STATUS_INDICATOR_HEIGHT = 9;
    private static final int ENERGY_STATUS_GREEN_X = 24;
    private static final int ENERGY_STATUS_YELLOW_X = 37;
    private static final int ENERGY_STATUS_RED_X = 50;
    private static final int ENERGY_STATUS_GRAY_X = 63;
    private static final int ENERGY_STATUS_TOOLTIP_WIDTH = 43;

    private static final int PROFILE_HOVER_X = 153;
    private static final int PROFILE_HOVER_Y = 51;
    private static final int PROFILE_HOVER_WIDTH = 84;
    private static final int PROFILE_HOVER_HEIGHT = 18;

    private static final int EXPANDED_BUTTON_X = 188;
    private static final int EXPANDED_BUTTON_Y = 89;
    private static final int EXPANDED_BUTTON_WIDTH = 14;
    private static final int EXPANDED_BUTTON_HEIGHT = 11;
    private static final int EXPANDED_BUTTON_NORMAL_TEXTURE_X = 76;
    private static final int EXPANDED_BUTTON_HOVER_TEXTURE_X = 90;

    private boolean profileHovered;
    private boolean expandedButtonHovered;

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
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(
            GuiGraphics guiGraphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        guiGraphics.blit(
                TEXTURE,
                leftPos,
                topPos,
                0,
                0,
                IMAGE_WIDTH,
                IMAGE_HEIGHT
        );

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
        guiGraphics.drawString(font, Component.translatable("gui.exoequipment.exoskeleton"), 9, 9, STATUS_TEXT_COLOR, false);
        guiGraphics.drawString(font, Component.translatable("gui.exoequipment.components"), 69, 32, STATUS_TEXT_COLOR, false);
        guiGraphics.drawString(font, Component.translatable("gui.exoequipment.matrices"), 69, 67, STATUS_TEXT_COLOR, false);
        guiGraphics.drawString(font, Component.translatable("gui.exoequipment.system"), 157, 7, STATUS_TEXT_COLOR, false);
        guiGraphics.drawString(font, Component.translatable("gui.exoequipment.inventory"), 42, 109, STATUS_TEXT_COLOR, false);

        drawEnergyStatusIndicator(guiGraphics);
        drawExpandedParametersButton(guiGraphics);
        drawProfileHover(guiGraphics);

        drawStatusValue(guiGraphics, formatEnergy(menu.getEnergyStored(), menu.getEnergyCapacity()), ENERGY_Y);
        drawStatusValue(guiGraphics, formatTemperature(menu.getTemperature()), TEMPERATURE_Y);
        drawStatusValue(guiGraphics, getProfileText(), PROFILE_Y);
        drawMatrixIndicators(guiGraphics);
    }

    private void drawEnergyStatusIndicator(GuiGraphics guiGraphics) {
        int textureX = switch (menu.getEnergyStatus()) {
            case ExoskeletonMenu.ENERGY_STATUS_GREEN -> ENERGY_STATUS_GREEN_X;
            case ExoskeletonMenu.ENERGY_STATUS_YELLOW -> ENERGY_STATUS_YELLOW_X;
            case ExoskeletonMenu.ENERGY_STATUS_RED -> ENERGY_STATUS_RED_X;
            default -> ENERGY_STATUS_GRAY_X;
        };

        int currentX = ENERGY_STATUS_INDICATOR_X;
        for (int i = 0; i < 4; i++) {
            guiGraphics.blit(
                    CONTROLS_TEXTURE,
                    currentX,
                    ENERGY_STATUS_INDICATOR_Y,
                    textureX,
                    0,
                    ENERGY_STATUS_INDICATOR_WIDTH,
                    ENERGY_STATUS_INDICATOR_HEIGHT,
                    IMAGE_WIDTH,
                    IMAGE_HEIGHT
            );
            currentX += ENERGY_STATUS_INDICATOR_GAP;
        }
    }

    private void drawExpandedParametersButton(GuiGraphics guiGraphics) {
        guiGraphics.blit(
                CONTROLS_TEXTURE,
                EXPANDED_BUTTON_X,
                EXPANDED_BUTTON_Y,
                expandedButtonHovered
                        ? EXPANDED_BUTTON_HOVER_TEXTURE_X
                        : EXPANDED_BUTTON_NORMAL_TEXTURE_X,
                0,
                EXPANDED_BUTTON_WIDTH,
                EXPANDED_BUTTON_HEIGHT,
                IMAGE_WIDTH,
                IMAGE_HEIGHT
        );
    }

    private void drawProfileHover(GuiGraphics guiGraphics) {
        if (!profileHovered || menu.getActiveProfile() < 0) {
            return;
        }

        guiGraphics.blit(
                CONTROLS_TEXTURE,
                PROFILE_HOVER_X,
                PROFILE_HOVER_Y,
                0,
                11,
                PROFILE_HOVER_WIDTH,
                PROFILE_HOVER_HEIGHT,
                IMAGE_WIDTH,
                IMAGE_HEIGHT
        );
    }

    private void drawMatrixIndicators(GuiGraphics guiGraphics) {
        for (int slot = 0; slot < ExoskeletonData.MAX_MATRICES; slot++) {
            int x = STATUS_VALUE_X + slot * (MATRIX_INDICATOR_SIZE + MATRIX_INDICATOR_GAP);
            int y = MATRICES_Y;
            ItemStack matrixStack = menu.getSlot(ExoskeletonMenu.MATRIX_START_SLOT + slot).getItem();

            int textureX;
            if (matrixStack.isEmpty()) {
                textureX = MATRIX_EMPTY_TEXTURE_X;
            } else if (menu.isMatrixActive(slot)) {
                textureX = MATRIX_ACTIVE_TEXTURE_X;
            } else {
                textureX = MATRIX_INSTALLED_TEXTURE_X;
            }

            guiGraphics.blit(
                    CONTROLS_TEXTURE,
                    x,
                    y,
                    textureX,
                    MATRIX_INDICATOR_TEXTURE_Y,
                    MATRIX_INDICATOR_SIZE,
                    MATRIX_INDICATOR_SIZE,
                    IMAGE_WIDTH,
                    IMAGE_HEIGHT
            );
        }
    }

    private String formatTemperature(double temperature) {
        return String.format(java.util.Locale.ROOT, "%.1f°C", temperature);
    }

    private String getProfileText() {
        return truncate(menu.getActiveProfileName(), PROFILE_MAX_WIDTH);
    }

    private String formatEnergy(int stored, int capacity) {
        return truncate(
                formatEnergyValue(stored) + "/" + formatEnergyValue(capacity) + " FE",
                PROFILE_MAX_WIDTH
        );
    }

    private String formatEnergyValue(int value) {
        if (value < 1_000) {
            return Integer.toString(value);
        }
        if (value < 1_000_000) {
            return compactValue(value, 1_000, "K");
        }
        if (value < 1_000_000_000) {
            return compactValue(value, 1_000_000, "M");
        }
        return compactValue(value, 1_000_000_000, "B");
    }

    private String compactValue(int value, int divisor, String suffix) {
        int whole = value / divisor;
        int remainder = value % divisor;
        if (remainder == 0) {
            return whole + suffix;
        }

        int decimal = (remainder * 10) / divisor;
        if (decimal == 0) {
            return whole + suffix;
        }
        return whole + "." + decimal + suffix;
    }

    private void drawStatusValue(GuiGraphics guiGraphics, String text, int y) {
        guiGraphics.drawString(font, text, STATUS_VALUE_X, y, STATUS_TEXT_COLOR, false);
    }

    private String truncate(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        if (font.width(ellipsis) > maxWidth) {
            return "";
        }

        String result = text;
        while (!result.isEmpty() && font.width(result + ellipsis) > maxWidth) {
            result = result.substring(0, result.length() - 1);
        }

        return result + ellipsis;
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (slot.index == ExoskeletonMenu.SOURCE_SLOT) {
            return;
        }

        super.renderSlot(guiGraphics, slot);
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

        super.renderSlotHighlight(guiGraphics, slot, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int localMouseX = mouseX - leftPos;
        int localMouseY = mouseY - topPos;

        if (isInside(
                localMouseX,
                localMouseY,
                ENERGY_STATUS_INDICATOR_X,
                ENERGY_STATUS_INDICATOR_Y,
                ENERGY_STATUS_TOOLTIP_WIDTH,
                ENERGY_STATUS_INDICATOR_HEIGHT
        )) {
            renderSystemStatusTooltip(guiGraphics, mouseX, mouseY);
            return;
        }

        Slot hoveredSlot = getSlotUnderMouse();

        if (hoveredSlot != null && hoveredSlot.index == ExoskeletonMenu.SOURCE_SLOT) {
            return;
        }

        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderSystemStatusTooltip(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("gui.exoequipment.system_status").withStyle(ChatFormatting.AQUA));

        int flags = menu.getStatusFlags();

        if ((flags & SystemStatus.FLAG_ENERGY_CRITICAL) != 0) {
            lines.add(Component.translatable("gui.exoequipment.status.energy_critical"));
        } else if ((flags & SystemStatus.FLAG_ENERGY_WARNING) != 0) {
            lines.add(Component.translatable("gui.exoequipment.status.energy_warning"));
        } else if (menu.getEnergyStatus() == SystemStatus.GREEN) {
            lines.add(Component.translatable("gui.exoequipment.status.energy_stable"));
        } else if (menu.getEnergyStatus() == SystemStatus.GRAY) {
            lines.add(Component.translatable("gui.exoequipment.status.energy_unavailable"));
        }

        if ((flags & SystemStatus.FLAG_CONTROLLER_MISSING) != 0) {
            lines.add(Component.translatable("gui.exoequipment.status.controller_missing"));
        }

        if ((flags & SystemStatus.FLAG_FRAME_MISSING) != 0) {
            lines.add(Component.translatable("gui.exoequipment.status.frame_missing"));
        }

        if ((flags & SystemStatus.FLAG_OVERSIZED_MODULE) != 0) {
            lines.add(Component.translatable("gui.exoequipment.status.module_too_large"));
        }

        guiGraphics.renderComponentTooltip(font, lines, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 && hasShiftDown()) {
            Slot hoveredSlot = getSlotUnderMouse();

            if (hoveredSlot != null
                    && hoveredSlot.index >= ExoskeletonMenu.MATRIX_START_SLOT
                    && hoveredSlot.index < ExoskeletonMenu.MATRIX_START_SLOT + ExoskeletonMenu.MATRIX_COUNT
                    && hoveredSlot.hasItem()) {
                int matrixSlot = hoveredSlot.index - ExoskeletonMenu.MATRIX_START_SLOT;
                PacketDistributor.sendToServer(new OpenMatrixPayload(matrixSlot));
                return true;
            }
        }

        if (button == 0
                && menu.getActiveProfile() >= 0
                && mouseX >= leftPos + PROFILE_HOVER_X
                && mouseX < leftPos + PROFILE_HOVER_X + PROFILE_HOVER_WIDTH
                && mouseY >= topPos + PROFILE_HOVER_Y
                && mouseY < topPos + PROFILE_HOVER_Y + PROFILE_HOVER_HEIGHT) {
            PacketDistributor.sendToServer(new OpenProfilePayload());
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int localMouseX = mouseX - leftPos;
        int localMouseY = mouseY - topPos;

        expandedButtonHovered = isInside(
                localMouseX,
                localMouseY,
                EXPANDED_BUTTON_X,
                EXPANDED_BUTTON_Y,
                EXPANDED_BUTTON_WIDTH,
                EXPANDED_BUTTON_HEIGHT
        );

        profileHovered = isInside(
                localMouseX,
                localMouseY,
                PROFILE_HOVER_X,
                PROFILE_HOVER_Y,
                PROFILE_HOVER_WIDTH,
                PROFILE_HOVER_HEIGHT
        );

        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private boolean isInside(
            int mouseX,
            int mouseY,
            int x,
            int y,
            int width,
            int height
    ) {
        return mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
    }
}
