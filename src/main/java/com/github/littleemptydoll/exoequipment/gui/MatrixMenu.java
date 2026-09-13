package com.github.littleemptydoll.exoequipment.gui;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.item.MatrixItem;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixDefinition;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MatrixMenu extends AbstractContainerMenu {

    public static final int SOURCE_HAND = 0;
    public static final int SOURCE_EXOSKELETON = 1;

    public static final int PLAYER_INVENTORY_START = 0;
    public static final int PLAYER_INVENTORY_END = 36;

    public static final int CELL_SIZE = 18;
    public static final int GRID_X = 12;
    public static final int GRID_Y = 22;
    public static final int INVENTORY_X = 12;
    public static final int INVENTORY_GAP = 18;

    private final ItemStack matrixStack;
    private final int sourceType;
    private final int sourceIndex;
    private final int width;
    private final int height;
    private final int imageWidth;
    private final int imageHeight;

    // Client
    public MatrixMenu(
            int containerId,
            Inventory playerInventory,
            RegistryFriendlyByteBuf buffer
    ) {
        this(
                containerId,
                playerInventory,
                ItemStack.STREAM_CODEC.decode(buffer),
                buffer.readByte(),
                buffer.readByte()
        );
    }

    // Server
    public MatrixMenu(
            int containerId,
            Inventory playerInventory,
            ItemStack matrixStack,
            int sourceType,
            int sourceIndex
    ) {
        super(ModMenus.MATRIX.get(), containerId);

        if (!(matrixStack.getItem() instanceof MatrixItem matrixItem)) {
            throw new IllegalArgumentException("Source stack does not contain a matrix");
        }

        this.matrixStack = matrixStack;
        this.sourceType = sourceType;
        this.sourceIndex = sourceIndex;

        MatrixDefinition definition = matrixItem.getDefinition();
        this.width = definition.width();
        this.height = definition.height();

        this.imageWidth = Math.max(176, GRID_X * 2 + width * CELL_SIZE);
        int inventoryY = GRID_Y + height * CELL_SIZE + INVENTORY_GAP;
        this.imageHeight = inventoryY + 76;

        addPlayerInventory(playerInventory, inventoryY);
    }

    private void addPlayerInventory(Inventory inventory, int inventoryY) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        inventory,
                        column + row * 9 + 9,
                        INVENTORY_X + column * CELL_SIZE,
                        inventoryY + row * CELL_SIZE
                ));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(
                    inventory,
                    column,
                    INVENTORY_X + column * CELL_SIZE,
                    inventoryY + 58
            ));
        }
    }

    public ItemStack getMatrixStack() {
        return matrixStack;
    }

    public MatrixData getMatrixData() {
        MatrixData data = matrixStack.get(ModDataComponents.MATRIX_DATA.get());
        if (data == null) {
            throw new IllegalStateException("Matrix stack does not contain matrix data");
        }
        return data;
    }

    public MatrixDefinition getMatrixDefinition() {
        return MatrixItem.get(matrixStack).getDefinition();
    }

    public int getMatrixWidth() {
        return width;
    }

    public int getMatrixHeight() {
        return height;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public int getInventoryY() {
        return GRID_Y + height * CELL_SIZE + INVENTORY_GAP;
    }

    public int getSourceType() {
        return sourceType;
    }

    public int getSourceIndex() {
        return sourceIndex;
    }

    @Override
    public boolean stillValid(Player player) {
        if (!player.isAlive()) {
            return false;
        }

        if (sourceType == SOURCE_HAND) {
            if (sourceIndex < 0 || sourceIndex >= player.getInventory().getContainerSize()) {
                return false;
            }
            return player.getInventory().getItem(sourceIndex).getItem() instanceof MatrixItem;
        }

        if (sourceType == SOURCE_EXOSKELETON) {
            return ExoskeletonMenuProvider.findBodyExoskeleton(player)
                    .map(this::containsMatrix)
                    .orElse(false);
        }

        return false;
    }

    private boolean containsMatrix(ItemStack exoskeleton) {
        ExoskeletonData data = ExoskeletonItem.getData(exoskeleton);
        if (sourceIndex < 0 || sourceIndex >= ExoskeletonData.MAX_MATRICES) {
            return false;
        }
        return data.matrices().get(sourceIndex).matrix().isPresent();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < PLAYER_INVENTORY_START || index >= PLAYER_INVENTORY_END) {
            return ItemStack.EMPTY;
        }

        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (!moveItemStackTo(
                stack,
                PLAYER_INVENTORY_START,
                PLAYER_INVENTORY_END,
                true
        )) {
            return ItemStack.EMPTY;
        }

        slot.setChanged();
        return original;
    }
}
