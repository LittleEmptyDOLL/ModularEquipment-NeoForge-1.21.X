package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.DefenseOperations;
import com.github.littleemptydoll.exoequipment.module.ShieldOperations;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import com.github.littleemptydoll.exoequipment.registry.ModSounds;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class DefenseEvents {
    private DefenseEvents() {}

    @SubscribeEvent
    public static void onIncomingDamage(
            LivingIncomingDamageEvent event
    ) {
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
                com.github.littleemptydoll.exoequipment.registry.ModDataComponents
                        .EXOSKELETON_RUNTIME.get()
        );

        if (runtime == null) {
            runtime = ExoskeletonRuntimeState.empty();
        }
        DamageSource source = event.getSource();

        ResourceLocation damageType =
                source.typeHolder()
                        .unwrapKey()
                        .map(key -> key.location())
                        .orElse(null);

        if (damageType == null) {
            return;
        }

        ShieldOperations.ShieldDamageResult shieldResult =
                ShieldOperations.absorbDamage(
                        event.getAmount(),
                        data,
                        runtime
                );

        runtime = shieldResult.runtime();

        double remainingDamage = shieldResult.remainingDamage();

        remainingDamage = DefenseOperations.applyDamageReduction(
                remainingDamage,
                data,
                damageType,
                runtime.poweredModules()
        );

        stack.set(
                com.github.littleemptydoll.exoequipment.registry.ModDataComponents
                        .EXOSKELETON_RUNTIME.get(),
                runtime
        );

        if (remainingDamage <= 0.0D) {
            event.setCanceled(true);

            entity.level().playSound(
                    null,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    ModSounds.SHIELD_HIT.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );

            return;
        }

        event.setAmount((float) remainingDamage);
    }
}
