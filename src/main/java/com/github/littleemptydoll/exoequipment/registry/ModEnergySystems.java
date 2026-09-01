package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.energy.EnergySystemDefinition;
import com.github.littleemptydoll.exoequipment.item.EnergySystemItem;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ModEnergySystems {
    private ModEnergySystems() {}

    public static final DeferredRegister<EnergySystemDefinition> ENERGY_SYSTEMS =
            DeferredRegister.create(
                    ModRegistryKeys.ENERGY_SYSTEM_REGISTRY_KEY,
                    ExoEquipment.MODID
            );

    private static final Map<ResourceLocation,
            EquipmentEntry<EnergySystemDefinition, EnergySystemItem>> BY_ID
            = new HashMap<>();

    private static EquipmentEntry<EnergySystemDefinition, EnergySystemItem> register(
            String id,
            EquipmentProperties properties,
            int maxInput,
            int maxOutput,
            double efficiency
    ) {
        ResourceLocation resourceLocation =
                ResourceLocation.fromNamespaceAndPath(
                        ExoEquipment.MODID,
                        id
                );

        DeferredHolder<EnergySystemDefinition, EnergySystemDefinition> definition =
                ENERGY_SYSTEMS.register(
                        id,
                        () -> new EnergySystemDefinition(
                                resourceLocation,
                                properties,
                                maxInput,
                                maxOutput,
                                efficiency
                        )
                );

        Item.Properties itemProperties = new Item.Properties().rarity(properties.rarity());

        DeferredHolder<Item, EnergySystemItem> item =
                ModItems.ITEMS.register(
                        id + "_energy_system",
                        () -> new EnergySystemItem(
                                definition,
                                itemProperties
                        )
                );

        EquipmentEntry<EnergySystemDefinition, EnergySystemItem> entry =
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

    public static EquipmentEntry<EnergySystemDefinition, EnergySystemItem> getEntry(ResourceLocation id) {
        return BY_ID.get(id);
    }

    public static EnergySystemDefinition getDefinition(
            ResourceLocation id
    ) {
        return ENERGY_SYSTEMS
                .getEntries()
                .stream()
                .filter(entry -> entry.getId().equals(id))
                .findFirst()
                .map(DeferredHolder::get)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unknown energy system: " + id
                        )
                );
    }

    public static Optional<
            EquipmentEntry<EnergySystemDefinition, EnergySystemItem>
            > find(ResourceLocation id) {
        return Optional.ofNullable(
                BY_ID.get(id)
        );
    }

    // ToDo: Определить подходящие характеристики
    public static final EquipmentEntry<EnergySystemDefinition, EnergySystemItem> CIVILIAN = register(
            "civilian",
            new EquipmentProperties(
                    EquipmentTier.CIVILIAN,
                    Rarity.UNCOMMON
            ),
            100,
            100,
            1.0
    );

    public static final EquipmentEntry<EnergySystemDefinition, EnergySystemItem> MILITARY = register(
            "military",
            new EquipmentProperties(
                    EquipmentTier.MILITARY,
                    Rarity.RARE
            ),
            200,
            200,
            1.0
    );

    public static final EquipmentEntry<EnergySystemDefinition, EnergySystemItem> ENGINEERING = register(
            "engineering",
            new EquipmentProperties(
                    EquipmentTier.ENGINEERING,
                    Rarity.RARE
            ),
            300,
            200,
            1.0
    );

    public static final EquipmentEntry<EnergySystemDefinition, EnergySystemItem> EXPERIMENTAL = register(
            "experimental",
            new EquipmentProperties(
                    EquipmentTier.EXPERIMENTAL,
                    Rarity.EPIC
            ),
            400,
            500,
            1.1
    );
}
