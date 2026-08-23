package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.controller.ControllerDefinition;
import com.github.littleemptydoll.exoequipment.energy.EnergySystemDefinition;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonDefinition;
import com.github.littleemptydoll.exoequipment.frame.FrameDefinition;
import com.github.littleemptydoll.exoequipment.matrix.MatrixDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class ModRegistryKeys {
    private ModRegistryKeys() {}

    public static void register(NewRegistryEvent event) {
        event.register(MATRIX_REGISTRY);
        event.register(MODULE_REGISTRY);
        event.register(ENERGY_SYSTEM_REGISTRY);
        event.register(FRAME_REGISTRY);
        event.register(CONTROLLER_REGISTRY);
        event.register(EXOSKELETON_REGISTRY);
    }

    // Матрица
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

    // Модуль
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

    // Энергосистема
    public static final ResourceKey<Registry<EnergySystemDefinition>> ENERGY_SYSTEM_REGISTRY_KEY =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(
                            ExoEquipment.MODID,
                            "energy_system"
                    )
            );

    public static final Registry<EnergySystemDefinition> ENERGY_SYSTEM_REGISTRY =
            new RegistryBuilder<EnergySystemDefinition>(
                    ENERGY_SYSTEM_REGISTRY_KEY
            )
                    .sync(true)
                    .create();

    // Рама
    public static final ResourceKey<Registry<FrameDefinition>> FRAME_REGISTRY_KEY =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(
                            ExoEquipment.MODID,
                            "frame"
                    )
            );

    public static final Registry<FrameDefinition> FRAME_REGISTRY =
            new RegistryBuilder<FrameDefinition>(
                    FRAME_REGISTRY_KEY
            )
                    .sync(true)
                    .create();

    // Контроллер
    public static final ResourceKey<Registry<ControllerDefinition>> CONTROLLER_REGISTRY_KEY =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(
                            ExoEquipment.MODID,
                            "controller"
                    )
            );

    public static final Registry<ControllerDefinition> CONTROLLER_REGISTRY =
            new RegistryBuilder<ControllerDefinition>(
                    CONTROLLER_REGISTRY_KEY
            )
                    .sync(true)
                    .create();

    // Экзоскелет
    public static final ResourceKey<Registry<ExoskeletonDefinition>> EXOSKELETON_REGISTRY_KEY =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(
                            ExoEquipment.MODID,
                            "exoskeleton"
                    )
            );

    public static final Registry<ExoskeletonDefinition> EXOSKELETON_REGISTRY =
            new RegistryBuilder<ExoskeletonDefinition>(
                    EXOSKELETON_REGISTRY_KEY
            )
                    .sync(true)
                    .create();
}
