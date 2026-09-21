package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class JetpackOperations {
    private JetpackOperations() {}

    public static ExoskeletonData tick(
            Player player,
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        if (FlightOperations.hasActive(data)) {
            return data;
        }

        byte input = JetpackInputState.get(player);
        if (input == 0) {
            return data;
        }

        List<JetpackTarget> targets = collectTargets(data, poweredModules);
        if (targets.isEmpty()) {
            return data;
        }

        JetpackTarget target = targets.get(0);
        JetpackProperties properties = target.properties();

        if (player.isFallFlying()) {
            if (!JetpackInputState.has(input, JetpackInputState.UP)
                    || properties.elytra().isEmpty()) {
                return data;
            }

            applyElytraBoost(player, properties.elytra().get());
            return data;
        }

        applyNormalThrust(player, input, properties);
        return data;
    }

    private static void applyNormalThrust(
            Player player,
            byte input,
            JetpackProperties properties
    ) {
        Vec3 velocity = player.getDeltaMovement();

        double vertical = 0.0D;
        if (JetpackInputState.has(input, JetpackInputState.UP)) {
            vertical += properties.verticalThrust();
        }
        if (JetpackInputState.has(input, JetpackInputState.DOWN)) {
            vertical -= properties.verticalThrust();
        }

        double newY = velocity.y + vertical;
        newY = Math.max(
                -properties.maxVerticalSpeed(),
                Math.min(properties.maxVerticalSpeed(), newY)
        );

        double forwardInput = 0.0D;
        double strafeInput = 0.0D;
        if (JetpackInputState.has(input, JetpackInputState.FORWARD)) {
            forwardInput += 1.0D;
        }
        if (JetpackInputState.has(input, JetpackInputState.BACK)) {
            forwardInput -= 1.0D;
        }
        if (JetpackInputState.has(input, JetpackInputState.RIGHT)) {
            strafeInput += 1.0D;
        }
        if (JetpackInputState.has(input, JetpackInputState.LEFT)) {
            strafeInput -= 1.0D;
        }

        Vec3 horizontal = Vec3.ZERO;
        if (forwardInput != 0.0D || strafeInput != 0.0D) {
            Vec3 forward = player.getLookAngle();
            forward = new Vec3(forward.x, 0.0D, forward.z);

            if (forward.lengthSqr() > 1.0E-8D) {
                forward = forward.normalize();
            }

            Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
            Vec3 direction = forward.scale(forwardInput)
                    .add(right.scale(strafeInput));

            if (direction.lengthSqr() > 1.0E-8D) {
                horizontal = direction.normalize()
                        .scale(properties.horizontalSpeed());
            }
        }

        double horizontalSpeedSqr = velocity.x * velocity.x + velocity.z * velocity.z;
        Vec3 newHorizontal = horizontal;
        if (horizontalSpeedSqr > 0.0D && horizontal.lengthSqr() == 0.0D) {
            newHorizontal = new Vec3(velocity.x, 0.0D, velocity.z)
                    .scale(0.95D);
        }

        player.setDeltaMovement(
                new Vec3(
                        newHorizontal.x,
                        newY,
                        newHorizontal.z
                )
        );
    }

    private static void applyElytraBoost(
            Player player,
            ElytraBoostProperties properties
    ) {
        Vec3 velocity = player.getDeltaMovement();
        Vec3 direction = player.getLookAngle();

        if (direction.lengthSqr() < 1.0E-8D) {
            return;
        }

        Vec3 boosted = velocity.add(
                direction.normalize().scale(properties.acceleration())
        );

        double maxSpeed = properties.maxSpeed();
        if (boosted.lengthSqr() > maxSpeed * maxSpeed) {
            boosted = boosted.normalize().scale(maxSpeed);
        }

        player.setDeltaMovement(boosted);
    }

    private static List<JetpackTarget> collectTargets(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        List<JetpackTarget> targets = new ArrayList<>();

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            MatrixData matrix = data.matrices().get(slot).matrix().orElse(null);
            if (matrix == null) {
                continue;
            }

            for (int moduleIndex = 0; moduleIndex < matrix.modules().size(); moduleIndex++) {
                InstalledModuleReference reference =
                        new InstalledModuleReference(slot, moduleIndex);

                if (!poweredModules.contains(reference)) {
                    continue;
                }

                InstalledModule module = matrix.modules().get(moduleIndex);
                if (!FrameOperations.isModuleSupported(data, module)) {
                    continue;
                }

                ModModules.getDefinition(module.id())
                        .jetpack()
                        .ifPresent(properties ->
                                targets.add(new JetpackTarget(reference, properties))
                        );
            }
        }

        targets.sort(
                Comparator.comparingInt((JetpackTarget target) ->
                                target.reference().matrixSlot())
                        .thenComparingInt(target ->
                                target.reference().moduleIndex())
        );

        return targets;
    }

    private record JetpackTarget(
            InstalledModuleReference reference,
            JetpackProperties properties
    ) {}
}
