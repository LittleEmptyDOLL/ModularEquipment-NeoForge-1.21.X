package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.*;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ModModules {
    private  ModModules() {}

    public static final DeferredRegister<ModuleDefinition> MODULES =
            DeferredRegister.create(
                    ModRegistryKeys.MODULE_REGISTRY,
                    ExoEquipment.MODID
            );

    private static final Map<ResourceLocation,
            EquipmentEntry<ModuleDefinition, ModuleItem>> BY_ID
            = new HashMap<>();

    private static EquipmentEntry<ModuleDefinition, ModuleItem> register(
            String id,
            EquipmentProperties properties,
            ModuleCategory category,
            ModuleSize size,
            Optional<EnergyProperties> energy,
            Optional<ThermalProperties> thermal
    ) {
        ResourceLocation resourceLocation =
                ResourceLocation.fromNamespaceAndPath(
                        ExoEquipment.MODID,
                        id
                );

        DeferredHolder<ModuleDefinition, ModuleDefinition> definition =
                MODULES.register(
                        id,
                        () -> new ModuleDefinition(
                                resourceLocation,
                                properties,
                                category,
                                size,
                                energy,
                                thermal
                        )
                );

        Item.Properties itemProperties = new Item.Properties().rarity(properties.rarity());

        DeferredHolder<Item, ModuleItem> item =
                ModItems.ITEMS.register(
                        id + "_module",
                        () -> new ModuleItem(
                                definition,
                                itemProperties
                        )
                );

        EquipmentEntry<ModuleDefinition, ModuleItem> entry =
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

    public static EquipmentEntry<ModuleDefinition, ModuleItem> getEntry(ResourceLocation id) {
        return BY_ID.get(id);
    }

    public static ModuleDefinition getDefinition(
            ResourceLocation id
    ) {
        ModuleDefinition definition = ModRegistryKeys.MODULE_REGISTRY.get(id);

        if (definition == null) {
            throw new IllegalArgumentException("Unknown module: " + id);
        }

        return definition;
    }

    public static Optional<
            EquipmentEntry<ModuleDefinition, ModuleItem>
            > find(ResourceLocation id) {
        return Optional.ofNullable(
                BY_ID.get(id)
        );
    }

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST = register(
            "test",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            ModuleCategory.UTILITY,
            new ModuleSize(2,2),
            Optional.empty(),
            Optional.empty()
    );

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST_ENERGY = register(
            "test_energy",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            ModuleCategory.UTILITY,
            new ModuleSize(3,3),
            Optional.of(new EnergyProperties(20)),
            Optional.of(new ThermalProperties(10, 5))
    );
}
