package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.EnergyProperties;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.module.StatusProtectionProperties;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;

import java.util.Map;

final class ModStatusProtectionModules {
    private static final ModuleSize MEDIUM_SIZE =
            new ModuleSize(2, 2);
    private static final ModuleSize HUGE_SIZE =
            new ModuleSize(3, 3);

    private static final int PROTECTION_PRIORITY = 8;

    static final ResourceLocation BIOLOGICAL = tag("biological");
    static final ResourceLocation MOBILITY = tag("mobility");
    static final ResourceLocation SENSORY = tag("sensory");
    static final ResourceLocation COMBAT = tag("combat");
    static final ResourceLocation NEGATIVE_EFFECTS = tag("negative_effects");

    private ModStatusProtectionModules() {}

    static void register(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerProtection(
                registry,
                "biological_protection",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                BIOLOGICAL,
                0.70D,
                70,
                MEDIUM_SIZE
        );
        registerProtection(
                registry,
                "mobility_protection",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                MOBILITY,
                0.60D,
                60,
                MEDIUM_SIZE
        );
        registerProtection(
                registry,
                "sensory_protection",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                SENSORY,
                0.75D,
                75,
                MEDIUM_SIZE
        );
        registerProtection(
                registry,
                "combat_status_protection",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                COMBAT,
                0.75D,
                75,
                MEDIUM_SIZE
        );

        registerProtection(
                registry,
                "experimental_status_protection",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                NEGATIVE_EFFECTS,
                0.90D,
                120,
                HUGE_SIZE
        );
    }

    private static void registerProtection(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            ResourceLocation effectTag,
            double protection,
            int energyConsumption,
            ModuleSize size
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.DEFENSE,
                                        size
                                )
                                .energy(new EnergyProperties(
                                        energyConsumption,
                                        PROTECTION_PRIORITY
                                ))
                                .statusProtection(
                                        new StatusProtectionProperties(
                                                0.0D,
                                                Map.of(),
                                                Map.of(
                                                        effectTag,
                                                        protection
                                                )
                                        )
                                )
                                .build()
        );
    }

    private static ResourceLocation tag(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                ExoEquipment.MODID,
                "status_protection/" + path
        );
    }
}
