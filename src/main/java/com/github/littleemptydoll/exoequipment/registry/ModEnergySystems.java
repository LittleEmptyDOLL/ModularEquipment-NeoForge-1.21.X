package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.energy.EnergySystemDefinition;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEnergySystems {
    private ModEnergySystems() {}

    public static final DeferredRegister<EnergySystemDefinition> ENERGY_SYSTEMS =
            DeferredRegister.create(
                    ModRegistries.ENERGY_SYSTEM_REGISTRY_KEY,
                    ExoEquipment.MODID
            );

    private static DeferredHolder<EnergySystemDefinition, EnergySystemDefinition> register(
            String id,
            EquipmentTier tier,
            int maxInput,
            int maxOutput,
            double efficiency
    ) {
        ResourceLocation location =
                ResourceLocation.fromNamespaceAndPath(
                        ExoEquipment.MODID,
                        id
                );

        return ENERGY_SYSTEMS.register(
                id,
                () -> new EnergySystemDefinition(
                        location,
                        tier,
                        maxInput,
                        maxOutput,
                        efficiency
                )
        );
    }

    public static EnergySystemDefinition getDefinition(
            ResourceLocation id
    ) {
        return ENERGY_SYSTEMS
                .getEntries()
                .stream()
                .filter(entry -> entry.getId().equals(id))
                .findFirst()
                .map(DeferredHolder::get)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unknown energy system: " + id
                        )
                );
    }

    // ToDo: Определить подходящие характеристики
    public static final DeferredHolder<EnergySystemDefinition, EnergySystemDefinition> CIVILIAN = register(
            "civilian",
            EquipmentTier.CIVILIAN,
            100,
            100,
            1.0
    );

    public static final DeferredHolder<EnergySystemDefinition, EnergySystemDefinition> MILITARY = register(
            "military",
            EquipmentTier.MILITARY,
            200,
            200,
            1.0
    );

    public static final DeferredHolder<EnergySystemDefinition, EnergySystemDefinition> ENGINEERING = register(
            "engineering",
            EquipmentTier.ENGINEERING,
            300,
            200,
            1.0
    );

    public static final DeferredHolder<EnergySystemDefinition, EnergySystemDefinition> EXPERIMENTAL = register(
            "experimental",
            EquipmentTier.EXPERIMENTAL,
            300,
            300,
            1.1
    );
}
