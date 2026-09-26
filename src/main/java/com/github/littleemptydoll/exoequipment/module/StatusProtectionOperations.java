package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public final class StatusProtectionOperations {
    private StatusProtectionOperations() {}

    public static double calculateProtection(
            ExoskeletonData data,
            ResourceLocation effectId
    ) {
        return calculateProtection(data, effectId, null);
    }

    public static double calculateProtection(
            ExoskeletonData data,
            ResourceLocation effectId,
            Set<InstalledModuleReference> poweredModules
    ) {
        double remaining = 1.0D;

        for (ExoskeletonModules.ActiveModule activeModule
                : ExoskeletonModules.activeSupported(data)) {

            if (!ExoskeletonModules.isPowered(
                    activeModule,
                    poweredModules
            )) {
                continue;
            }

            StatusProtectionProperties properties =
                    activeModule.definition()
                            .statusProtection()
                            .orElse(null);

            if (properties == null) {
                continue;
            }

            double efficiency =
                    TemperatureOperations.calculateModuleEfficiency(
                            activeModule.definition(),
                            data.temperature()
                    );

            double protection =
                    properties.protection(effectId) * efficiency;

            remaining *= 1.0D - protection;

            if (remaining <= 0.0D) {
                return 1.0D;
            }
        }

        return Math.max(
                0.0D,
                Math.min(1.0D, 1.0D - remaining)
        );
    }

    public static void removeFullyProtectedEffects(
            LivingEntity entity,
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        entity.getActiveEffects().stream()
                .filter(effect -> {
                    ResourceLocation effectId = effect.getEffect()
                            .unwrapKey()
                            .map(key -> key.location())
                            .orElse(null);

                    return effectId != null
                            && calculateProtection(
                            data,
                            effectId,
                            poweredModules
                    ) >= 1.0D;
                })
                .map(effect -> effect.getEffect())
                .toList()
                .forEach(entity::removeEffect);
    }

    public static int applyProtection(
            int duration,
            ExoskeletonData data,
            ResourceLocation effectId,
            Set<InstalledModuleReference> poweredModules
    ) {
        return applyProtection(
                duration,
                calculateProtection(
                        data,
                        effectId,
                        poweredModules
                )
        );
    }

    public static int applyProtection(
            int duration,
            double protection
    ) {
        if (duration <= 0) {
            return 0;
        }

        if (!Double.isFinite(protection)) {
            throw new IllegalArgumentException(
                    "Status protection must be finite"
            );
        }

        double clampedProtection = Math.max(
                0.0D,
                Math.min(1.0D, protection)
        );

        if (clampedProtection >= 1.0D) {
            return 0;
        }

        return Math.max(
                0,
                (int) Math.floor(
                        duration * (1.0D - clampedProtection)
                )
        );
    }
}
