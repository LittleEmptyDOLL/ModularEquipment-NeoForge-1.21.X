package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.energy.EnergyOperations;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class BlinkOperations {
    private static final double COLLISION_EPSILON = 0.05D;
    private static final double SEARCH_STEP = 0.10D;

    private BlinkOperations() {}

    public static ActivationResult activate(
            ExoskeletonData data,
            ServerPlayer player
    ) {
        for (Target target : collectTargets(data)) {
            InstalledModule module = target.module();
            if (module.abilityCooldown() > 0) {
                continue;
            }

            Vec3 destination = findDestination(player, target.properties().distance());
            if (destination == null) {
                continue;
            }

            EnergyOperations.EnergyConsumptionResult energyResult =
                    EnergyOperations.consumeEnergy(
                            data,
                            target.properties().activationEnergy()
                    );

            if (!energyResult.sufficient()) {
                continue;
            }

            ExoskeletonData updated = replaceModule(
                    energyResult.data(),
                    target.reference(),
                    module.withAbilityCooldown(target.properties().cooldown())
            );

            player.teleportTo(destination.x, destination.y, destination.z);
            return new ActivationResult(updated, true);
        }

        return new ActivationResult(data, false);
    }

    public static ExoskeletonData tick(ExoskeletonData data) {
        ExoskeletonData updated = data;

        for (Target target : collectTargets(updated)) {
            InstalledModule module = getModule(updated, target.reference());
            if (module == null) {
                continue;
            }

            // CloakingOperations already owns cooldown ticking for modules
            // that provide both abilities.
            if (ModModules.getDefinition(module.id()).cloaking().isPresent()) {
                continue;
            }

            int cooldown = Math.max(0, module.abilityCooldown() - 1);
            if (cooldown != module.abilityCooldown()) {
                updated = replaceModule(
                        updated,
                        target.reference(),
                        module.withAbilityCooldown(cooldown)
                );
            }
        }

        return updated;
    }

    private static Vec3 findDestination(ServerPlayer player, double distance) {
        Vec3 start = player.getEyePosition();
        Vec3 direction = player.getViewVector(1.0F).normalize();
        Vec3 requested = start.add(direction.scale(distance));

        BlockHitResult hit = player.level().clip(new ClipContext(
                start,
                requested,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        Vec3 destination = hit.getType() == HitResult.Type.BLOCK
                ? hit.getLocation().subtract(direction.scale(COLLISION_EPSILON))
                : requested;

        Vec3 displacement = destination.subtract(start);
        double travelled = displacement.length();
        if (travelled <= 0.0D) {
            return null;
        }

        // The ray starts at the player's eyes, while teleportTo expects the
        // player's feet position. Apply the same displacement to the current
        // entity position and search backwards until the full bounding box fits.
        for (double offset = 0.0D; offset < travelled; offset += SEARCH_STEP) {
            Vec3 candidate = player.position()
                    .add(displacement)
                    .subtract(direction.scale(offset));
            if (isSafe(player, candidate)
                    && candidate.distanceTo(player.position()) > COLLISION_EPSILON) {
                return candidate;
            }
        }

        return null;
    }

    private static boolean isSafe(ServerPlayer player, Vec3 position) {
        Vec3 delta = position.subtract(player.position());
        AABB box = player.getBoundingBox().move(delta);
        return player.level().noCollision(player, box);
    }

    private static List<Target> collectTargets(ExoskeletonData data) {
        List<Target> targets = new ArrayList<>();

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            MatrixData matrix = data.matrices().get(slot).matrix().orElse(null);
            if (matrix == null) {
                continue;
            }

            for (int index = 0; index < matrix.modules().size(); index++) {
                InstalledModule module = matrix.modules().get(index);
                if (!com.github.littleemptydoll.exoequipment.frame.FrameOperations
                        .isModuleSupported(data, module)) {
                    continue;
                }

                BlinkProperties properties = ModModules.getDefinition(module.id())
                        .blink()
                        .orElse(null);
                if (properties == null) {
                    continue;
                }

                targets.add(new Target(
                        new InstalledModuleReference(slot, index),
                        module,
                        properties
                ));
            }
        }

        targets.sort(
                Comparator.comparingInt((Target target) -> target.reference().matrixSlot())
                        .thenComparingInt(target -> target.reference().moduleIndex())
        );
        return targets;
    }

    private static InstalledModule getModule(
            ExoskeletonData data,
            InstalledModuleReference reference
    ) {
        if (reference.matrixSlot() < 0
                || reference.matrixSlot() >= data.matrices().size()) {
            return null;
        }

        MatrixData matrix = data.matrices().get(reference.matrixSlot()).matrix().orElse(null);
        if (matrix == null
                || reference.moduleIndex() < 0
                || reference.moduleIndex() >= matrix.modules().size()) {
            return null;
        }

        return matrix.modules().get(reference.moduleIndex());
    }

    private static ExoskeletonData replaceModule(
            ExoskeletonData data,
            InstalledModuleReference reference,
            InstalledModule module
    ) {
        MatrixData matrix = data.matrices().get(reference.matrixSlot()).matrix().orElseThrow();
        List<InstalledModule> modules = new ArrayList<>(matrix.modules());
        modules.set(reference.moduleIndex(), module);
        return data.withMatrix(
                reference.matrixSlot(),
                new MatrixData(matrix.id(), modules)
        );
    }

    public record ActivationResult(
            ExoskeletonData data,
            boolean activated
    ) {}

    private record Target(
            InstalledModuleReference reference,
            InstalledModule module,
            BlinkProperties properties
    ) {}
}
