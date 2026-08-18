package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.matrix.MatrixDefinition;
import com.github.littleemptydoll.exoequipment.matrix.MatrixType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModMatrices {
    private ModMatrices() {}

    public static final DeferredRegister<MatrixDefinition> MATRICES =
            DeferredRegister.create(
                    ModRegistries.MATRIX_REGISTRY_KEY,
                    ExoEquipment.MODID
            );

    public static final DeferredHolder<MatrixDefinition, MatrixDefinition> CIVILIAN = register(
            "civilian",
            MatrixType.CIVILIAN,
            5,
            5
    );

    public static final DeferredHolder<MatrixDefinition, MatrixDefinition> MILITARY = register(
            "military",
            MatrixType.MILITARY,
            7,
            7
    );

    public static final DeferredHolder<MatrixDefinition, MatrixDefinition> EXPERIMENTAL = register(
            "experimental",
            MatrixType.EXPERIMENTAL,
            9,
            9
    );

    private static DeferredHolder<MatrixDefinition, MatrixDefinition> register(
            String id,
            MatrixType type,
            int width,
            int height
    ) {
        ResourceLocation location =
                ResourceLocation.fromNamespaceAndPath(
                        ExoEquipment.MODID,
                        id
                );

        return MATRICES.register(
                id,
                () -> new MatrixDefinition(
                        location,
                        type,
                        width,
                        height
                )
        );
    }
}
