package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.matrix.MatrixDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

public final class ModRegistries {
    private ModRegistries() {}

    public static final ResourceKey<Registry<MatrixDefinition>> MATRIX_REGISTRY_KEY =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(
                            ExoEquipment.MODID,
                            "matrix"
                    )
            );

    public static final Registry<MatrixDefinition> MATRIX_REGISTRY =
            new RegistryBuilder<MatrixDefinition>(
                    MATRIX_REGISTRY_KEY
            )
                    .sync(true)
                    .create();

    public static final ResourceKey<Registry<ModuleDefinition>> MODULE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(
                            ExoEquipment.MODID,
                            "module"
                    )
            );

    public static final Registry<ModuleDefinition> MODULE_REGISTRY =
            new RegistryBuilder<ModuleDefinition>(
                    MODULE_REGISTRY_KEY
            )
                    .sync(true)
                    .create();

    public static void register(NewRegistryEvent event) {
        event.register(MATRIX_REGISTRY);
        event.register(MODULE_REGISTRY);
    }
}
