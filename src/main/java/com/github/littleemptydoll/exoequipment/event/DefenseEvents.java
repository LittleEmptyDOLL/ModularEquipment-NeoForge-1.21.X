package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.DefenseOperations;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;
import java.util.Set;

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
        ExoskeletonItem exoskeleton = (ExoskeletonItem) stack.getItem();

        var data = ExoskeletonItem.getData(stack);
        DamageSource source = event.getSource();

        ResourceLocation damageType =
                source.typeHolder()
                        .unwrapKey()
                        .map(key -> key.location())
                        .orElse(null);

        if (damageType == null) {
            return;
        }

        double multiplier = DefenseOperations.calculateDamageMultiplier(
                data,
                damageType
        );

        if (multiplier >= 1.0D) {
            return;
        }

        event.setAmount(
                (float) Math.max(
                        0.0D,
                        event.getAmount() * multiplier
                )
        );
    }
}
