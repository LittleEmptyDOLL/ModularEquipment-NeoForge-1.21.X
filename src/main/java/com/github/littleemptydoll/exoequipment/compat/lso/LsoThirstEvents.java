package com.github.littleemptydoll.exoequipment.compat.lso;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import sfiomn.legendarysurvivaloverhaul.api.thirst.ThirstUtil;
import sfiomn.legendarysurvivaloverhaul.common.attachments.thirst.ThirstAttachment;
import sfiomn.legendarysurvivaloverhaul.util.AttachmentUtil;

import java.util.Map;
import java.util.WeakHashMap;

public final class LsoThirstEvents {
    private static final Map<Player, Float> PREVIOUS_EXHAUSTION =
            new WeakHashMap<>();

    private LsoThirstEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }

        if (!ThirstUtil.isThirstActive(player)) {
            PREVIOUS_EXHAUSTION.remove(player);
            return;
        }

        var context = ExoskeletonAccess.findContext(player).orElse(null);

        if (context == null) {
            PREVIOUS_EXHAUSTION.remove(player);
            return;
        }

        ThirstAttachment thirst = AttachmentUtil.getThirstAttachment(player);
        float currentExhaustion = thirst.getThirstExhaustion();

        Float previousExhaustion = PREVIOUS_EXHAUSTION.get(player);

        if (previousExhaustion == null) {
            PREVIOUS_EXHAUSTION.put(player, currentExhaustion);
            return;
        }

        float reducedExhaustion = LsoThirstOperations.applyExhaustionReduction(
                previousExhaustion,
                currentExhaustion,
                context.data(),
                context.poweredModules()
        );

        if (reducedExhaustion != currentExhaustion) {
            thirst.setExhaustion(reducedExhaustion);
        }

        PREVIOUS_EXHAUSTION.put(player, reducedExhaustion);
    }
}
