package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.RevivalOperations;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class RevivalEvents {
    private RevivalEvents() {}

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        Optional<ItemStack> exoskeletonStack =
                CuriosApi.getCuriosInventory(entity)
                        .flatMap(curios ->
                                curios.findFirstCurio(
                                        stack ->
                                                stack.getItem()
                                                        instanceof ExoskeletonItem
                                )
                        )
                        .map(result -> result.stack());

        if (exoskeletonStack.isEmpty()) {
            return;
        }

        ItemStack stack = exoskeletonStack.get();
        var data = ExoskeletonItem.getData(stack);

        ExoskeletonRuntimeState runtime = stack.get(
                ModDataComponents.EXOSKELETON_RUNTIME.get()
        );

        if (runtime == null) {
            runtime = ExoskeletonRuntimeState.empty();
        }

        RevivalOperations.RevivalResult result =
                RevivalOperations.tryRevive(
                        data,
                        runtime.poweredModules()
                );

        if (!result.revived()) {
            return;
        }

        entity.setHealth(
                (float) Math.min(
                        result.properties().restoreHealth(),
                        entity.getMaxHealth()
                )
        );

        entity.clearFire();
        entity.fallDistance = 0.0F;

        entity.level().playSound(
                null,
                entity.blockPosition(),
                SoundEvents.TOTEM_USE,
                entity.getSoundSource(),
                1.0F,
                1.0F
        );

        if (result.properties().invulnerabilityTicks() > 0) {
            entity.addEffect(
                    new MobEffectInstance(
                            MobEffects.DAMAGE_RESISTANCE,
                            result.properties().invulnerabilityTicks(),
                            4,
                            false,
                            false,
                            false
                    )
            );
        }

        stack.set(
                ModDataComponents.EXOSKELETON_DATA.get(),
                result.data()
        );

        event.setCanceled(true);
    }
}
