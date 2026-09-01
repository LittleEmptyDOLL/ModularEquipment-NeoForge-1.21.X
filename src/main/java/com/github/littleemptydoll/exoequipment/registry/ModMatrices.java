package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.MatrixItem;
import com.github.littleemptydoll.exoequipment.matrix.MatrixDefinition;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMatrices {
    private ModMatrices() {}

    public static final DeferredRegister<MatrixDefinition> MATRICES =
            DeferredRegister.create(
                    ModRegistryKeys.MATRIX_REGISTRY_KEY,
                    ExoEquipment.MODID
            );

    private static final EquipmentRegistry<
            MatrixDefinition,
            MatrixItem
    > REGISTRY =
            new EquipmentRegistry<>(
                    MATRICES,
                    ModItems.ITEMS,
                    "_matrix",
                    MatrixItem::new
            );

    public static MatrixDefinition getDefinition(
            ResourceLocation id
    ) {
        return REGISTRY.getDefinition(id);
    }

    public static EquipmentEntry<
            MatrixDefinition,
            MatrixItem
    > find(
            ResourceLocation id
    ) {
        return REGISTRY.find(id);
    }

    public static final EquipmentEntry<
            MatrixDefinition,
            MatrixItem
    > CIVILIAN = REGISTRY.register(
            "civilian",
            new EquipmentProperties(
                    EquipmentTier.CIVILIAN,
                    Rarity.UNCOMMON
            ),
            (id, properties) ->
                    new MatrixDefinition(
                            id,
                            properties,
                            5,
                            5
                    )
    );

    public static final EquipmentEntry<
            MatrixDefinition,
            MatrixItem
    > MILITARY = REGISTRY.register(
            "military",
            new EquipmentProperties(
                    EquipmentTier.MILITARY,
                    Rarity.RARE
            ),
            (id, properties) ->
                    new MatrixDefinition(
                            id,
                            properties,
                            7,
                            7
                    )
    );

    public static final EquipmentEntry<
            MatrixDefinition,
            MatrixItem
    > ENGINEERING = REGISTRY.register(
            "engineering",
            new EquipmentProperties(
                    EquipmentTier.ENGINEERING,
                    Rarity.RARE
            ),
            (id, properties) ->
                    new MatrixDefinition(
                            id,
                            properties,
                            7,
                            8
                    )
    );

    public static final EquipmentEntry<
            MatrixDefinition,
            MatrixItem
    > EXPERIMENTAL = REGISTRY.register(
            "experimental",
            new EquipmentProperties(
                    EquipmentTier.EXPERIMENTAL,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    new MatrixDefinition(
                            id,
                            properties,
                            9,
                            9
                    )
    );
}
