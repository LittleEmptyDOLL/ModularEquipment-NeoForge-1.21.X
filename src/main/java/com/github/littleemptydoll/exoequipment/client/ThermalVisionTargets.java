package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Shared eligibility rule for model layers and renderer-specific thermal adapters. */
public final class ThermalVisionTargets {
    private static final TagKey<EntityType<?>> IGNORED =
            TagKey.create(Registries.ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(
                            ExoEquipment.MODID, "thermal_vision_ignored"));

    private ThermalVisionTargets() {}

    public static boolean shouldHighlight(LivingEntity entity, Player viewer) {
        return viewer != null && entity != viewer && entity.isAlive()
                && !entity.isSpectator() && !entity.isInvisible()
                && !entity.getType().is(IGNORED);
    }
}
