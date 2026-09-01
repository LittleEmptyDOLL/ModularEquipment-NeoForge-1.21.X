package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonDefinition;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ModExoskeletons {
    private ModExoskeletons() {}

    public static final DeferredRegister<ExoskeletonDefinition> EXOSKELETONS =
            DeferredRegister.create(
                    ModRegistryKeys.EXOSKELETON_REGISTRY_KEY,
                    ExoEquipment.MODID
            );

    private static final Map<ResourceLocation,
            EquipmentEntry<ExoskeletonDefinition, ExoskeletonItem>> BY_ID
            = new HashMap<>();

    private static EquipmentEntry<ExoskeletonDefinition, ExoskeletonItem> register(
            String id,
            EquipmentProperties properties
    ) {
        ResourceLocation resourceLocation =
                ResourceLocation.fromNamespaceAndPath(
                        ExoEquipment.MODID,
                        id
                );

        DeferredHolder<ExoskeletonDefinition, ExoskeletonDefinition> definition =
                EXOSKELETONS.register(
                        id,
                        () -> new ExoskeletonDefinition(
                                resourceLocation,
                                properties
                        )
                );

        Item.Properties itemProperties = new Item.Properties().rarity(properties.rarity());

        DeferredHolder<Item, ExoskeletonItem> item =
                ModItems.ITEMS.register(
                        id + "_exoskeleton",
                        () -> new ExoskeletonItem(
                                definition,
                                itemProperties
                        )
                );

        EquipmentEntry<ExoskeletonDefinition, ExoskeletonItem> entry =
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

    public static EquipmentEntry<ExoskeletonDefinition, ExoskeletonItem> getEntry(ResourceLocation id) {
        return BY_ID.get(id);
    }

    public static ExoskeletonDefinition getDefinition(
            ResourceLocation id
    ) {
        return EXOSKELETONS
                .getEntries()
                .stream()
                .filter(entry -> entry.getId().equals(id))
                .findFirst()
                .map(DeferredHolder::get)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unknown exoskeleton: " + id
                        )
                );
    }

    public static Optional<
            EquipmentEntry<ExoskeletonDefinition, ExoskeletonItem>
            > find(ResourceLocation id) {
        return Optional.ofNullable(
                BY_ID.get(id)
        );
    }

    public static final EquipmentEntry<ExoskeletonDefinition, ExoskeletonItem> BASIC = register(
            "basic",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.UNCOMMON
            )
    );
}
