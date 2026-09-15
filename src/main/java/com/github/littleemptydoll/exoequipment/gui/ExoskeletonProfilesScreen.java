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

public class ExoskeletonProfilesScreen extends AbstractContainerScreen<ExoskeletonProfileMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, "textures/gui/profile.png");
    private static final int WIDTH = 230, HEIGHT = 132, TEXTURE_SIZE = 256;
    private static final int TEXT_COLOR = 0xFFD8EAF5, DISABLED_TEXT_COLOR = 0xFF808080;

    private static final int PROFILE_X = 7, PROFILE_Y = 28, PROFILE_WIDTH = 80, PROFILE_HEIGHT = 18, PROFILE_STEP = 17, VISIBLE_PROFILES = 4;
    private static final int MATRIX_X = 102, MATRIX_Y = 21, MATRIX_WIDTH = 59, MATRIX_HEIGHT = 18, MATRIX_GAP = 3;
    private static final int CREATE_X = 7, DELETE_X = 60, BACK_X = 173, ACTION_Y = 107, ACTION_WIDTH = 50, ACTION_HEIGHT = 18;
    private static final int SCROLL_X = 90, SCROLL_Y = 29, SCROLL_WIDTH = 3, SCROLL_HEIGHT = 67;
    private static final int SCROLL_CONTROL_X = 90, SCROLL_DOWN_X = 90, SCROLL_ARROW_UP_Y = 29, SCROLL_ARROW_DOWN_Y = 92;
    private static final int SCROLL_ARROW_WIDTH = 3, SCROLL_ARROW_HEIGHT = 4, SCROLL_THUMB_X = 90, SCROLL_THUMB_INACTIVE_X = 90;
    private static final int SCROLL_THUMB_Y = 33, SCROLL_THUMB_HEIGHT = 24;
    private static final int NORMAL_Y = 132, HOVER_Y = 150, SELECTED_Y = 168, DISABLED_Y = 168;

    private int scrollOffset, scrollDragOffset;
    private boolean draggingScroll;
    private int hoveredProfile = -1, hoveredMatrix = -1, hoveredAction = -2;
    private boolean hoveredScrollUp, hoveredScrollDown;
    private long lastProfileClickTime;
    private int lastProfileClick = -1;
    private EditBox renameBox;
    private int renameProfile = -1;

    public ExoskeletonProfilesScreen(ExoskeletonProfileMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        updateScrollOffset();
        clearRenameBox();
    }

    private void sendAction(int action, int profile, int matrix) { sendAction(action, profile, matrix, ""); }

    private void sendAction(int action, int profile, int matrix, String name) {
        PacketDistributor.sendToServer(new ProfileActionPayload(action, profile, matrix, name));
    }

    private void selectProfile(int profile) {
        finishRename(true);
        sendAction(ProfileActionPayload.SELECT, profile, -1);
    }

    private void enterRename(int profile) {
        if (profile < 0 || profile >= menu.getProfileCount()) return;
        clearRenameBox();
        renameProfile = profile;
        renameBox = new EditBox(font, leftPos + PROFILE_X + 7,
                topPos + PROFILE_Y + (profile - scrollOffset) * PROFILE_STEP + 2,
                PROFILE_WIDTH - 14, 14, Component.empty());
        renameBox.setMaxLength(32);
        renameBox.setValue(menu.getProfileName(profile));
        renameBox.setBordered(false);
        renameBox.setTextColor(TEXT_COLOR);
        renameBox.setTextColorUneditable(TEXT_COLOR);
        renameBox.setFocused(true);
        renameBox.moveCursorToEnd(true);
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
        if (renameBox == null) return;
        String name = renameBox.getValue().trim();
        int profile = renameProfile;
        clearRenameBox();
        if (save && profile >= 0 && !name.isEmpty()) sendAction(ProfileActionPayload.RENAME, profile, -1, name);
    }

    private int getMaxScrollOffset() { return Math.max(0, menu.getProfileCount() - VISIBLE_PROFILES); }
    private boolean isScrollable() { return getMaxScrollOffset() > 0; }
    private void updateScrollOffset() { scrollOffset = Math.max(0, Math.min(scrollOffset, getMaxScrollOffset())); }
    private int getThumbTravel() { return SCROLL_HEIGHT - SCROLL_ARROW_HEIGHT * 2 - SCROLL_THUMB_HEIGHT; }

    private int getThumbY() {
        int maxOffset = getMaxScrollOffset();
        return maxOffset <= 0 ? SCROLL_THUMB_Y : SCROLL_THUMB_Y + Math.round(getThumbTravel() * scrollOffset / (float) maxOffset);
    }

    private int getScrollOffsetFromMouse(int mouseY) {
        int maxOffset = getMaxScrollOffset();
        if (maxOffset <= 0) return 0;
        int clamped = Math.max(SCROLL_THUMB_Y, Math.min(SCROLL_THUMB_Y + getThumbTravel(), mouseY - topPos - scrollDragOffset));
        return Math.round((clamped - SCROLL_THUMB_Y) * maxOffset / (float) getThumbTravel());
    }

    private void scrollBy(int amount) {
        finishRename(true);
        scrollOffset += amount;
        updateScrollOffset();
    }

    private void drawButton(GuiGraphics graphics, int x, int y, int width, int height, int sourceX, int sourceY) {
        graphics.blit(TEXTURE, x, y, sourceX, sourceY, width, height, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private void drawText(GuiGraphics graphics, String text, int x, int y, int maxWidth, int color) {
        if (font.width(text) > maxWidth) text = font.plainSubstrByWidth(text, Math.max(0, maxWidth - font.width("..."))) + "...";
        graphics.drawString(font, text, x, y, color, false);
    }

    private void drawText(GuiGraphics graphics, Component text, int x, int y, int maxWidth, int color) {
        drawText(graphics, text.getString(), x, y, maxWidth, color);
    }

    private void drawProfileButton(GuiGraphics graphics, int profile, int visibleIndex) {
        int sourceY = menu.isProfileActive(profile) ? SELECTED_Y : hoveredProfile == profile ? HOVER_Y : NORMAL_Y;
        int y = PROFILE_Y + visibleIndex * PROFILE_STEP;
        drawButton(graphics, PROFILE_X, y, PROFILE_WIDTH, PROFILE_HEIGHT, 0, sourceY);
        if (renameProfile != profile) drawText(graphics, menu.getProfileName(profile), PROFILE_X + 5, y + 5, PROFILE_WIDTH - 10, TEXT_COLOR);
    }

    private void drawMatrixButton(GuiGraphics graphics, int matrix) {
        int sourceY = menu.isMatrixActive(menu.getActiveProfile(), matrix) ? SELECTED_Y : hoveredMatrix == matrix ? HOVER_Y : NORMAL_Y;
        int column = matrix % 2, row = matrix / 2;
        int x = MATRIX_X + column * (MATRIX_WIDTH + MATRIX_GAP), y = MATRIX_Y + row * (MATRIX_HEIGHT + MATRIX_GAP);
        drawButton(graphics, x, y, MATRIX_WIDTH, MATRIX_HEIGHT, 80, sourceY);
        drawText(graphics, Component.translatable("gui.exoequipment.matrix_slot", matrix + 1), x + 5, y + 5, MATRIX_WIDTH - 10, TEXT_COLOR);
    }

    private void drawActionButton(GuiGraphics graphics, int action, int x, boolean enabled, Component text) {
        int sourceY = !enabled ? DISABLED_Y : hoveredAction == action ? HOVER_Y : NORMAL_Y;
        drawButton(graphics, x, ACTION_Y, ACTION_WIDTH, ACTION_HEIGHT, 139, sourceY);
        drawText(graphics, text, x + 5, ACTION_Y + 5, ACTION_WIDTH - 10, enabled ? TEXT_COLOR : DISABLED_TEXT_COLOR);
    }

    private void drawScrollbar(GuiGraphics graphics) {
        graphics.blit(TEXTURE, SCROLL_X, SCROLL_Y, 0, 186, SCROLL_WIDTH, SCROLL_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        boolean scrollable = isScrollable();
        boolean upActive = scrollable && scrollOffset > 0, downActive = scrollable && scrollOffset < getMaxScrollOffset();
        drawButton(graphics, SCROLL_CONTROL_X, SCROLL_ARROW_UP_Y, SCROLL_ARROW_WIDTH, SCROLL_ARROW_HEIGHT, 3, upActive ? 186 : 190);
        drawButton(graphics, SCROLL_DOWN_X, SCROLL_ARROW_DOWN_Y, SCROLL_ARROW_WIDTH, SCROLL_ARROW_HEIGHT, 6, downActive ? 186 : 190);
        int thumbX = scrollable ? SCROLL_THUMB_X : SCROLL_THUMB_INACTIVE_X;
        graphics.blit(TEXTURE, thumbX, getThumbY(), scrollable ? 3 : 6, 194, SCROLL_WIDTH, SCROLL_THUMB_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, WIDTH, HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        drawText(graphics, Component.translatable("gui.exoequipment.profiles_count", menu.getProfileCount(), menu.getMaxProfiles()), 9, 9, 88, TEXT_COLOR);
        drawText(graphics, Component.translatable("gui.exoequipment.active_matrices_count", menu.getActiveMatrixCount(), menu.getMaxActiveMatrices()), 104, 9, 119, TEXT_COLOR);
        for (int visible = 0; visible < VISIBLE_PROFILES; visible++) {
            int profile = scrollOffset + visible;
            if (profile >= menu.getProfileCount()) break;
            drawProfileButton(graphics, profile, visible);
        }
        for (int matrix = 0; matrix < 4; matrix++) drawMatrixButton(graphics, matrix);
        drawActionButton(graphics, ProfileActionPayload.CREATE, CREATE_X, menu.getProfileCount() < menu.getMaxProfiles(), Component.translatable("gui.exoequipment.profile_create"));
        drawActionButton(graphics, ProfileActionPayload.REMOVE, DELETE_X, menu.getProfileCount() > 1, Component.translatable("gui.exoequipment.profile_delete"));
        drawActionButton(graphics, -1, BACK_X, true, Component.translatable("gui.exoequipment.back"));
        drawScrollbar(graphics);
    }

    private int profileAt(double mouseX, double mouseY) {
        int localX = (int) mouseX - leftPos, localY = (int) mouseY - topPos;
        if (localX < PROFILE_X || localX >= PROFILE_X + PROFILE_WIDTH || localY < PROFILE_Y) return -1;
        int visible = (localY - PROFILE_Y) / PROFILE_STEP;
        if (visible < 0 || visible >= VISIBLE_PROFILES) return -1;
        int profile = scrollOffset + visible;
        return profile < menu.getProfileCount() ? profile : -1;
    }

    private int matrixAt(double mouseX, double mouseY) {
        int localX = (int) mouseX - leftPos, localY = (int) mouseY - topPos;
        for (int matrix = 0; matrix < 4; matrix++) {
            int column = matrix % 2, row = matrix / 2;
            int x = MATRIX_X + column * (MATRIX_WIDTH + MATRIX_GAP), y = MATRIX_Y + row * (MATRIX_HEIGHT + MATRIX_GAP);
            if (localX >= x && localX < x + MATRIX_WIDTH && localY >= y && localY < y + MATRIX_HEIGHT) return matrix;
        }
        return -1;
    }

    private int actionAt(double mouseX, double mouseY) {
        int localX = (int) mouseX - leftPos, localY = (int) mouseY - topPos;
        if (localY < ACTION_Y || localY >= ACTION_Y + ACTION_HEIGHT) return -2;
        if (localX >= CREATE_X && localX < CREATE_X + ACTION_WIDTH) return ProfileActionPayload.CREATE;
        if (localX >= DELETE_X && localX < DELETE_X + ACTION_WIDTH) return ProfileActionPayload.REMOVE;
        if (localX >= BACK_X && localX < BACK_X + ACTION_WIDTH) return -1;
        return -2;
    }

    private void updateHover(int mouseX, int mouseY) {
        hoveredProfile = profileAt(mouseX, mouseY);
        hoveredMatrix = matrixAt(mouseX, mouseY);
        hoveredAction = actionAt(mouseX, mouseY);
        int localX = mouseX - leftPos, localY = mouseY - topPos;
        hoveredScrollUp = localX >= SCROLL_CONTROL_X && localX < SCROLL_CONTROL_X + SCROLL_ARROW_WIDTH && localY >= SCROLL_ARROW_UP_Y && localY < SCROLL_ARROW_UP_Y + SCROLL_ARROW_HEIGHT;
        hoveredScrollDown = localX >= SCROLL_DOWN_X && localX < SCROLL_DOWN_X + SCROLL_ARROW_WIDTH && localY >= SCROLL_ARROW_DOWN_Y && localY < SCROLL_ARROW_DOWN_Y + SCROLL_ARROW_HEIGHT;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
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
        if (hoveredScrollUp && isScrollable()) { scrollBy(-1); return true; }
        if (hoveredScrollDown && isScrollable()) { scrollBy(1); return true; }

        if (isScrollable()) {
            int thumbY = getThumbY(), localX = (int) mouseX - leftPos, localY = (int) mouseY - topPos;
            if (localX >= SCROLL_THUMB_X && localX < SCROLL_THUMB_X + SCROLL_WIDTH && localY >= thumbY && localY < thumbY + SCROLL_THUMB_HEIGHT) {
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
                sendAction(ProfileActionPayload.TOGGLE_MATRIX, menu.getActiveProfile(), matrix);
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
        if (isScrollable() && mouseX >= leftPos + PROFILE_X && mouseX < leftPos + SCROLL_X + SCROLL_WIDTH
                && mouseY >= topPos + PROFILE_Y && mouseY < topPos + PROFILE_Y + VISIBLE_PROFILES * PROFILE_STEP) {
            scrollBy(scrollY > 0 ? -1 : 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        updateScrollOffset();
        if (renameBox != null && renameProfile >= 0) {
            int visible = renameProfile - scrollOffset;
            if (visible < 0 || visible >= VISIBLE_PROFILES) {
                finishRename(true);
            } else {
                renameBox.setX(leftPos + PROFILE_X + 7);
                renameBox.setY(topPos + PROFILE_Y + visible * PROFILE_STEP + 2);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateHover(mouseX, mouseY);
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (renameBox != null) {
            if (keyCode == 257 || keyCode == 335) { finishRename(true); return true; }
            if (keyCode == 256) { finishRename(false); return true; }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
