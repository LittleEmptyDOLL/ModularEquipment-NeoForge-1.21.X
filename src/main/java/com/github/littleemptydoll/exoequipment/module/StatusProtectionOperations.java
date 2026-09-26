package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Set;
import java.util.function.ToDoubleFunction;

public final class StatusProtectionOperations {
    private StatusProtectionOperations() {}

    public static double calculateProtection(
            ExoskeletonData data,
            ResourceLocation effectId
    ) {
        return calculateProtection(data, null, effectId, null);
    }

    public static double calculateProtection(
            ExoskeletonData data,
            ResourceLocation effectId,
            Set<InstalledModuleReference> poweredModules
    ) {
        return calculateProtection(data, null, effectId, poweredModules);
    }

    public static double calculateProtection(
            ExoskeletonData data,
            int matrixSlot,
            ResourceLocation effectId,
            Set<InstalledModuleReference> poweredModules
    ) {
        return calculateProtection(
                data,
                Integer.valueOf(matrixSlot),
                effectId,
                poweredModules
        );
    }

    public static double calculateProtection(
            ExoskeletonData data,
            Holder<MobEffect> effect,
            Set<InstalledModuleReference> poweredModules
    ) {
        return calculateProtection(
                data,
                null,
                effect,
                poweredModules
        );
    }

    public static double calculateProtection(
            ExoskeletonData data,
            int matrixSlot,
            Holder<MobEffect> effect,
            Set<InstalledModuleReference> poweredModules
    ) {
        return calculateProtection(
                data,
                Integer.valueOf(matrixSlot),
                effect,
                poweredModules
        );
    }

    public static double calculateHarmfulProtection(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        return calculate(
                data,
                null,
                poweredModules,
                StatusProtectionProperties::harmfulProtection
        );
    }

    public static double calculateHarmfulProtection(
            ExoskeletonData data,
            int matrixSlot,
            Set<InstalledModuleReference> poweredModules
    ) {
        return calculate(
                data,
                Integer.valueOf(matrixSlot),
                poweredModules,
                StatusProtectionProperties::harmfulProtection
        );
    }

    public static double calculateTagProtection(
            ExoskeletonData data,
            ResourceLocation tagId,
            Set<InstalledModuleReference> poweredModules
    ) {
        return calculate(
                data,
                null,
                poweredModules,
                properties -> properties.tagProtection(tagId)
        );
    }

    public static double calculateTagProtection(
            ExoskeletonData data,
            int matrixSlot,
            ResourceLocation tagId,
            Set<InstalledModuleReference> poweredModules
    ) {
        return calculate(
                data,
                Integer.valueOf(matrixSlot),
                poweredModules,
                properties -> properties.tagProtection(tagId)
        );
    }

    private static double calculateProtection(
            ExoskeletonData data,
            Integer matrixSlot,
            ResourceLocation effectId,
            Set<InstalledModuleReference> poweredModules
    ) {
        if (effectId == null) {
            return 0.0D;
        }

        Holder<MobEffect> effect = BuiltInRegistries.MOB_EFFECT
                .getHolder(effectId)
                .orElse(null);

        if (effect != null) {
            return calculateProtection(
                    data,
                    matrixSlot,
                    effect,
                    poweredModules
            );
        }

        return calculate(
                data,
                matrixSlot,
                poweredModules,
                properties -> properties.protection(
                        effectId,
                        false
                )
        );
    }

    private static double calculateProtection(
            ExoskeletonData data,
            Integer matrixSlot,
            Holder<MobEffect> effect,
            Set<InstalledModuleReference> poweredModules
    ) {
        if (effect == null) {
            return 0.0D;
        }

        return calculate(
                data,
                matrixSlot,
                poweredModules,
                properties -> properties.protection(effect)
        );
    }

    private static double calculate(
            ExoskeletonData data,
            Integer matrixSlot,
            Set<InstalledModuleReference> poweredModules,
            ToDoubleFunction<StatusProtectionProperties> protectionResolver
    ) {
        double remaining = 1.0D;
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

            double protection = Math.min(
                    1.0D,
                    Math.max(
                            0.0D,
                            protectionResolver.applyAsDouble(properties)
                                    * efficiency
                    )
            );

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
                .filter(effect ->
                        calculateProtection(
                                data,
                                effect.getEffect(),
                                poweredModules
                        ) >= 1.0D
                )
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
