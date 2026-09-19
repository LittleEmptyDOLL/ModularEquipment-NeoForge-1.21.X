package com.github.littleemptydoll.exoequipment.compat.lso;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.BodyDamageProtectionOperations;
import com.github.littleemptydoll.exoequipment.module.BodyPart;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import sfiomn.legendarysurvivaloverhaul.api.bodydamage.BodyPartEnum;
import sfiomn.legendarysurvivaloverhaul.util.AttachmentUtil;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public final class LsoBodyDamageEvents {
    private static final ThreadLocal<Map<BodyPartEnum, Float>> BEFORE_DAMAGE =
            new ThreadLocal<>();

    private LsoBodyDamageEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void captureBeforeBodyDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player)
                || player.level().isClientSide()) {
            return;
        }

        Optional<ItemStack> exoskeletonStack = findExoskeleton(player);

        if (exoskeletonStack.isEmpty()) {
            BEFORE_DAMAGE.remove();
            return;
        }

        var bodyDamage = AttachmentUtil.getBodyDamageAttachment(player);
        Map<BodyPartEnum, Float> snapshot = new EnumMap<>(BodyPartEnum.class);

        for (BodyPartEnum bodyPart : BodyPartEnum.values()) {
            snapshot.put(bodyPart, bodyDamage.getBodyPartDamage(bodyPart));
        }

        BEFORE_DAMAGE.set(snapshot);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void applyBodyProtection(LivingDamageEvent.Pre event) {
        Map<BodyPartEnum, Float> before = BEFORE_DAMAGE.get();
        BEFORE_DAMAGE.remove();

        if (before == null
                || !(event.getEntity() instanceof Player player)
                || player.level().isClientSide()) {
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

        var bodyDamage = AttachmentUtil.getBodyDamageAttachment(player);
        boolean changed = false;

        for (BodyPartEnum lsoBodyPart : BodyPartEnum.values()) {
            float oldDamage = before.getOrDefault(lsoBodyPart, 0.0F);
            float currentDamage = bodyDamage.getBodyPartDamage(lsoBodyPart);
            float inflictedDamage = currentDamage - oldDamage;

            if (inflictedDamage <= 0.0F) {
                continue;
            }

            BodyPart bodyPart = BodyPart.valueOf(lsoBodyPart.name());

            double chance = BodyDamageProtectionOperations.calculateChance(
                    data,
                    bodyPart,
                    runtime.poweredModules()
            );

            double multiplier;

            if (chance >= 1.0D || Math.random() < chance) {
                multiplier = 0.0D;
            } else {
                multiplier = BodyDamageProtectionOperations.calculateDamageMultiplier(
                        data,
                        bodyPart,
                        runtime.poweredModules()
                );
            }

            float protectedDamage = (float) (inflictedDamage * multiplier);
            float targetDamage = oldDamage + protectedDamage;

            if (Math.abs(targetDamage - currentDamage) > 0.0001F) {
                bodyDamage.setBodyPartDamage(lsoBodyPart, targetDamage);
                changed = true;
            }
        }

        if (changed) {
            bodyDamage.updateBrokenHearts(player);
            bodyDamage.setManualDirty();
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
