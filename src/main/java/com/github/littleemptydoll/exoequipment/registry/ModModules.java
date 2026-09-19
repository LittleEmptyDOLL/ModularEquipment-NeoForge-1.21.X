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
                                    Optional.of(new TemperatureBonus(20, 40, 0.5))))
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
                            ModuleCategory.ENERGY,
                            new ModuleSize(3, 3)
                    )
                            .generation(new GenerationProperties(40))
                            .thermal(new ThermalProperties(10, 0))
                            .temperature(new TemperatureProperties(
                                    40,
                                    80,
                                    Optional.empty(),
                                    Optional.of(new TemperatureBonus(60, 80, 1))))
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
                            ModuleCategory.ENERGY,
                            new ModuleSize(2, 3)
                    )
                            .storage(new StorageProperties(10_000, 100, 200))
                            .temperature(new TemperatureProperties(
                                    -20,
                                    50,
                                    Optional.empty(),
                                    Optional.empty()))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_SHIELD = REGISTRY.register(
            "test_shield",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.DEFENSE,
                                    new ModuleSize(2, 2)
                            )
                            .energy(new EnergyProperties(5, 1))
                            .shield(new ShieldProperties(
                                    50,
                                    5,
                                    100
                            ))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_STATUS_PROTECTION = REGISTRY.register(
            "test_status_protection",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.DEFENSE,
                                    new ModuleSize(2, 2)
                            )
                            .statusProtection(new StatusProtectionProperties(
                                    java.util.Map.of(
                                            ResourceLocation.fromNamespaceAndPath(
                                                    "minecraft",
                                                    "poison"
                                            ),
                                            0.75D,
                                            ResourceLocation.fromNamespaceAndPath(
                                                    "minecraft",
                                                    "wither"
                                            ),
                                            1.0D
                                    )
                            ))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_DAMAGE_REDUCTION = REGISTRY.register(
            "test_damage_reduction",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.DEFENSE,
                                    new ModuleSize(2, 2)
                            )
                            .damageReduction(new DamageReductionProperties(
                                    java.util.Map.of(
                                            ResourceLocation.fromNamespaceAndPath(
                                                    "minecraft",
                                                    "player_attack"
                                            ),
                                            0.25D
                                    )
                            ))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_MOBILITY = REGISTRY.register(
            "test_mobility",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.MOBILITY,
                                    new ModuleSize(2, 2)
                            )
                            .mobility(new MobilityProperties(
                                    0.15D,
                                    0.25D,
                                    0.20D,
                                    0.50D
                            ))
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
                                    ModuleCategory.THERMAL,
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
                                    ModuleCategory.THERMAL,
                                    new ModuleSize(2, 2)
                            )
                            .energy(new EnergyProperties(10))
                            .thermal(new ThermalProperties(20, 0))
                            .build()
    );
}
