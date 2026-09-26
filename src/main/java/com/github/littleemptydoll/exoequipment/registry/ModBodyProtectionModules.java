package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.BodyDamageProtectionProperties;
import com.github.littleemptydoll.exoequipment.module.BodyPart;
import com.github.littleemptydoll.exoequipment.module.EnergyProperties;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.world.item.Rarity;

import java.util.Set;

final class ModBodyProtectionModules {
    private static final ModuleSize GENERAL_SIZE = new ModuleSize(2, 3);
    private static final ModuleSize SPECIALIZED_SIZE = new ModuleSize(2, 2);

    private static final int PRIORITY = 8;

    private static final Set<BodyPart> VITAL_PARTS = Set.of(
            BodyPart.HEAD,
            BodyPart.CHEST
    );

    private static final Set<BodyPart> LIMB_PARTS = Set.of(
            BodyPart.RIGHT_ARM,
            BodyPart.LEFT_ARM,
            BodyPart.RIGHT_LEG,
            BodyPart.RIGHT_FOOT,
            BodyPart.LEFT_LEG,
            BodyPart.LEFT_FOOT
    );

    private ModBodyProtectionModules() {}

    static void register(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerGeneralSeries(registry);
        registerSpecializedSeries(
                registry,
                "vital_body_protection",
                VITAL_PARTS
        );
        registerSpecializedSeries(
                registry,
                "limb_body_protection",
                LIMB_PARTS
        );
    }

    private static void registerGeneralSeries(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        register(
                registry,
                "civilian_body_protection",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                GENERAL_SIZE,
                Set.of(),
                0.05D,
                0.10D,
                15
        );
        register(
                registry,
                "engineering_body_protection",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                GENERAL_SIZE,
                Set.of(),
                0.10D,
                0.20D,
                30
        );
        register(
                registry,
                "military_body_protection",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                GENERAL_SIZE,
                Set.of(),
                0.15D,
                0.30D,
                50
        );
        register(
                registry,
                "experimental_body_protection",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                GENERAL_SIZE,
                Set.of(),
                0.20D,
                0.40D,
                80
        );
    }

    private static void registerSpecializedSeries(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String suffix,
            Set<BodyPart> bodyParts
    ) {
        register(
                registry,
                "civilian_" + suffix,
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                SPECIALIZED_SIZE,
                bodyParts,
                0.10D,
                0.20D,
                20
        );
        register(
                registry,
                "engineering_" + suffix,
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                SPECIALIZED_SIZE,
                bodyParts,
                0.15D,
                0.35D,
                40
        );
        register(
                registry,
                "military_" + suffix,
                EquipmentTier.MILITARY,
                Rarity.RARE,
                SPECIALIZED_SIZE,
                bodyParts,
                0.20D,
                0.50D,
                65
        );
        register(
                registry,
                "experimental_" + suffix,
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                SPECIALIZED_SIZE,
                bodyParts,
                0.30D,
                0.65D,
                100
        );
    }

    private static void register(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            ModuleSize size,
            Set<BodyPart> bodyParts,
            double blockChance,
            double damageReduction,
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
                                        size
                                )
                                .energy(new EnergyProperties(
                                        energyConsumption,
                                        PRIORITY
                                ))
                                .bodyDamageProtection(
                                        new BodyDamageProtectionProperties(
                                                blockChance,
                                                damageReduction,
                                                bodyParts
                                        )
                                )
                                .build()
        );
    }
}
