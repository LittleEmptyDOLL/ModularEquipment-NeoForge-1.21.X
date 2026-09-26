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
import java.util.Objects;
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
                            amplifier,
                            true
                    )) {
                        applied.put(effectId, amplifier);
                    }
                });

        return Map.copyOf(applied);
    }

    public static Map<ResourceLocation, Integer> reconcile(
            LivingEntity entity,
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules,
            Map<ResourceLocation, Integer> previouslyApplied
    ) {
        Map<ResourceLocation, Integer> desired =
                collectEffects(data, poweredModules);

        for (Map.Entry<ResourceLocation, Integer> previous
                : previouslyApplied.entrySet()) {
            Integer desiredAmplifier = desired.get(previous.getKey());

            if (!Objects.equals(
                    desiredAmplifier,
                    previous.getValue()
            )) {
                removeEffect(
                        entity,
                        previous.getKey(),
                        previous.getValue()
                );
            }
        }

        Map<ResourceLocation, Integer> applied = new HashMap<>();

        desired.forEach((effectId, amplifier) -> {
            boolean previouslyOwned = Objects.equals(
                    previouslyApplied.get(effectId),
                    amplifier
            );

            if (applyEffect(
                    entity,
                    effectId,
                    amplifier,
                    previouslyOwned
            )) {
                applied.put(effectId, amplifier);
            }
        });

        return Map.copyOf(applied);
    }

    public static void clear(
            LivingEntity entity,
            Map<ResourceLocation, Integer> previouslyApplied
    ) {
        previouslyApplied.forEach(
                (effectId, amplifier) ->
                        removeEffect(entity, effectId, amplifier)
        );
    }

    private static boolean applyEffect(
            LivingEntity entity,
            ResourceLocation effectId,
            int amplifier,
            boolean ownExistingInfiniteEffect
    ) {
        Holder<MobEffect> effect = resolve(effectId);

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
                return ownExistingInfiniteEffect;
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

    private static void removeEffect(
            LivingEntity entity,
            ResourceLocation effectId,
            int amplifier
    ) {
        Holder<MobEffect> effect = resolve(effectId);

        if (effect == null) {
            return;
        }

        MobEffectInstance current = entity.getEffect(effect);

        if (current != null
                && current.getAmplifier() == amplifier
                && current.getDuration() == EFFECT_DURATION) {
            entity.removeEffect(effect);
        }
    }

    private static Holder<MobEffect> resolve(ResourceLocation effectId) {
        return BuiltInRegistries.MOB_EFFECT
                .getHolder(effectId)
                .orElse(null);
    }
}
