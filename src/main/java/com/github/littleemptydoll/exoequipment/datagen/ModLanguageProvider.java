package com.github.littleemptydoll.exoequipment.datagen;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(
            PackOutput output
    ) {
        super(
                output,
                ExoEquipment.MODID,
                ExoEquipment.LOCALE
        );
    }

    @Override
    protected void addTranslations() {
        add(
                "tooltip.exoequipment.type",
                "Type: %s"
        );

        add(
                "tooltip.exoequipment.tier",
                "Tier: %s"
        );

        add(
                "tooltip.exoequipment.size",
                "Size: %s x %s"
        );

        add(
                "tooltip.exoequipment.input",
                "Max input: %s FE/t"
        );

        add(
                "tooltip.exoequipment.output",
                "Max output: %s FE/t"
        );

        add(
                "tooltip.exoequipment.efficiency",
                "Efficiency: %s%%"
        );

        add(
                "tooltip.exoequipment.frame",
                "Frame: %s"
        );

        add(
                "tooltip.exoequipment.controller",
                "Controller: %s"
        );

        add(
                "tooltip.exoequipment.energy_system",
                "Energy System: %s"
        );

        add(
                "tooltip.exoequipment.matrices",
                "Matrices: %s / %s"
        );

        add(
                "tooltip.exoequipment.active_profile",
                "Active profile: %s"
        );

        add(
                "tooltip.exoequipment.installed_matrices",
                "Installed Matrices:"
        );

        add(
                "tooltip.exoequipment.empty",
                "Empty"
        );

        add(
                "tooltip.exoequipment.max_profiles",
                "Max profiles: %s"
        );

        add(
                "tooltip.exoequipment.max_active_matrices",
                "Max Active Matrices: %s"
        );

        add(
                "tooltip.exoequipment.max_module_size",
                "Max Module Size: %s x %s"
        );
    }
}
