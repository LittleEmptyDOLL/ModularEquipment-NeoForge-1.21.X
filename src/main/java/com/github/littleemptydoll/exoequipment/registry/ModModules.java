package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;

public final class ModModules {
    private ModModules() {}

    public static final DeferredRegister<ModuleDefinition> MODULES =
            DeferredRegister.create(
                    ModRegistryKeys.MODULE_REGISTRY,
                    ExoEquipment.MODID
            );

    private static final EquipmentRegistry<
            ModuleDefinition,
            ModuleItem
    > REGISTRY = new EquipmentRegistry<>(
            MODULES,
            ModItems.ITEMS,
            "_module",
            ModuleItem::new
    );

    private static final Map<ResourceLocation, ModuleDefinition>
            TRANSIENT_DEFINITIONS = new HashMap<>();

    static {
        ModEnergyModules.register(REGISTRY);
        ModDefenseModules.register(REGISTRY);
        ModStatusProtectionModules.register(REGISTRY);
        ModBodyProtectionModules.register(REGISTRY);
        ModMobilityModules.register(REGISTRY);
        ModSurvivalModules.register(REGISTRY);
        ModSensorModules.register(REGISTRY);
    }

    static void registerTransientDefinition(ModuleDefinition definition) {
        TRANSIENT_DEFINITIONS.put(definition.id(), definition);
    }

    public static ModuleDefinition getDefinition(
            ResourceLocation id
    ) {
        ModuleDefinition transientDefinition =
                TRANSIENT_DEFINITIONS.get(id);

        if (transientDefinition != null) {
            return transientDefinition;
        }

        return REGISTRY.getDefinition(id);
    }

    public static EquipmentEntry<
            ModuleDefinition,
            ModuleItem
    > find(
            ResourceLocation id
    ) {
        return REGISTRY.find(id);
    }
}
