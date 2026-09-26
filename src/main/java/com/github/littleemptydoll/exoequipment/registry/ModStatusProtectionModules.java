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
    private static final ModuleSize SPECIALIZED_SIZE =
            new ModuleSize(2, 2);
    private static final ModuleSize IMMUNITY_SIZE =
            new ModuleSize(2, 3);

    private static final int PROTECTION_PRIORITY = 8;
    private static final int IMMUNITY_PRIORITY = 9;

    static final ResourceLocation BIOLOGICAL = tag("biological");
    static final ResourceLocation MOBILITY = tag("mobility");
    static final ResourceLocation SENSORY = tag("sensory");
    static final ResourceLocation COMBAT = tag("combat");

    private ModStatusProtectionModules() {}

    static void register(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerProtectionSeries(
                registry,
                "biological_status_protection",
                BIOLOGICAL
        );
        registerProtectionSeries(
                registry,
                "mobility_status_protection",
                MOBILITY
        );
        registerProtectionSeries(
                registry,
                "sensory_status_protection",
                SENSORY
        );
        registerProtectionSeries(
                registry,
                "combat_status_protection",
                COMBAT
        );

        registerImmunity(
                registry,
                "experimental_biological_status_immunity",
                BIOLOGICAL,
                120
        );
        registerImmunity(
                registry,
                "experimental_mobility_status_immunity",
                MOBILITY,
                120
        );
        registerImmunity(
                registry,
                "experimental_sensory_status_immunity",
                SENSORY,
                120
        );
        registerImmunity(
                registry,
                "experimental_combat_status_immunity",
                COMBAT,
                120
        );
    }

    private static void registerProtectionSeries(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String suffix,
            ResourceLocation effectTag
    ) {
        registerProtection(
                registry,
                "civilian_" + suffix,
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                effectTag,
                0.35D,
                15
        );
        registerProtection(
                registry,
                "engineering_" + suffix,
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                effectTag,
                0.55D,
                30
        );
        registerProtection(
                registry,
                "military_" + suffix,
                EquipmentTier.MILITARY,
                Rarity.RARE,
                effectTag,
                0.75D,
                50
        );
        registerProtection(
                registry,
                "experimental_" + suffix,
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                effectTag,
                0.90D,
                80
        );
    }

    private static void registerProtection(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            ResourceLocation effectTag,
            double protection,
            int energyConsumption
    ) {
        register(
                registry,
                id,
                tier,
                rarity,
                SPECIALIZED_SIZE,
                effectTag,
                protection,
                energyConsumption,
                PROTECTION_PRIORITY
        );
    }

    private static void registerImmunity(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            ResourceLocation effectTag,
            int energyConsumption
    ) {
        register(
                registry,
                id,
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                IMMUNITY_SIZE,
                effectTag,
                1.0D,
                energyConsumption,
                IMMUNITY_PRIORITY
        );
    }

    private static void register(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            ModuleSize size,
            ResourceLocation effectTag,
            double protection,
            int energyConsumption,
            int priority
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
                                        priority
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
