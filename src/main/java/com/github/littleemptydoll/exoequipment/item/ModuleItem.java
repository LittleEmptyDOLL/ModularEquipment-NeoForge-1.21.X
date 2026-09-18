package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.client.TooltipHelper;
import com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations;
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
        appendHoverTextWithTemperature(stack, context, tooltip, flag, Double.NaN);
    }

    public void appendHoverTextWithTemperature(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag,
            double temperature
    ) {
        ModuleDefinition definition = getDefinition();
        double efficiency = definition.temperature().isPresent() && !Double.isNaN(temperature)
                ? TemperatureOperations.calculateModuleEfficiency(definition, temperature)
                : 1.0D;

        appendEquipmentTooltip(tooltip);
        tooltip.add(TooltipHelper.category(definition.category()));
        tooltip.add(TooltipHelper.size(definition.size().width(), definition.size().height()));

        if (definition.energy().isPresent()) {
            int consumption = definition.energy().get().consumption();
            tooltip.add(TooltipHelper.energyConsumption(applyEfficiency(consumption, efficiency), efficiency));
        }

        if (definition.generation().isPresent()) {
            int generation = definition.generation().get().generation();
            tooltip.add(TooltipHelper.energyGeneration(applyEfficiency(generation, efficiency), efficiency));
        }

        if (definition.storage().isPresent()) {
            var storage = definition.storage().get();
            tooltip.add(TooltipHelper.input(applyEfficiency(storage.maxInput(), efficiency), efficiency));
            tooltip.add(TooltipHelper.output(applyEfficiency(storage.maxOutput(), efficiency), efficiency));

            int stored = Math.min(
                    stack.getOrDefault(ModDataComponents.MODULE_STORED_ENERGY.get(), 0),
                    storage.capacity()
            );
            int capacity = applyEfficiency(storage.capacity(), efficiency);
            tooltip.add(TooltipHelper.capacity(stored, capacity, efficiency));
        }

        if (definition.thermal().isPresent()) {
            var thermal = definition.thermal().get();
            if (thermal.cooling() > 0) {
                tooltip.add(TooltipHelper.cooling(applyEfficiency(thermal.cooling(), efficiency), efficiency));
            }
            if (thermal.heatGeneration() > 0) {
                tooltip.add(TooltipHelper.heatGeneration(applyEfficiency(thermal.heatGeneration(), efficiency), efficiency));
            }
        }

        if (definition.temperature().isPresent()) {
            var temperatureProperties = definition.temperature().get();
            tooltip.add(TooltipHelper.temperature(
                    temperatureProperties.minTemperature(),
                    temperatureProperties.maxTemperature()
            ));
            if (temperatureProperties.bonus().isPresent()) {
                tooltip.add(TooltipHelper.temperature_bonus(
                        temperatureProperties.bonus().get().minTemperature(),
                        temperatureProperties.bonus().get().maxTemperature()
                ));
            }
        }
    }

    private static int applyEfficiency(int value, double efficiency) {
        return Math.max(0, (int) Math.round(value * efficiency));
    }
}
