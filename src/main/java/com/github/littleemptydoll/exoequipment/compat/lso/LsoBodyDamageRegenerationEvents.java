package com.github.littleemptydoll.exoequipment.compat.lso;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.module.BodyDamageRegenerationOperations;
import com.github.littleemptydoll.exoequipment.module.BodyPart;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import sfiomn.legendarysurvivaloverhaul.api.bodydamage.BodyDamageUtil;
import sfiomn.legendarysurvivaloverhaul.api.bodydamage.BodyPartEnum;
import sfiomn.legendarysurvivaloverhaul.util.AttachmentUtil;

public final class LsoBodyDamageRegenerationEvents {
    private static final int INTERVAL = 20;

    private LsoBodyDamageRegenerationEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()
                || player.tickCount % INTERVAL != 0
                || player.isDeadOrDying()) {
            return;
        }

        var context = ExoskeletonAccess.findContext(player).orElse(null);
        if (context == null) {
            return;
        }

        boolean healed = false;

        for (BodyPartEnum lsoBodyPart : BodyPartEnum.values()) {
            BodyPart bodyPart = BodyPart.valueOf(lsoBodyPart.name());

            double healthPerSecond =
                    BodyDamageRegenerationOperations.calculateHealthPerSecond(
                            context.data(),
                            bodyPart,
                            context.poweredModules()
                    );

            if (healthPerSecond <= 0.0D
                    || BodyDamageUtil.getHealthRatio(player, lsoBodyPart) >= 1.0F) {
                continue;
            }

            BodyDamageUtil.healBodyPart(
                    player,
                    lsoBodyPart,
                    (float) healthPerSecond
            );

            healed = true;
        }

        if (healed) {
            BodyDamageUtil.updatePlayerBrokenHeartAttribute(player);
            AttachmentUtil.getBodyDamageAttachment(player).setManualDirty();
        }
    }
}
