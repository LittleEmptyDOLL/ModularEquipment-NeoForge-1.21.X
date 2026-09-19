package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.StatusProtectionOperations;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.resources.ResourceLocation;
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
    public static void onApplicable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide() || REAPPLYING.get()) {
            return;
        }

        ItemStack stack = findExoskeleton(entity).orElse(null);
        if (stack == null) {
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

        ExoskeletonRuntimeState runtime = getRuntime(stack);

        double protection = StatusProtectionOperations.calculateProtection(
                ExoskeletonItem.getData(stack),
                effectId,
                runtime.poweredModules()
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
                ExoskeletonItem.getData(stack),
                effectId,
                runtime.poweredModules()
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
