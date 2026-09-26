package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.EnergyProperties;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.module.ShieldProtectionProperties;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.world.item.Rarity;

final class ModShieldProtectionModules {
    private static final ModuleSize SIZE = new ModuleSize(1, 2);
    private static final int PRIORITY = 8;

    private ModShieldProtectionModules() {}

    static void register(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        register(
                registry,
                "civilian_shield_protection",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                0.25D,
                10
        );
        register(
                registry,
                "engineering_shield_protection",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                0.50D,
                20
        );
        register(
                registry,
                "military_shield_protection",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                0.75D,
                35
        );
        register(
                registry,
                "experimental_shield_protection",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                1.0D,
                50
        );
    }

    private static void register(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            double transfer,
            int energyConsumption
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.DEFENSE,
                                        SIZE
                                )
                                .energy(new EnergyProperties(
                                        energyConsumption,
                                        PRIORITY
                                ))
                                .shieldProtection(
                                        new ShieldProtectionProperties(
                                                transfer
                                        )
                                )
                                .build()
        );
    }
}
