package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.client.TooltipHelper;
import com.github.littleemptydoll.exoequipment.controller.ControllerDefinition;
import com.github.littleemptydoll.exoequipment.registry.EquipmentItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class ControllerItem extends EquipmentItem<ControllerDefinition> {

    public ControllerItem(
            DeferredHolder<
                    ControllerDefinition,
                    ControllerDefinition
                    > definition,
            Properties properties
    ) {
        super(
                definition,
                properties
        );
    }

    public static ControllerItem get(ItemStack stack) {
        if (!(stack.getItem() instanceof ControllerItem controllerItem)) {
            throw new IllegalArgumentException(
                    "ItemStack is not an controller"
            );
        }

        return controllerItem;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        appendEquipmentTooltip(tooltip);

        ControllerDefinition definition = getDefinition();

        tooltip.add(
                TooltipHelper.maxProfiles(
                        definition.maxProfiles()
                )
        );

        tooltip.add(
                TooltipHelper.maxActiveMatrices(
                        definition.maxActiveMatrices()
                )
        );
    }
}
