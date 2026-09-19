package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.FallProtectionOperations;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class FallProtectionEvents {
    private FallProtectionEvents() {}

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()
                || !event.getSource().is(DamageTypeTags.IS_FALL)) {
            return;
        }

        Optional<ItemStack> exoskeletonStack =
                CuriosApi.getCuriosInventory(entity)
                        .flatMap(curios ->
                                curios.findFirstCurio(
                                        stack -> stack.getItem() instanceof ExoskeletonItem
                                )
                        )
                        .map(result -> result.stack());

        if (exoskeletonStack.isEmpty()) {
            return;
        }

        ItemStack stack = exoskeletonStack.get();
        var data = ExoskeletonItem.getData(stack);

        ExoskeletonRuntimeState runtime =
                stack.get(ModDataComponents.EXOSKELETON_RUNTIME.get());

        if (runtime == null) {
            runtime = ExoskeletonRuntimeState.empty();
        }

        double damage = FallProtectionOperations.applyProtection(
                event.getAmount(),
                data,
                runtime.poweredModules()
        );

        if (damage <= 0.0D) {
            event.setCanceled(true);
        } else {
            event.setAmount((float) damage);
        }
    }
}
