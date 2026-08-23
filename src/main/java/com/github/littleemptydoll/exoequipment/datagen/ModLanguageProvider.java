package com.github.littleemptydoll.exoequipment.datagen;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.controller.ControllerDefinition;
import com.github.littleemptydoll.exoequipment.registry.ModControllers;
import com.github.littleemptydoll.exoequipment.registry.ModItems;
import com.github.littleemptydoll.exoequipment.util.NameUtils;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

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
        for (DeferredHolder<Item, ? extends Item> holder : ModItems.ITEMS.getEntries()) {
            ResourceLocation id = holder.getId();

            add(
                    "item." + id.getNamespace() + "." + id.getPath(),
                    NameUtils.toDisplayName(id.getPath())
            );
        }

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

        add(
                "tooltip.exoequipment.matrix_slot",
                "Matrix %s: %s"
        );

        add(
                "tooltip.exoequipment.modules",
                "Modules: %s"
        );

        add(
                "tooltip.exoequipment.installed_modules",
                "Installed Modules:"
        );

        add(
                "tooltip.exoequipment.module_entry",
                "  %s"
        );
    }
}
