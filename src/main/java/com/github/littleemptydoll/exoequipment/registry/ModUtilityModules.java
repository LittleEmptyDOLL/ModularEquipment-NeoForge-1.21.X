package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.CloakingProperties;
import com.github.littleemptydoll.exoequipment.module.EnergyProperties;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.module.PickupMagnetProperties;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.world.item.Rarity;

final class ModUtilityModules {
    private static final ModuleSize SMALL_SIZE = new ModuleSize(1, 2);
    private static final ModuleSize MEDIUM_SIZE = new ModuleSize(2, 2);
    private static final int UTILITY_PRIORITY = 2;

    private ModUtilityModules() {}

    static void register(EquipmentRegistry<ModuleDefinition, ModuleItem> registry) {
        registerMagnet(registry, "pickup_magnet", EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON, 15, 5.0D, PickupMagnetProperties.Mode.ITEMS);
        registerMagnet(registry, "advanced_pickup_magnet", EquipmentTier.ENGINEERING,
                Rarity.RARE, 25, 10.0D, PickupMagnetProperties.Mode.BOTH);

        registerCloaking(registry, "military_cloaking", EquipmentTier.MILITARY,
                Rarity.RARE, 20, 2500, 150, 200);
        registerCloaking(registry, "experimental_cloaking", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, 40, 5000, 250, 100);
    }

    private static void registerMagnet(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id, EquipmentTier tier, Rarity rarity,
            int energyConsumption, double radius, PickupMagnetProperties.Mode mode
    ) {
        registry.register(id, new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) -> ModuleDefinition.builder(
                                resourceLocation, properties,
                                ModuleCategory.UTILITY, SMALL_SIZE
                        )
                        .energy(new EnergyProperties(energyConsumption, UTILITY_PRIORITY))
                        .pickupMagnet(new PickupMagnetProperties(radius, mode))
                        .build());
    }

    private static void registerCloaking(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id, EquipmentTier tier, Rarity rarity,
            int energyConsumption, int activationEnergy,
            int activeConsumption, int cooldown
    ) {
        registry.register(id, new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) -> ModuleDefinition.builder(
                                resourceLocation, properties,
                                ModuleCategory.UTILITY, MEDIUM_SIZE
                        )
                        .energy(new EnergyProperties(energyConsumption, UTILITY_PRIORITY))
                        .cloaking(new CloakingProperties(activationEnergy,
                                activeConsumption, cooldown))
                        .build());
    }
}
