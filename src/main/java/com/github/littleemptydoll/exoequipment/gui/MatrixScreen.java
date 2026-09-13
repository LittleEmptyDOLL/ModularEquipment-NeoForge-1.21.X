package com.github.littleemptydoll.exoequipment.gui;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.network.MatrixActionPayload;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class MatrixScreen extends AbstractContainerScreen<MatrixMenu> {
    private static final int BACKGROUND_FALLBACK_COLOR = 0xFF10151A;
    private static final int GRID_BACKGROUND_COLOR = 0xFF1A2229;
    private static final int GRID_LINE_COLOR = 0xFF35434D;
    private static final int MODULE_BACKGROUND_COLOR = 0xFF2C5366;
    private static final int MODULE_BORDER_COLOR = 0xFF76B5C9;
    private static final int MODULE_PREVIEW_BACKGROUND_COLOR = 0x553F7185;
    private static final int MODULE_PREVIEW_BORDER_COLOR = 0xFF9ED9EA;

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
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight,
                BACKGROUND_FALLBACK_COLOR);
        renderMatrixGrid(guiGraphics, mouseX, mouseY);
    }

    private void renderMatrixGrid(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int gridX = leftPos + MatrixMenu.GRID_X;
        int gridY = topPos + MatrixMenu.GRID_Y;
        int gridWidth = menu.getMatrixWidth() * MatrixMenu.CELL_SIZE;
        int gridHeight = menu.getMatrixHeight() * MatrixMenu.CELL_SIZE;

        guiGraphics.fill(gridX, gridY, gridX + gridWidth, gridY + gridHeight, GRID_BACKGROUND_COLOR);
        for (int column = 0; column <= menu.getMatrixWidth(); column++) {
            int lineX = gridX + column * MatrixMenu.CELL_SIZE;
            guiGraphics.fill(lineX, gridY, lineX + 1, gridY + gridHeight, GRID_LINE_COLOR);
        }
        for (int row = 0; row <= menu.getMatrixHeight(); row++) {
            int lineY = gridY + row * MatrixMenu.CELL_SIZE;
            guiGraphics.fill(gridX, lineY, gridX + gridWidth, lineY + 1, GRID_LINE_COLOR);
        }

        if (pendingMove && isPendingMoveApplied()) {
            pendingMove = false;
            draggedModule = null;
        }

        int[] previewOrigin = getPreviewOrigin(mouseX, mouseY);
        for (InstalledModule module : menu.getMatrixData().modules()) {
            if (draggingModule && module.equals(draggedModule)) {
                if (previewOrigin != null) {
                    renderModule(guiGraphics, module, gridX, gridY,
                            previewOrigin[0], previewOrigin[1], draggedRotation);
                }
            } else {
                renderModule(guiGraphics, module, gridX, gridY,
                        module.x(), module.y(), module.rotation());
            }
        }

        if (previewOrigin != null && draggedModule == null && menu.getCarried().getItem() instanceof ModuleItem) {
            renderCarriedModulePreview(guiGraphics, gridX, gridY, previewOrigin[0], previewOrigin[1]);
        }
    }

    private void renderCarriedModulePreview(GuiGraphics guiGraphics, int gridX, int gridY,
                                            int moduleX, int moduleY) {
        ModuleItem moduleItem = (ModuleItem) menu.getCarried().getItem();
        ModuleSize size = MatrixOperations.getRotatedSize(moduleItem.getDefinition().size(), carriedModuleRotation);
        int x = gridX + moduleX * MatrixMenu.CELL_SIZE + 2;
        int y = gridY + moduleY * MatrixMenu.CELL_SIZE + 2;
        int width = size.width() * MatrixMenu.CELL_SIZE - 3;
        int height = size.height() * MatrixMenu.CELL_SIZE - 3;

        guiGraphics.fill(x, y, x + width, y + height, MODULE_PREVIEW_BACKGROUND_COLOR);
        guiGraphics.renderOutline(x, y, width, height, MODULE_PREVIEW_BORDER_COLOR);
        guiGraphics.renderItem(menu.getCarried(), x + Math.max(0, (width - 16) / 2),
                y + Math.max(0, (height - 16) / 2));
    }

    private void renderModule(GuiGraphics guiGraphics, InstalledModule module,
                              int gridX, int gridY, int moduleX, int moduleY, int rotation) {
        ModuleDefinition definition = ModModules.getDefinition(module.id());
        ModuleSize size = MatrixOperations.getRotatedSize(definition.size(), rotation);
        int x = gridX + moduleX * MatrixMenu.CELL_SIZE + 2;
        int y = gridY + moduleY * MatrixMenu.CELL_SIZE + 2;
        int width = size.width() * MatrixMenu.CELL_SIZE - 3;
        int height = size.height() * MatrixMenu.CELL_SIZE - 3;

        guiGraphics.fill(x, y, x + width, y + height, MODULE_BACKGROUND_COLOR);
        guiGraphics.renderOutline(x, y, width, height, MODULE_BORDER_COLOR);
        var entry = ModModules.find(module.id());
        if (entry != null) {
            ItemStack stack = entry.getItem().getDefaultInstance();
            guiGraphics.renderItem(stack, x + Math.max(0, (width - 16) / 2),
                    y + Math.max(0, (height - 16) / 2));
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, Component.translatable("gui.exoequipment.matrix"),
                12, 7, 0xFFD8EAF5, false);
        guiGraphics.drawString(font, menu.getMatrixWidth() + " x " + menu.getMatrixHeight(),
                12, 8 + font.lineHeight, 0xFF9DB4C0, false);
        guiGraphics.drawString(font, Component.translatable("gui.exoequipment.inventory"),
                MatrixMenu.INVENTORY_X, menu.getInventoryY() - 11, 0xFFD8EAF5, false);
    }

    /**
     * Container slots are handled by AbstractContainerScreen. The matrix grid
     * is not made of Slot instances, so its module tooltip is rendered
     * explicitly after the normal screen rendering pass.
     */
    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderMatrixTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        InstalledModule module = getModuleAtMouse(mouseX, mouseY);
        if (module == null) {
            return;
        }

        var entry = ModModules.find(module.id());
        if (entry != null) {
            guiGraphics.renderTooltip(font, entry.getItem().getDefaultInstance(), mouseX, mouseY);
        }
    }

    private InstalledModule getModuleAtMouse(int mouseX, int mouseY) {
        int[] cell = getCellAtMouse(mouseX, mouseY);
        if (cell == null) return null;
        return MatrixOperations.getModuleAt(menu.getMatrixData(), cell[0], cell[1]);
    }

    private int[] getCellAtMouse(int mouseX, int mouseY) {
        int gridX = leftPos + MatrixMenu.GRID_X;
        int gridY = topPos + MatrixMenu.GRID_Y;
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
        int originX = centerX - size.width() / 2;
        int originY = centerY - size.height() / 2;
        return clampOrigin(originX, originY, size);
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
            if (hasLastDragPreview) {
                return new int[]{lastDragPreviewX, lastDragPreviewY};
            }
            return null;
        }
        if (menu.getCarried().getItem() instanceof ModuleItem moduleItem) {
            if (carriedRotationItem != moduleItem) {
                carriedRotationItem = moduleItem;
                carriedModuleRotation = 0;
            }
            if (cell == null) return null;
            return getCenteredOrigin(cell[0], cell[1],
                    MatrixOperations.getRotatedSize(moduleItem.getDefinition().size(), carriedModuleRotation));
        }
        carriedRotationItem = null;
        carriedModuleRotation = 0;
        return null;
    }

    private boolean isPendingMoveApplied() {
        if (!pendingMove || draggedModule == null) return false;

        InstalledModule target = MatrixOperations.getModuleAt(
                menu.getMatrixData(), pendingMoveX, pendingMoveY);
        return target != null
                && target.id().equals(draggedModule.id())
                && target.x() == pendingMoveX
                && target.y() == pendingMoveY
                && target.rotation() == pendingMoveRotation;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int[] cell = getCellAtMouse((int) mouseX, (int) mouseY);
        if (button == 1) {
            if (draggingModule && draggedModule != null) {
                draggedRotation = MatrixOperations.normalizeRotation(draggedRotation + 90);
                if (hasLastDragPreview) {
                    int[] origin = getCenteredOrigin(lastDragPreviewX, lastDragPreviewY,
                            getModuleSize(draggedModule));
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
                if (target != null) {
                    sendAction(MatrixMenu.ACTION_PLACE, target[0], target[1],
                            target[0], target[1], carriedModuleRotation);
                }
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
            if (cell != null && (cell[0] != dragStartMouseX || cell[1] != dragStartMouseY)) {
                dragMoved = true;
            }
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
                sendAction(MatrixMenu.ACTION_REMOVE, dragStartX, dragStartY,
                        dragStartX, dragStartY, 0);
            } else if (target[0] != dragStartX || target[1] != dragStartY || rotationChanged) {
                pendingMove = true;
                pendingMoveX = target[0];
                pendingMoveY = target[1];
                pendingMoveRotation = draggedRotation;
                sendAction(MatrixMenu.ACTION_MOVE, dragStartX, dragStartY,
                        target[0], target[1], draggedRotation);
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
        renderMatrixTooltip(guiGraphics, mouseX, mouseY);
    }
}
