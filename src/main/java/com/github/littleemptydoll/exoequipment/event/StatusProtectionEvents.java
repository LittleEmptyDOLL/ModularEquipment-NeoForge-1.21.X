package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.StatusProtectionOperations;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
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
        ResourceLocation effectId = event.getEffectInstance()
                .getEffect()
                .unwrapKey()
                .map(key -> key.location())
                .orElse(null);

        if (effectId == null) {
            return;
        }

        double protection = StatusProtectionOperations.calculateProtection(
                ExoskeletonItem.getData(stack),
                effectId,
                runtime.poweredModules()
        );

        if (protection >= 1.0D) {
            event.setResult(MobEffectEvent.Applicable.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        MobEffectInstance effect = event.getEffectInstance();

        ItemStack stack = findExoskeleton(entity).orElse(null);
        if (stack == null) {
            return;
        }

        ExoskeletonRuntimeState runtime = getRuntime(stack);
        ResourceLocation effectId = effect.getEffect()
                .unwrapKey()
                .map(key -> key.location())
                .orElse(null);

        if (effectId == null) {
            return;
        }

        effect.mapDuration(duration ->
                StatusProtectionOperations.applyProtection(
                        duration,
                        ExoskeletonItem.getData(stack),
                        effectId,
                        runtime.poweredModules()
                )
        );
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
