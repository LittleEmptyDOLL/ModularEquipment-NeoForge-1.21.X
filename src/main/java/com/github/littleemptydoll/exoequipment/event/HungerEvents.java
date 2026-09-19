package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.HungerOperations;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class HungerEvents {
    private static final Map<Player, Float> PREVIOUS_EXHAUSTION =
            new WeakHashMap<>();

    private HungerEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }

        float currentExhaustion = player.getFoodData().getExhaustionLevel();

        Float previousExhaustion = PREVIOUS_EXHAUSTION.get(player);

        if (previousExhaustion == null) {
            PREVIOUS_EXHAUSTION.put(player, currentExhaustion);
            return;
        }

        if (currentExhaustion <= previousExhaustion) {
            PREVIOUS_EXHAUSTION.put(player, currentExhaustion);
            return;
        }

        Optional<ItemStack> exoskeletonStack =
                CuriosApi.getCuriosInventory(player)
                        .flatMap(curios ->
                                curios.findFirstCurio(
                                        stack ->
                                                stack.getItem()
                                                        instanceof ExoskeletonItem
                                )
                        )
                        .map(result -> result.stack());

        if (exoskeletonStack.isEmpty()) {
            PREVIOUS_EXHAUSTION.put(player, currentExhaustion);
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

        float reducedExhaustion = HungerOperations.applyExhaustionReduction(
                previousExhaustion,
                currentExhaustion,
                data,
                runtime.poweredModules()
        );

        if (reducedExhaustion != currentExhaustion) {
            player.getFoodData().setExhaustion(reducedExhaustion);
        }

        PREVIOUS_EXHAUSTION.put(player, reducedExhaustion);
    }
}
