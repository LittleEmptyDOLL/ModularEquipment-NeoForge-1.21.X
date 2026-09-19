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

        if (entity.level().isClientSide()) {
            return;
        }

        ItemStack stack = findExoskeleton(entity).orElse(null);
        if (stack == null) {
            return;
        }

        ExoskeletonRuntimeState runtime = getRuntime(stack);
        MobEffectInstance effect = event.getEffectInstance();
        ResourceLocation effectId = getEffectId(effect);

        if (effectId == null) {
            return;
        }

        int originalDuration = effect.getDuration();

        if (originalDuration <= 0) {
            return;
        }

        int protectedDuration = StatusProtectionOperations.applyProtection(
                originalDuration,
                ExoskeletonItem.getData(stack),
                effectId,
                runtime.poweredModules()
        );

        if (protectedDuration >= originalDuration) {
            return;
        }

        // Added is fired before LivingEntity stores the resulting effect.
        // Change the incoming instance immediately so the normal add/update
        // path receives the protected duration.
        effect.mapDuration(duration -> protectedDuration);

        // For an existing effect, vanilla may merge the incoming instance
        // into another MobEffectInstance after this event. Correct the
        // actually stored instance after the add/update operation completes.
        entity.level().getServer().execute(() ->
                applyProtectedDuration(
                        entity,
                        effect.getEffect(),
                        protectedDuration
                )
        );
    }

    private static void applyProtectedDuration(
            LivingEntity entity,
            net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
            int protectedDuration
    ) {
        if (!entity.isAlive()) {
            return;
        }

        MobEffectInstance activeEffect = entity.getEffect(effect);

        if (activeEffect == null) {
            return;
        }

        activeEffect.mapDuration(duration ->
                Math.min(duration, protectedDuration)
        );
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
