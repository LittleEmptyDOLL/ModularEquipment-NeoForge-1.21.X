package com.github.littleemptydoll.exoequipment.datagen;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.registry.ModItems;
import com.github.littleemptydoll.exoequipment.util.NameUtils;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(PackOutput output) {
        super(output, ExoEquipment.MODID, ExoEquipment.LOCALE);
    }

    @Override
    protected void addTranslations() {
        for (DeferredHolder<Item, ? extends Item> holder : ModItems.ITEMS.getEntries()) {
            ResourceLocation id = holder.getId();
            add("item." + id.getNamespace() + "." + id.getPath(),
                    NameUtils.toDisplayName(id.getPath()));
        }

        add("tooltip.exoequipment.type", "Type: %s");
        add("tooltip.exoequipment.tier", "Tier: %s");
        add("tooltip.exoequipment.size", "Size: %s x %s");
        add("tooltip.exoequipment.input", "Max input: %s FE/t");
        add("tooltip.exoequipment.output", "Max output: %s FE/t");
        add("tooltip.exoequipment.efficiency", "Efficiency: %s%%");
        add("tooltip.exoequipment.frame", "Frame: %s");
        add("tooltip.exoequipment.controller", "Controller: %s");
        add("tooltip.exoequipment.energy_system", "Energy system: %s");
        add("tooltip.exoequipment.matrices", "Matrices: %s / %s");
        add("tooltip.exoequipment.active_profile", "Active profile: %s");
        add("tooltip.exoequipment.installed_matrices", "Installed matrices:");
        add("tooltip.exoequipment.empty", "Empty");
        add("tooltip.exoequipment.max_profiles", "Max profiles: %s");
        add("tooltip.exoequipment.max_active_matrices", "Max active matrices: %s");
        add("tooltip.exoequipment.max_module_size", "Max module size: %s x %s");
        add("tooltip.exoequipment.matrix_slot", "Matrix %s: %s");
        add("tooltip.exoequipment.modules", "Modules: %s");
        add("tooltip.exoequipment.installed_modules", "Installed modules:");
        add("tooltip.exoequipment.module_entry", "  %s");
        add("tooltip.exoequipment.category", "Category: %s");
        add("tooltip.exoequipment.consumption", "Consumption: %s FE/t");
        add("tooltip.exoequipment.generation", "Generation: %s FE/t");
        add("tooltip.exoequipment.capacity", "Capacity: %s/%s FE");
        add("tooltip.exoequipment.cooling", "Сooling: %s°C");
        add("tooltip.exoequipment.heat_generation", "Heat generation: %s°C");
        add("tooltip.exoequipment.temperature", "Operating temperature: %s°C - %s°C");
        add("tooltip.exoequipment.temperature_bonus", "Effective temperature: %s°C - %s°C");

        add("gui.exoequipment.exoskeleton", "Exoskeleton");
        add("gui.exoequipment.components", "Components");
        add("gui.exoequipment.matrices", "Matrices");
        add("gui.exoequipment.matrix", "Matrix");
        add("gui.exoequipment.matrix_size", "Matrix %s/%s");
        add("gui.exoequipment.system", "System status");
        add("gui.exoequipment.system_status", "System status");
        add("gui.exoequipment.status.energy_stable", "Energy system stable");
        add("gui.exoequipment.status.energy_warning", "Insufficient energy");
        add("gui.exoequipment.status.energy_critical", "Energy system cannot supply active modules");
        add("gui.exoequipment.status.energy_unavailable", "Energy system unavailable");
        add("gui.exoequipment.status.controller_missing", "Controller not installed");
        add("gui.exoequipment.status.frame_missing", "Frame not installed");
        add("gui.exoequipment.status.module_too_large", "Module exceeds frame size");
        add("gui.exoequipment.inventory", "Inventory");
        add("gui.exoequipment.profiles", "Profiles");
        add("gui.exoequipment.profiles_count", "Profiles %s/%s");
        add("gui.exoequipment.profile", "Profile %s");
        add("gui.exoequipment.profile_create", "Create");
        add("gui.exoequipment.profile_remove", "Remove");
        add("gui.exoequipment.profile_delete", "Delete");
        add("gui.exoequipment.matrix_short", "M%s");
        add("gui.exoequipment.matrix_slot", "Slot %s");
        add("gui.exoequipment.active_profile", "Active profile:");
        add("gui.exoequipment.active_matrices", "Active matrices:");
        add("gui.exoequipment.active_matrices_count", "Active Matrices %s/%s");
        add("gui.exoequipment.back", "Back");
        add("gui.exoequipment.characteristics", "Characteristics");
        add("gui.exoequipment.yes", "Yes");
        add("gui.exoequipment.no", "No");
        add("gui.exoequipment.characteristic.category.energy", "Energy");
        add("gui.exoequipment.characteristic.category.thermal", "Thermal");
        add("gui.exoequipment.characteristic.category.defense", "Defense");
        add("gui.exoequipment.characteristic.category.shield", "Shield");
        add("gui.exoequipment.characteristic.category.attributes", "Attributes");
        add("gui.exoequipment.characteristic.category.regeneration", "Regeneration");
        add("gui.exoequipment.characteristic.category.mobility", "Mobility");
        add("gui.exoequipment.characteristic.category.survival", "Survival");
        add("gui.exoequipment.characteristic.category.sensor", "Sensors");
        add("gui.exoequipment.characteristic.category.utility", "Utility");
        add("gui.exoequipment.characteristic.category.combat", "Combat");

        add("gui.exoequipment.characteristic.consumption", "Consumption");
        add("gui.exoequipment.characteristic.generation", "Generation");
        add("gui.exoequipment.characteristic.storage_capacity", "Storage capacity");
        add("gui.exoequipment.characteristic.storage_input", "Storage input");
        add("gui.exoequipment.characteristic.storage_output", "Storage output");
        add("gui.exoequipment.characteristic.max_input", "Maximum input");
        add("gui.exoequipment.characteristic.max_output", "Maximum output");
        add("gui.exoequipment.characteristic.heat_generation", "Heat generation");
        add("gui.exoequipment.characteristic.cooling", "Cooling");
        add("gui.exoequipment.characteristic.thermal_balance", "Thermal balance");
        add("gui.exoequipment.characteristic.current_energy", "Current energy");
        add("gui.exoequipment.characteristic.capacity", "Shield capacity");
        add("gui.exoequipment.characteristic.health_per_second", "Health regeneration");
        add("gui.exoequipment.characteristic.fall_damage_reduction", "Fall damage reduction");
        add("gui.exoequipment.characteristic.flight", "Flight");
        add("gui.exoequipment.characteristic.jetpack.vertical_thrust", "Jetpack vertical thrust");
        add("gui.exoequipment.characteristic.jetpack.horizontal_speed", "Jetpack horizontal speed");
        add("gui.exoequipment.characteristic.elytra.acceleration", "Elytra acceleration");
        add("gui.exoequipment.characteristic.elytra.max_speed", "Elytra maximum speed");
        add("gui.exoequipment.characteristic.blink.distance", "Blink distance");
        add("gui.exoequipment.characteristic.blink.activation_energy", "Blink energy");
        add("gui.exoequipment.characteristic.blink.cooldown", "Blink cooldown");
        add("gui.exoequipment.characteristic.hunger.exhaustion_reduction", "Hunger exhaustion reduction");
        add("gui.exoequipment.characteristic.revival.restore_health", "Revival health");
        add("gui.exoequipment.characteristic.revival.cooldown", "Revival cooldown");
        add("gui.exoequipment.characteristic.thirst.exhaustion_reduction", "Thirst exhaustion reduction");
        add("gui.exoequipment.characteristic.painkiller", "Painkiller");
        add("gui.exoequipment.characteristic.night_vision", "Night vision");
        add("gui.exoequipment.characteristic.entity_detection.range", "Entity detection range");
        add("gui.exoequipment.characteristic.entity_detection.players", "Detect players");
        add("gui.exoequipment.characteristic.entity_detection.mobs", "Detect mobs");
        add("gui.exoequipment.characteristic.entity_detection.hostile", "Detect hostile");
        add("gui.exoequipment.characteristic.block_scanner.range", "Block scanner range");
        add("gui.exoequipment.characteristic.pickup_magnet.radius", "Pickup magnet radius");
        add("gui.exoequipment.characteristic.pickup_magnet.items", "Pickup items");
        add("gui.exoequipment.characteristic.pickup_magnet.experience", "Pickup experience");
        add("gui.exoequipment.characteristic.cloaking.active_consumption", "Cloaking consumption");
        add("gui.exoequipment.characteristic.cloaking.active", "Cloaking active");
        add("gui.exoequipment.characteristic.emergency_shield.restore", "Emergency shield restore");
        add("gui.exoequipment.characteristic.emergency_shield.cooldown", "Emergency shield cooldown");


        add("menu.exoequipment.matrix", "Matrix");
        add("menu.exoequipment.profiles", "Profiles");
        add("key.exoequipment.open_exoskeleton", "Open exoskeleton");
        add("key.exoequipment.activate_cloaking", "Activate cloaking");
        add("key.exoequipment.activate_blink", "Activate blink");
        add("key.exoequipment.activate_flight", "Activate flight");
        add("key.categories.exoequipment", "ExoEquipment");
    }
}
