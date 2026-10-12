package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.module.DefenseOperations;
import com.github.littleemptydoll.exoequipment.module.EmergencyShieldOperations;
import com.github.littleemptydoll.exoequipment.module.ShieldInvulnerabilityOperations;
import com.github.littleemptydoll.exoequipment.module.ShieldOperations;
import com.github.littleemptydoll.exoequipment.module.ShieldProtectionOperations;
import com.github.littleemptydoll.exoequipment.network.ShieldHitPayload;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.registry.ModSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;

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

        double shieldDamage =
                incomingDamage * shieldDamageMultiplier;

        if (ShieldInvulnerabilityOperations.hasAvailableShield(
                data,
                poweredModules
        )) {
            shieldDamage =
                    ShieldInvulnerabilityOperations.filterDamage(
                            entity,
                            source,
                            shieldDamage
                    );
        }

        ShieldOperations.ShieldStatus shieldStatusBefore =
                ShieldOperations.getStatus(data);

        ShieldOperations.ShieldDamageResult shieldResult =
                ShieldOperations.absorbDamage(
                        shieldDamage,
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
            sendShieldHitVisual(
                    entity,
                    source,
                    shieldResult.absorbedDamage(),
                    shieldStatusBefore.capacity()
            );

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

    private static void sendShieldHitVisual(
            LivingEntity entity,
            DamageSource source,
            double absorbedDamage,
            int shieldCapacity
    ) {
        int seed = entity.getId() * 0x5F356495
                ^ entity.tickCount * 0x9E3779B9
                ^ Float.floatToIntBits((float) absorbedDamage);

        Entity directEntity = source.getDirectEntity();
        if (directEntity != null) {
            seed ^= Integer.rotateLeft(directEntity.getId() * 0x45D9F3B, 11);
        }

        Vec3 sourcePosition = source.getSourcePosition();
        float localX = 0.0F;
        float localZ = 0.0F;
        int partIndex = -1;

        if (sourcePosition != null) {
            double dx = sourcePosition.x - entity.getX();
            double dz = sourcePosition.z - entity.getZ();
            double horizontalLength = Math.sqrt((dx * dx) + (dz * dz));

            if (horizontalLength > 1.0E-4D) {
                dx /= horizontalLength;
                dz /= horizontalLength;

                // LivingEntityRenderer rotates humanoid model space by
                // (180 - bodyYaw). Apply the inverse here so the payload's
                // direction uses the same body-local axes as ShieldFieldMesh.
                double angle = Math.toRadians(entity.yBodyRot - 180.0F);
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);

                localX = (float) ((cos * dx) + (sin * dz));
                localZ = (float) ((-sin * dx) + (cos * dz));

                float hitHeight = resolveHitHeight(
                        entity,
                        source,
                        sourcePosition
                );
                partIndex = selectShieldPart(
                        hitHeight,
                        localX,
                        seed
                );
            }
        }

        float energyLoss = shieldCapacity <= 0
                ? 0.0F
                : clamp01((float) (absorbedDamage / shieldCapacity));

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                entity,
                new ShieldHitPayload(
                        entity.getId(),
                        seed,
                        partIndex,
                        localX,
                        localZ,
                        energyLoss
                )
        );
    }

    private static float resolveHitHeight(
            LivingEntity target,
            DamageSource source,
            Vec3 sourcePosition
    ) {
        Entity directEntity = source.getDirectEntity();
        Entity causingEntity = source.getEntity();

        // For direct melee hits the source position is the attacker's entity
        // position, which is anchored at its feet rather than at the actual
        // contact point. Keep melee impacts around the torso instead.
        if (directEntity != null
                && directEntity == causingEntity
                && directEntity instanceof LivingEntity) {
            return 0.58F;
        }

        double height = Math.max(0.001D, target.getBbHeight());
        return clamp01((float) (
                (sourcePosition.y - target.getY()) / height
        ));
    }

    private static int selectShieldPart(
            float hitHeight,
            float localX,
            int seed
    ) {
        // ShieldFieldMesh render order:
        // 0 head, 1 body, 2 right arm, 3 left arm, 4 right leg, 5 left leg.
        if (hitHeight >= 0.78F) {
            return 0;
        }

        if (hitHeight >= 0.43F) {
            if (localX <= -0.68F) {
                return 2;
            }
            if (localX >= 0.68F) {
                return 3;
            }
            return 1;
        }

        if (Math.abs(localX) < 0.16F) {
            return (seed & 1) == 0 ? 4 : 5;
        }
        return localX < 0.0F ? 4 : 5;
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
