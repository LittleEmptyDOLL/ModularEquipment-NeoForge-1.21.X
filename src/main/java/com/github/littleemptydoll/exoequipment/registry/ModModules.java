package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.module.*;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

public class ModModules {
    private  ModModules() {}

    public static final DeferredRegister<ModuleDefinition> MODULES =
            DeferredRegister.create(
                    ModRegistries.MODULE_REGISTRY,
                    ExoEquipment.MODID
            );

    public static final DeferredHolder<
            ModuleDefinition,
            ModuleDefinition
    > TEST_MODULE = register(
            "test_module",
            new ModuleDefinition(
                    ResourceLocation.fromNamespaceAndPath(
                            ExoEquipment.MODID,
                            "test_module"
                    ),
                    new ModuleSize(2,2),
                    ModuleCategory.UTILITY,
                    EquipmentTier.CIVILIAN,
                    Optional.of(new EnergyProperties(20)),
                    Optional.of(new ThermalProperties(2, 15))
            )
    );

    private static DeferredHolder<
            ModuleDefinition,
            ModuleDefinition
            > register(
            String id,
            ModuleDefinition definition
    ) {
        return MODULES.register(
                id,
                () -> definition
        );
    }

    public static ModuleDefinition getDefinition(
            ResourceLocation id
    ) {
        ModuleDefinition definition = ModRegistries.MODULE_REGISTRY.get(id);

        if (definition == null) {
            throw new IllegalArgumentException("Unknown module: " + id);
        }

        return definition;
    }
}
