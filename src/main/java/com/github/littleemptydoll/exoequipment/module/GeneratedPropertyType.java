package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.neoforged.fml.ModList;

public enum GeneratedPropertyType {
    STORAGE(true, true, 4),
    GENERATION(true, true, 6),

    COOLING(true, false, 4, ConflictSide.COOLING),
    HEATING(true, false, 4, ConflictSide.HEATING),
    PERSONAL_COOLING(true, false, 4, Dependency.LSO, ConflictSide.COOLING),
    PERSONAL_HEATING(true, false, 4, Dependency.LSO, ConflictSide.HEATING),
    THERMAL_RESISTANCE(false, true, 2, Dependency.LSO),
    TEMPERATURE_ISOLATION(true, false, 4, Dependency.LSO),

    SHIELD(true, true, 4),
    GENERAL_DAMAGE_REDUCTION(false, true, 6),
    SPECIALIZED_DAMAGE_REDUCTION(true, true, 4),
    DAMAGE_IMMUNITY(false, false, 6),
    EMERGENCY_SHIELD(false, true, 4),
    SHIELD_REINFORCEMENT(true, true, 2),
    ARMOR(false, true, 2),
    REACTIVE_ARMOR(false, true, 6),
    FALL_PROTECTION(true, true, 4),
    KNOCKBACK_STABILIZATION(false, true, 4),
    DODGE(false, true, 2, Dependency.APOTHIC),
    STATUS_PROTECTION(true, true, 4),
    BODY_PROTECTION(true, true, 4, Dependency.LSO),

    MAX_HEALTH(false, true, 2),
    HUNGER_ASSIST(true, false, 2),
    THIRST_ASSIST(true, false, 2, Dependency.LSO),
    REGENERATION(true, true, 4),
    REVIVAL(false, true, 6),
    BODY_REGENERATION(true, true, 6, Dependency.LSO),
    OXYGEN_SUPPORT(true, false, 2),

    MOVEMENT_SPEED(false, true, 1),
    JUMP_ASSIST(true, false, 1),
    SWIM_ASSIST(true, true, 2),
    JETPACK(true, true, 6, ConflictSide.JETPACK),
    BLINK(true, true, 4),
    ELYTRA(true, false, 4, Dependency.APOTHIC),
    FLIGHT(false, false, 9, ConflictSide.FLIGHT),

    ATTACK_DAMAGE(true, true, 1),
    ATTACK_SPEED(true, false, 1),
    ATTACK_KNOCKBACK(false, false, 1),
    INTERACTION_RANGE(false, false, 1),
    CRIT_CHANCE(true, true, 1, Dependency.APOTHIC),
    CRIT_DAMAGE(true, true, 1, Dependency.APOTHIC),
    CURRENT_HP_DAMAGE(false, true, 4, Dependency.APOTHIC),
    LIFE_STEAL(false, true, 4, Dependency.APOTHIC),
    OVERHEAL(false, false, 6, Dependency.APOTHIC),
    ARMOR_PIERCE(false, true, 4, Dependency.APOTHIC),
    ARMOR_SHRED(true, false, 4, Dependency.APOTHIC),
    RANGED_DAMAGE(true, false, 4, Dependency.APOTHIC),
    DRAW_SPEED(true, false, 1, Dependency.APOTHIC),
    FIRE_DAMAGE(false, true, 4, Dependency.APOTHIC),
    COLD_DAMAGE(true, false, 4, Dependency.APOTHIC),
    ADRENALINE(false, true, 4),
    LASER_DEFENSE(true, true, 4),
    DISCHARGE_DEFENSE(true, true, 6),

    NIGHT_VISION(false, false, 1),
    ENTITY_SENSOR(true, true, 4),
    BLOCK_SCANNER(true, false, 4),
    THERMAL_VISION(false, true, 6),

    PICKUP_MAGNET(true, false, 2),
    CLOAKING(false, true, 4),
    MINING_SPEED(true, false, 2),
    BLOCK_REACH(true, false, 2),
    UNDERWATER_MINING(true, false, 4),
    LUCK(true, false, 1),
    EXPERIENCE_GAIN(true, false, 1, Dependency.APOTHIC);

    private final boolean engineering;
    private final boolean military;
    private final int legacyMinArea;
    private final Dependency dependency;
    private final ConflictSide legacyConflictSide;

    GeneratedPropertyType(boolean engineering, boolean military, int minArea) {
        this(engineering, military, minArea, Dependency.NONE, ConflictSide.NONE);
    }

    GeneratedPropertyType(
            boolean engineering,
            boolean military,
            int minArea,
            Dependency dependency
    ) {
        this(engineering, military, minArea, dependency, ConflictSide.NONE);
    }

    GeneratedPropertyType(
            boolean engineering,
            boolean military,
            int minArea,
            ConflictSide conflictSide
    ) {
        this(engineering, military, minArea, Dependency.NONE, conflictSide);
    }

    GeneratedPropertyType(
            boolean engineering,
            boolean military,
            int minArea,
            Dependency dependency,
            ConflictSide conflictSide
    ) {
        this.engineering = engineering;
        this.military = military;
        this.legacyMinArea = minArea;
        this.dependency = dependency;
        this.legacyConflictSide = conflictSide;
    }

    public int minArea() {
        return GeneratedModuleRules.minArea(this, legacyMinArea);
    }

    public boolean isAvailableFor(EquipmentTier tier) {
        if (!dependency.isLoaded()) {
            return false;
        }

        boolean legacyAvailable = switch (tier) {
            case ENGINEERING -> engineering;
            case MILITARY -> military;
            case EXPERIMENTAL -> true;
            default -> false;
        };

        return GeneratedModuleRules.isAvailableFor(this, tier, legacyAvailable);
    }

    public boolean conflictsWith(GeneratedPropertyType other) {
        if (other == null || other == this) {
            return false;
        }
        return GeneratedModuleRules.conflictsWith(
                this,
                other,
                legacyConflictsWith(other)
        );
    }

    private boolean legacyConflictsWith(GeneratedPropertyType other) {
        return (legacyConflictSide == ConflictSide.HEATING
                && other.legacyConflictSide == ConflictSide.COOLING)
                || (legacyConflictSide == ConflictSide.COOLING
                && other.legacyConflictSide == ConflictSide.HEATING)
                || (legacyConflictSide == ConflictSide.JETPACK
                && other.legacyConflictSide == ConflictSide.FLIGHT)
                || (legacyConflictSide == ConflictSide.FLIGHT
                && other.legacyConflictSide == ConflictSide.JETPACK);
    }

    private enum ConflictSide {
        NONE,
        HEATING,
        COOLING,
        JETPACK,
        FLIGHT
    }

    private enum Dependency {
        NONE(null),
        LSO("legendarysurvivaloverhaul"),
        APOTHIC("apothic_attributes");

        private final String modId;

        Dependency(String modId) {
            this.modId = modId;
        }

        boolean isLoaded() {
            return modId == null
                    || (ModList.get() != null && ModList.get().isLoaded(modId));
        }
    }
}
