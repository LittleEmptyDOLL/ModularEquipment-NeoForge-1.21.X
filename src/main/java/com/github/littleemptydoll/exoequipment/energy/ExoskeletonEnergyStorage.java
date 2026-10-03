package com.github.littleemptydoll.exoequipment.energy;

import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.registry.ModExoskeletons;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/** Exposes the buffer and installed batteries to item chargers, including Curios chargers. */
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
        var server = ServerLifecycleHooks.getCurrentServer();
        return receiveEnergyAtTick(maxReceive, simulate,
                server == null ? -1L : server.getTickCount());
    }

    int receiveEnergyAtTick(int maxReceive, boolean simulate, long tick) {
        var data = ExoskeletonItem.getData(stack);
        ExoskeletonRuntimeState runtime = stack.getOrDefault(
                ModDataComponents.EXOSKELETON_RUNTIME.get(),
                ExoskeletonRuntimeState.empty()
        );
        int remaining = runtime.remainingInput(tick,
                EnergyState.calculate(data).maxInput());
        EnergyOperations.EnergyReceiveResult result =
                EnergyOperations.receiveExternalEnergy(
                        data, Math.min(maxReceive, remaining), simulate
                );
        if (!simulate && result.received() > 0) {
            stack.set(ModDataComponents.EXOSKELETON_DATA.get(), result.data());
            stack.set(ModDataComponents.EXOSKELETON_RUNTIME.get(),
                    runtime.withInput(tick, result.received()));
        }
        return result.received();
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        EnergyState state = EnergyState.calculate(ExoskeletonItem.getData(stack));
        return state.storedEnergy() + state.bufferStored();
    }

    @Override
    public int getMaxEnergyStored() {
        EnergyState state = EnergyState.calculate(ExoskeletonItem.getData(stack));
        return state.storageCapacity() + state.bufferCapacity();
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        EnergyState state = EnergyState.calculate(ExoskeletonItem.getData(stack));
        return state.maxInput() > 0
                && (state.bufferCapacity() > 0
                    || state.storageCapacity() > 0 && state.storageInput() > 0);
    }
}
