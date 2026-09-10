package com.github.littleemptydoll.exoequipment.gui;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonContainer;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.item.*;
import com.github.littleemptydoll.exoequipment.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ExoskeletonMenu extends AbstractContainerMenu {

    public static final int SOURCE_SLOT = 0;

    public static final int COMPONENT_START = 1;

    public static final int FRAME_SLOT = COMPONENT_START;
    public static final int CONTROLLER_SLOT = COMPONENT_START + 1;
    public static final int ENERGY_SYSTEM_SLOT = COMPONENT_START + 2;
    public static final int MATRIX_START_SLOT = COMPONENT_START + 3;
    public static final int MATRIX_COUNT = ExoskeletonData.MAX_MATRICES;

    public static final int PLAYER_INVENTORY_START = COMPONENT_START + ExoskeletonContainer.SLOT_COUNT;

    public static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 36;

    public static final int HOTBAR_START = PLAYER_INVENTORY_END;

    public static final int HOTBAR_END = HOTBAR_START + 9;

    private final Player player;
    private final Inventory playerInventory;
    private final int sourceInventorySlot;
    private final ItemStack exoskeleton;

    private final ExoskeletonContainer exoskeletonContainer;

    // Client
    public ExoskeletonMenu(
            int containerId,
            Inventory playerInventory,
            RegistryFriendlyByteBuf buffer
    ) {
        this(
                containerId,
                playerInventory,
                buffer.readVarInt()
        );
    }

    // Server
    public ExoskeletonMenu(
            int containerId,
            Inventory playerInventory,
            int sourceInventorySlot
    ) {
        super(
                ModMenus.EXOSKELETON.get(),
                containerId
        );

        this.player = playerInventory.player;
        this.playerInventory = playerInventory;
        this.sourceInventorySlot = sourceInventorySlot;

        ItemStack stack = playerInventory.getItem(sourceInventorySlot);

        if (!(stack.getItem() instanceof ExoskeletonItem)) {
            throw new IllegalArgumentException(
                    "Source slot does not contain an exoskeleton"
            );
        }

        this.exoskeleton = stack;
        this.exoskeletonContainer = new ExoskeletonContainer(stack);

        addEquipmentSlots();
        addPlayerInventory();
    }

    private void addEquipmentSlots() {
        // Source exoskeleton
        addSlot(
                new ExoskeletonSourceSlot(
                        new SingleStackContainer(exoskeleton),
                        0,
                        0,
                        0
                )
        );

        // Frame
        addSlot(
                new ExoskeletonComponentSlot(
                        exoskeletonContainer,
                        ExoskeletonContainer.FRAME_SLOT,
                        67,
                        43,
                        FrameItem.class
                )
        );

        // Controller
        addSlot(
                new ExoskeletonComponentSlot(
                        exoskeletonContainer,
                        ExoskeletonContainer.CONTROLLER_SLOT,
                        94,
                        43,
                        ControllerItem.class
                )
        );

        // Energy system
        addSlot(
                new ExoskeletonComponentSlot(
                        exoskeletonContainer,
                        ExoskeletonContainer.ENERGY_SYSTEM_SLOT,
                        121,
                        43,
                        EnergySystemItem.class
                )
        );

        // Matrices
        for (int i = 0; i < MATRIX_COUNT; i++) {
            addSlot(
                    new ExoskeletonComponentSlot(
                            exoskeletonContainer,
                            ExoskeletonContainer.MATRIX_START_SLOT + i,
                            67 + i * 18,
                            78,
                            MatrixItem.class
                    )
            );
        }
    }

    private void addPlayerInventory() {
        // Main inventory
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int inventorySlot = column + row * 9 + 9;

                Slot slot;

                if (inventorySlot == sourceInventorySlot) {
                    slot = new ExoskeletonInventorySlot(
                            playerInventory,
                            inventorySlot,
                            42 + column * 18,
                            126 + row * 18,
                            exoskeleton
                    );
                } else {
                    slot = new Slot(
                            playerInventory,
                            inventorySlot,
                            42 + column * 18,
                            126 + row * 18
                    );
                }

                addSlot(slot);
            }
        }

        // Hotbar
        for (int column = 0; column < 9; column++) {

            Slot slot;

            if (column == sourceInventorySlot) {
                slot = new ExoskeletonInventorySlot(
                        playerInventory,
                        column,
                        42 + column * 18,
                        184,
                        exoskeleton
                );
            } else {
                slot = new Slot(
                        playerInventory,
                        column,
                        42 + column * 18,
                        184
                );
            }

            addSlot(slot);
        }
    }

    @Override
    public ItemStack quickMoveStack(
            Player player,
            int index
    ) {
        Slot slot = slots.get(index);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        if (slot instanceof ExoskeletonInventorySlot) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index >= PLAYER_INVENTORY_START) {
            if (!moveToExoskeleton(stack)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            return original;
        }

        // Equipment -> player inventory
        if (index >= COMPONENT_START && index < PLAYER_INVENTORY_START) {
            if (!moveItemStackTo(
                    stack,
                    PLAYER_INVENTORY_START,
                    PLAYER_INVENTORY_END,
                    true
            )) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            return original;
        }

        return ItemStack.EMPTY;
    }

    private boolean moveToExoskeleton(ItemStack stack) {
        int slotIndex = findEquipmentSlot(stack);

        if (slotIndex == -1) {
            return false;
        }

        Slot target = slots.get(slotIndex);

        if (!target.mayPlace(stack) || target.hasItem()) {
            return false;
        }

        target.setByPlayer(stack.copy());

        stack.setCount(0);

        return true;
    }

    private int findEquipmentSlot(ItemStack stack) {

        if (stack.getItem() instanceof FrameItem) {
            return findEmptySlot(FRAME_SLOT);
        }

        if (stack.getItem() instanceof ControllerItem) {
            return findEmptySlot(CONTROLLER_SLOT);
        }

        if (stack.getItem() instanceof EnergySystemItem) {
            return findEmptySlot(ENERGY_SYSTEM_SLOT);
        }

        if (stack.getItem() instanceof MatrixItem) {
            for (int i = 0; i < MATRIX_COUNT; i++) {
                int slot = MATRIX_START_SLOT + i;

                if (!slots.get(slot).hasItem()) {
                    return slot;
                }
            }
        }

        return -1;
    }

    private int findEmptySlot(int slot) {
        return slots.get(slot).hasItem()
                ? -1
                : slot;
    }

    @Override
    public boolean stillValid(Player player) {
        if (player.level().isClientSide) {
            return true;
        }

        if (!player.isAlive()) {
            return false;
        }

        ItemStack current =
                player.getInventory()
                        .getItem(sourceInventorySlot);

        return current == exoskeleton;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        if (!player.level().isClientSide()) {
            exoskeletonContainer.setChanged();
        }
    }

    private boolean isExoskeletonSourceSlot(int index) {
        return slots.get(index) instanceof ExoskeletonInventorySlot;
    }

    @Override
    public void clicked(
            int slotId,
            int button,
            ClickType clickType,
            Player player
    ) {
        if (slotId >= 0 && isExoskeletonSourceSlot(slotId)) {
            return;
        }

        super.clicked(slotId, button, clickType, player);
    }

    private static class SingleStackContainer implements Container {

        private final ItemStack stack;

        private SingleStackContainer(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int getContainerSize() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return stack.isEmpty();
        }

        @Override
        public ItemStack getItem(int slot) {
            return slot == 0 ? stack : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
        }
    }
}
