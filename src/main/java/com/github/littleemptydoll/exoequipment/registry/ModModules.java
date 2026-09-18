package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.*;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

public class ModModules {
    private ModModules() {}

    public static final DeferredRegister<ModuleDefinition> MODULES =
            DeferredRegister.create(
                    ModRegistryKeys.MODULE_REGISTRY,
                    ExoEquipment.MODID
            );

    private static final EquipmentRegistry<
            ModuleDefinition,
            ModuleItem
    > REGISTRY =
            new EquipmentRegistry<>(
                    MODULES,
                    ModItems.ITEMS,
                    "_module",
                    ModuleItem::new
            );

    public static ModuleDefinition getDefinition(
            ResourceLocation id
    ) {
        return REGISTRY.getDefinition(id);
    }

    public static EquipmentEntry<
            ModuleDefinition,
            ModuleItem
    > find(
            ResourceLocation id
    ) {
        return REGISTRY.find(id);
    }

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
    > TEST = REGISTRY.register(
            "test",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.UTILITY,
                            new ModuleSize(1, 2)
                    ).build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
    > TEST_CONSUMER = REGISTRY.register(
            "test_consumer",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.UTILITY,
                            new ModuleSize(2, 2)
                    )
                            .energy(new EnergyProperties(20))
                            .thermal(new ThermalProperties(20, 0))
                            .temperature(new TemperatureProperties(
                                    0,
                                    40,
                                    Optional.empty(),
                                    Optional.of(new TemperatureBonus(20, 40, 50))))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
    > TEST_GENERATOR = REGISTRY.register(
            "test_generator",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.UTILITY,
                            new ModuleSize(3, 3)
                    )
                            .generation(new GenerationProperties(40))
                            .thermal(new ThermalProperties(10, 0))
                            .temperature(new TemperatureProperties(
                                    40,
                                    80,
                                    Optional.empty(),
                                    Optional.of(new TemperatureBonus(60, 80, 100))))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
    > TEST_BATTERY = REGISTRY.register(
            "test_battery",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.UTILITY,
                            new ModuleSize(2, 3)
                    )
                            .storage(new StorageProperties(10_000, 100, 200))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_COOLER = REGISTRY.register(
            "test_cooler",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.UTILITY,
                                    new ModuleSize(2, 2)
                            )
                            .energy(new EnergyProperties(10))
                            .thermal(new ThermalProperties(0, 20))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_HEATER = REGISTRY.register(
            "test_heater",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.UTILITY,
                                    new ModuleSize(2, 2)
                            )
                            .energy(new EnergyProperties(10))
                            .thermal(new ThermalProperties(20, 0))
                            .build()
    );
}
