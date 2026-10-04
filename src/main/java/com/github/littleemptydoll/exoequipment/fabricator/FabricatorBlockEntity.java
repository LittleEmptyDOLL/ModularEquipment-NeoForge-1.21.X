package com.github.littleemptydoll.exoequipment.fabricator;

import com.github.littleemptydoll.exoequipment.registry.ModBlockEntities;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class FabricatorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 1_000_000;
    public static final int MAX_RECEIVE = 20_000;

    private final ItemStackHandler upgrades = new ItemStackHandler(FabricatorUnlocks.UPGRADE_SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return FabricatorUnlocks.accepts(slot, stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final StoredEnergy energy = new StoredEnergy();

    public FabricatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FABRICATOR.get(), pos, state);
    }

    public ItemStackHandler upgrades() {
        return upgrades;
    }

    public EnergyStorage energyStorage() {
        return energy;
    }

    public int storedEnergy() {
        return energy.getEnergyStored();
    }

    public boolean canCraft(EquipmentTier tier, int energyCost) {
        return energyCost > 0 && energyCost <= storedEnergy()
                && FabricatorUnlocks.unlocked(tier,
                        upgrades.getStackInSlot(FabricatorUnlocks.MILITARY_SLOT),
                        upgrades.getStackInSlot(FabricatorUnlocks.ENGINEERING_SLOT),
                        upgrades.getStackInSlot(FabricatorUnlocks.EXPERIMENTAL_SLOT));
    }

    /** Call after all recipe, inventory-space and ingredient checks have passed. */
    public boolean spendEnergy(int energyCost) {
        return energy.spend(energyCost);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.put("Upgrades", upgrades.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy.restore(tag.getInt("Energy"));
        if (tag.contains("Upgrades", Tag.TAG_COMPOUND)) {
            CompoundTag savedUpgrades = tag.getCompound("Upgrades").copy();
            savedUpgrades.putInt("Size", FabricatorUnlocks.UPGRADE_SLOT_COUNT);
            upgrades.deserializeNBT(registries, savedUpgrades);
            for (int slot = 0; slot < FabricatorUnlocks.UPGRADE_SLOT_COUNT; slot++) {
                ItemStack stack = upgrades.getStackInSlot(slot);
                if (!FabricatorUnlocks.accepts(slot, stack)) {
                    upgrades.setStackInSlot(slot, ItemStack.EMPTY);
                } else if (stack.getCount() > 1) {
                    upgrades.setStackInSlot(slot, stack.copyWithCount(1));
                }
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.exoequipment.fabricator");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FabricatorMenu(id, inventory, this);
    }

    private final class StoredEnergy extends EnergyStorage {
        StoredEnergy() {
            super(CAPACITY, MAX_RECEIVE, 0);
        }

        @Override
        public int receiveEnergy(int amount, boolean simulate) {
            int received = super.receiveEnergy(amount, simulate);
            if (!simulate && received > 0) setChanged();
            return received;
        }

        boolean spend(int cost) {
            if (cost < 0 || cost > energy) return false;
            energy -= cost;
            setChanged();
            return true;
        }

        void restore(int amount) {
            energy = Math.max(0, Math.min(capacity, amount));
        }
    }
}
