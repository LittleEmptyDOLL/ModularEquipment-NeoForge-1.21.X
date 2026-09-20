package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.Set;

public final class PickupMagnetOperations {
    private static final double MIN_ACCELERATION = 0.025D;
    private static final double MAX_ACCELERATION = 0.18D;

    private PickupMagnetOperations() {}

    public static Optional<PickupMagnetProperties> findProperties(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        double radius = 0.0D;
        boolean items = false;
        boolean experience = false;

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            MatrixData matrix = data.matrices().get(slot).matrix().orElse(null);
            if (matrix == null) {
                continue;
            }

            for (int moduleIndex = 0; moduleIndex < matrix.modules().size(); moduleIndex++) {
                InstalledModule module = matrix.modules().get(moduleIndex);

                if (!FrameOperations.isModuleSupported(data, module)) {
                    continue;
                }

                InstalledModuleReference reference =
                        new InstalledModuleReference(slot, moduleIndex);

                if (!isPowered(module.id(), reference, poweredModules)) {
                    continue;
                }

                PickupMagnetProperties properties =
                        ModModules.getDefinition(module.id())
                                .pickupMagnet()
                                .orElse(null);

                if (properties == null) {
                    continue;
                }

                radius = Math.max(radius, properties.radius());
                items |= properties.mode().acceptsItems();
                experience |= properties.mode().acceptsExperience();
            }
        }

        if (radius <= 0.0D || (!items && !experience)) {
            return Optional.empty();
        }

        PickupMagnetProperties.Mode mode =
                items && experience
                        ? PickupMagnetProperties.Mode.BOTH
                        : items
                                ? PickupMagnetProperties.Mode.ITEMS
                                : PickupMagnetProperties.Mode.EXPERIENCE;

        return Optional.of(new PickupMagnetProperties(radius, mode));
    }

    public static void pull(Entity target, Player player, double radius) {
        var offset = player.position()
                .add(0.0D, player.getBbHeight() * 0.5D, 0.0D)
                .subtract(target.position());

        double distance = offset.length();
        if (distance <= 0.001D || distance > radius) {
            return;
        }

        double distanceFactor = 1.0D - distance / radius;
        double acceleration =
                MIN_ACCELERATION
                        + (MAX_ACCELERATION - MIN_ACCELERATION)
                        * distanceFactor
                        * distanceFactor;

        var direction = offset.scale(1.0D / distance);
        var desiredVelocity = direction.scale(acceleration);

        target.setDeltaMovement(
                target.getDeltaMovement().lerp(desiredVelocity, 0.35D)
        );
        target.hurtMarked = true;
    }

    private static boolean isPowered(
            ResourceLocation moduleId,
            InstalledModuleReference reference,
            Set<InstalledModuleReference> poweredModules
    ) {
        EnergyProperties energy =
                ModModules.getDefinition(moduleId).energy().orElse(null);

        return energy == null
                || energy.consumption() <= 0
                || poweredModules.contains(reference);
    }
}
