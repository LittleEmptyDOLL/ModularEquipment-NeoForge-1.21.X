package com.github.littleemptydoll.exoequipment.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Map;

public final class AttributeNameUtils {
    private static final Map<ResourceLocation, String> VANILLA_NAMES = Map.ofEntries(
            entry(Attributes.MAX_HEALTH, "attribute.name.generic.max_health"),
            entry(Attributes.FOLLOW_RANGE, "attribute.name.generic.follow_range"),
            entry(Attributes.KNOCKBACK_RESISTANCE, "attribute.name.generic.knockback_resistance"),
            entry(Attributes.MOVEMENT_SPEED, "attribute.name.generic.movement_speed"),
            entry(Attributes.FLYING_SPEED, "attribute.name.generic.flying_speed"),
            entry(Attributes.ATTACK_DAMAGE, "attribute.name.generic.attack_damage"),
            entry(Attributes.ATTACK_KNOCKBACK, "attribute.name.generic.attack_knockback"),
            entry(Attributes.ATTACK_SPEED, "attribute.name.generic.attack_speed"),
            entry(Attributes.ARMOR, "attribute.name.generic.armor"),
            entry(Attributes.ARMOR_TOUGHNESS, "attribute.name.generic.armor_toughness"),
            entry(Attributes.LUCK, "attribute.name.generic.luck"),
            entry(Attributes.BLOCK_BREAK_SPEED, "attribute.name.generic.block_break_speed"),
            entry(Attributes.BLOCK_INTERACTION_RANGE, "attribute.name.generic.block_interaction_range"),
            entry(Attributes.ENTITY_INTERACTION_RANGE, "attribute.name.generic.entity_interaction_range"),
            entry(Attributes.STEP_HEIGHT, "attribute.name.generic.step_height"),
            entry(Attributes.GRAVITY, "attribute.name.generic.gravity"),
            entry(Attributes.SAFE_FALL_DISTANCE, "attribute.name.generic.safe_fall_distance"),
            entry(Attributes.FALL_DAMAGE_MULTIPLIER, "attribute.name.generic.fall_damage_multiplier"),
            entry(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE, "attribute.name.generic.explosion_knockback_resistance"),
            entry(Attributes.MOVEMENT_EFFICIENCY, "attribute.name.generic.movement_efficiency"),
            entry(Attributes.WATER_MOVEMENT_EFFICIENCY, "attribute.name.generic.water_movement_efficiency"),
            entry(Attributes.SUBMERGED_MINING_SPEED, "attribute.name.generic.submerged_mining_speed"),
            entry(Attributes.MINING_EFFICIENCY, "attribute.name.generic.mining_efficiency"),
            entry(Attributes.OXYGEN_BONUS, "attribute.name.generic.oxygen_bonus"),
            entry(Attributes.SNEAKING_SPEED, "attribute.name.generic.sneaking_speed"),
            entry(Attributes.SWEEPING_DAMAGE_RATIO, "attribute.name.generic.sweeping_damage_ratio")
    );

    private AttributeNameUtils() {}

    private static Map.Entry<ResourceLocation, String> entry(
            Holder<Attribute> attribute,
            String translationKey
    ) {
        ResourceLocation id = BuiltInRegistries.ATTRIBUTE.getKey(attribute.value());
        return Map.entry(id, translationKey);
    }

    public static Component getName(ResourceLocation id) {
        return Component.literal(NameUtils.toDisplayName(id.getPath()));
    }
}
