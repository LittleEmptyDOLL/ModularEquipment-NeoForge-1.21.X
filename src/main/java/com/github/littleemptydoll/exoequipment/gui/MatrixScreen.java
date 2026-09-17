package com.github.littleemptydoll.exoequipment.gui;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.network.MatrixActionPayload;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class MatrixScreen extends AbstractContainerScreen<MatrixMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, "textures/gui/matrix.png");
    private static final ResourceLocation CONTROLS_TEXTURE = ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, "textures/gui/controls.png");
    private static final int TEXTURE_SIZE = 256;
    private static final int INVENTORY_WIDTH = 188;
    private static final int INVENTORY_HEIGHT = 111;
    private static final int INVENTORY_HEADER_WIDTH = 196;
    private static final int INVENTORY_HEADER_HEIGHT = 9;
    private static final int INVENTORY_OFFSET_Y = -5;
    private static final int MATRIX_GRID_OFFSET_Y = 4;
    private static final int MATRIX_BOTTOM_OFFSET_Y = 4;
    private static final int TOP_CORNER_SIZE = 9;
    private static final int TOP_HEIGHT = 22;
    private static final int SIDE_WIDTH = 9;
    private static final int SIDE_HEIGHT = 18;
    private static final int GRID_BORDER = 7;
    private static final int BUTTON_WIDTH = 14;
    private static final int BUTTON_HEIGHT = 11;
    private static final int BUTTON_Y = 6;
    private static final int BUTTON_NORMAL_U = 76;
    private static final int BUTTON_HOVER_U = 90;
    private static final int MODULE_BACKGROUND_COLOR = 0xFF2C5366;
    private static final int MODULE_BORDER_COLOR = 0xFF76B5C9;
    private static final int MODULE_PREVIEW_BACKGROUND_COLOR = 0x553F7185;
    private static final int MODULE_PREVIEW_BORDER_COLOR = 0xFF9ED9EA;

    private boolean characteristicsHovered;
    private boolean draggingModule;
    private InstalledModule draggedModule;
    private int draggedRotation;
    private int dragStartX;
    private int dragStartY;
    private int dragStartMouseX;
    private int dragStartMouseY;
    private boolean dragMoved;
    private int lastDragPreviewX;
    private int lastDragPreviewY;
    private boolean hasLastDragPreview;
    private int carriedModuleRotation;
    private ModuleItem carriedRotationItem;
    private int pendingMoveX;
    private int pendingMoveY;
    private int pendingMoveRotation;
    private boolean pendingMove;

    public MatrixScreen(MatrixMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = menu.getImageWidth();
        this.imageHeight = menu.getImageHeight();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        renderMatrixBackground(guiGraphics);
        renderMatrixGrid(guiGraphics, mouseX, mouseY);
        renderInventoryBackground(guiGraphics);
    }

    private void renderMatrixBackground(GuiGraphics guiGraphics) {
        int width = imageWidth;
        int gridX = leftPos + 9;
        int gridY = topPos + 22;
        int gridWidth = width - 18;
        int gridBottom = MatrixMenu.GRID_Y + MATRIX_GRID_OFFSET_Y + menu.getMatrixHeight() * MatrixMenu.CELL_SIZE;
        int lowerBorderY = gridBottom + MATRIX_BOTTOM_OFFSET_Y;

        guiGraphics.blit(TEXTURE, leftPos, topPos, 188, 0, 9, 22, TEXTURE_SIZE, TEXTURE_SIZE);
        guiGraphics.blit(TEXTURE, leftPos + width - TOP_CORNER_SIZE, topPos, 197, 0, 9, 22, TEXTURE_SIZE, TEXTURE_SIZE);
        drawTopEdge(guiGraphics, leftPos + TOP_CORNER_SIZE, topPos, width - TOP_CORNER_SIZE * 2);

        // Rear matrix background. It is rendered before the matrix slot frame,
        // so the 18x18 texture fills the area behind the slots instead of becoming their fill.
        fillMatrixBackground(guiGraphics, gridX, gridY, gridWidth, lowerBorderY - MatrixMenu.GRID_Y);

        int sideStartY = topPos + TOP_HEIGHT;
        int sideEndY = topPos + lowerBorderY;
        drawVerticalRepeat(guiGraphics, leftPos, sideStartY, sideEndY - sideStartY, 224, 9, SIDE_WIDTH, SIDE_HEIGHT);
        drawVerticalRepeat(guiGraphics, leftPos + width - SIDE_WIDTH, sideStartY, sideEndY - sideStartY, 233, 9, SIDE_WIDTH, SIDE_HEIGHT);

        if (menu.getMatrixWidth() <= 9) {
            guiGraphics.blit(TEXTURE, leftPos, topPos + lowerBorderY, 224, 0, 9, 9, TEXTURE_SIZE, TEXTURE_SIZE);
            guiGraphics.blit(TEXTURE, leftPos + width - 9, topPos + lowerBorderY, 233, 0, 9, 9, TEXTURE_SIZE, TEXTURE_SIZE);
        } else {
            guiGraphics.blit(TEXTURE, leftPos, topPos + lowerBorderY, 206, 0, 9, 9, TEXTURE_SIZE, TEXTURE_SIZE);
            guiGraphics.blit(TEXTURE, leftPos + width - 9, topPos + lowerBorderY, 215, 0, 9, 9, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        drawHorizontalRepeat(guiGraphics, leftPos + 9, topPos + lowerBorderY, width - 18, 206, 9, 18, 9);
    }

    private void fillMatrixBackground(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int remainingHeight = height;
        int currentY = y;
        while (remainingHeight > 0) {
            int partHeight = Math.min(18, remainingHeight);
            int remainingWidth = width;
            int currentX = x;
            while (remainingWidth > 0) {
                int partWidth = Math.min(18, remainingWidth);
                blitPartial(guiGraphics, currentX, currentY, 188, 22, partWidth, partHeight);
                currentX += partWidth;
                remainingWidth -= partWidth;
            }
            currentY += partHeight;
            remainingHeight -= partHeight;
        }
    }

    private void drawTopEdge(GuiGraphics guiGraphics, int x, int y, int width) {
        int remaining = width;
        int currentX = x;
        int leftWidth = Math.min(78, remaining);
        if (leftWidth > 0) {
            blitPartial(guiGraphics, currentX, y, 0, 120, leftWidth, 22);
            currentX += leftWidth;
            remaining -= leftWidth;
        }
        if (remaining > 0) {
            int rightWidth = Math.min(20, remaining);
            int middleWidth = remaining - rightWidth;
            drawHorizontalRepeat(guiGraphics, currentX, y, middleWidth, 78, 120, 18, 22);
            currentX += middleWidth;
            remaining -= middleWidth;
            if (remaining > 0) blitPartial(guiGraphics, currentX, y, 96, 120, rightWidth, 22);
        }
    }

    private void drawHorizontalRepeat(GuiGraphics guiGraphics, int x, int y, int width, int u, int v, int sourceWidth, int sourceHeight) {
        int remaining = width;
        int currentX = x;
        while (remaining > 0) {
            int part = Math.min(sourceWidth, remaining);
            blitPartial(guiGraphics, currentX, y, u, v, part, sourceHeight);
            currentX += part;
            remaining -= part;
        }
    }

    private void drawVerticalRepeat(GuiGraphics guiGraphics, int x, int y, int height, int u, int v, int sourceWidth, int sourceHeight) {
        int remaining = height;
        int currentY = y;
        while (remaining > 0) {
            int part = Math.min(sourceHeight, remaining);
            blitPartial(guiGraphics, x, currentY, u, v, sourceWidth, part);
            currentY += part;
            remaining -= part;
        }
    }

    private void blitPartial(GuiGraphics guiGraphics, int x, int y, int u, int v, int width, int height) {
        guiGraphics.blit(TEXTURE, x, y, u, v, width, height, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private void renderMatrixGrid(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int gridX = leftPos + menu.getGridX();
        int gridY = topPos + MatrixMenu.GRID_Y + MATRIX_GRID_OFFSET_Y;
        int gridWidth = menu.getMatrixWidth() * MatrixMenu.CELL_SIZE;
        int gridHeight = menu.getMatrixHeight() * MatrixMenu.CELL_SIZE;
        drawGridFrame(guiGraphics, gridX, gridY, gridWidth, gridHeight);

        if (pendingMove && isPendingMoveApplied()) {
            pendingMove = false;
            draggedModule = null;
            hasLastDragPreview = false;
        }

        int[] previewOrigin = getPreviewOrigin(mouseX, mouseY);
        for (InstalledModule module : menu.getMatrixData().modules()) {
            if (draggingModule && module.equals(draggedModule)) {
                if (previewOrigin != null) renderModule(guiGraphics, module, gridX, gridY, previewOrigin[0], previewOrigin[1], draggedRotation);
            } else {
                renderModule(guiGraphics, module, gridX, gridY, module.x(), module.y(), module.rotation());
            }
        }
        if (previewOrigin != null && draggedModule == null && menu.getCarried().getItem() instanceof ModuleItem) {
            renderCarriedModulePreview(guiGraphics, gridX, gridY, previewOrigin[0], previewOrigin[1]);
        }
    }

    private void drawGridFrame(GuiGraphics guiGraphics, int gridX, int gridY,
                               int gridWidth, int gridHeight) {
        int frameX = gridX - GRID_BORDER;
        int frameY = gridY - GRID_BORDER;
        int frameWidth = gridWidth + GRID_BORDER * 2;
        int frameHeight = gridHeight + GRID_BORDER * 2;

        guiGraphics.blit(TEXTURE, frameX, frameY, 0, 142, 7, 7,
                TEXTURE_SIZE, TEXTURE_SIZE);
        guiGraphics.blit(TEXTURE, frameX + frameWidth - 7, frameY, 7, 142, 7, 7,
                TEXTURE_SIZE, TEXTURE_SIZE);
        guiGraphics.blit(TEXTURE, frameX, frameY + frameHeight - 7, 14, 142, 7, 7,
                TEXTURE_SIZE, TEXTURE_SIZE);
        guiGraphics.blit(TEXTURE, frameX + frameWidth - 7, frameY + frameHeight - 7,
                21, 142, 7, 7, TEXTURE_SIZE, TEXTURE_SIZE);

        drawHorizontalRepeat(guiGraphics, frameX + 7, frameY, gridWidth,
                28, 142, 18, 7);
        drawHorizontalRepeat(guiGraphics, frameX + 7, frameY + frameHeight - 7,
                gridWidth, 46, 142, 18, 7);
        drawVerticalRepeat(guiGraphics, frameX, frameY + 7, gridHeight,
                0, 149, 7, 18);
        drawVerticalRepeat(guiGraphics, frameX + frameWidth - 7, frameY + 7, gridHeight,
                7, 149, 7, 18);

        for (int row = 0; row < menu.getMatrixHeight(); row++) {
            for (int column = 0; column < menu.getMatrixWidth(); column++) {
                guiGraphics.blit(TEXTURE,
                        gridX + column * MatrixMenu.CELL_SIZE,
                        gridY + row * MatrixMenu.CELL_SIZE,
                        14, 149, 18, 18, TEXTURE_SIZE, TEXTURE_SIZE);
            }
        }
    }

    private void renderInventoryBackground(GuiGraphics guiGraphics) {
        int inventoryX = leftPos + (imageWidth - INVENTORY_WIDTH) / 2;
        int inventoryY = topPos + menu.getInventoryY() + INVENTORY_OFFSET_Y;
        guiGraphics.blit(TEXTURE, inventoryX, inventoryY, 0, 0, INVENTORY_WIDTH, INVENTORY_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        if (menu.getMatrixWidth() > 9) {
            guiGraphics.blit(TEXTURE, inventoryX - 4, inventoryY, 0, 111, INVENTORY_HEADER_WIDTH, INVENTORY_HEADER_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        guiGraphics.drawString(font, Component.translatable("gui.exoequipment.inventory"), inventoryX + 14, inventoryY + 6, 0xFFD8EAF5, false);
    }

    private void renderCarriedModulePreview(GuiGraphics guiGraphics, int gridX, int gridY, int moduleX, int moduleY) {
        ModuleItem moduleItem = (ModuleItem) menu.getCarried().getItem();
        ModuleSize size = MatrixOperations.getRotatedSize(moduleItem.getDefinition().size(), carriedModuleRotation);
        int x = gridX + moduleX * MatrixMenu.CELL_SIZE + 2;
        int y = gridY + moduleY * MatrixMenu.CELL_SIZE + 2;
        int width = size.width() * MatrixMenu.CELL_SIZE - 3;
        int height = size.height() * MatrixMenu.CELL_SIZE - 3;
        guiGraphics.fill(x, y, x + width, y + height, MODULE_PREVIEW_BACKGROUND_COLOR);
        guiGraphics.renderOutline(x, y, width, height, MODULE_PREVIEW_BORDER_COLOR);
        renderScaledItem(guiGraphics, menu.getCarried(), x, y, width, height);
    }

    private void renderModule(GuiGraphics guiGraphics, InstalledModule module, int gridX, int gridY, int moduleX, int moduleY, int rotation) {
        ModuleDefinition definition = ModModules.getDefinition(module.id());
        ModuleSize size = MatrixOperations.getRotatedSize(definition.size(), rotation);
        int x = gridX + moduleX * MatrixMenu.CELL_SIZE + 2;
        int y = gridY + moduleY * MatrixMenu.CELL_SIZE + 2;
        int width = size.width() * MatrixMenu.CELL_SIZE - 3;
        int height = size.height() * MatrixMenu.CELL_SIZE - 3;
        guiGraphics.fill(x, y, x + width, y + height, MODULE_BACKGROUND_COLOR);
        guiGraphics.renderOutline(x, y, width, height, MODULE_BORDER_COLOR);
        ItemStack stack = createModuleStack(module);
        if (!stack.isEmpty()) renderScaledItem(guiGraphics, stack, x, y, width, height);
    }

    private void renderScaledItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y, int width, int height) {
        float scale = Math.min(width, height) / 16.0F;
        if (scale <= 1.0F) {
            guiGraphics.renderItem(stack, x + Math.max(0, (width - 16) / 2), y + Math.max(0, (height - 16) / 2));
            return;
        }
        int renderedSize = Math.round(16.0F * scale);
        int renderX = x + (width - renderedSize) / 2;
        int renderY = y + (height - renderedSize) / 2;
        var pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(renderX, renderY, 0);
        pose.scale(scale, scale, 1.0F);
        guiGraphics.renderItem(stack, 0, 0);
        pose.popPose();
    }

    private ItemStack createModuleStack(InstalledModule module) {
        var entry = ModModules.find(module.id());
        if (entry == null) return ItemStack.EMPTY;
        ItemStack stack = entry.getItem().getDefaultInstance();
        ModModules.getDefinition(module.id()).storage().ifPresent(storage -> {
            int storedEnergy = Math.min(menu.getSyncedStoredEnergy(module.x(), module.y()), storage.capacity());
            if (storedEnergy > 0) stack.set(ModDataComponents.MODULE_STORED_ENERGY.get(), storedEnergy);
        });
        return stack;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, Component.translatable("gui.exoequipment.matrix_size", menu.getMatrixWidth(), menu.getMatrixHeight()),
                14, 8, 0xFFD8EAF5, false);
        drawCharacteristicsButton(guiGraphics);
    }

    private void drawCharacteristicsButton(GuiGraphics guiGraphics) {
        int x = imageWidth - 23;
        guiGraphics.blit(CONTROLS_TEXTURE, x, BUTTON_Y, characteristicsHovered ? BUTTON_HOVER_U : BUTTON_NORMAL_U,
                0, BUTTON_WIDTH, BUTTON_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (!slot.hasItem()) return;
        guiGraphics.renderItem(slot.getItem(), slot.x, slot.y);
        guiGraphics.renderItemDecorations(font, slot.getItem(), slot.x, slot.y);
    }

    @Override
    protected void renderSlotHighlight(GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, float partialTick) {
        super.renderSlotHighlight(guiGraphics, slot, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        InstalledModule module = getModuleAtMouse(mouseX, mouseY);
        if (module == null) {
            super.renderTooltip(guiGraphics, mouseX, mouseY);
            return;
        }
        ItemStack stack = createModuleStack(module);
        if (!stack.isEmpty()) guiGraphics.renderTooltip(font, stack, mouseX, mouseY);
    }

    private InstalledModule getModuleAtMouse(int mouseX, int mouseY) {
        int[] cell = getCellAtMouse(mouseX, mouseY);
        if (cell == null) return null;
        return MatrixOperations.getModuleAt(menu.getMatrixData(), cell[0], cell[1]);
    }

    private int[] getCellAtMouse(int mouseX, int mouseY) {
        int gridX = leftPos + menu.getGridX();
        int gridY = topPos + MatrixMenu.GRID_Y + MATRIX_GRID_OFFSET_Y;
        int localX = mouseX - gridX;
        int localY = mouseY - gridY;
        if (localX < 0 || localY < 0) return null;
        int cellX = localX / MatrixMenu.CELL_SIZE;
        int cellY = localY / MatrixMenu.CELL_SIZE;
        if (cellX >= menu.getMatrixWidth() || cellY >= menu.getMatrixHeight()) return null;
        return new int[]{cellX, cellY};
    }

    private ModuleSize getModuleSize(InstalledModule module) {
        ModuleDefinition definition = ModModules.getDefinition(module.id());
        return MatrixOperations.getRotatedSize(definition.size(), draggedRotation);
    }

    private int[] getCenteredOrigin(int centerX, int centerY, ModuleSize size) {
        return clampOrigin(centerX - size.width() / 2, centerY - size.height() / 2, size);
    }

    private int[] clampOrigin(int originX, int originY, ModuleSize size) {
        int maxX = Math.max(0, menu.getMatrixWidth() - size.width());
        int maxY = Math.max(0, menu.getMatrixHeight() - size.height());
        return new int[]{Math.max(0, Math.min(originX, maxX)), Math.max(0, Math.min(originY, maxY))};
    }

    private int[] getPreviewOrigin(int mouseX, int mouseY) {
        int[] cell = getCellAtMouse(mouseX, mouseY);
        if (draggedModule != null) {
            if (cell != null) {
                int[] origin = getCenteredOrigin(cell[0], cell[1], getModuleSize(draggedModule));
                lastDragPreviewX = origin[0];
                lastDragPreviewY = origin[1];
                hasLastDragPreview = true;
                return origin;
            }
            if (hasLastDragPreview) return new int[]{lastDragPreviewX, lastDragPreviewY};
            return null;
        }
        if (menu.getCarried().getItem() instanceof ModuleItem moduleItem) {
            if (carriedRotationItem != moduleItem) {
                carriedRotationItem = moduleItem;
                carriedModuleRotation = 0;
            }
            if (cell == null) return null;
            return getCenteredOrigin(cell[0], cell[1], MatrixOperations.getRotatedSize(moduleItem.getDefinition().size(), carriedModuleRotation));
        }
        carriedRotationItem = null;
        carriedModuleRotation = 0;
        return null;
    }

    private boolean isPendingMoveApplied() {
        if (!pendingMove || draggedModule == null) return false;
        InstalledModule target = MatrixOperations.getModuleAt(menu.getMatrixData(), pendingMoveX, pendingMoveY);
        return target != null && target.id().equals(draggedModule.id()) && target.x() == pendingMoveX && target.y() == pendingMoveY && target.rotation() == pendingMoveRotation;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int[] cell = getCellAtMouse((int) mouseX, (int) mouseY);
        if (button == 1) {
            if (draggingModule && draggedModule != null) {
                draggedRotation = MatrixOperations.normalizeRotation(draggedRotation + 90);
                if (hasLastDragPreview) {
                    int[] origin = getCenteredOrigin(lastDragPreviewX, lastDragPreviewY, getModuleSize(draggedModule));
                    lastDragPreviewX = origin[0];
                    lastDragPreviewY = origin[1];
                }
                return true;
            }
            if (cell != null && menu.getCarried().getItem() instanceof ModuleItem moduleItem) {
                if (carriedRotationItem != moduleItem) {
                    carriedRotationItem = moduleItem;
                    carriedModuleRotation = 0;
                }
                carriedModuleRotation = MatrixOperations.normalizeRotation(carriedModuleRotation + 90);
                return true;
            }
            if (cell != null) {
                InstalledModule module = MatrixOperations.getModuleAt(menu.getMatrixData(), cell[0], cell[1]);
                if (module != null) {
                    sendAction(MatrixMenu.ACTION_ROTATE, cell[0], cell[1], cell[0], cell[1], 0);
                    return true;
                }
            }
        }
        if (cell != null && button == 0) {
            InstalledModule module = MatrixOperations.getModuleAt(menu.getMatrixData(), cell[0], cell[1]);
            if (menu.getCarried().getItem() instanceof ModuleItem) {
                int[] target = getPreviewOrigin((int) mouseX, (int) mouseY);
                if (target != null) sendAction(MatrixMenu.ACTION_PLACE, target[0], target[1], target[0], target[1], carriedModuleRotation);
                return true;
            }
            if (module != null) {
                draggingModule = true;
                draggedModule = module;
                draggedRotation = module.rotation();
                dragStartX = module.x();
                dragStartY = module.y();
                dragStartMouseX = cell[0];
                dragStartMouseY = cell[1];
                dragMoved = false;
                hasLastDragPreview = true;
                lastDragPreviewX = module.x();
                lastDragPreviewY = module.y();
                pendingMove = false;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && draggingModule) {
            int[] cell = getCellAtMouse((int) mouseX, (int) mouseY);
            if (cell != null && (cell[0] != dragStartMouseX || cell[1] != dragStartMouseY)) dragMoved = true;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingModule) {
            int[] cell = getCellAtMouse((int) mouseX, (int) mouseY);
            InstalledModule module = draggedModule;
            draggingModule = false;
            if (cell == null) {
                draggedModule = null;
                hasLastDragPreview = false;
                return true;
            }
            int[] target = getCenteredOrigin(cell[0], cell[1], getModuleSize(module));
            boolean rotationChanged = draggedRotation != module.rotation();
            boolean remove = !dragMoved && !rotationChanged;
            if (remove) {
                draggedModule = null;
                hasLastDragPreview = false;
                sendAction(MatrixMenu.ACTION_REMOVE, dragStartX, dragStartY, dragStartX, dragStartY, 0);
            } else if (target[0] != dragStartX || target[1] != dragStartY || rotationChanged) {
                pendingMove = true;
                pendingMoveX = target[0];
                pendingMoveY = target[1];
                pendingMoveRotation = draggedRotation;
                sendAction(MatrixMenu.ACTION_MOVE, dragStartX, dragStartY, target[0], target[1], draggedRotation);
            } else {
                draggedModule = null;
                hasLastDragPreview = false;
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void sendAction(int action, int x, int y, int targetX, int targetY, int rotation) {
        PacketDistributor.sendToServer(new MatrixActionPayload(action, x, y, targetX, targetY, rotation));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
