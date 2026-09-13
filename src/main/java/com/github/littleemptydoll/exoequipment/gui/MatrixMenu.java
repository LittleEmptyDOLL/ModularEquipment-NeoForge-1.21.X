package com.github.littleemptydoll.exoequipment.gui;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.item.MatrixItem;
import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixDefinition;
import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.registry.ModMenus;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MatrixMenu extends AbstractContainerMenu {

    public static final int SOURCE_HAND = 0;
    public static final int SOURCE_EXOSKELETON = 1;

    public static final int ACTION_PLACE = 0;
    public static final int ACTION_REMOVE = 1;
    public static final int ACTION_ROTATE = 2;
    public static final int ACTION_MOVE = 3;

    public static final int PLAYER_INVENTORY_START = 0;
    public static final int PLAYER_INVENTORY_END = 36;

    public static final int CELL_SIZE = 18;
    public static final int GRID_X = 12;
    public static final int GRID_Y = 22;
    public static final int INVENTORY_X = 12;
    public static final int INVENTORY_GAP = 18;

    private ItemStack matrixStack;
    private final ItemStack sourceExoskeleton;
    private final int sourceType;
    private final int sourceIndex;
    private final int width;
    private final int height;
    private final int imageWidth;
    private final int imageHeight;

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
                buffer.readByte(),
                null
        );
    }

    public MatrixMenu(
            int containerId,
            Inventory playerInventory,
            ItemStack matrixStack,
            int sourceType,
            int sourceIndex
    ) {
        this(containerId, playerInventory, matrixStack, sourceType, sourceIndex, null);
    }

    public MatrixMenu(
            int containerId,
            Inventory playerInventory,
            ItemStack matrixStack,
            int sourceType,
            int sourceIndex,
            ItemStack sourceExoskeleton
    ) {
        super(ModMenus.MATRIX.get(), containerId);

        if (!(matrixStack.getItem() instanceof MatrixItem matrixItem)) {
            throw new IllegalArgumentException("Source stack does not contain a matrix");
        }
        if (sourceType != SOURCE_HAND && sourceType != SOURCE_EXOSKELETON) {
            throw new IllegalArgumentException("Unknown matrix source type: " + sourceType);
        }
        if (sourceType == SOURCE_EXOSKELETON && sourceExoskeleton == null) {
            throw new IllegalArgumentException("Exoskeleton source is required");
        }

        this.matrixStack = matrixStack;
        this.sourceExoskeleton = sourceExoskeleton;
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

    public void setMatrixStack(ItemStack matrixStack) {
        if (matrixStack.getItem() instanceof MatrixItem) {
            this.matrixStack = matrixStack;
        }
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
            ItemStack stack = player.getInventory().getItem(sourceIndex);
            return stack.getItem() instanceof MatrixItem
                    && MatrixItem.get(stack).getDefinition().id().equals(getMatrixData().id());
        }

        if (sourceType == SOURCE_EXOSKELETON) {
            return ExoskeletonMenuProvider.findBodyExoskeleton(player)
                    .map(exoskeleton -> {
                        ExoskeletonData data = ExoskeletonItem.getData(exoskeleton);
                        if (sourceIndex < 0 || sourceIndex >= ExoskeletonData.MAX_MATRICES) {
                            return false;
                        }
                        return data.matrices().get(sourceIndex).matrix()
                                .map(matrix -> matrix.id().equals(getMatrixData().id()))
                                .orElse(false);
                    })
                    .orElse(false);
        }

        return false;
    }

    public boolean handleAction(
            ServerPlayer player,
            int action,
            int x,
            int y,
            int targetX,
            int targetY
    ) {
        MatrixData matrix = getServerMatrixData(player);
        MatrixDefinition definition = getMatrixDefinition();

        try {
            return switch (action) {
                case ACTION_PLACE -> placeModule(player, matrix, definition, x, y);
                case ACTION_REMOVE -> removeModule(player, matrix, x, y);
                case ACTION_ROTATE -> rotateModule(player, matrix, definition, x, y);
                case ACTION_MOVE -> moveModule(player, matrix, definition, x, y, targetX, targetY);
                default -> false;
            };
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return false;
        }
    }

    private boolean placeModule(
            ServerPlayer player,
            MatrixData matrix,
            MatrixDefinition definition,
            int x,
            int y
    ) {
        ItemStack carried = getCarried();
        if (!(carried.getItem() instanceof ModuleItem moduleItem)) {
            return false;
        }

        InstalledModule module = new InstalledModule(
                moduleItem.getDefinition().id(),
                x,
                y,
                0
        );

        MatrixData updated = MatrixOperations.addModule(
                matrix,
                definition,
                module
        );

        applyMatrixData(player, updated);
        carried.shrink(1);
        setCarried(carried);
        return true;
    }

    private boolean removeModule(
            ServerPlayer player,
            MatrixData matrix,
            int x,
            int y
    ) {
        InstalledModule module = MatrixOperations.getModuleAt(matrix, x, y);
        if (module == null) {
            return false;
        }

        ItemStack moduleStack = ModModules.find(module.id()).getItem().getDefaultInstance();
        if (!giveModule(player, moduleStack)) {
            return false;
        }

        applyMatrixData(player, MatrixOperations.removeModule(matrix, x, y));
        return true;
    }

    private boolean rotateModule(
            ServerPlayer player,
            MatrixData matrix,
            MatrixDefinition definition,
            int x,
            int y
    ) {
        MatrixData updated = MatrixOperations.rotateModule(
                matrix,
                definition,
                x,
                y
        );
        if (updated.equals(matrix)) {
            return false;
        }

        applyMatrixData(player, updated);
        return true;
    }

    private boolean moveModule(
            ServerPlayer player,
            MatrixData matrix,
            MatrixDefinition definition,
            int fromX,
            int fromY,
            int toX,
            int toY
    ) {
        if (fromX == toX && fromY == toY) {
            return false;
        }

        MatrixData updated = MatrixOperations.moveModule(
                matrix,
                definition,
                fromX,
                fromY,
                toX,
                toY
        );
        if (updated.equals(matrix)) {
            return false;
        }

        applyMatrixData(player, updated);
        return true;
    }

    private boolean giveModule(ServerPlayer player, ItemStack moduleStack) {
        if (getCarried().isEmpty()) {
            setCarried(moduleStack);
            return true;
        }

        return player.getInventory().add(moduleStack);
    }

    private MatrixData getServerMatrixData(ServerPlayer player) {
        ItemStack stack = getServerMatrixStack(player);
        MatrixData data = stack.get(ModDataComponents.MATRIX_DATA.get());
        if (data == null) {
            throw new IllegalStateException("Matrix stack does not contain matrix data");
        }
        return data;
    }

    private ItemStack getServerMatrixStack(ServerPlayer player) {
        if (sourceType == SOURCE_HAND) {
            if (sourceIndex < 0 || sourceIndex >= player.getInventory().getContainerSize()) {
                throw new IllegalStateException("Invalid matrix inventory slot");
            }
            ItemStack stack = player.getInventory().getItem(sourceIndex);
            if (!(stack.getItem() instanceof MatrixItem)) {
                throw new IllegalStateException("Matrix is no longer in the source slot");
            }
            return stack;
        }

        ItemStack exoskeleton = ExoskeletonMenuProvider.findBodyExoskeleton(player)
                .orElseThrow(() -> new IllegalStateException("Exoskeleton is no longer equipped"));
        ExoskeletonData data = ExoskeletonItem.getData(exoskeleton);
        if (sourceIndex < 0 || sourceIndex >= ExoskeletonData.MAX_MATRICES) {
            throw new IllegalStateException("Invalid exoskeleton matrix slot");
        }

        return data.matrices().get(sourceIndex).matrix()
                .orElseThrow(() -> new IllegalStateException("Matrix is no longer installed"));
    }

    private void applyMatrixData(ServerPlayer player, MatrixData data) {
        if (sourceType == SOURCE_HAND) {
            ItemStack stack = getServerMatrixStack(player);
            stack.set(ModDataComponents.MATRIX_DATA.get(), data);
            this.matrixStack = stack;
            return;
        }

        ItemStack exoskeleton = ExoskeletonMenuProvider.findBodyExoskeleton(player)
                .orElseThrow(() -> new IllegalStateException("Exoskeleton is no longer equipped"));
        ExoskeletonData exoskeletonData = ExoskeletonItem.getData(exoskeleton);
        ExoskeletonData updatedData = exoskeletonData.withMatrix(sourceIndex, data);
        exoskeleton.set(
                ModDataComponents.EXOSKELETON_DATA.get(),
                updatedData
        );

        this.matrixStack = updatedData.matrices().get(sourceIndex).matrix()
                .map(ItemStack::copy)
                .orElseThrow(() -> new IllegalStateException("Matrix is no longer installed"));
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
