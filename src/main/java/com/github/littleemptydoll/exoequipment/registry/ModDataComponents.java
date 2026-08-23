package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.controller.Controller;
import com.github.littleemptydoll.exoequipment.energy.EnergySystem;
import com.github.littleemptydoll.exoequipment.exoskeleton.Exoskeleton;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.frame.Frame;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.module.Module;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModDataComponents {
    private ModDataComponents() {}

    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(
                    Registries.DATA_COMPONENT_TYPE,
                    ExoEquipment.MODID
            );

    public static final Supplier<DataComponentType<MatrixData>> MATRIX_DATA =
            COMPONENTS.registerComponentType(
                    "matrix_data",
                    builder -> builder
                            .persistent(MatrixData.CODEC)
            );

    public static final Supplier<DataComponentType<EnergySystem>> ENERGY_SYSTEM =
            COMPONENTS.registerComponentType(
                    "energy_system",
                    builder -> builder
                            .persistent(EnergySystem.CODEC)
            );

    public static final Supplier<DataComponentType<Frame>> FRAME =
            COMPONENTS.registerComponentType(
                    "frame",
                    builder -> builder
                            .persistent(Frame.CODEC)
            );

    public static final Supplier<DataComponentType<Controller>> CONTROLLER =
            COMPONENTS.registerComponentType(
                    "controller",
                    builder -> builder
                            .persistent(Controller.CODEC)
            );

    public static final Supplier<DataComponentType<Exoskeleton>> EXOSKELETON =
            COMPONENTS.registerComponentType(
                    "exoskeleton",
                    builder -> builder
                            .persistent(Exoskeleton.CODEC)
            );

    public static final Supplier<DataComponentType<ExoskeletonData>> EXOSKELETON_DATA =
            COMPONENTS.registerComponentType(
                    "exoskeleton_data",
                    builder -> builder
                            .persistent(ExoskeletonData.CODEC)
            );

    public static final Supplier<DataComponentType<Module>> MODULE =
            COMPONENTS.registerComponentType(
                    "module",
                    builder -> builder
                            .persistent(Module.CODEC)
            );

    public static void register(
            net.neoforged.bus.api.IEventBus eventBus
    ) {
        COMPONENTS.register(eventBus);
    }
}
