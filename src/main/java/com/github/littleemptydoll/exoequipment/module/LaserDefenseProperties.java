package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record LaserDefenseProperties(
        double range, float damage, int cooldown, int energyCost,
        boolean targetPlayers, boolean targetHostile, boolean targetAggressive
) {
    public static final Codec<LaserDefenseProperties> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("range").forGetter(LaserDefenseProperties::range),
                    Codec.FLOAT.fieldOf("damage").forGetter(LaserDefenseProperties::damage),
                    Codec.INT.fieldOf("cooldown").forGetter(LaserDefenseProperties::cooldown),
                    Codec.INT.fieldOf("energy_cost").forGetter(LaserDefenseProperties::energyCost),
                    Codec.BOOL.optionalFieldOf("target_players", false).forGetter(LaserDefenseProperties::targetPlayers),
                    Codec.BOOL.optionalFieldOf("target_hostile", true).forGetter(LaserDefenseProperties::targetHostile),
                    Codec.BOOL.optionalFieldOf("target_aggressive", true).forGetter(LaserDefenseProperties::targetAggressive)
            ).apply(instance, LaserDefenseProperties::new));

    public LaserDefenseProperties {
        if (!Double.isFinite(range) || range <= 0 || !Float.isFinite(damage) || damage <= 0
                || cooldown <= 0 || energyCost <= 0) {
            throw new IllegalArgumentException("Invalid laser defense range, damage, cooldown or energy cost");
        }
    }
}
