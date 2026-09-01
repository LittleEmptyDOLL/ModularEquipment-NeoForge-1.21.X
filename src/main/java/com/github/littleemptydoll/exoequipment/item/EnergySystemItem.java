package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.client.TooltipHelper;
import com.github.littleemptydoll.exoequipment.energy.EnergySystemDefinition;
import com.github.littleemptydoll.exoequipment.registry.EquipmentItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class EnergySystemItem extends EquipmentItem<EnergySystemDefinition> {

    public EnergySystemItem(
            DeferredHolder<
                    EnergySystemDefinition,
                    EnergySystemDefinition
                    > definition,
            Properties properties
    ) {
        super(
                definition,
                properties
        );
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
        appendEquipmentTooltip(tooltip);

        EnergySystemDefinition definition = getDefinition();

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
