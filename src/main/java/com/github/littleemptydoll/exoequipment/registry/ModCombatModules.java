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

import java.util.Map;

final class ModCombatModules {
    private static final ModuleSize DAMAGE_SIZE = new ModuleSize(2, 2);
    private static final ModuleSize ATTACK_SPEED_SIZE = new ModuleSize(1, 2);
    private static final int COMBAT_PRIORITY = 4;
    private static final ResourceLocation ATTACK_DAMAGE =
            ResourceLocation.parse("minecraft:generic.attack_damage");
    private static final ResourceLocation ATTACK_SPEED =
            ResourceLocation.parse("minecraft:generic.attack_speed");

    private ModCombatModules() {}

    static void register(EquipmentRegistry<ModuleDefinition, ModuleItem> registry) {
        registerAttribute(registry, "civilian_attack_damage", EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON, DAMAGE_SIZE, 4, ATTACK_DAMAGE, 1.0D);
        registerAttribute(registry, "engineering_attack_damage", EquipmentTier.ENGINEERING,
                Rarity.RARE, DAMAGE_SIZE, 7, ATTACK_DAMAGE, 2.0D);
        registerAttribute(registry, "military_attack_damage", EquipmentTier.MILITARY,
                Rarity.RARE, DAMAGE_SIZE, 10, ATTACK_DAMAGE, 3.0D);
        registerAttribute(registry, "experimental_attack_damage", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, DAMAGE_SIZE, 16, ATTACK_DAMAGE, 4.0D);

        registerAttribute(registry, "civilian_attack_speed", EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON, ATTACK_SPEED_SIZE, 4, ATTACK_SPEED, 0.1D);
        registerAttribute(registry, "engineering_attack_speed", EquipmentTier.ENGINEERING,
                Rarity.RARE, ATTACK_SPEED_SIZE, 7, ATTACK_SPEED, 0.2D);
        registerAttribute(registry, "military_attack_speed", EquipmentTier.MILITARY,
                Rarity.RARE, ATTACK_SPEED_SIZE, 10, ATTACK_SPEED, 0.3D);
        registerAttribute(registry, "experimental_attack_speed", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, ATTACK_SPEED_SIZE, 16, ATTACK_SPEED, 0.4D);
    }

    private static void registerAttribute(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id, EquipmentTier tier, Rarity rarity, ModuleSize size,
            int energyConsumption, ResourceLocation attribute, double amount
    ) {
        registry.register(id, new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) -> ModuleDefinition.builder(
                                resourceLocation, properties,
                                ModuleCategory.COMBAT, size
                        )
                        .energy(new EnergyProperties(energyConsumption, COMBAT_PRIORITY))
                        .attributes(new AttributeProperties(Map.of(attribute,
                                new AttributeModifierProperties(amount,
                                        AttributeModifier.Operation.ADD_VALUE))))
                        .build());
    }
}
