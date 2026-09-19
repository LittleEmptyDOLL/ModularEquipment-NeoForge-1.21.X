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
import com.github.littleemptydoll.exoequipment.registry.ModMatrices;
import com.github.littleemptydoll.exoequipment.registry.ModMenus;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
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
    public static final int GRID_Y = 22;
    public static final int INVENTORY_X = 14;
    public static final int INVENTORY_GAP = 18;
    public static final int INVENTORY_BACKGROUND_WIDTH = 188;
    public static final int INVENTORY_BACKGROUND_HEIGHT = 111;
    public static final int GRID_BORDER = 7;

    private ItemStack matrixStack;
    private final ItemStack sourceExoskeleton;
    private final int sourceType;
    private final int sourceIndex;
    private final int width;
    private final int height;
    private final int imageWidth;
    private final int imageHeight;
    private final int inventoryY;
    private final int[] syncedModuleEnergy;
    private int syncedTemperature = 2000;
    private final Inventory playerInventory;

    public MatrixMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, ItemStack.STREAM_CODEC.decode(buffer),
                buffer.readByte(), buffer.readByte(), null);
    }

    public MatrixMenu(int containerId, Inventory playerInventory, ItemStack matrixStack,
                      int sourceType, int sourceIndex) {
        this(containerId, playerInventory, matrixStack, sourceType, sourceIndex, null);
    }

    public MatrixMenu(int containerId, Inventory playerInventory, ItemStack matrixStack,
                      int sourceType, int sourceIndex, ItemStack sourceExoskeleton) {
        super(ModMenus.MATRIX.get(), containerId);
        if (!(matrixStack.getItem() instanceof MatrixItem matrixItem)) {
            throw new IllegalArgumentException("Source stack does not contain a matrix");
        }
        if (sourceType != SOURCE_HAND && sourceType != SOURCE_EXOSKELETON) {
            throw new IllegalArgumentException("Unknown matrix source type: " + sourceType);
        }
        this.matrixStack = matrixStack;
        this.sourceExoskeleton = sourceExoskeleton;
        this.sourceType = sourceType;
        this.sourceIndex = sourceIndex;
        MatrixDefinition definition = matrixItem.getDefinition();
        this.width = definition.width();
        this.height = definition.height();
        int effectiveWidth = getEffectiveBackgroundWidth(this.width);
        this.imageWidth = Math.max(INVENTORY_BACKGROUND_WIDTH,
                effectiveWidth * CELL_SIZE + GRID_BORDER * 2 + 12);
        this.inventoryY = GRID_Y + height * CELL_SIZE + INVENTORY_GAP;
        this.imageHeight = inventoryY + INVENTORY_BACKGROUND_HEIGHT - 3;
        this.syncedModuleEnergy = new int[Math.max(1, width * height)];
        this.playerInventory = playerInventory;
        addPlayerInventory(playerInventory, inventoryY + 18);
        addModuleEnergyDataSlots();
        addTemperatureDataSlot();
    }

    private static int getEffectiveBackgroundWidth(int matrixWidth) {
        return matrixWidth > 9 ? Math.max(11, matrixWidth) : 0;
    }

    public int getGridX() {
        int gridWidth = width * CELL_SIZE + GRID_BORDER * 2;
        return (imageWidth - gridWidth) / 2 + GRID_BORDER;
    }

    public int getInventoryX() {
        return (imageWidth - INVENTORY_BACKGROUND_WIDTH) / 2 + INVENTORY_X;
    }

    private void addModuleEnergyDataSlots() {
        for (int cell = 0; cell < syncedModuleEnergy.length; cell++) {
            final int dataIndex = cell;
            final int x = cell % width;
            final int y = cell / width;
            addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    if (levelIsClient()) return syncedModuleEnergy[dataIndex];
                    return getServerModuleEnergy(x, y);
                }

                @Override
                public void set(int value) {
                    syncedModuleEnergy[dataIndex] = Math.max(0, value);
                }
            });
        }
    }

    private boolean levelIsClient() {
        return minecraftPlayer() != null && minecraftPlayer().level().isClientSide;
    }

    private Player minecraftPlayer() {
        return getPlayer();
    }

    private Player getPlayer() {
        return playerInventory.player;
    }

    private int getServerModuleEnergy(int x, int y) {
        if (getPlayer().level().isClientSide) return syncedModuleEnergy[y * width + x];

        MatrixData data;
        if (sourceType == SOURCE_HAND) {
            if (sourceIndex < 0 || sourceIndex >= getPlayer().getInventory().getContainerSize()) return 0;
            ItemStack stack = getPlayer().getInventory().getItem(sourceIndex);
            data = stack.get(ModDataComponents.MATRIX_DATA.get());
        } else {
            ItemStack exoskeleton = ExoskeletonMenuProvider.findBodyExoskeleton(getPlayer()).orElse(null);
            if (exoskeleton == null) return 0;
            ExoskeletonData exoskeletonData = ExoskeletonItem.getData(exoskeleton);
            if (sourceIndex < 0 || sourceIndex >= ExoskeletonData.MAX_MATRICES) return 0;
            data = exoskeletonData.matrices().get(sourceIndex).matrix().orElse(null);
        }

        if (data == null) return 0;
        InstalledModule module = MatrixOperations.getModuleAt(data, x, y);
        if (module == null) return 0;
        return ModModules.getDefinition(module.id()).storage()
                .map(storage -> Math.min(module.storedEnergy(), storage.capacity()))
                .orElse(0);
    }

    private void addTemperatureDataSlot() {
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                if (levelIsClient()) return syncedTemperature;
                return getServerTemperature();
            }

            @Override
            public void set(int value) {
                syncedTemperature = value;
            }
        });
    }

    private int getServerTemperature() {
        if (sourceType != SOURCE_EXOSKELETON) return 2000;

        ItemStack exoskeleton = ExoskeletonMenuProvider.findBodyExoskeleton(getPlayer()).orElse(null);
        if (exoskeleton == null) return 2000;

        double temperature = ExoskeletonItem.getData(exoskeleton).temperature();
        return Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, (int) Math.round(temperature * 10.0D)));
    }

    public double getTemperature() {
        return sourceType == SOURCE_EXOSKELETON
                ? syncedTemperature / 10.0D
                : Double.NaN;
    }

    public int getSyncedStoredEnergy(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return 0;
        return syncedModuleEnergy[y * width + x];
    }

    private void addPlayerInventory(Inventory inventory, int inventoryY) {
        int inventoryX = getInventoryX();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int inventorySlot = column + row * 9 + 9;
                addSlot(createInventorySlot(inventory, inventorySlot,
                        inventoryX + column * CELL_SIZE, inventoryY + row * CELL_SIZE));
            }
        }
        for (int column = 0; column < 9; column++) {
            int inventorySlot = column;
            addSlot(createInventorySlot(inventory, inventorySlot,
                    inventoryX + column * CELL_SIZE, inventoryY + 58));
        }
    }

    private Slot createInventorySlot(Inventory inventory, int inventorySlot, int x, int y) {
        if (sourceType == SOURCE_HAND && inventorySlot == sourceIndex) {
            return new MatrixSourceSlot(inventory, inventorySlot, x, y);
        }
        return new Slot(inventory, inventorySlot, x, y);
    }

    public ItemStack getMatrixStack() { return matrixStack; }
    public void setMatrixStack(ItemStack matrixStack) { if (matrixStack.getItem() instanceof MatrixItem) this.matrixStack = matrixStack; }
    public MatrixData getMatrixData() {
        MatrixData data = matrixStack.get(ModDataComponents.MATRIX_DATA.get());
        if (data == null) throw new IllegalStateException("Matrix stack does not contain matrix data");
        return data;
    }
    public MatrixDefinition getMatrixDefinition() { return MatrixItem.get(matrixStack).getDefinition(); }
    public int getMatrixWidth() { return width; }
    public int getMatrixHeight() { return height; }
    public int getImageWidth() { return imageWidth; }
    public int getImageHeight() { return imageHeight; }
    public int getInventoryY() { return inventoryY; }
    public int getSourceType() { return sourceType; }
    public int getSourceIndex() { return sourceIndex; }

    @Override
    public boolean stillValid(Player player) {
        if (!player.isAlive()) return false;
        if (sourceType == SOURCE_HAND) {
            if (sourceIndex < 0 || sourceIndex >= player.getInventory().getContainerSize()) return false;
            ItemStack stack = player.getInventory().getItem(sourceIndex);
            return stack.getItem() instanceof MatrixItem
                    && MatrixItem.get(stack).getDefinition().id().equals(getMatrixData().id());
        }
        if (sourceType == SOURCE_EXOSKELETON) {
            return ExoskeletonMenuProvider.findBodyExoskeleton(player).map(exoskeleton -> {
                ExoskeletonData data = ExoskeletonItem.getData(exoskeleton);
                if (sourceIndex < 0 || sourceIndex >= ExoskeletonData.MAX_MATRICES) return false;
                return data.matrices().get(sourceIndex).matrix()
                        .map(matrix -> matrix.id().equals(getMatrixData().id())).orElse(false);
            }).orElse(false);
        }
        return false;
    }

    public boolean handleAction(ServerPlayer player, int action, int x, int y, int targetX, int targetY, int rotation) {
        try {
            MatrixData matrix = getServerMatrixData(player);
            MatrixDefinition definition = getMatrixDefinition();
            return switch (action) {
                case ACTION_PLACE -> placeModule(player, matrix, definition, x, y, rotation);
                case ACTION_REMOVE -> removeModule(player, matrix, x, y);
                case ACTION_ROTATE -> rotateModule(player, matrix, definition, x, y);
                case ACTION_MOVE -> moveModule(player, matrix, definition, x, y, targetX, targetY, rotation);
                default -> false;
            };
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return false;
        }
    }

    private boolean placeModule(ServerPlayer player, MatrixData matrix, MatrixDefinition definition,
                                int x, int y, int rotation) {
        ItemStack carried = getCarried();
        if (!(carried.getItem() instanceof ModuleItem moduleItem)) return false;
        var moduleDefinition = moduleItem.getDefinition();
        int storedEnergy = carried.getOrDefault(
                ModDataComponents.MODULE_STORED_ENERGY.get(),
                0
        );
        double shieldEnergy = carried.getOrDefault(
                ModDataComponents.MODULE_SHIELD_ENERGY.get(),
                0.0D
        );
        int shieldRechargeCooldown = carried.getOrDefault(
                ModDataComponents.MODULE_SHIELD_RECHARGE_COOLDOWN.get(),
                0
        );
        int revivalCooldown = carried.getOrDefault(
                ModDataComponents.MODULE_REVIVAL_COOLDOWN.get(),
                0
        );

        int finalStoredEnergy = storedEnergy;
        storedEnergy = moduleDefinition.storage()
                .map(storage -> Math.min(finalStoredEnergy, storage.capacity()))
                .orElse(0);

        double finalShieldEnergy = shieldEnergy;
        shieldEnergy = moduleDefinition.shield()
                .map(shield -> Math.min(finalShieldEnergy, shield.capacity()))
                .orElse(0.0D);

        int finalShieldRechargeCooldown = shieldRechargeCooldown;
        shieldRechargeCooldown = moduleDefinition.shield()
                .map(shield -> Math.min(finalShieldRechargeCooldown, shield.rechargeDelay()))
                .orElse(0);

        int finalRevivalCooldown = revivalCooldown;
        revivalCooldown = moduleDefinition.revival()
                .map(revival -> Math.min(finalRevivalCooldown, revival.cooldown()))
                .orElse(0);

        InstalledModule module = new InstalledModule(
                moduleDefinition.id(),
                x,
                y,
                rotation,
                storedEnergy,
                shieldEnergy,
                shieldRechargeCooldown,
                revivalCooldown
        );
        MatrixData updated = MatrixOperations.addModule(matrix, definition, module);
        applyMatrixData(player, updated);
        carried.shrink(1);
        setCarried(carried);
        return true;
    }

    private boolean removeModule(ServerPlayer player, MatrixData matrix, int x, int y) {
        InstalledModule module = MatrixOperations.getModuleAt(matrix, x, y);
        if (module == null) return false;
        ItemStack moduleStack = ModModules.find(module.id()).getItem().getDefaultInstance();
        var definition = ModModules.getDefinition(module.id());

        var storage = definition.storage();
        if (storage.isPresent() && module.storedEnergy() > 0) {
            int storedEnergy = Math.min(module.storedEnergy(), storage.get().capacity());
            moduleStack.set(
                    ModDataComponents.MODULE_STORED_ENERGY.get(),
                    storedEnergy
            );
        }

        var shield = definition.shield();
        if (shield.isPresent()) {
            double shieldEnergy = Math.min(
                    module.shieldEnergy(),
                    shield.get().capacity()
            );

            if (shieldEnergy > 0.0D) {
                moduleStack.set(
                        ModDataComponents.MODULE_SHIELD_ENERGY.get(),
                        shieldEnergy
                );
            }

            if (module.shieldRechargeCooldown() > 0) {
                moduleStack.set(
                        ModDataComponents.MODULE_SHIELD_RECHARGE_COOLDOWN.get(),
                        Math.min(
                                module.shieldRechargeCooldown(),
                                shield.get().rechargeDelay()
                        )
                );
            }
        }

        var revival = definition.revival();
        if (revival.isPresent() && module.revivalCooldown() > 0) {
            moduleStack.set(
                    ModDataComponents.MODULE_REVIVAL_COOLDOWN.get(),
                    Math.min(
                            module.revivalCooldown(),
                            revival.get().cooldown()
                    )
            );
        }
        if (!giveModule(player, moduleStack)) return false;
        applyMatrixData(player, MatrixOperations.removeModule(matrix, x, y));
        return true;
    }

    private boolean rotateModule(ServerPlayer player, MatrixData matrix, MatrixDefinition definition, int x, int y) {
        MatrixData updated = MatrixOperations.rotateModule(matrix, definition, x, y);
        if (updated.equals(matrix)) return false;
        applyMatrixData(player, updated);
        return true;
    }

    private boolean moveModule(ServerPlayer player, MatrixData matrix, MatrixDefinition definition,
                               int fromX, int fromY, int toX, int toY, int rotation) {
        InstalledModule module = MatrixOperations.getModuleAt(matrix, fromX, fromY);
        if (module == null) return false;
        InstalledModule movedModule = new InstalledModule(
                module.id(),
                toX,
                toY,
                rotation,
                module.storedEnergy(),
                module.shieldEnergy(),
                module.shieldRechargeCooldown(),
                module.revivalCooldown()
        );
        MatrixData updated = replaceModule(matrix, definition, module, movedModule);
        if (updated.equals(matrix)) return false;
        applyMatrixData(player, updated);
        return true;
    }

    private MatrixData replaceModule(MatrixData matrix, MatrixDefinition definition,
                                     InstalledModule oldModule, InstalledModule newModule) {
        var modules = new java.util.ArrayList<>(matrix.modules());
        modules.remove(oldModule);
        MatrixData withoutModule = new MatrixData(matrix.id(), modules);
        if (!MatrixOperations.canPlace(withoutModule, definition, ModModules.getDefinition(newModule.id()),
                newModule.x(), newModule.y(), newModule.rotation())) {
            throw new IllegalArgumentException("Module cannot be placed at the requested position");
        }
        modules.add(newModule);
        return new MatrixData(matrix.id(), modules);
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
        if (data == null) throw new IllegalStateException("Matrix stack does not contain matrix data");
        return data;
    }

    private ItemStack getServerMatrixStack(ServerPlayer player) {
        if (sourceType == SOURCE_HAND) {
            if (sourceIndex < 0 || sourceIndex >= player.getInventory().getContainerSize())
                throw new IllegalStateException("Invalid matrix inventory slot");
            ItemStack stack = player.getInventory().getItem(sourceIndex);
            if (!(stack.getItem() instanceof MatrixItem))
                throw new IllegalStateException("Matrix is no longer in the source slot");
            return stack;
        }
        ItemStack exoskeleton = ExoskeletonMenuProvider.findBodyExoskeleton(player)
                .orElseThrow(() -> new IllegalStateException("Exoskeleton is no longer equipped"));
        ExoskeletonData data = ExoskeletonItem.getData(exoskeleton);
        if (sourceIndex < 0 || sourceIndex >= ExoskeletonData.MAX_MATRICES)
            throw new IllegalStateException("Invalid exoskeleton matrix slot");
        MatrixData matrix = data.matrices().get(sourceIndex).matrix()
                .orElseThrow(() -> new IllegalStateException("Matrix is no longer installed"));
        return createMatrixStack(matrix);
    }

    private ItemStack createMatrixStack(MatrixData data) {
        var entry = ModMatrices.find(data.id());
        if (entry == null) throw new IllegalStateException("Unknown matrix definition: " + data.id());
        ItemStack stack = entry.getItem().getDefaultInstance();
        stack.set(ModDataComponents.MATRIX_DATA.get(), data);
        return stack;
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
        exoskeleton.set(ModDataComponents.EXOSKELETON_DATA.get(), exoskeletonData.withMatrix(sourceIndex, data));
        this.matrixStack = createMatrixStack(data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < PLAYER_INVENTORY_START || index >= PLAYER_INVENTORY_END) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot instanceof MatrixSourceSlot || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, true)) return ItemStack.EMPTY;
        slot.setChanged();
        return original;
    }
}
