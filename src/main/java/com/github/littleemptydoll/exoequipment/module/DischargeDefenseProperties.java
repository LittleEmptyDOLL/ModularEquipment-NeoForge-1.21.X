package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DischargeDefenseProperties(
        double range, double jumpRange, float damage, float falloff,
        int targets, int bounces, int cooldown, int energyCost,
        boolean targetPlayers, boolean targetHostile, boolean targetAggressive
) {
    public static final Codec<DischargeDefenseProperties> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("range").forGetter(DischargeDefenseProperties::range),
                    Codec.DOUBLE.fieldOf("jump_range").forGetter(DischargeDefenseProperties::jumpRange),
                    Codec.FLOAT.fieldOf("damage").forGetter(DischargeDefenseProperties::damage),
                    Codec.FLOAT.fieldOf("falloff").forGetter(DischargeDefenseProperties::falloff),
                    Codec.INT.fieldOf("targets").forGetter(DischargeDefenseProperties::targets),
                    Codec.INT.fieldOf("bounces").forGetter(DischargeDefenseProperties::bounces),
                    Codec.INT.fieldOf("cooldown").forGetter(DischargeDefenseProperties::cooldown),
                    Codec.INT.fieldOf("energy_cost").forGetter(DischargeDefenseProperties::energyCost),
                    Codec.BOOL.optionalFieldOf("target_players", false).forGetter(DischargeDefenseProperties::targetPlayers),
                    Codec.BOOL.optionalFieldOf("target_hostile", true).forGetter(DischargeDefenseProperties::targetHostile),
                    Codec.BOOL.optionalFieldOf("target_aggressive", true).forGetter(DischargeDefenseProperties::targetAggressive)
            ).apply(instance, DischargeDefenseProperties::new));

    public DischargeDefenseProperties {
        if (!Double.isFinite(range) || range <= 0 || !Double.isFinite(jumpRange) || jumpRange <= 0
                || !Float.isFinite(damage) || damage <= 0 || !Float.isFinite(falloff)
                || falloff <= 0 || falloff > 1 || targets <= 0 || bounces < 0
                || cooldown <= 0 || energyCost <= 0) {
            throw new IllegalArgumentException("Invalid discharge defense settings");
        }
    }
}
