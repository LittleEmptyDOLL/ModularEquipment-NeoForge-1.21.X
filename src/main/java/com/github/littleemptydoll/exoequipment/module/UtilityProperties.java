package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record UtilityProperties(
        double blockBreakSpeed,
        double blockInteractionRange,
        double burningTime,
        double luck,
        double miningEfficiency,
        double submergedMiningSpeed,
        double movementEfficiency,
        double waterMovementEfficiency,
        double oxygenBonus,
        double safeFallDistance,
        double stepHeight
) {
    public static final Codec<UtilityProperties> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.DOUBLE.fieldOf("block_break_speed").forGetter(UtilityProperties::blockBreakSpeed),
                    Codec.DOUBLE.fieldOf("block_interaction_range").forGetter(UtilityProperties::blockInteractionRange),
                    Codec.DOUBLE.fieldOf("burning_time").forGetter(UtilityProperties::burningTime),
                    Codec.DOUBLE.fieldOf("luck").forGetter(UtilityProperties::luck),
                    Codec.DOUBLE.fieldOf("mining_efficiency").forGetter(UtilityProperties::miningEfficiency),
                    Codec.DOUBLE.fieldOf("submerged_mining_speed").forGetter(UtilityProperties::submergedMiningSpeed),
                    Codec.DOUBLE.fieldOf("movement_efficiency").forGetter(UtilityProperties::movementEfficiency),
                    Codec.DOUBLE.fieldOf("water_movement_efficiency").forGetter(UtilityProperties::waterMovementEfficiency),
                    Codec.DOUBLE.fieldOf("oxygen_bonus").forGetter(UtilityProperties::oxygenBonus),
                    Codec.DOUBLE.fieldOf("safe_fall_distance").forGetter(UtilityProperties::safeFallDistance),
                    Codec.DOUBLE.fieldOf("step_height").forGetter(UtilityProperties::stepHeight)
            ).apply(instance, UtilityProperties::new));

    public UtilityProperties {
        if (!allFinite(
                blockBreakSpeed, blockInteractionRange, burningTime, luck,
                miningEfficiency, submergedMiningSpeed, movementEfficiency,
                waterMovementEfficiency, oxygenBonus, safeFallDistance, stepHeight
        )) {
            throw new IllegalArgumentException("Utility values must be finite");
        }
    }

    private static boolean allFinite(double... values) {
        for (double value : values) {
            if (!Double.isFinite(value)) return false;
        }
        return true;
    }
}
