package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.client.TooltipHelper;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.registry.EquipmentItem;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class ModuleItem extends EquipmentItem<ModuleDefinition> {

    public ModuleItem(
            DeferredHolder<ModuleDefinition, ModuleDefinition> definition,
            Properties properties
    ) {
        super(definition, properties);
    }

    public static ModuleItem get(ItemStack stack) {
        if (!(stack.getItem() instanceof ModuleItem moduleItem)) {
            throw new IllegalArgumentException("ItemStack is not an module");
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

        appendEquipmentTooltip(tooltip);
        tooltip.add(TooltipHelper.category(definition.category()));
        tooltip.add(TooltipHelper.size(definition.size().width(), definition.size().height()));

        if (definition.energy().isPresent()) {
            tooltip.add(TooltipHelper.energyConsumption(definition.energy().get().consumption()));
        }

        if (definition.generation().isPresent()) {
            tooltip.add(TooltipHelper.energyGeneration(definition.generation().get().generation()));
        }

        if (definition.storage().isPresent()) {
            var storage = definition.storage().get();
            tooltip.add(TooltipHelper.input(storage.maxInput()));
            tooltip.add(TooltipHelper.output(storage.maxOutput()));
            int stored = Math.min(
                    stack.getOrDefault(ModDataComponents.MODULE_STORED_ENERGY.get(), 0),
                    storage.capacity()
            );
            tooltip.add(TooltipHelper.capacity(stored, storage.capacity()));
        }

        if (definition.thermal().isPresent()) {
            tooltip.add(TooltipHelper.cooling(definition.thermal().get().cooling()));
            tooltip.add(TooltipHelper.heatGeneration(definition.thermal().get().heatGeneration()));
        }
    }
}
