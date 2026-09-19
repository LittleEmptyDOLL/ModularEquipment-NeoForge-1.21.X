package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.StatusProtectionOperations;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class StatusProtectionEvents {
    private static final ThreadLocal<Boolean> REAPPLYING =
            ThreadLocal.withInitial(() -> false);

    private StatusProtectionEvents() {}

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        ItemStack stack = findExoskeleton(entity).orElse(null);
        if (stack == null) {
            return;
        }

        ExoskeletonRuntimeState runtime = getRuntime(stack);
        ResourceLocation effectId = getEffectId(event.getEffectInstance());

        if (effectId == null) {
            return;
        }

        double protection = StatusProtectionOperations.calculateProtection(
                ExoskeletonItem.getData(stack),
                effectId,
                runtime.poweredModules()
        );

        if (protection >= 1.0D) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide() || REAPPLYING.get()) {
            return;
        }

        ItemStack stack = findExoskeleton(entity).orElse(null);
        if (stack == null) {
            return;
        }

        MobEffectInstance addedEffect = event.getEffectInstance();
        ResourceLocation effectId = getEffectId(addedEffect);

        if (effectId == null || addedEffect.isInfiniteDuration()) {
            return;
        }

        ExoskeletonRuntimeState runtime = getRuntime(stack);

        double protection = StatusProtectionOperations.calculateProtection(
                ExoskeletonItem.getData(stack),
                effectId,
                runtime.poweredModules()
        );

        if (protection <= 0.0D) {
            return;
        }

        Holder<MobEffect> effect = addedEffect.getEffect();

        /*
         * MobEffectEvent.Added is fired while LivingEntity.addEffect() is
         * still processing the incoming effect. We therefore wait until the
         * current add/update operation has completed and then replace the
         * actual stored effect with a new instance.
         */
        entity.level().getServer().execute(() ->
                replaceEffectWithProtectedDuration(
                        entity,
                        effectId,
                        protection
                )
        );
    }

    private static void replaceEffectWithProtectedDuration(
            LivingEntity entity,
            ResourceLocation effectId,
            double protection
    ) {
        if (!entity.isAlive()) {
            return;
        }

        MobEffectInstance current = entity.getActiveEffects().stream()
                .filter(effect -> effectId.equals(getEffectId(effect)))
                .findFirst()
                .orElse(null);

        if (current == null || current.isInfiniteDuration()) {
            return;
        }

        int currentDuration = current.getDuration();
        int protectedDuration = Math.max(
                0,
                (int) Math.floor(currentDuration * (1.0D - protection))
        );

        if (protectedDuration >= currentDuration) {
            return;
        }

        int amplifier = current.getAmplifier();
        boolean ambient = current.isAmbient();
        boolean visible = current.isVisible();
        boolean showIcon = current.showIcon();

        Holder<MobEffect> effect = current.getEffect();
        entity.removeEffect(effect);

        if (protectedDuration <= 0) {
            return;
        }

        MobEffectInstance protectedEffect = new MobEffectInstance(
                effect,
                protectedDuration,
                amplifier,
                ambient,
                visible,
                showIcon
        );

        try {
            REAPPLYING.set(true);
            entity.addEffect(protectedEffect);
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

    private static ExoskeletonRuntimeState getRuntime(ItemStack stack) {
        ExoskeletonRuntimeState runtime =
                stack.get(ModDataComponents.EXOSKELETON_RUNTIME.get());

        return runtime != null
                ? runtime
                : ExoskeletonRuntimeState.empty();
    }

    private static Optional<ItemStack> findExoskeleton(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(curios ->
                        curios.findFirstCurio(
                                stack -> stack.getItem() instanceof ExoskeletonItem
                        )
                )
                .map(result -> result.stack());
    }
}
