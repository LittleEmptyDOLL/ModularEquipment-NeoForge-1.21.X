package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
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
                candidate -> candidate instanceof net.minecraft.world.entity.LivingEntity
        )) {
            double distanceSqr = scanner.distanceToSqr(entity);

            for (DetectionTarget target : targets) {
                if (distanceSqr > target.properties().range() * target.properties().range()) {
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

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            MatrixData matrix = data.matrices()
                    .get(slot)
                    .matrix()
                    .orElse(null);

            if (matrix == null) {
                continue;
            }

            for (int moduleIndex = 0;
                 moduleIndex < matrix.modules().size();
                 moduleIndex++) {

                InstalledModule module = matrix.modules().get(moduleIndex);

                if (!FrameOperations.isModuleSupported(data, module)) {
                    continue;
                }

                InstalledModuleReference reference =
                        new InstalledModuleReference(slot, moduleIndex);

                if (!isPowered(module.id(), reference, poweredModules)) {
                    continue;
                }

                ModModules.getDefinition(module.id())
                        .entityDetection()
                        .ifPresent(properties ->
                                targets.add(
                                        new DetectionTarget(
                                                reference,
                                                module.id(),
                                                properties
                                        )
                                )
                        );
            }
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

    private static boolean isPowered(
            net.minecraft.resources.ResourceLocation moduleId,
            InstalledModuleReference reference,
            Set<InstalledModuleReference> poweredModules
    ) {
        var energy = ModModules.getDefinition(moduleId).energy();

        return energy.isEmpty()
                || energy.get().consumption() <= 0
                || poweredModules.contains(reference);
    }

    private record DetectionTarget(
            InstalledModuleReference reference,
            net.minecraft.resources.ResourceLocation moduleId,
            EntityDetectionProperties properties
    ) {}

    public record DetectedEntity(
            Entity entity,
            double distance
    ) {}
}
