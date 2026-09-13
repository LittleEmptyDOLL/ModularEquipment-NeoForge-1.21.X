package com.github.littleemptydoll.exoequipment.gui;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.network.MatrixActionPayload;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class MatrixScreen extends AbstractContainerScreen<MatrixMenu> {

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "textures/gui/matrix.png"
            );

    private static final int BACKGROUND_FALLBACK_COLOR = 0xFF10151A;
    private static final int GRID_BACKGROUND_COLOR = 0xFF1A2229;
    private static final int GRID_LINE_COLOR = 0xFF35434D;
    private static final int MODULE_BACKGROUND_COLOR = 0xFF2C5366;
    private static final int MODULE_BORDER_COLOR = 0xFF76B5C9;

    private boolean draggingModule;
    private int dragStartX;
    private int dragStartY;

    public MatrixScreen(
            MatrixMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
        this.imageWidth = menu.getImageWidth();
        this.imageHeight = menu.getImageHeight();
    }

    @Override
    protected void renderBg(
            GuiGraphics guiGraphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        int x = leftPos;
        int y = topPos;

        guiGraphics.fill(
                x,
                y,
                x + imageWidth,
                y + imageHeight,
                BACKGROUND_FALLBACK_COLOR
        );

        renderMatrixGrid(guiGraphics);
    }

    private void renderMatrixGrid(GuiGraphics guiGraphics) {
        int x = leftPos + MatrixMenu.GRID_X;
        int y = topPos + MatrixMenu.GRID_Y;
        int width = menu.getMatrixWidth() * MatrixMenu.CELL_SIZE;
        int height = menu.getMatrixHeight() * MatrixMenu.CELL_SIZE;

        guiGraphics.fill(
                x,
                y,
                x + width,
                y + height,
                GRID_BACKGROUND_COLOR
        );

        for (int column = 0; column <= menu.getMatrixWidth(); column++) {
            int lineX = x + column * MatrixMenu.CELL_SIZE;
            guiGraphics.fill(
                    lineX,
                    y,
                    lineX + 1,
                    y + height,
                    GRID_LINE_COLOR
            );
        }

        for (int row = 0; row <= menu.getMatrixHeight(); row++) {
            int lineY = y + row * MatrixMenu.CELL_SIZE;
            guiGraphics.fill(
                    x,
                    lineY,
                    x + width,
                    lineY + 1,
                    GRID_LINE_COLOR
            );
        }

        for (InstalledModule module : menu.getMatrixData().modules()) {
            renderModule(guiGraphics, module, x, y);
        }
    }

    private void renderModule(
            GuiGraphics guiGraphics,
            InstalledModule module,
            int gridX,
            int gridY
    ) {
        ModuleDefinition definition = ModModules.getDefinition(module.id());
        ModuleSize size = MatrixOperations.getRotatedSize(
                definition.size(),
                module.rotation()
        );

        int x = gridX + module.x() * MatrixMenu.CELL_SIZE + 2;
        int y = gridY + module.y() * MatrixMenu.CELL_SIZE + 2;
        int width = size.width() * MatrixMenu.CELL_SIZE - 3;
        int height = size.height() * MatrixMenu.CELL_SIZE - 3;

        guiGraphics.fill(
                x,
                y,
                x + width,
                y + height,
                MODULE_BACKGROUND_COLOR
        );

        guiGraphics.renderOutline(
                x,
                y,
                width,
                height,
                MODULE_BORDER_COLOR
        );

        var entry = ModModules.find(module.id());
        if (entry != null) {
            ItemStack stack = entry.getItem().getDefaultInstance();
            guiGraphics.renderItem(
                    stack,
                    x + Math.max(0, (width - 16) / 2),
                    y + Math.max(0, (height - 16) / 2)
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
                Component.translatable("gui.exoequipment.matrix"),
                12,
                7,
                0xFFD8EAF5,
                false
        );

        guiGraphics.drawString(
                font,
                menu.getMatrixWidth() + " x " + menu.getMatrixHeight(),
                12,
                8 + font.lineHeight,
                0xFF9DB4C0,
                false
        );

        guiGraphics.drawString(
                font,
                Component.translatable("gui.exoequipment.inventory"),
                MatrixMenu.INVENTORY_X,
                menu.getInventoryY() - 11,
                0xFFD8EAF5,
                false
        );
    }

    @Override
    protected void renderTooltip(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        InstalledModule module = getModuleAtMouse(mouseX, mouseY);
        if (module == null) {
            return;
        }

        var entry = ModModules.find(module.id());
        if (entry == null) {
            return;
        }

        renderComponentTooltip(
                guiGraphics,
                entry.getItem().getDefaultInstance().getHoverName(),
                mouseX,
                mouseY
        );
    }

    private InstalledModule getModuleAtMouse(int mouseX, int mouseY) {
        int[] cell = getCellAtMouse(mouseX, mouseY);
        if (cell == null) {
            return null;
        }

        return MatrixOperations.getModuleAt(
                menu.getMatrixData(),
                cell[0],
                cell[1]
        );
    }

    private int[] getCellAtMouse(int mouseX, int mouseY) {
        int gridX = leftPos + MatrixMenu.GRID_X;
        int gridY = topPos + MatrixMenu.GRID_Y;
        int localX = mouseX - gridX;
        int localY = mouseY - gridY;

        if (localX < 0 || localY < 0) {
            return null;
        }

        int cellX = localX / MatrixMenu.CELL_SIZE;
        int cellY = localY / MatrixMenu.CELL_SIZE;

        if (cellX >= menu.getMatrixWidth() || cellY >= menu.getMatrixHeight()) {
            return null;
        }

        return new int[]{cellX, cellY};
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int[] cell = getCellAtMouse((int) mouseX, (int) mouseY);
        if (cell != null) {
            InstalledModule module = MatrixOperations.getModuleAt(
                    menu.getMatrixData(),
                    cell[0],
                    cell[1]
            );

            if (button == 0) {
                if (menu.getCarried().getItem() instanceof ModuleItem) {
                    sendAction(
                            MatrixMenu.ACTION_PLACE,
                            cell[0],
                            cell[1],
                            cell[0],
                            cell[1]
                    );
                    return true;
                }

                if (module != null) {
                    draggingModule = true;
                    dragStartX = module.x();
                    dragStartY = module.y();
                    return true;
                }
            }

            if (button == 1 && module != null) {
                sendAction(
                        MatrixMenu.ACTION_ROTATE,
                        cell[0],
                        cell[1],
                        cell[0],
                        cell[1]
                );
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingModule) {
            draggingModule = false;

            int[] cell = getCellAtMouse((int) mouseX, (int) mouseY);
            if (cell == null) {
                return true;
            }

            if (cell[0] == dragStartX && cell[1] == dragStartY) {
                sendAction(
                        MatrixMenu.ACTION_REMOVE,
                        dragStartX,
                        dragStartY,
                        dragStartX,
                        dragStartY
                );
            } else {
                sendAction(
                        MatrixMenu.ACTION_MOVE,
                        dragStartX,
                        dragStartY,
                        cell[0],
                        cell[1]
                );
            }

            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void sendAction(
            int action,
            int x,
            int y,
            int targetX,
            int targetY
    ) {
        PacketDistributor.sendToServer(
                new MatrixActionPayload(action, x, y, targetX, targetY)
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
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
