package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.controller.ControllerDefinition;
import com.github.littleemptydoll.exoequipment.item.ControllerItem;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ModControllers {
    private ModControllers() {}

    public static final DeferredRegister<ControllerDefinition> CONTROLLERS =
            DeferredRegister.create(
                    ModRegistryKeys.CONTROLLER_REGISTRY_KEY,
                    ExoEquipment.MODID
            );

    private static final Map<ResourceLocation,
            EquipmentEntry<ControllerDefinition, ControllerItem>> BY_ID
            = new HashMap<>();

    private static EquipmentEntry<ControllerDefinition, ControllerItem> register(
            String id,
            EquipmentProperties properties,
            int maxProfiles,
            int maxActiveMatrices
    ) {
        ResourceLocation resourceLocation =
                ResourceLocation.fromNamespaceAndPath(
                        ExoEquipment.MODID,
                        id
                );

        DeferredHolder<ControllerDefinition, ControllerDefinition> definition =
                CONTROLLERS.register(
                        id,
                        () -> new ControllerDefinition(
                                resourceLocation,
                                properties,
                                maxProfiles,
                                maxActiveMatrices
                        )
                );

        Item.Properties itemProperties = new Item.Properties().rarity(properties.rarity());

        DeferredHolder<Item, ControllerItem> item =
                ModItems.ITEMS.register(
                        id + "_controller",
                        () -> new ControllerItem(
                                definition,
                                itemProperties
                        )
                );

        EquipmentEntry<ControllerDefinition, ControllerItem> entry =
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

    public static EquipmentEntry<ControllerDefinition, ControllerItem> getEntry(ResourceLocation id) {
        return BY_ID.get(id);
    }

    public static ControllerDefinition getDefinition(
            ResourceLocation id
    ) {
        return CONTROLLERS
                .getEntries()
                .stream()
                .filter(entry -> entry.getId().equals(id))
                .findFirst()
                .map(DeferredHolder::get)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unknown controller: " + id
                        )
                );
    }

    public static Optional<
            EquipmentEntry<ControllerDefinition, ControllerItem>
            > find(ResourceLocation id) {
        return Optional.ofNullable(
                BY_ID.get(id)
        );
    }

    public static final EquipmentEntry<ControllerDefinition, ControllerItem> CIVILIAN = register(
            "civilian",
            new EquipmentProperties(
                    EquipmentTier.CIVILIAN,
                    Rarity.UNCOMMON
            ),
            1,
            1
    );

    public static final EquipmentEntry<ControllerDefinition, ControllerItem> MILITARY = register(
            "military",
            new EquipmentProperties(
                    EquipmentTier.MILITARY,
                    Rarity.RARE
            ),
            2,
            2
    );

    public static final EquipmentEntry<ControllerDefinition, ControllerItem> ENGINEERING = register(
            "engineering",
            new EquipmentProperties(
                    EquipmentTier.ENGINEERING,
                    Rarity.RARE
            ),
            2,
            2
    );

    public static final EquipmentEntry<ControllerDefinition, ControllerItem> EXPERIMENTAL = register(
            "experimental",
            new EquipmentProperties(
                    EquipmentTier.EXPERIMENTAL,
                    Rarity.EPIC
            ),
            3,
            3
    );
}
