package com.github.littleemptydoll.exoequipment.fabricator;

import com.github.littleemptydoll.exoequipment.registry.ModBlocks;
import com.github.littleemptydoll.exoequipment.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class FabricatorMenu extends AbstractContainerMenu {
    private static final int PLAYER_START = FabricatorUnlocks.UPGRADE_SLOT_COUNT;
    private static final int PLAYER_END = PLAYER_START + 36;

    private final ContainerLevelAccess access;
    private final FabricatorBlockEntity blockEntity;
    private int syncedEnergyLow;
    private int syncedEnergyHigh;

    public FabricatorMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos(), null);
    }

    public FabricatorMenu(int id, Inventory inventory, FabricatorBlockEntity blockEntity) {
        this(id, inventory, blockEntity.getBlockPos(), blockEntity);
    }

    private FabricatorMenu(int id, Inventory inventory, BlockPos pos, FabricatorBlockEntity blockEntity) {
        super(ModMenus.FABRICATOR.get(), id);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(inventory.player.level(), pos);
        ItemStackHandler handler = blockEntity == null
                ? new ItemStackHandler(FabricatorUnlocks.UPGRADE_SLOT_COUNT)
                : blockEntity.upgrades();

        for (int index = 0; index < FabricatorUnlocks.UPGRADE_SLOT_COUNT; index++) {
            final int slot = index;
            addSlot(new SlotItemHandler(handler, index, 7, 155 + index * 20) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return FabricatorUnlocks.accepts(slot, stack);
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 41 + col * 18, 149 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 41 + col * 18, 207));
        }

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity == null ? syncedEnergyLow : blockEntity.storedEnergy() & 0xFFFF;
            }

            @Override
            public void set(int value) {
                syncedEnergyLow = value & 0xFFFF;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity == null ? syncedEnergyHigh : blockEntity.storedEnergy() >>> 16;
            }

            @Override
            public void set(int value) {
                syncedEnergyHigh = value & 0xFFFF;
            }
        });
    }

    public FabricatorBlockEntity machine() {
        return blockEntity;
    }

    public int energy() {
        return blockEntity == null ? (syncedEnergyHigh << 16) | syncedEnergyLow : blockEntity.storedEnergy();
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, ModBlocks.FABRICATOR.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot source = slots.get(index);
        if (!source.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = source.getItem();
        ItemStack original = stack.copy();
        if (index < PLAYER_START) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) return ItemStack.EMPTY;
        } else {
            boolean moved = false;
            for (int slot = 0; slot < FabricatorUnlocks.UPGRADE_SLOT_COUNT; slot++) {
                if (FabricatorUnlocks.accepts(slot, stack)
                        && moveItemStackTo(stack, slot, slot + 1, false)) {
                    moved = true;
                    break;
                }
            }
            if (!moved) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) source.setByPlayer(ItemStack.EMPTY);
        else source.setChanged();
        return original;
    }
}
