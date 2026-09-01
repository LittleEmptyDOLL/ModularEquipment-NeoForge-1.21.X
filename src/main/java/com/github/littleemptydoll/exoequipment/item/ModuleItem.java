package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.client.TooltipHelper;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.registry.EquipmentItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class ModuleItem extends EquipmentItem<ModuleDefinition> {

    public ModuleItem(
            DeferredHolder<
                    ModuleDefinition,
                    ModuleDefinition
                    > definition,
            Properties properties
    ) {
        super(
                definition,
                properties
        );
    }

    public static ModuleItem get(ItemStack stack) {
        if (!(stack.getItem() instanceof ModuleItem moduleItem)) {
            throw new IllegalArgumentException(
                    "ItemStack is not an module"
            );
        }

        return moduleItem;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        ModuleDefinition definition = getDefinition();

        tooltip.add(
                TooltipHelper.tier(
                        definition.tier()
                )
        );
    }
}
