package com.github.littleemptydoll.exoequipment.compat.lso;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import sfiomn.legendarysurvivaloverhaul.registry.MobEffectRegistry;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

public final class LsoPainkillerEvents {
    private static final int EFFECT_DURATION = 40;

    private LsoPainkillerEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }

        Optional<ItemStack> exoskeletonStack =
                CuriosApi.getCuriosInventory(player)
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
        ExoskeletonData data = ExoskeletonItem.getData(stack);

        ExoskeletonRuntimeState runtime = stack.get(
                ModDataComponents.EXOSKELETON_RUNTIME.get()
        );

        if (runtime == null) {
            runtime = ExoskeletonRuntimeState.empty();
        }

        if (!LsoPainkillerOperations.hasPainkiller(
                data,
                runtime.poweredModules()
        )) {
            return;
        }

        player.addEffect(
                new MobEffectInstance(
                        MobEffectRegistry.PAINKILLER,
                        EFFECT_DURATION,
                        0,
                        false,
                        false,
                        true
                )
        );
    }
}
