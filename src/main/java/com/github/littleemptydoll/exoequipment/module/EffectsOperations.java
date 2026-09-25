package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class EffectsOperations {
    private static final int EFFECT_DURATION =
            MobEffectInstance.INFINITE_DURATION;

    private EffectsOperations() {}

    public static Map<ResourceLocation, Integer> collectEffects(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        Map<ResourceLocation, Integer> effects = new HashMap<>();

        for (ExoskeletonModules.ActiveModule activeModule
                : ExoskeletonModules.activeSupported(data)) {

            if (!ExoskeletonModules.isPowered(
                    activeModule,
                    poweredModules
            )) {
                continue;
            }

            activeModule.definition()
                    .effects()
                    .ifPresent(properties ->
                            properties.effects().forEach(
                                    (effectId, amplifier) ->
                                            effects.merge(
                                                    effectId,
                                                    amplifier,
                                                    Math::max
                                            )
                            )
                    );
        }

        return Map.copyOf(effects);
    }

    public static Map<ResourceLocation, Integer> apply(
            LivingEntity entity,
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        Map<ResourceLocation, Integer> applied = new HashMap<>();

        collectEffects(data, poweredModules)
                .forEach((effectId, amplifier) -> {
                    if (applyEffect(
                            entity,
                            effectId,
                            amplifier
                    )) {
                        applied.put(effectId, amplifier);
                    }
                });

        return Map.copyOf(applied);
    }

    private static boolean applyEffect(
            LivingEntity entity,
            ResourceLocation effectId,
            int amplifier
    ) {
        Holder<MobEffect> effect =
                BuiltInRegistries.MOB_EFFECT
                        .getHolder(effectId)
                        .orElse(null);

        if (effect == null) {
            return false;
        }

        MobEffectInstance existing = entity.getEffect(effect);

        if (existing != null) {
            if (existing.getAmplifier() > amplifier) {
                return false;
            }

            if (existing.getAmplifier() == amplifier
                    && existing.getDuration() == EFFECT_DURATION) {
                return true;
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

        return true;
    }
}
