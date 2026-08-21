package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.energy.EnergySystem;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class EnergySystemItem extends Item {
    public EnergySystemItem(Properties properties) {
        super(properties);
    }

    public EnergySystem getEnergySystem(ItemStack stack) {
        return stack.get(ModDataComponents.ENERGY_SYSTEM.get());
    }
}
