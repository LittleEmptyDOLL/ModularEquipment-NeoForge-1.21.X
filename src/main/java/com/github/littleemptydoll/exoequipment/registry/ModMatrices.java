package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.matrix.MatrixDefinition;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMatrices {
    private ModMatrices() {}

    public static final DeferredRegister<MatrixDefinition> MATRICES =
            DeferredRegister.create(
                    ModRegistryKeys.MATRIX_REGISTRY_KEY,
                    ExoEquipment.MODID
            );

    public static final DeferredHolder<MatrixDefinition, MatrixDefinition> CIVILIAN = register(
            "civilian",
            EquipmentTier.CIVILIAN,
            Rarity.UNCOMMON,
            5,
            5
    );

    public static final DeferredHolder<MatrixDefinition, MatrixDefinition> MILITARY = register(
            "military",
            EquipmentTier.MILITARY,
            Rarity.RARE,
            7,
            7
    );

    public static final DeferredHolder<MatrixDefinition, MatrixDefinition> ENGINEERING = register(
            "engineering",
            EquipmentTier.ENGINEERING,
            Rarity.RARE,
            7,
            8
    );

    public static final DeferredHolder<MatrixDefinition, MatrixDefinition> EXPERIMENTAL = register(
            "experimental",
            EquipmentTier.EXPERIMENTAL,
            Rarity.EPIC,
            9,
            9
    );

    private static DeferredHolder<MatrixDefinition, MatrixDefinition> register(
            String id,
            EquipmentTier tier,
            Rarity rarity,
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
                        rarity,
                        width,
                        height
                )
        );
    }
}
