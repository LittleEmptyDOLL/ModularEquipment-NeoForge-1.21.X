package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.energy.EnergyState;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/** Server-authoritative auto-defense; each module owns its charge and cooldown. */
public final class CombatDefenseOperations {
    private CombatDefenseOperations() {}

    public static ExoskeletonData tick(ServerPlayer owner, ExoskeletonData data) {
        ExoskeletonData updated = data;
        boolean hasBattery = data.energySystem().isPresent()
                && EnergyState.calculate(data).storageCapacity() > 0;
        Set<UUID> claimed = new HashSet<>();
        boolean firedAny = false;

        for (var active : ExoskeletonModules.activeSupported(data)) {
            LaserDefenseProperties laser = active.definition().laserDefense().orElse(null);
            DischargeDefenseProperties discharge = active.definition().dischargeDefense().orElse(null);
            if (laser == null && discharge == null) continue;

            InstalledModule module = ExoskeletonModules.get(updated, active.reference()).orElseThrow();
            int cooldown = Math.max(0, module.abilityCooldown() - 1);
            if (cooldown != module.abilityCooldown()) {
                module = module.withAbilityCooldown(cooldown);
                updated = ExoskeletonModules.update(updated, active.reference(), module);
            }
            if (!hasBattery || !module.active() || cooldown > 0) continue;

            int cost = laser != null ? laser.energyCost() : discharge.energyCost();
            if (module.weaponCharge() < cost) continue;

            boolean fired = laser != null
                    ? fireLaser(owner, laser, claimed)
                    : fireDischarge(owner, discharge, claimed);
            if (fired) {
                firedAny = true;
                updated = ExoskeletonModules.update(updated, active.reference(),
                        module.withWeaponCharge(module.weaponCharge() - cost)
                                .withAbilityCooldown(laser != null ? laser.cooldown() : discharge.cooldown()));
            }
        }
        if (firedAny) {
            for (var active : ExoskeletonModules.activeSupported(updated)) {
                if (active.module().active() && active.definition().cloaking().isPresent()) {
                    updated = CloakingOperations.deactivate(updated, active.reference());
                }
            }
        }
        return updated;
    }

    private static boolean fireLaser(ServerPlayer owner, LaserDefenseProperties settings,
                                     Set<UUID> claimed) {
        Predicate<LivingEntity> eligible = entity -> CombatTargeting.eligible(owner, entity,
                settings.targetPlayers(), settings.targetHostile(), settings.targetAggressive());
        for (LivingEntity target : candidates(owner, owner, settings.range(), eligible, Set.of(), claimed)) {
            if (!hit(owner, target, settings.damage())) continue;
            claimed.add(target.getUUID());
            trace((ServerLevel) owner.level(), owner.getEyePosition(), center(target),
                    ParticleTypes.END_ROD, false);
            return true;
        }
        return false;
    }

    private static boolean fireDischarge(ServerPlayer owner, DischargeDefenseProperties settings,
                                         Set<UUID> claimed) {
        Predicate<LivingEntity> eligible = entity -> CombatTargeting.eligible(owner, entity,
                settings.targetPlayers(), settings.targetHostile(), settings.targetAggressive());
        Set<UUID> starts = new HashSet<>();
        boolean fired = false;

        for (int initial = 0; initial < settings.targets(); initial++) {
            LivingEntity first = null;
            for (LivingEntity candidate : candidates(owner, owner, settings.range(), eligible, starts, claimed)) {
                if (hit(owner, candidate, settings.damage())) {
                    first = candidate;
                    break;
                }
            }
            if (first == null) break;
            fired = true;
            starts.add(first.getUUID());
            claimed.add(first.getUUID());
            ServerLevel level = (ServerLevel) owner.level();
            trace(level, owner.getEyePosition(), center(first), ParticleTypes.ELECTRIC_SPARK, true);

            Set<UUID> visited = new HashSet<>();
            visited.add(first.getUUID());
            LivingEntity current = first;
            for (int bounce = 1; bounce <= settings.bounces(); bounce++) {
                float damage = (float) (settings.damage() * Math.pow(settings.falloff(), bounce));
                LivingEntity next = null;
                for (LivingEntity candidate : candidates(owner, current, settings.jumpRange(),
                        eligible, visited, claimed)) {
                    if (hit(owner, candidate, damage)) {
                        next = candidate;
                        break;
                    }
                }
                if (next == null) break;
                trace(level, center(current), center(next), ParticleTypes.ELECTRIC_SPARK, true);
                visited.add(next.getUUID());
                claimed.add(next.getUUID());
                current = next;
            }
        }
        return fired;
    }

    private static List<LivingEntity> candidates(ServerPlayer owner, LivingEntity source,
                                                  double range, Predicate<LivingEntity> eligible,
                                                  Set<UUID> excluded, Set<UUID> claimed) {
        List<LivingEntity> found = owner.level().getEntitiesOfClass(LivingEntity.class,
                        new AABB(source.blockPosition()).inflate(range), entity ->
                                !excluded.contains(entity.getUUID()) && eligible.test(entity)
                                        && source.distanceToSqr(entity) <= range * range
                                        && source.hasLineOfSight(entity))
                .stream().sorted(Comparator.comparingDouble(source::distanceToSqr)).toList();
        // Prefer a different target for each installed module. If the area is
        // crowded with modules but has few enemies, focus fire is allowed.
        List<LivingEntity> unused = found.stream().filter(e -> !claimed.contains(e.getUUID())).toList();
        if (unused.isEmpty()) return found;
        java.util.ArrayList<LivingEntity> ordered = new java.util.ArrayList<>(unused);
        found.stream().filter(e -> claimed.contains(e.getUUID())).forEach(ordered::add);
        return ordered;
    }

    private static boolean hit(ServerPlayer owner, LivingEntity target, float damage) {
        int previous = target.invulnerableTime;
        target.invulnerableTime = 0;
        boolean hurt = target.hurt(owner.damageSources().playerAttack(owner), damage);
        if (!hurt) target.invulnerableTime = previous;
        return hurt;
    }

    private static Vec3 center(LivingEntity entity) {
        return entity.position().add(0, entity.getBbHeight() * 0.6D, 0);
    }

    private static void trace(ServerLevel level, Vec3 start, Vec3 end,
                              ParticleOptions particle, boolean arc) {
        Vec3 delta = end.subtract(start);
        int steps = Math.max(1, (int) Math.ceil(delta.length() * (arc ? 3 : 2)));
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            Vec3 point = start.add(delta.scale(t));
            if (arc && i != 0 && i != steps) {
                point = point.add((level.random.nextDouble() - 0.5D) * 0.35D,
                        (level.random.nextDouble() - 0.5D) * 0.35D,
                        (level.random.nextDouble() - 0.5D) * 0.35D);
            }
            level.sendParticles(particle, point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
    }
}
