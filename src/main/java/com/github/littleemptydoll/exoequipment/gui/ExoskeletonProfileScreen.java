package com.github.littleemptydoll.exoequipment.gui;

import com.github.littleemptydoll.exoequipment.network.OpenExoskeletonPayload;
import com.github.littleemptydoll.exoequipment.network.ProfileActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class ExoskeletonProfileScreen extends AbstractContainerScreen<ExoskeletonProfileMenu> {
    private static final int WIDTH = 256;
    private static final int HEIGHT = 180;
    private static final int PANEL_COLOR = 0xD0101010;
    private static final int TEXT_COLOR = 0xFFD8EAF5;

    private int lastProfileCount = -1;
    private int lastActiveProfile = -2;
    private int lastActiveMask = -1;

    public ExoskeletonProfileScreen(
            ExoskeletonProfileMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        clearWidgets();

        int profileCount = menu.getProfileCount();
        for (int profile = 0; profile < profileCount; profile++) {
            final int profileIndex = profile;
            addRenderableWidget(Button.builder(
                    Component.translatable("gui.exoequipment.profile", profile + 1),
                    button -> sendAction(ProfileActionPayload.SELECT, profileIndex, -1)
            ).bounds(leftPos + 18, topPos + 28 + profile * 22, 72, 20).build());
        }

        addRenderableWidget(Button.builder(
                Component.translatable("gui.exoequipment.profile_create"),
                button -> sendAction(ProfileActionPayload.CREATE, -1, -1)
        ).bounds(leftPos + 18, topPos + 120, 72, 20).build());

        Button removeButton = Button.builder(
                Component.translatable("gui.exoequipment.profile_remove"),
                button -> sendAction(ProfileActionPayload.REMOVE, menu.getActiveProfile(), -1)
        ).bounds(leftPos + 94, topPos + 120, 72, 20).build();
        removeButton.active = profileCount > 1;
        addRenderableWidget(removeButton);

        int activeProfile = menu.getActiveProfile();
        for (int matrix = 0; matrix < 4; matrix++) {
            final int matrixIndex = matrix;
            boolean installed = menu.isMatrixInstalled(matrix);
            boolean active = menu.isMatrixActive(activeProfile, matrix);
            String suffix = active ? "ON" : installed ? "OFF" : "-";

            Button matrixButton = Button.builder(
                    Component.literal("M" + (matrix + 1) + " " + suffix),
                    button -> sendAction(
                            ProfileActionPayload.TOGGLE_MATRIX,
                            menu.getActiveProfile(),
                            matrixIndex
                    )
            ).bounds(
                    leftPos + 112 + (matrix % 2) * 62,
                    topPos + 42 + (matrix / 2) * 26,
                    58,
                    20
            ).build();
            matrixButton.active = installed;
            addRenderableWidget(matrixButton);
        }

        addRenderableWidget(Button.builder(
                Component.translatable("gui.exoequipment.back"),
                button -> PacketDistributor.sendToServer(new OpenExoskeletonPayload())
        ).bounds(leftPos + 176, topPos + 120, 62, 20).build());

        lastProfileCount = menu.getProfileCount();
        lastActiveProfile = menu.getActiveProfile();
        lastActiveMask = getActiveMask();
    }

    private int getActiveMask() {
        int mask = 0;
        int profile = menu.getActiveProfile();
        for (int matrix = 0; matrix < 4; matrix++) {
            if (menu.isMatrixActive(profile, matrix)) {
                mask |= 1 << matrix;
            }
        }
        return mask;
    }

    private void sendAction(int action, int profile, int matrix) {
        PacketDistributor.sendToServer(
                new ProfileActionPayload(action, profile, matrix)
        );
    }

    @Override
    protected void renderBg(
            GuiGraphics guiGraphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        guiGraphics.fill(
                leftPos,
                topPos,
                leftPos + imageWidth,
                topPos + imageHeight,
                PANEL_COLOR
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
                Component.translatable("gui.exoequipment.profiles"),
                18,
                10,
                TEXT_COLOR,
                false
        );

        guiGraphics.drawString(
                font,
                Component.translatable("gui.exoequipment.active_profile"),
                112,
                24,
                TEXT_COLOR,
                false
        );

        int activeProfile = menu.getActiveProfile();
        guiGraphics.drawString(
                font,
                activeProfile >= 0 ? "#" + (activeProfile + 1) : "None",
                184,
                24,
                TEXT_COLOR,
                false
        );

        guiGraphics.drawString(
                font,
                Component.translatable("gui.exoequipment.active_matrices"),
                112,
                76,
                TEXT_COLOR,
                false
        );

        guiGraphics.drawString(
                font,
                menu.getMaxActiveMatrices() + " / 4",
                184,
                76,
                TEXT_COLOR,
                false
        );

        guiGraphics.drawString(
                font,
                menu.getProfileCount() + " / " + menu.getMaxProfiles(),
                18,
                146,
                TEXT_COLOR,
                false
        );
    }

    @Override
    public void containerTick() {
        super.containerTick();

        if (menu.getProfileCount() != lastProfileCount
                || menu.getActiveProfile() != lastActiveProfile
                || getActiveMask() != lastActiveMask) {
            rebuildWidgets();
        }
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
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
