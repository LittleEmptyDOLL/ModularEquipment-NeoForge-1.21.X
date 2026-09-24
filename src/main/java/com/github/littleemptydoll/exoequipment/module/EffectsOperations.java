package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public final class EffectsOperations {
    private static final int EFFECT_DURATION = 5;

    private EffectsOperations() {}

    public static void apply(
            LivingEntity entity,
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            var matrix = data.matrices().get(slot).matrix().orElse(null);
            if (matrix == null) continue;

            for (int moduleIndex = 0; moduleIndex < matrix.modules().size(); moduleIndex++) {
                InstalledModule module = matrix.modules().get(moduleIndex);
                if (!FrameOperations.isModuleSupported(data, module)) continue;

                InstalledModuleReference reference =
                        new InstalledModuleReference(slot, moduleIndex);
                if (!isPowered(module.id(), reference, poweredModules)) continue;

                ModModules.getDefinition(module.id())
                        .effects()
                        .ifPresent(properties -> properties.effects().forEach(
                                (effectId, amplifier) -> applyEffect(entity, effectId, amplifier)
                        ));
            }
        }
    }

    private static void applyEffect(
            LivingEntity entity,
            ResourceLocation effectId,
            int amplifier
    ) {
        Holder<MobEffect> effect = BuiltInRegistries.MOB_EFFECT.getHolder(effectId).orElse(null);
        if (effect == null) return;

        MobEffectInstance existing = entity.getEffect(effect);
        if (existing != null) {
            if (existing.getAmplifier() > amplifier) {
                return;
            }
            if (existing.getAmplifier() == amplifier
                    && existing.getDuration() > EFFECT_DURATION) {
                return;
            }
        }

        entity.addEffect(new MobEffectInstance(
                effect,
                EFFECT_DURATION,
                amplifier,
                true,
                false,
                false
        ));
    }

    private static boolean isPowered(
            ResourceLocation moduleId,
            InstalledModuleReference reference,
            Set<InstalledModuleReference> poweredModules
    ) {
        var energy = ModModules.getDefinition(moduleId).energy();

        return energy.isEmpty()
                || energy.get().consumption() <= 0
                || poweredModules.contains(reference);
    }
}
