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
    private static final ModuleSize MAGNET_SIZE = new ModuleSize(1, 2);
    private static final ModuleSize CLOAKING_SIZE = new ModuleSize(2, 2);
    private static final int UTILITY_PRIORITY = 2;

    private ModUtilityModules() {}

    static void register(EquipmentRegistry<ModuleDefinition, ModuleItem> registry) {
        registerMagnet(registry, "civilian_pickup_magnet", EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON, 3, 4.0D, PickupMagnetProperties.Mode.ITEMS);
        registerMagnet(registry, "engineering_pickup_magnet", EquipmentTier.ENGINEERING,
                Rarity.RARE, 5, 6.0D, PickupMagnetProperties.Mode.BOTH);
        registerMagnet(registry, "military_pickup_magnet", EquipmentTier.MILITARY,
                Rarity.RARE, 7, 8.0D, PickupMagnetProperties.Mode.BOTH);
        registerMagnet(registry, "experimental_pickup_magnet", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, 10, 12.0D, PickupMagnetProperties.Mode.BOTH);

        registerCloaking(registry, "military_cloaking", EquipmentTier.MILITARY,
                Rarity.RARE, 12, 100, 12, 200);
        registerCloaking(registry, "experimental_cloaking", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, 18, 80, 18, 100);
    }

    private static void registerMagnet(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id, EquipmentTier tier, Rarity rarity,
            int energyConsumption, double radius, PickupMagnetProperties.Mode mode
    ) {
        registry.register(id, new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) -> ModuleDefinition.builder(
                                resourceLocation, properties,
                                ModuleCategory.UTILITY, MAGNET_SIZE
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
                                ModuleCategory.UTILITY, CLOAKING_SIZE
                        )
                        .energy(new EnergyProperties(energyConsumption, UTILITY_PRIORITY))
                        .cloaking(new CloakingProperties(activationEnergy,
                                activeConsumption, cooldown))
                        .build());
    }
}
