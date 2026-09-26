package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.module.StatusProtectionOperations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class StatusProtectionEvents {
    private static final ThreadLocal<Boolean> REAPPLYING =
            ThreadLocal.withInitial(() -> false);

    private StatusProtectionEvents() {}

    @SubscribeEvent
    public static void onApplicable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide() || REAPPLYING.get()) {
            return;
        }

        var context = ExoskeletonAccess.findContext(entity).orElse(null);
        if (context == null) {
            return;
        }

        MobEffectInstance incoming = event.getEffectInstance();

        if (incoming.isInfiniteDuration()) {
            return;
        }

        ResourceLocation effectId = getEffectId(incoming);
        if (effectId == null) {
            return;
        }

        double protection = StatusProtectionOperations.calculateProtection(
                context.data(),
                effectId,
                context.poweredModules()
        );

        if (protection <= 0.0D) {
            return;
        }

        event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);

        if (protection >= 1.0D) {
            return;
        }

        int duration = StatusProtectionOperations.applyProtection(
                incoming.getDuration(),
                protection
        );

        if (duration <= 0) {
            return;
        }

        REAPPLYING.set(true);
        try {
            entity.addEffect(new MobEffectInstance(
                    incoming.getEffect(),
                    duration,
                    incoming.getAmplifier(),
                    incoming.isAmbient(),
                    incoming.isVisible(),
                    incoming.showIcon()
            ));
        } finally {
            REAPPLYING.set(false);
        }
    }

    private static ResourceLocation getEffectId(MobEffectInstance effect) {
        return effect.getEffect()
                .unwrapKey()
                .map(key -> key.location())
                .orElse(null);
    }
}
