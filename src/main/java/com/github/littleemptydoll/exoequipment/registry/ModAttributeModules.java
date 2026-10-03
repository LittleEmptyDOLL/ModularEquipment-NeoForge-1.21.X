package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.AttributeCondition;
import com.github.littleemptydoll.exoequipment.module.AttributeModifierProperties;
import com.github.littleemptydoll.exoequipment.module.AttributeProperties;
import com.github.littleemptydoll.exoequipment.module.ConditionalAttributeProperties;
import com.github.littleemptydoll.exoequipment.module.EnergyProperties;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.ModList;

import java.util.List;
import java.util.Map;
import java.util.Optional;

final class ModAttributeModules {
    private static final ModuleSize SMALL = new ModuleSize(1, 2);
    private static final ModuleSize MEDIUM = new ModuleSize(2, 2);
    private static final int[] ENERGY = {3, 5, 8, 12};
    private static final EquipmentTier[] TIERS = {
            EquipmentTier.CIVILIAN, EquipmentTier.ENGINEERING,
            EquipmentTier.MILITARY, EquipmentTier.EXPERIMENTAL
    };
    private static final Rarity[] RARITIES = {
            Rarity.UNCOMMON, Rarity.RARE, Rarity.RARE, Rarity.EPIC
    };
    private static final String[] PREFIXES = {
            "civilian_", "engineering_", "military_", "experimental_"
    };

    private static final List<Series> SERIES = List.of(
            series("armor", ModuleCategory.DEFENSE, MEDIUM, "minecraft:generic.armor",
                    AttributeModifier.Operation.ADD_VALUE, 8, 2, 4, 6, 10),
            series("armor_toughness", ModuleCategory.DEFENSE, SMALL, "minecraft:generic.armor_toughness",
                    AttributeModifier.Operation.ADD_VALUE, 8, 1, 2, 3, 5),
            series("knockback_resistance", ModuleCategory.DEFENSE, SMALL,
                    "minecraft:generic.knockback_resistance", AttributeModifier.Operation.ADD_VALUE,
                    8, 0.1D, 0.2D, 0.3D, 0.5D),
            series("oxygen_reserve", ModuleCategory.SURVIVAL, SMALL,
                    "minecraft:generic.oxygen_bonus", AttributeModifier.Operation.ADD_VALUE,
                    7, 1, 2, 3, 5),
            series("safe_fall", ModuleCategory.MOBILITY, SMALL,
                    "minecraft:generic.safe_fall_distance", AttributeModifier.Operation.ADD_VALUE,
                    5, 1, 2, 3, 5),
            series("mining_speed", ModuleCategory.UTILITY, MEDIUM,
                    "minecraft:generic.block_break_speed", AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                    2, 0.1D, 0.2D, 0.3D, 0.5D),
            series("mining_range", ModuleCategory.UTILITY, SMALL,
                    "minecraft:generic.block_interaction_range", AttributeModifier.Operation.ADD_VALUE,
                    2, 0.5D, 1.0D, 1.5D, 2.0D),
            series("underwater_mining", ModuleCategory.UTILITY, SMALL,
                    "minecraft:generic.submerged_mining_speed", AttributeModifier.Operation.ADD_VALUE,
                    2, 0.1D, 0.2D, 0.3D, 0.5D),
            series("luck", ModuleCategory.UTILITY, SMALL,
                    "minecraft:generic.luck", AttributeModifier.Operation.ADD_VALUE,
                    2, 0.5D, 1.0D, 1.5D, 2.0D)
    );

    private static final List<Series> APOTHIC_SERIES = List.of(
            series("dodge_chance", ModuleCategory.DEFENSE, MEDIUM,
                    "apothic_attributes:dodge_chance", AttributeModifier.Operation.ADD_VALUE,
                    8, 0.02D, 0.04D, 0.06D, 0.10D),
            series("experience_gained", ModuleCategory.UTILITY, SMALL,
                    "apothic_attributes:experience_gained", AttributeModifier.Operation.ADD_VALUE,
                    2, 0.05D, 0.10D, 0.15D, 0.25D)
    );

    private ModAttributeModules() {}

    static void register(EquipmentRegistry<ModuleDefinition, ModuleItem> registry) {
        registerSeries(registry, SERIES);
        if (ModList.get() != null && ModList.get().isLoaded("apothic_attributes")) {
            registerSeries(registry, APOTHIC_SERIES);
        }
        for (int tier = 0; tier < TIERS.length; tier++) {
            registerAdrenaline(registry, tier);
        }
    }

    private static void registerSeries(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry, List<Series> seriesList
    ) {
        for (Series series : seriesList) {
            for (int tier = 0; tier < TIERS.length; tier++) {
                registerAttribute(registry, PREFIXES[tier] + series.name(),
                        TIERS[tier], RARITIES[tier], series, tier);
            }
        }
    }

    private static void registerAttribute(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id, EquipmentTier tier, Rarity rarity, Series series, int index
    ) {
        registry.register(id, new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) -> ModuleDefinition.builder(
                                resourceLocation, properties, series.category(), series.size())
                        .energy(new EnergyProperties(ENERGY[index], series.priority()))
                        .attributes(new AttributeProperties(Map.of(series.attribute(),
                                new AttributeModifierProperties(
                                        series.amounts()[index], series.operation()))))
                        .build());
    }

    private static void registerAdrenaline(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry, int tier
    ) {
        registry.register(PREFIXES[tier] + "adrenaline",
                new EquipmentProperties(TIERS[tier], RARITIES[tier]),
                (resourceLocation, properties) -> ModuleDefinition.builder(
                                resourceLocation, properties, ModuleCategory.COMBAT, MEDIUM)
                        .energy(new EnergyProperties(ENERGY[tier], 4))
                        .conditionalAttributes(new ConditionalAttributeProperties(
                                List.of(new AttributeCondition(
                                        AttributeCondition.Type.HEALTH_BELOW, Optional.of(0.30D))),
                                new AttributeProperties(Map.of(
                                        ResourceLocation.parse("minecraft:generic.attack_damage"),
                                        new AttributeModifierProperties(tier + 1.0D,
                                                AttributeModifier.Operation.ADD_VALUE)))))
                        .build());
    }

    private static Series series(
            String name, ModuleCategory category, ModuleSize size, String attribute,
            AttributeModifier.Operation operation, int priority, double... amounts
    ) {
        return new Series(name, category, size, ResourceLocation.parse(attribute),
                operation, priority, amounts);
    }

    private record Series(
            String name, ModuleCategory category, ModuleSize size,
            ResourceLocation attribute, AttributeModifier.Operation operation,
            int priority, double[] amounts
    ) {}
}
