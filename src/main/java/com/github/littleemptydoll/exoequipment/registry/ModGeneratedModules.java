package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.GeneratedModuleItem;
import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.world.item.Rarity;

final class ModGeneratedModules {
    private static final ModuleSize PLACEHOLDER_SIZE = new ModuleSize(1, 1);

    private ModGeneratedModules() {}

    static void register(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerPrototype(
                registry,
                "military_prototype",
                EquipmentTier.MILITARY,
                Rarity.RARE
        );
        registerPrototype(
                registry,
                "engineering_prototype",
                EquipmentTier.ENGINEERING,
                Rarity.RARE
        );
        registerPrototype(
                registry,
                "experimental_prototype",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC
        );
    }

    private static void registerPrototype(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.EXPERIMENTAL,
                                        PLACEHOLDER_SIZE
                                )
                                .build(),
                GeneratedModuleItem::new
        );
    }
}
