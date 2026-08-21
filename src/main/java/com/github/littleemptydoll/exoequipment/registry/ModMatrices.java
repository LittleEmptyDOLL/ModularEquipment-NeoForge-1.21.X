package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.matrix.MatrixDefinition;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMatrices {
    private ModMatrices() {}

    public static final DeferredRegister<MatrixDefinition> MATRICES =
            DeferredRegister.create(
                    ModRegistries.MATRIX_REGISTRY_KEY,
                    ExoEquipment.MODID
            );

    public static final DeferredHolder<MatrixDefinition, MatrixDefinition> CIVILIAN = register(
            "civilian",
            EquipmentTier.CIVILIAN,
            5,
            5
    );

    public static final DeferredHolder<MatrixDefinition, MatrixDefinition> MILITARY = register(
            "military",
            EquipmentTier.MILITARY,
            7,
            7
    );

    public static final DeferredHolder<MatrixDefinition, MatrixDefinition> EXPERIMENTAL = register(
            "experimental",
            EquipmentTier.EXPERIMENTAL,
            9,
            9
    );

    private static DeferredHolder<MatrixDefinition, MatrixDefinition> register(
            String id,
            EquipmentTier tier,
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
                        tier,
                        width,
                        height
                )
        );
    }
}
