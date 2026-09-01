package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.MatrixItem;
import com.github.littleemptydoll.exoequipment.matrix.MatrixDefinition;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ModMatrices {
    private ModMatrices() {}

    public static final DeferredRegister<MatrixDefinition> MATRICES =
            DeferredRegister.create(
                    ModRegistryKeys.MATRIX_REGISTRY_KEY,
                    ExoEquipment.MODID
            );

    private static final Map<ResourceLocation,
            EquipmentEntry<MatrixDefinition, MatrixItem>> BY_ID
            = new HashMap<>();

    private static EquipmentEntry<MatrixDefinition, MatrixItem> register(
            String id,
            EquipmentProperties properties,
            int width,
            int height
    ) {
        ResourceLocation resourceLocation =
                ResourceLocation.fromNamespaceAndPath(
                        ExoEquipment.MODID,
                        id
                );

        DeferredHolder<MatrixDefinition, MatrixDefinition> definition =
                MATRICES.register(
                        id,
                        () -> new MatrixDefinition(
                                resourceLocation,
                                properties,
                                width,
                                height
                        )
                );

        Item.Properties itemProperties = new Item.Properties().rarity(properties.rarity());

        DeferredHolder<Item, MatrixItem> item =
                ModItems.ITEMS.register(
                        id + "_matrix",
                        () -> new MatrixItem(
                                definition,
                                itemProperties
                        )
                );

        EquipmentEntry<MatrixDefinition, MatrixItem> entry =
                new EquipmentEntry<>(
                        definition,
                        item
                );

        BY_ID.put(
                resourceLocation,
                entry
        );

        return entry;
    }

    public static EquipmentEntry<MatrixDefinition, MatrixItem> getEntry(ResourceLocation id) {
        return BY_ID.get(id);
    }

    public static MatrixDefinition getDefinition(ResourceLocation id) {
        EquipmentEntry<MatrixDefinition, MatrixItem> entry = getEntry(id);

        if (entry == null) {
            throw new IllegalArgumentException(
                    "Unknown matrix: " + id
            );
        }

        return entry.getDefinition();
    }

    public static Optional<
            EquipmentEntry<MatrixDefinition, MatrixItem>
    > find(ResourceLocation id) {
        return Optional.ofNullable(
                BY_ID.get(id)
        );
    }

    public static final EquipmentEntry<MatrixDefinition, MatrixItem> CIVILIAN = register(
            "civilian",
            new EquipmentProperties(
                    EquipmentTier.CIVILIAN,
                    Rarity.UNCOMMON
            ),
            5,
            5
    );

    public static final EquipmentEntry<MatrixDefinition, MatrixItem> MILITARY = register(
            "military",
            new EquipmentProperties(
                    EquipmentTier.MILITARY,
                    Rarity.RARE
            ),
            7,
            7
    );

    public static final EquipmentEntry<MatrixDefinition, MatrixItem> ENGINEERING = register(
            "engineering",
            new EquipmentProperties(
                    EquipmentTier.ENGINEERING,
                    Rarity.RARE
            ),
            7,
            8
    );

    public static final EquipmentEntry<MatrixDefinition, MatrixItem> EXPERIMENTAL = register(
            "experimental",
            new EquipmentProperties(
                    EquipmentTier.EXPERIMENTAL,
                    Rarity.EPIC
            ),
            9,
            9
    );
}
