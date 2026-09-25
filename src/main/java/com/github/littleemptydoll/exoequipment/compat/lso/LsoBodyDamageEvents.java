package com.github.littleemptydoll.exoequipment.compat.lso;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.module.BodyDamageProtectionOperations;
import com.github.littleemptydoll.exoequipment.module.BodyPart;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import sfiomn.legendarysurvivaloverhaul.api.bodydamage.BodyPartEnum;
import sfiomn.legendarysurvivaloverhaul.util.AttachmentUtil;

import java.util.EnumMap;
import java.util.Map;

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

        if (ExoskeletonAccess.findEquipped(player).isEmpty()) {
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

        var context = ExoskeletonAccess.findContext(player).orElse(null);
        if (context == null) {
            return;
        }

        var data = context.data();
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
                    context.poweredModules()
            );

            double multiplier;

            if (chance >= 1.0D || Math.random() < chance) {
                multiplier = 0.0D;
            } else {
                multiplier = BodyDamageProtectionOperations.calculateDamageMultiplier(
                        data,
                        bodyPart,
                        context.poweredModules()
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
}
