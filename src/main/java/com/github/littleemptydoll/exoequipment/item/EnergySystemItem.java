package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.client.TooltipHelper;
import com.github.littleemptydoll.exoequipment.energy.EnergySystem;
import com.github.littleemptydoll.exoequipment.energy.EnergySystemDefinition;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.registry.ModEnergySystems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class EnergySystemItem extends Item {

    private final DeferredHolder<
            EnergySystemDefinition,
            EnergySystemDefinition
            > definition;

    public EnergySystemItem(
            DeferredHolder<
                    EnergySystemDefinition,
                    EnergySystemDefinition
                    > definition
    ) {
        super(
                new Properties()
                        .component(
                                ModDataComponents.ENERGY_SYSTEM.get(),
                                new EnergySystem(
                                        definition.getId()
                                )
                        )
        );

        this.definition = definition;
    }

    public EnergySystemDefinition getDefinition() {
        return definition.get();
    }

    public EnergySystemDefinition getDefinition(ItemStack stack) {
        EnergySystem energySystem = getEnergySystem(stack);

        if (energySystem == null) {
            return null;
        }

        return ModEnergySystems.getDefinition(
                energySystem.definitionId()
        );
    }

    public EnergySystem getEnergySystem(ItemStack stack) {
        EnergySystem data = stack.get(
                ModDataComponents.ENERGY_SYSTEM.get()
        );

        if (data == null) {
            throw new IllegalStateException(
                    "Energy system item does not contain energy system data"
            );
        }

        return data;
    }

    public static EnergySystemItem get(ItemStack stack) {
        if (!(stack.getItem() instanceof EnergySystemItem energySystemItem)) {
            throw new IllegalArgumentException(
                    "ItemStack is not an energy system"
            );
        }

        return energySystemItem;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        EnergySystem energySystem = getEnergySystem(stack);

        if (energySystem == null) {
            tooltip.add(
                    Component.literal("No energy system data")
            );
            return;
        }

        EnergySystemDefinition definition = getDefinition(stack);

        tooltip.add(
                TooltipHelper.tier(
                        definition.tier()
                )
        );

        tooltip.add(
                TooltipHelper.input(
                        definition.maxInput()
                )
        );

        tooltip.add(
                TooltipHelper.output(
                        definition.maxOutput()
                )
        );

        tooltip.add(
                TooltipHelper.efficiency(
                        definition.efficiency()
                )
        );
    }
}
