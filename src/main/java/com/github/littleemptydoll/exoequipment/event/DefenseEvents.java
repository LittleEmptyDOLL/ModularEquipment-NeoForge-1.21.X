package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.module.DefenseOperations;
import com.github.littleemptydoll.exoequipment.module.EmergencyShieldOperations;
import com.github.littleemptydoll.exoequipment.module.ShieldOperations;
import com.github.littleemptydoll.exoequipment.module.ShieldProtectionOperations;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.registry.ModSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

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

        var context = ExoskeletonAccess.findContext(entity).orElse(null);
        if (context == null) {
            return;
        }

        var stack = context.stack();
        var data = context.data();
        var poweredModules = context.poweredModules();

        DamageSource source = event.getSource();
        double incomingDamage = event.getAmount();

        double playerDamageMultiplier =
                DefenseOperations.calculateDamageSourceMultiplier(
                        data,
                        source,
                        poweredModules
                );

        double protectionTransfer =
                ShieldProtectionOperations.calculateTransfer(
                        data,
                        poweredModules
                );

        double shieldDamageMultiplier =
                ShieldProtectionOperations.calculateShieldDamageMultiplier(
                        playerDamageMultiplier,
                        protectionTransfer
                );

        ShieldOperations.ShieldDamageResult shieldResult =
                ShieldOperations.absorbDamage(
                        incomingDamage * shieldDamageMultiplier,
                        data,
                        poweredModules
                );

        data = shieldResult.data();

        if (shieldResult.shieldDestroyed()) {
            EmergencyShieldOperations.EmergencyShieldResult emergencyResult =
                    EmergencyShieldOperations.activate(
                            data,
                            poweredModules
                    );

            data = emergencyResult.data();

            if (emergencyResult.activated()) {
                entity.level().playSound(
                        null,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        SoundEvents.NOTE_BLOCK_PLING,
                        SoundSource.PLAYERS,
                        0.8F,
                        1.6F
                );
            }
        }

        double remainingRawDamage =
                shieldDamageMultiplier <= 0.0D
                        ? incomingDamage
                        : Math.min(
                                incomingDamage,
                                shieldResult.remainingDamage()
                                        / shieldDamageMultiplier
                        );

        double remainingDamage =
                remainingRawDamage * playerDamageMultiplier;

        stack.set(
                ModDataComponents.EXOSKELETON_DATA.get(),
                data
        );

        if (shieldResult.absorbedDamage() > 0.0D) {
            entity.level().playSound(
                    null,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    ModSounds.SHIELD_HIT.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }

        if (remainingDamage <= 0.0D) {
            event.setCanceled(true);
            return;
        }

        event.setAmount((float) remainingDamage);
    }
}
