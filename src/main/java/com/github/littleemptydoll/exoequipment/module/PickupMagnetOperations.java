package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class PickupMagnetOperations {
    private static final double MIN_ACCELERATION = 0.04D;
    private static final double MAX_ACCELERATION = 1.0D;

    private PickupMagnetOperations() {}

    public static Optional<PickupMagnetProperties> findProperties(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        return findProperties(data, null, poweredModules);
    }

    public static Optional<PickupMagnetProperties> findProperties(
            ExoskeletonData data,
            int matrixSlot,
            Set<InstalledModuleReference> poweredModules
    ) {
        return findProperties(
                data,
                Integer.valueOf(matrixSlot),
                poweredModules
        );
    }

    private static Optional<PickupMagnetProperties> findProperties(
            ExoskeletonData data,
            Integer matrixSlot,
            Set<InstalledModuleReference> poweredModules
    ) {
        double radius = 0.0D;
        boolean items = false;
        boolean experience = false;
        List<ExoskeletonModules.ActiveModule> modules =
                matrixSlot == null
                        ? ExoskeletonModules.activeSupported(data)
                        : ExoskeletonModules.supportedInMatrix(
                                data,
                                matrixSlot
                        );

        for (ExoskeletonModules.ActiveModule activeModule : modules) {
            if (!ExoskeletonModules.isPowered(
                    activeModule,
                    poweredModules
            )) {
                continue;
            }

            PickupMagnetProperties properties =
                    activeModule.definition()
                            .pickupMagnet()
                            .orElse(null);

            if (properties == null) {
                continue;
            }

            double efficiency =
                    TemperatureOperations.calculateModuleEfficiency(
                            activeModule.definition(),
                            data.temperature()
                    );

            radius = Math.max(
                    radius,
                    properties.radius() * efficiency
            );
            items |= properties.mode().acceptsItems();
            experience |= properties.mode().acceptsExperience();
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

        return Optional.of(
                new PickupMagnetProperties(radius, mode)
        );
    }

    public static void pull(
            Entity target,
            Player player,
            double radius
    ) {
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
                target.getDeltaMovement()
                        .lerp(desiredVelocity, 0.35D)
        );
        target.hurtMarked = true;
    }
}
