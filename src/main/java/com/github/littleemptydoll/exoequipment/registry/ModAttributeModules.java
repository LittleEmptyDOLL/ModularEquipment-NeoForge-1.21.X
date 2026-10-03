package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.AttributeModifierProperties;
import com.github.littleemptydoll.exoequipment.module.AttributeProperties;
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
            series("safe_fall", ModuleCategory.DEFENSE, SMALL,
                    "minecraft:generic.safe_fall_distance", AttributeModifier.Operation.ADD_VALUE,
                    5, 1, 2, 3, 5)
    );

    private static final List<Series> APOTHIC_SERIES = List.of(
            series("dodge_chance", ModuleCategory.DEFENSE, MEDIUM,
                    "apothic_attributes:dodge_chance", AttributeModifier.Operation.ADD_VALUE,
                    8, 0.02D, 0.04D, 0.06D, 0.10D)
    );

    private ModAttributeModules() {}

    static void register(EquipmentRegistry<ModuleDefinition, ModuleItem> registry) {
        registerSeries(registry, SERIES);
        if (ModList.get() != null && ModList.get().isLoaded("apothic_attributes")) {
            registerSeries(registry, APOTHIC_SERIES);
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
