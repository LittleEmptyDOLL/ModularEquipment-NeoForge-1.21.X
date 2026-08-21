package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.energy.EnergySystemDefinition;
import com.github.littleemptydoll.exoequipment.energy.EnergySystemType;
import com.github.littleemptydoll.exoequipment.matrix.MatrixDefinition;
import com.github.littleemptydoll.exoequipment.matrix.MatrixType;
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
            EnergySystemType type,
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
                        type,
                        maxInput,
                        maxOutput,
                        efficiency
                )
        );
    }

    public static final DeferredHolder<EnergySystemDefinition, EnergySystemDefinition> CIVILIAN = register(
            "civilian",
            EnergySystemType.CIVILIAN,
            100,
            100,
            1.0
    );

    public static final DeferredHolder<EnergySystemDefinition, EnergySystemDefinition> MILITARY = register(
            "military",
            EnergySystemType.MILITARY,
            200,
            200,
            1.0
    );

    public static final DeferredHolder<EnergySystemDefinition, EnergySystemDefinition> EXPERIMENTAL = register(
            "experimental",
            EnergySystemType.EXPERIMENTAL,
            300,
            300,
            1.1
    );
}
