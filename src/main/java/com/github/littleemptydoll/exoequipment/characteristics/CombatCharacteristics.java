package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.module.DischargeDefenseProperties;
import com.github.littleemptydoll.exoequipment.module.LaserDefenseProperties;

import java.util.List;

/** Installed automated defenses, including modules temporarily switched off. */
final class CombatCharacteristics {
    private CombatCharacteristics() {}

    static void add(List<Characteristic> result, CharacteristicsContext context) {
        int lasers = 0, activeLasers = 0, discharges = 0, activeDischarges = 0;
        double laserRange = 0, laserDamage = 0, dischargeRange = 0, dischargeDamage = 0;
        double jumpRange = 0, falloff = 0;
        int laserCost = 0, laserCooldown = Integer.MAX_VALUE;
        int dischargeCost = 0, dischargeCooldown = Integer.MAX_VALUE;
        int targets = 0, bounces = 0;

        for (var module : CharacteristicsSupport.installedModules(context)) {
            LaserDefenseProperties laser = module.definition().laserDefense().orElse(null);
            if (laser != null) {
                lasers++;
                if (module.module().active()) activeLasers++;
                laserRange = Math.max(laserRange, laser.range());
                laserDamage = Math.max(laserDamage, laser.damage());
                laserCost = Math.max(laserCost, laser.energyCost());
                laserCooldown = Math.min(laserCooldown, laser.cooldown());
            }
            DischargeDefenseProperties discharge = module.definition().dischargeDefense().orElse(null);
            if (discharge != null) {
                discharges++;
                if (module.module().active()) activeDischarges++;
                dischargeRange = Math.max(dischargeRange, discharge.range());
                dischargeDamage = Math.max(dischargeDamage, discharge.damage());
                jumpRange = Math.max(jumpRange, discharge.jumpRange());
                falloff = Math.max(falloff, discharge.falloff());
                targets = Math.max(targets, discharge.targets());
                bounces = Math.max(bounces, discharge.bounces());
                dischargeCost = Math.max(dischargeCost, discharge.energyCost());
                dischargeCooldown = Math.min(dischargeCooldown, discharge.cooldown());
            }
        }

        if (lasers > 0) {
            add(result, "laser.modules", lasers);
            add(result, "laser.active_modules", activeLasers);
            add(result, "laser.range", laserRange);
            add(result, "laser.damage", laserDamage);
            add(result, "laser.energy_cost", laserCost);
            add(result, "laser.cooldown", laserCooldown);
        }
        if (discharges > 0) {
            add(result, "discharge.modules", discharges);
            add(result, "discharge.active_modules", activeDischarges);
            add(result, "discharge.range", dischargeRange);
            add(result, "discharge.jump_range", jumpRange);
            add(result, "discharge.damage", dischargeDamage);
            add(result, "discharge.falloff", falloff);
            add(result, "discharge.targets", targets);
            add(result, "discharge.bounces", bounces);
            add(result, "discharge.energy_cost", dischargeCost);
            add(result, "discharge.cooldown", dischargeCooldown);
        }
    }

    private static void add(List<Characteristic> result, String key, double value) {
        result.add(new Characteristic(CharacteristicCategory.COMBAT, key, CharacteristicType.STATIC, value));
    }
}
