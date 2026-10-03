package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.BodyDamageRegenerationProperties;
import com.github.littleemptydoll.exoequipment.module.EnergyProperties;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.module.TemperatureImpactProperties;
import com.github.littleemptydoll.exoequipment.module.TemperatureModifierProperties;
import com.github.littleemptydoll.exoequipment.module.ThirstProperties;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.world.item.Rarity;

final class ModLsoModules {
    private static final ModuleSize THIRST_SIZE = new ModuleSize(1, 2);
    private static final ModuleSize TEMPERATURE_SIZE = new ModuleSize(2, 2);
    private static final ModuleSize REGENERATION_SIZE = new ModuleSize(2, 3);
    private static final int PRIORITY = 7;

    private ModLsoModules() {}

    static void register(EquipmentRegistry<ModuleDefinition, ModuleItem> registry) {
        registerThirst(registry, "civilian_thirst_assist", EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON, 2, 0.10D);
        registerThirst(registry, "engineering_thirst_assist", EquipmentTier.ENGINEERING,
                Rarity.RARE, 3, 0.20D);
        registerThirst(registry, "military_thirst_assist", EquipmentTier.MILITARY,
                Rarity.RARE, 5, 0.30D);
        registerThirst(registry, "experimental_thirst_assist", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, 8, 0.45D);

        registerBodyRegeneration(registry, "civilian_body_regeneration", EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON, 6, 0.05D);
        registerBodyRegeneration(registry, "engineering_body_regeneration", EquipmentTier.ENGINEERING,
                Rarity.RARE, 10, 0.10D);
        registerBodyRegeneration(registry, "military_body_regeneration", EquipmentTier.MILITARY,
                Rarity.RARE, 16, 0.20D);
        registerBodyRegeneration(registry, "experimental_body_regeneration", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, 25, 0.35D);
    }

    private static void registerThirst(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id, EquipmentTier tier, Rarity rarity,
            int consumption, double reduction
    ) {
        registry.register(id, new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) -> ModuleDefinition.builder(
                                resourceLocation, properties, ModuleCategory.SURVIVAL, THIRST_SIZE)
                        .energy(new EnergyProperties(consumption, PRIORITY))
                        .thirst(new ThirstProperties(reduction))
                        .build());
    }

    private static void registerBodyRegeneration(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id, EquipmentTier tier, Rarity rarity,
            int consumption, double healthPerSecond
    ) {
        registry.register(id, new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) -> ModuleDefinition.builder(
                                resourceLocation, properties, ModuleCategory.SURVIVAL, REGENERATION_SIZE)
                        .energy(new EnergyProperties(consumption, PRIORITY))
                        .bodyDamageRegeneration(new BodyDamageRegenerationProperties(healthPerSecond))
                        .build());
    }
}
