package com.github.littleemptydoll.exoequipment.gui;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.network.OpenExoskeletonPayload;
import com.github.littleemptydoll.exoequipment.network.ProfileActionPayload;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class ExoskeletonProfileScreen extends AbstractContainerScreen<ExoskeletonProfileMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "textures/gui/profile.png"
            );

    private static final int WIDTH = 230;
    private static final int HEIGHT = 132;
    private static final int TEXTURE_SIZE = 256;
    private static final int TEXT_COLOR = 0xFFD8EAF5;

    private static final int PROFILE_X = 8;
    private static final int PROFILE_Y = 29;
    private static final int PROFILE_WIDTH = 80;
    private static final int PROFILE_HEIGHT = 18;
    private static final int VISIBLE_PROFILES = 4;

    private static final int MATRIX_X = 111;
    private static final int MATRIX_Y = 29;
    private static final int MATRIX_WIDTH = 59;
    private static final int MATRIX_HEIGHT = 18;
    private static final int MATRIX_GAP = 4;

    private static final int CREATE_X = 8;
    private static final int DELETE_X = 67;
    private static final int BACK_X = 170;
    private static final int ACTION_Y = 108;
    private static final int ACTION_WIDTH = 50;
    private static final int ACTION_HEIGHT = 18;

    private static final int SCROLL_X = 100;
    private static final int SCROLL_Y = 29;
    private static final int SCROLL_WIDTH = 3;
    private static final int SCROLL_HEIGHT = 67;
    private static final int SCROLL_CONTROL_X = 103;
    private static final int SCROLL_DOWN_X = 106;
    private static final int SCROLL_ARROW_Y = 29;
    private static final int SCROLL_ARROW_WIDTH = 3;
    private static final int SCROLL_ARROW_HEIGHT = 4;
    private static final int SCROLL_THUMB_X = 103;
    private static final int SCROLL_THUMB_INACTIVE_X = 106;
    private static final int SCROLL_THUMB_Y = 33;
    private static final int SCROLL_THUMB_HEIGHT = 24;

    private static final int NORMAL_Y = 132;
    private static final int HOVER_Y = 150;
    private static final int SELECTED_Y = 168;
    private static final int DISABLED_Y = 168;

    private int scrollOffset;
    private boolean draggingScroll;
    private int scrollDragOffset;

    private int hoveredProfile = -1;
    private int hoveredMatrix = -1;
    private int hoveredAction = -1;
    private boolean hoveredScrollUp;
    private boolean hoveredScrollDown;

    private long lastProfileClickTime;
    private int lastProfileClick = -1;

    private EditBox renameBox;
    private int renameProfile = -1;

    private int lastProfileCount = -1;
    private int lastActiveProfile = -2;
    private int lastActiveMask = -1;
    private String lastActiveProfileName = "";

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
        updateScrollOffset();
        clearRenameBox();
        cacheState();
    }

    private void cacheState() {
        lastProfileCount = menu.getProfileCount();
        lastActiveProfile = menu.getActiveProfile();
        lastActiveMask = getActiveMask();
        lastActiveProfileName = getActiveProfileName();
    }

    private int getActiveMask() {
        int profile = menu.getActiveProfile();
        int mask = 0;
        for (int matrix = 0; matrix < 4; matrix++) {
            if (menu.isMatrixActive(profile, matrix)) {
                mask |= 1 << matrix;
            }
        }
        return mask;
    }

    private String getActiveProfileName() {
        return menu.getProfileName(menu.getActiveProfile());
    }

    private void sendAction(int action, int profile, int matrix) {
        sendAction(action, profile, matrix, "");
    }

    private void sendAction(int action, int profile, int matrix, String name) {
        PacketDistributor.sendToServer(
                new ProfileActionPayload(action, profile, matrix, name)
        );
    }

    private void selectProfile(int profile) {
        finishRename(true);
        sendAction(ProfileActionPayload.SELECT, profile, -1);
    }

    private void enterRename(int profile) {
        if (profile < 0 || profile >= menu.getProfileCount()) {
            return;
        }

        clearRenameBox();
        renameProfile = profile;
        renameBox = new EditBox(
                font,
                leftPos + PROFILE_X + 7,
                topPos + PROFILE_Y + (profile - scrollOffset) * PROFILE_HEIGHT + 2,
                PROFILE_WIDTH - 14,
                14,
                Component.empty()
        );
        renameBox.setMaxLength(32);
        renameBox.setValue(menu.getProfileName(profile));
        renameBox.setBordered(false);
        renameBox.setTextColor(TEXT_COLOR);
        renameBox.setTextColorUneditable(TEXT_COLOR);
        renameBox.setFocused(true);
        renameBox.moveCursorToEnd();
        addRenderableWidget(renameBox);
    }

    private void clearRenameBox() {
        if (renameBox != null) {
            removeWidget(renameBox);
            renameBox = null;
        }
        renameProfile = -1;
    }

    private void finishRename(boolean save) {
        if (renameBox == null) {
            return;
        }

        String name = renameBox.getValue().trim();
        int profile = renameProfile;
        clearRenameBox();

        if (save && profile >= 0 && !name.isEmpty()) {
            sendAction(ProfileActionPayload.RENAME, profile, -1, name);
        }
    }

    private void updateScrollOffset() {
        int maxOffset = getMaxScrollOffset();
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxOffset));
    }

    private int getMaxScrollOffset() {
        return Math.max(0, menu.getProfileCount() - VISIBLE_PROFILES);
    }

    private boolean isScrollable() {
        return getMaxScrollOffset() > 0;
    }

    private int getThumbTravel() {
        return SCROLL_HEIGHT - SCROLL_ARROW_HEIGHT * 2 - SCROLL_THUMB_HEIGHT;
    }

    private int getThumbY() {
        int maxOffset = getMaxScrollOffset();
        if (maxOffset <= 0) {
            return SCROLL_THUMB_Y;
        }
        return SCROLL_THUMB_Y + Math.round(
                getThumbTravel() * (scrollOffset / (float) maxOffset)
        );
    }

    private int getScrollOffsetFromMouse(int mouseY) {
        int maxOffset = getMaxScrollOffset();
        if (maxOffset <= 0) {
            return 0;
        }

        int clamped = Math.max(
                SCROLL_THUMB_Y,
                Math.min(
                        SCROLL_THUMB_Y + getThumbTravel(),
                        mouseY - topPos - scrollDragOffset
                )
        );

        return Math.round(
                (clamped - SCROLL_THUMB_Y) * maxOffset / (float) getThumbTravel()
        );
    }

    private void scrollBy(int amount) {
        finishRename(true);
        scrollOffset += amount;
        updateScrollOffset();
    }

    private void drawButton(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int width,
            int height,
            int sourceX,
            int sourceY
    ) {
        guiGraphics.blit(
                TEXTURE,
                x,
                y,
                sourceX,
                sourceY,
                width,
                height,
                TEXTURE_SIZE,
                TEXTURE_SIZE
        );
    }

    private void drawProfileButton(GuiGraphics guiGraphics, int profile, int visibleIndex) {
        int sourceY;
        if (menu.isProfileActive(profile)) {
            sourceY = SELECTED_Y;
        } else if (hoveredProfile == profile) {
            sourceY = HOVER_Y;
        } else {
            sourceY = NORMAL_Y;
        }

        int y = PROFILE_Y + visibleIndex * PROFILE_HEIGHT;
        drawButton(
                guiGraphics,
                PROFILE_X,
                y,
                PROFILE_WIDTH,
                PROFILE_HEIGHT,
                0,
                sourceY
        );

        if (renameProfile != profile) {
            drawStringClipped(
                    guiGraphics,
                    menu.getProfileName(profile),
                    PROFILE_X + 8,
                    y + 5,
                    PROFILE_WIDTH - 16
            );
        }
    }

    private void drawMatrixButton(GuiGraphics guiGraphics, int matrix) {
        int sourceY = hoveredMatrix == matrix ? HOVER_Y : NORMAL_Y;
        if (menu.isMatrixActive(menu.getActiveProfile(), matrix)) {
            sourceY = SELECTED_Y;
        }

        int column = matrix % 2;
        int row = matrix / 2;
        int x = MATRIX_X + column * (MATRIX_WIDTH + MATRIX_GAP);
        int y = MATRIX_Y + row * (MATRIX_HEIGHT + MATRIX_GAP);

        drawButton(
                guiGraphics,
                x,
                y,
                MATRIX_WIDTH,
                MATRIX_HEIGHT,
                80,
                sourceY
        );

        drawStringClipped(
                guiGraphics,
                "Slot " + (matrix + 1),
                x + 8,
                y + 5,
                MATRIX_WIDTH - 16
        );
    }

    private void drawActionButton(
            GuiGraphics guiGraphics,
            int action,
            int x,
            boolean enabled,
            String text
    ) {
        int sourceY;
        if (!enabled) {
            sourceY = DISABLED_Y;
        } else if (hoveredAction == action) {
            sourceY = HOVER_Y;
        } else {
            sourceY = NORMAL_Y;
        }

        drawButton(
                guiGraphics,
                x,
                ACTION_Y,
                ACTION_WIDTH,
                ACTION_HEIGHT,
                139,
                sourceY
        );

        drawStringClipped(
                guiGraphics,
                text,
                x + 7,
                ACTION_Y + 5,
                ACTION_WIDTH - 14
        );
    }

    private void drawScrollbar(GuiGraphics guiGraphics) {
        guiGraphics.blit(
                TEXTURE,
                SCROLL_X,
                SCROLL_Y,
                0,
                186,
                SCROLL_WIDTH,
                SCROLL_HEIGHT,
                TEXTURE_SIZE,
                TEXTURE_SIZE
        );

        boolean scrollable = isScrollable();
        boolean upActive = scrollable && scrollOffset > 0;
        boolean downActive = scrollable && scrollOffset < getMaxScrollOffset();

        drawButton(
                guiGraphics,
                SCROLL_CONTROL_X,
                SCROLL_ARROW_Y,
                SCROLL_ARROW_WIDTH,
                SCROLL_ARROW_HEIGHT,
                upActive ? 3 : 3,
                upActive ? 186 : 190
        );
        drawButton(
                guiGraphics,
                SCROLL_DOWN_X,
                SCROLL_ARROW_Y,
                SCROLL_ARROW_WIDTH,
                SCROLL_ARROW_HEIGHT,
                downActive ? 6 : 6,
                downActive ? 186 : 190
        );

        int thumbX = scrollable ? SCROLL_THUMB_X : SCROLL_THUMB_INACTIVE_X;
        guiGraphics.blit(
                TEXTURE,
                thumbX,
                getThumbY(),
                thumbX == SCROLL_THUMB_X ? 3 : 6,
                194,
                SCROLL_WIDTH,
                SCROLL_THUMB_HEIGHT,
                TEXTURE_SIZE,
                TEXTURE_SIZE
        );
    }

    private void drawStringClipped(
            GuiGraphics guiGraphics,
            String text,
            int x,
            int y,
            int maxWidth
    ) {
        String value = text;
        if (font.width(value) > maxWidth) {
            value = font.plainSubstrByWidth(value, Math.max(0, maxWidth - font.width("..."))) + "...";
        }
        guiGraphics.drawString(font, value, x, y, TEXT_COLOR, false);
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
                WIDTH,
                HEIGHT,
                TEXTURE_SIZE,
                TEXTURE_SIZE
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
                "Profiles " + menu.getProfileCount() + "/" + menu.getMaxProfiles(),
                8,
                8,
                TEXT_COLOR,
                false
        );

        guiGraphics.drawString(
                font,
                "Active Matrices " + menu.getActiveMatrixCount() + "/" + menu.getMaxActiveMatrices(),
                111,
                8,
                TEXT_COLOR,
                false
        );

        for (int visible = 0; visible < VISIBLE_PROFILES; visible++) {
            int profile = scrollOffset + visible;
            if (profile >= menu.getProfileCount()) {
                break;
            }
            drawProfileButton(guiGraphics, profile, visible);
        }

        for (int matrix = 0; matrix < 4; matrix++) {
            drawMatrixButton(guiGraphics, matrix);
        }

        drawActionButton(
                guiGraphics,
                ProfileActionPayload.CREATE,
                CREATE_X,
                menu.getProfileCount() < menu.getMaxProfiles(),
                "Create"
        );
        drawActionButton(
                guiGraphics,
                ProfileActionPayload.REMOVE,
                DELETE_X,
                menu.getProfileCount() > 1,
                "Delete"
        );
        drawActionButton(
                guiGraphics,
                -1,
                BACK_X,
                true,
                "Back"
        );

        drawScrollbar(guiGraphics);
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        int localX = (int) mouseX - leftPos;
        int localY = (int) mouseY - topPos;
        return localX >= x
                && localX < x + width
                && localY >= y
                && localY < y + height;
    }

    private int profileAt(double mouseX, double mouseY) {
        int localX = (int) mouseX - leftPos;
        int localY = (int) mouseY - topPos;

        if (localX < PROFILE_X || localX >= PROFILE_X + PROFILE_WIDTH) {
            return -1;
        }

        int visible = (localY - PROFILE_Y) / PROFILE_HEIGHT;
        if (localY < PROFILE_Y || visible < 0 || visible >= VISIBLE_PROFILES) {
            return -1;
        }

        int profile = scrollOffset + visible;
        return profile < menu.getProfileCount() ? profile : -1;
    }

    private int matrixAt(double mouseX, double mouseY) {
        int localX = (int) mouseX - leftPos;
        int localY = (int) mouseY - topPos;

        for (int matrix = 0; matrix < 4; matrix++) {
            int column = matrix % 2;
            int row = matrix / 2;
            int x = MATRIX_X + column * (MATRIX_WIDTH + MATRIX_GAP);
            int y = MATRIX_Y + row * (MATRIX_HEIGHT + MATRIX_GAP);
            if (localX >= x && localX < x + MATRIX_WIDTH
                    && localY >= y && localY < y + MATRIX_HEIGHT) {
                return matrix;
            }
        }
        return -1;
    }

    private int actionAt(double mouseX, double mouseY) {
        int localX = (int) mouseX - leftPos;
        int localY = (int) mouseY - topPos;
        if (localY < ACTION_Y || localY >= ACTION_Y + ACTION_HEIGHT) {
            return -2;
        }

        if (localX >= CREATE_X && localX < CREATE_X + ACTION_WIDTH) {
            return ProfileActionPayload.CREATE;
        }
        if (localX >= DELETE_X && localX < DELETE_X + ACTION_WIDTH) {
            return ProfileActionPayload.REMOVE;
        }
        if (localX >= BACK_X && localX < BACK_X + ACTION_WIDTH) {
            return -1;
        }
        return -2;
    }

    private void updateHover(int mouseX, int mouseY) {
        hoveredProfile = profileAt(mouseX, mouseY);
        hoveredMatrix = matrixAt(mouseX, mouseY);
        hoveredAction = actionAt(mouseX, mouseY);

        int localX = mouseX - leftPos;
        int localY = mouseY - topPos;
        hoveredScrollUp = localX >= SCROLL_CONTROL_X
                && localX < SCROLL_CONTROL_X + SCROLL_ARROW_WIDTH
                && localY >= SCROLL_ARROW_Y
                && localY < SCROLL_ARROW_Y + SCROLL_ARROW_HEIGHT;
        hoveredScrollDown = localX >= SCROLL_DOWN_X
                && localX < SCROLL_DOWN_X + SCROLL_ARROW_WIDTH
                && localY >= SCROLL_ARROW_Y
                && localY < SCROLL_ARROW_Y + SCROLL_ARROW_HEIGHT;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int profile = profileAt(mouseX, mouseY);
            if (profile >= 0) {
                long now = Util.getMillis();
                if (profile == lastProfileClick && now - lastProfileClickTime <= 250L) {
                    enterRename(profile);
                    lastProfileClick = -1;
                } else {
                    selectProfile(profile);
                    lastProfileClick = profile;
                    lastProfileClickTime = now;
                }
                return true;
            }

            if (hoveredScrollUp && isScrollable()) {
                scrollBy(-1);
                return true;
            }
            if (hoveredScrollDown && isScrollable()) {
                scrollBy(1);
                return true;
            }

            if (isScrollable()) {
                int thumbY = getThumbY();
                int localX = (int) mouseX - leftPos;
                int localY = (int) mouseY - topPos;
                if (localX >= SCROLL_THUMB_X
                        && localX < SCROLL_THUMB_X + SCROLL_WIDTH
                        && localY >= thumbY
                        && localY < thumbY + SCROLL_THUMB_HEIGHT) {
                    finishRename(true);
                    draggingScroll = true;
                    scrollDragOffset = localY - thumbY;
                    return true;
                }
            }

            int matrix = matrixAt(mouseX, mouseY);
            if (matrix >= 0) {
                if (menu.isMatrixInstalled(matrix) && menu.getActiveProfile() >= 0) {
                    finishRename(true);
                    sendAction(
                            ProfileActionPayload.TOGGLE_MATRIX,
                            menu.getActiveProfile(),
                            matrix
                    );
                }
                return true;
            }

            int action = actionAt(mouseX, mouseY);
            if (action == ProfileActionPayload.CREATE) {
                if (menu.getProfileCount() < menu.getMaxProfiles()) {
                    finishRename(true);
                    sendAction(action, -1, -1);
                }
                return true;
            }
            if (action == ProfileActionPayload.REMOVE) {
                if (menu.getProfileCount() > 1 && menu.getActiveProfile() >= 0) {
                    finishRename(true);
                    sendAction(action, menu.getActiveProfile(), -1);
                }
                return true;
            }
            if (action == -1) {
                finishRename(true);
                PacketDistributor.sendToServer(new OpenExoskeletonPayload());
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingScroll) {
            draggingScroll = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && draggingScroll) {
            finishRename(true);
            scrollOffset = getScrollOffsetFromMouse((int) mouseY);
            updateScrollOffset();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isScrollable()
                && mouseX >= leftPos + PROFILE_X
                && mouseX < leftPos + SCROLL_X + SCROLL_WIDTH
                && mouseY >= topPos + PROFILE_Y
                && mouseY < topPos + PROFILE_Y + VISIBLE_PROFILES * PROFILE_HEIGHT) {
            scrollBy(scrollY > 0 ? -1 : 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void containerTick() {
        super.containerTick();

        if (menu.getProfileCount() != lastProfileCount
                || menu.getActiveProfile() != lastActiveProfile
                || getActiveMask() != lastActiveMask
                || !getActiveProfileName().equals(lastActiveProfileName)) {
            updateScrollOffset();
            if (renameBox == null) {
                cacheState();
            }
        }

        if (renameBox != null && renameProfile >= 0) {
            int visible = renameProfile - scrollOffset;
            if (visible < 0 || visible >= VISIBLE_PROFILES) {
                finishRename(true);
            } else {
                renameBox.setX(leftPos + PROFILE_X + 7);
                renameBox.setY(topPos + PROFILE_Y + visible * PROFILE_HEIGHT + 2);
            }
        }
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        updateHover(mouseX, mouseY);
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (renameBox != null) {
            if (keyCode == 257 || keyCode == 335) {
                finishRename(true);
                return true;
            }
            if (keyCode == 256) {
                finishRename(false);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
