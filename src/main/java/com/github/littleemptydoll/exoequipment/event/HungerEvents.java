package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.module.HungerOperations;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
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

        float currentExhaustion =
                player.getFoodData().getExhaustionLevel();

        Float previousExhaustion =
                PREVIOUS_EXHAUSTION.get(player);

        if (previousExhaustion == null) {
            PREVIOUS_EXHAUSTION.put(
                    player,
                    currentExhaustion
            );
            return;
        }

        if (currentExhaustion <= previousExhaustion) {
            PREVIOUS_EXHAUSTION.put(
                    player,
                    currentExhaustion
            );
            return;
        }

        var context = ExoskeletonAccess.findContext(player);

        if (context.isEmpty()) {
            PREVIOUS_EXHAUSTION.put(
                    player,
                    currentExhaustion
            );
            return;
        }

        float reducedExhaustion =
                HungerOperations.applyExhaustionReduction(
                        previousExhaustion,
                        currentExhaustion,
                        context.get().data(),
                        context.get().poweredModules()
                );

        if (reducedExhaustion != currentExhaustion) {
            player.getFoodData().setExhaustion(
                    reducedExhaustion
            );
        }

        PREVIOUS_EXHAUSTION.put(
                player,
                reducedExhaustion
        );
    }
}
