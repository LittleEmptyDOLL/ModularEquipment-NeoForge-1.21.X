package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.RegenerationOperations;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class RegenerationEvents {
    private static final int REGENERATION_INTERVAL = 20;

    private RegenerationEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()
                || player.tickCount % REGENERATION_INTERVAL != 0
                || player.isDeadOrDying()
                || player.getHealth() >= player.getMaxHealth()) {
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
        var data = ExoskeletonItem.getData(stack);

        ExoskeletonRuntimeState runtime =
                stack.get(ModDataComponents.EXOSKELETON_RUNTIME.get());

        if (runtime == null) {
            runtime = ExoskeletonRuntimeState.empty();
        }

        double healthPerSecond = RegenerationOperations.calculateHealthPerSecond(
                data,
                runtime.poweredModules()
        );

        if (healthPerSecond <= 0.0D) {
            return;
        }

        player.heal((float) healthPerSecond);
    }
}
