package com.github.littleemptydoll.exoequipment.energy;

import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.registry.ModExoskeletons;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;

/** Exposes the installed batteries to item chargers, including Curios chargers. */
public final class ExoskeletonEnergyStorage implements IEnergyStorage {
    private final ItemStack stack;

    public ExoskeletonEnergyStorage(ItemStack stack) {
        this.stack = stack;
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.EnergyStorage.ITEM,
                (stack, context) -> new ExoskeletonEnergyStorage(stack),
                ModExoskeletons.BASIC.getItem()
        );
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        EnergyOperations.EnergyReceiveResult result =
                EnergyOperations.receiveExternalEnergy(
                        ExoskeletonItem.getData(stack), maxReceive, simulate
                );
        if (!simulate && result.received() > 0) {
            stack.set(ModDataComponents.EXOSKELETON_DATA.get(), result.data());
        }
        return result.received();
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return EnergyState.calculate(ExoskeletonItem.getData(stack))
                .storedEnergy();
    }

    @Override
    public int getMaxEnergyStored() {
        return EnergyState.calculate(ExoskeletonItem.getData(stack))
                .storageCapacity();
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        EnergyState state = EnergyState.calculate(ExoskeletonItem.getData(stack));
        return state.maxInput() > 0
                && state.storageCapacity() > 0
                && state.storageInput() > 0;
    }
}
