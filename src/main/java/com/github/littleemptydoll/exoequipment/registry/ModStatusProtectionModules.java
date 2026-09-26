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
                "experimental_poison_immunity",
                minecraftEffect("poison"),
                100
        );
        registerImmunity(
                registry,
                "experimental_wither_immunity",
                minecraftEffect("wither"),
                140
        );
        registerImmunity(
                registry,
                "experimental_blindness_immunity",
                minecraftEffect("blindness"),
                90
        );
        registerImmunity(
                registry,
                "experimental_darkness_immunity",
                minecraftEffect("darkness"),
                100
        );
        registerImmunity(
                registry,
                "experimental_slowness_immunity",
                minecraftEffect("slowness"),
                90
        );
        registerImmunity(
                registry,
                "experimental_mining_fatigue_immunity",
                minecraftEffect("mining_fatigue"),
                90
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
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.DEFENSE,
                                        SPECIALIZED_SIZE
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

    private static void registerImmunity(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            ResourceLocation effectId,
            int energyConsumption
    ) {
        registry.register(
                id,
                new EquipmentProperties(
                        EquipmentTier.EXPERIMENTAL,
                        Rarity.EPIC
                ),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.DEFENSE,
                                        IMMUNITY_SIZE
                                )
                                .energy(new EnergyProperties(
                                        energyConsumption,
                                        IMMUNITY_PRIORITY
                                ))
                                .statusProtection(
                                        new StatusProtectionProperties(
                                                Map.of(effectId, 1.0D)
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

    private static ResourceLocation minecraftEffect(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                "minecraft",
                path
        );
    }
}
