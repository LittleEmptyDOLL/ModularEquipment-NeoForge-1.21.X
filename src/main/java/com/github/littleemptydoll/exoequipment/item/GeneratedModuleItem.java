package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.module.GeneratedModuleData;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public final class GeneratedModuleItem extends ModuleItem {
    private final ThreadLocal<ItemStack> tooltipStack = new ThreadLocal<>();

    public GeneratedModuleItem(
            DeferredHolder<ModuleDefinition, ModuleDefinition> definition,
            Properties properties
    ) {
        super(definition, properties);
    }

    public ModuleDefinition getDefinition(ItemStack stack) {
        GeneratedModuleData generated =
                stack.get(ModDataComponents.GENERATED_MODULE_DATA.get());

        if (generated == null) {
            return super.getDefinition();
        }

        ModuleDefinition base = super.getDefinition();

        if (!generated.baseDefinitionId().equals(base.id())
                || !generated.matchesTier(base.tier())) {
            return base;
        }

        ModModules.registerGeneratedData(generated);
        return generated.definition();
    }

    @Override
    public ModuleDefinition getDefinition() {
        ItemStack stack = tooltipStack.get();
        return stack == null ? super.getDefinition() : getDefinition(stack);
    }

    @Override
    public void appendHoverTextWithTemperature(
            ItemStack stack,
            List<Component> tooltip,
            double temperature
    ) {
        ItemStack previous = tooltipStack.get();
        try {
            tooltipStack.set(stack);
            super.appendHoverTextWithTemperature(stack, tooltip, temperature);
        } finally {
            if (previous == null) {
                tooltipStack.remove();
            } else {
                tooltipStack.set(previous);
            }
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        GeneratedModuleData generated =
                stack.get(ModDataComponents.GENERATED_MODULE_DATA.get());

        if (generated == null) {
            return super.getName(stack);
        }

        return super.getName(stack)
                .copy()
                .append(" ")
                .append(generated.modelCode());
    }
}
