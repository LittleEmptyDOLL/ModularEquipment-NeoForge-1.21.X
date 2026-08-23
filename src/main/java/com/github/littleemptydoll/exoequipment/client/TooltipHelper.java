package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.util.NameUtils;
import net.minecraft.network.chat.Component;

public final class TooltipHelper {
    private TooltipHelper() {}

    public static Component type(
            String type
    ) {
        return Component.translatable(
                "tooltip.exoequipment.type",
                type
        );
    }

    public static Component tier(
            Object tier
    ) {
        return Component.translatable(
                "tooltip.exoequipment.tier",
                NameUtils.toDisplayName(tier.toString())
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
}
