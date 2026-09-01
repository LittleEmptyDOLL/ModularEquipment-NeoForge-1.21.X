package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import com.github.littleemptydoll.exoequipment.util.NameUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class TooltipHelper {
    private TooltipHelper() {}

    public static boolean isShiftDown() {
        return Screen.hasShiftDown();
    }

    public static Component tier(
            EquipmentTier tier
    ) {
        return Component.translatable(
                "tooltip.exoequipment.tier",
                NameUtils.toDisplayName(tier.name())
        );
    }

    public static Component size(
            int width,
            int height
    ) {
        return Component.translatable(
                "tooltip.exoequipment.size",
                width,
                height
        );
    }

    public static Component input(
            int value
    ) {
        return Component.translatable(
                "tooltip.exoequipment.input",
                value
        );
    }

    public static Component output(
            int value
    ) {
        return Component.translatable(
                "tooltip.exoequipment.output",
                value
        );
    }

    public static Component efficiency(
            double value
    ) {
        return Component.translatable(
                "tooltip.exoequipment.efficiency",
                Math.round(value * 100)
        );
    }

    public static Component matrices(
            int size,
            int maxMatrices
    ) {
        return Component.translatable(
                "tooltip.exoequipment.matrices",
                size,
                maxMatrices
        );
    }

    public static Component activeProfile(
            int value
    ) {
        return Component.translatable(
                "tooltip.exoequipment.active_profile",
                value
        );
    }

    public static Component maxProfiles(
            int value
    ) {
        return Component.translatable(
                "tooltip.exoequipment.max_profiles",
                value
        );
    }

    public static Component maxActiveMatrices(
            int value
    ) {
        return Component.translatable(
                "tooltip.exoequipment.max_active_matrices",
                value
        );
    }

    public static Component maxModuleSize(
            int width,
            int height
    ) {
        return Component.translatable(
                "tooltip.exoequipment.max_module_size",
                width,
                height
        );
    }

    public static Component modules(
            int count
    ) {
        return Component.translatable(
                "tooltip.exoequipment.modules",
                count
        );
    }

    public static Component installedModules() {
        return Component.translatable(
                "tooltip.exoequipment.installed_modules"
        );
    }

    public static Component moduleEntry(
            Component module
    ) {
        return Component.translatable(
                "tooltip.exoequipment.module_entry",
                module
        );
    }

    public static Component frame(
            Component frame
    ) {
        return Component.translatable(
                "tooltip.exoequipment.frame",
                frame
        );
    }

    public static Component controller(
            Component controller
    ) {
        return Component.translatable(
                "tooltip.exoequipment.controller",
                controller
        );
    }

    public static Component energySystem(
            Component energySystem
    ) {
        return Component.translatable(
                "tooltip.exoequipment.energy_system",
                energySystem
        );
    }

    public static Component installedMatrices() {
        return Component.translatable(
                "tooltip.exoequipment.installed_matrices"
        );
    }

    public static Component empty() {
        return Component.translatable(
                "tooltip.exoequipment.empty"
        );
    }

    public static Component matrixSlot(
            int slot,
            Component matrixName
    ) {
        return Component.translatable(
                "tooltip.exoequipment.matrix_slot",
                slot,
                matrixName
        );
    }
}
