package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import com.github.littleemptydoll.exoequipment.util.NameUtils;
import com.github.littleemptydoll.exoequipment.util.NumberFormatter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.Locale;

public final class TooltipHelper {
    private TooltipHelper() {}

    public static boolean isShiftDown() {
        return Screen.hasShiftDown();
    }

    private static Component styledValue(int value, double efficiency) {
        Component valueComponent = Component.literal(NumberFormatter.format(value));

        if (efficiency > 1.0D) {
            valueComponent = valueComponent.copy().withStyle(ChatFormatting.GREEN);
        } else if (efficiency < 1.0D) {
            valueComponent = valueComponent.copy().withStyle(ChatFormatting.RED);
        }

        return valueComponent;
    }

    public static Component tier(EquipmentTier tier) {
        String key = "gui.exoequipment.fabricator." + tier.name().toLowerCase(Locale.ROOT);
        return Component.translatable("tooltip.exoequipment.tier",
                I18n.exists(key) ? Component.translatable(key) : NameUtils.toDisplayName(tier.name()));
    }

    public static Component size(int width, int height) {
        return Component.translatable("tooltip.exoequipment.size", width, height);
    }

    public static Component input(int value) {
        return Component.translatable("tooltip.exoequipment.input", NumberFormatter.format(value));
    }

    public static Component input(int value, double efficiency) {
        return Component.translatable("tooltip.exoequipment.input", styledValue(value, efficiency));
    }

    public static Component output(int value) {
        return Component.translatable("tooltip.exoequipment.output", NumberFormatter.format(value));
    }

    public static Component output(int value, double efficiency) {
        return Component.translatable("tooltip.exoequipment.output", styledValue(value, efficiency));
    }

    public static Component efficiency(double value) {
        return Component.translatable("tooltip.exoequipment.efficiency", NumberFormatter.format(value * 100));
    }

    public static Component matrices(int size, int maxMatrices) {
        return Component.translatable("tooltip.exoequipment.matrices", size, maxMatrices);
    }

    public static Component activeProfile(int value) {
        return Component.translatable("tooltip.exoequipment.active_profile", value);
    }

    public static Component maxProfiles(int value) {
        return Component.translatable("tooltip.exoequipment.max_profiles", value);
    }

    public static Component maxActiveMatrices(int value) {
        return Component.translatable("tooltip.exoequipment.max_active_matrices", value);
    }

    public static Component maxModuleSize(int width, int height) {
        return Component.translatable("tooltip.exoequipment.max_module_size", width, height);
    }

    public static Component modules(int count) {
        return Component.translatable("tooltip.exoequipment.modules", count);
    }

    public static Component installedModules() {
        return Component.translatable("tooltip.exoequipment.installed_modules");
    }

    public static Component moduleEntry(Component module) {
        return Component.translatable("tooltip.exoequipment.module_entry", module);
    }

    public static Component frame(Component frame) {
        return Component.translatable("tooltip.exoequipment.frame", frame);
    }

    public static Component controller(Component controller) {
        return Component.translatable("tooltip.exoequipment.controller", controller);
    }

    public static Component energySystem(Component energySystem) {
        return Component.translatable("tooltip.exoequipment.energy_system", energySystem);
    }

    public static Component installedMatrices() {
        return Component.translatable("tooltip.exoequipment.installed_matrices");
    }

    public static Component empty() {
        return Component.translatable("tooltip.exoequipment.empty");
    }

    public static Component matrixSlot(int slot, Component matrixName) {
        return Component.translatable("tooltip.exoequipment.matrix_slot", slot, matrixName);
    }

    public static Component category(ModuleCategory category) {
        return Component.translatable("tooltip.exoequipment.category",
                Component.translatable("gui.exoequipment.fabricator.module_category."
                        + category.name().toLowerCase(Locale.ROOT)));
    }

    public static Component energyConsumption(int consumption) {
        return Component.translatable("tooltip.exoequipment.consumption", NumberFormatter.format(consumption));
    }

    public static Component energyConsumption(int consumption, double efficiency) {
        return Component.translatable("tooltip.exoequipment.consumption", styledValue(consumption, efficiency));
    }

    public static Component energyGeneration(int generation) {
        return Component.translatable("tooltip.exoequipment.generation", NumberFormatter.format(generation));
    }

    public static Component energyGeneration(int generation, double efficiency) {
        return Component.translatable("tooltip.exoequipment.generation", styledValue(generation, efficiency));
    }

    public static Component capacity(int stored, int capacity) {
        return Component.translatable("tooltip.exoequipment.capacity", NumberFormatter.format(stored), NumberFormatter.format(capacity));
    }

    public static Component capacity(int stored, int capacity, double efficiency) {
        return Component.translatable("tooltip.exoequipment.capacity", NumberFormatter.format(stored), styledValue(capacity, efficiency));
    }

    public static Component cooling(int cooling) {
        return Component.translatable("tooltip.exoequipment.cooling", NumberFormatter.format(cooling));
    }

    public static Component cooling(int cooling, double efficiency) {
        return Component.translatable("tooltip.exoequipment.cooling", styledValue(cooling, efficiency));
    }

    public static Component heatGeneration(int heatGeneration) {
        return Component.translatable("tooltip.exoequipment.heat_generation", NumberFormatter.format(heatGeneration));
    }

    public static Component heatGeneration(int heatGeneration, double efficiency) {
        return Component.translatable("tooltip.exoequipment.heat_generation", styledValue(heatGeneration, efficiency));
    }

    public static Component property(String name, Object value) {
        String displayed = value instanceof Number number ? NumberFormatter.format(number.doubleValue()) : String.valueOf(value);
        if (value instanceof Boolean state) {
            displayed = Component.translatable(state ? "gui.exoequipment.yes" : "gui.exoequipment.no").getString();
        }
        return property(name, Component.literal(displayed));
    }

    public static Component property(String name, Component value) {
        String key = "tooltip.exoequipment.property." + name.toLowerCase(Locale.ROOT).replace(' ', '_');
        Component label = I18n.exists(key) ? Component.translatable(key) : Component.literal(NameUtils.toDisplayName(name));
        return label.copy().append(": ").append(value);
    }

    public static Component temperature(double min, double max) {
        return Component.translatable("tooltip.exoequipment.temperature", NumberFormatter.format(min), NumberFormatter.format(max));
    }

    public static Component temperature_bonus(double min, double max) {
        return Component.translatable("tooltip.exoequipment.temperature_bonus", NumberFormatter.format(min), NumberFormatter.format(max));
    }
}
