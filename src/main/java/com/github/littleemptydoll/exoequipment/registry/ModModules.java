package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.GeneratedModuleData;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    private static final Map<ResourceLocation, GeneratedModuleData>
            GENERATED_DATA = new HashMap<>();

    static {
        ModGeneratedModules.register(REGISTRY);
        ModEnergyModules.register(REGISTRY);
        ModThermalModules.register(REGISTRY);
        ModDefenseModules.register(REGISTRY);
        ModStatusProtectionModules.register(REGISTRY);
        ModBodyProtectionModules.register(REGISTRY);
        ModSurvivalModules.register(REGISTRY);
        ModCombatModules.register(REGISTRY);
        ModMobilityModules.register(REGISTRY);
        ModUtilityModules.register(REGISTRY);
        ModSensorModules.register(REGISTRY);
    }

    public static void registerTransientDefinition(ModuleDefinition definition) {
        TRANSIENT_DEFINITIONS.put(definition.id(), definition);
    }

    public static void registerGeneratedData(GeneratedModuleData generated) {
        GENERATED_DATA.put(generated.definition().id(), generated);
        registerTransientDefinition(generated.definition());
    }

    public static Optional<GeneratedModuleData> getGeneratedData(
            ResourceLocation id
    ) {
        return Optional.ofNullable(GENERATED_DATA.get(id));
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
