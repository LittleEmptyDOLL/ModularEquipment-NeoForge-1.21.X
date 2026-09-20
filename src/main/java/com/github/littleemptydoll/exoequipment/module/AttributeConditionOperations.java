package com.github.littleemptydoll.exoequipment.module;

import net.minecraft.world.entity.player.Player;

public final class AttributeConditionOperations {
    private AttributeConditionOperations() {}

    public static boolean matches(
            Player player,
            AttributeCondition condition
    ) {
        return switch (condition.type()) {
            case DAY -> player.level().isDay();
            case NIGHT -> player.level().isNight();
            case IN_WATER -> player.isInWater();
            case UNDER_WATER -> player.isUnderWater();
            case ON_FIRE -> player.isOnFire();
            case ON_GROUND -> player.onGround();
            case SNEAKING -> player.isCrouching();
            case SPRINTING -> player.isSprinting();
            case RIDDEN -> player.isPassenger();
            case HEALTH_BELOW -> player.getHealth() <= player.getMaxHealth() * condition.value().orElseThrow();
            case HEALTH_ABOVE -> player.getHealth() >= player.getMaxHealth() * condition.value().orElseThrow();
        };
    }
}
