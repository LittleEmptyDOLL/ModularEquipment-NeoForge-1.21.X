package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.module.GeneratedModuleData;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class GeneratedModuleItem extends ModuleItem {
    public GeneratedModuleItem(
            DeferredHolder<ModuleDefinition, ModuleDefinition> definition,
            Properties properties
    ) {
        super(definition, properties);
    }

    @Override
    public ModuleDefinition getDefinition(ItemStack stack) {
        GeneratedModuleData generated =
                stack.get(ModDataComponents.GENERATED_MODULE_DATA.get());

        if (generated == null) {
            return super.getDefinition(stack);
        }

        ModuleDefinition base = super.getDefinition();
        ModuleDefinition generatedDefinition = generated.definition();

        if (!generatedDefinition.id().equals(base.id())
                || !generated.matchesTier(base.tier())) {
            return base;
        }

        return generatedDefinition;
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
