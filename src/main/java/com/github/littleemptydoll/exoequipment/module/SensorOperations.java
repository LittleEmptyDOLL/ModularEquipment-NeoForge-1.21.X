package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SensorOperations {
    private SensorOperations() {}

    public static List<DetectedEntity> detectEntities(
            Entity scanner,
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        List<DetectionTarget> targets = collectDetectionTargets(
                data,
                poweredModules
        );

        if (targets.isEmpty()) {
            return List.of();
        }

        double maxRange = targets.stream()
                .mapToDouble(target -> target.properties().range())
                .max()
                .orElse(0.0D);

        List<DetectedEntity> result = new ArrayList<>();

        for (Entity entity : scanner.level().getEntities(
                scanner,
                scanner.getBoundingBox().inflate(maxRange),
                candidate -> candidate
                        instanceof net.minecraft.world.entity.LivingEntity
        )) {
            double distanceSqr = scanner.distanceToSqr(entity);

            for (DetectionTarget target : targets) {
                if (distanceSqr
                        > target.properties().range()
                        * target.properties().range()) {
                    continue;
                }

                if (matches(entity, target.properties())) {
                    result.add(new DetectedEntity(
                            entity,
                            Math.sqrt(distanceSqr)
                    ));
                    break;
                }
            }
        }

        return List.copyOf(result);
    }

    private static List<DetectionTarget> collectDetectionTargets(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        List<DetectionTarget> targets = new ArrayList<>();

        for (ExoskeletonModules.ActiveModule activeModule
                : ExoskeletonModules.activeSupported(data)) {

            if (!ExoskeletonModules.isPowered(
                    activeModule,
                    poweredModules
            )) {
                continue;
            }

            activeModule.definition()
                    .entityDetection()
                    .ifPresent(properties ->
                            targets.add(
                                    new DetectionTarget(properties)
                            )
                    );
        }

        return List.copyOf(targets);
    }

    private static boolean matches(
            Entity entity,
            EntityDetectionProperties properties
    ) {
        if (properties.players() && entity instanceof Player) {
            return true;
        }

        if (properties.hostile() && entity instanceof Enemy) {
            return true;
        }

        return properties.mobs()
                && entity instanceof Mob
                && !(entity instanceof Enemy);
    }

    private record DetectionTarget(
            EntityDetectionProperties properties
    ) {}

    public record DetectedEntity(
            Entity entity,
            double distance
    ) {}
}
