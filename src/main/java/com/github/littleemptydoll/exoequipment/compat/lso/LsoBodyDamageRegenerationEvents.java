package com.github.littleemptydoll.exoequipment.compat.lso;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.BodyDamageRegenerationOperations;
import com.github.littleemptydoll.exoequipment.module.BodyPart;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import sfiomn.legendarysurvivaloverhaul.api.bodydamage.BodyDamageUtil;
import sfiomn.legendarysurvivaloverhaul.api.bodydamage.BodyPartEnum;
import sfiomn.legendarysurvivaloverhaul.util.AttachmentUtil;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

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

        Optional<ItemStack> exoskeletonStack = findExoskeleton(player);

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

        boolean healed = false;

        for (BodyPartEnum lsoBodyPart : BodyPartEnum.values()) {
            BodyPart bodyPart = BodyPart.valueOf(lsoBodyPart.name());

            double healthPerSecond =
                    BodyDamageRegenerationOperations.calculateHealthPerSecond(
                            data,
                            bodyPart,
                            runtime.poweredModules()
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

    private static Optional<ItemStack> findExoskeleton(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(curios ->
                        curios.findFirstCurio(
                                stack -> stack.getItem() instanceof ExoskeletonItem
                        )
                )
                .map(result -> result.stack());
    }
}
