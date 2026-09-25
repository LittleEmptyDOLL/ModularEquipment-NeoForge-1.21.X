package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.energy.EnergyOperations;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public final class BlinkOperations {
    private static final double COLLISION_EPSILON = 0.5D;
    private static final double SEARCH_STEP = 0.10D;

    private BlinkOperations() {}

    public static ActivationResult activate(
            ExoskeletonData data,
            ServerPlayer player
    ) {
        for (Target target : collectTargets(data)) {
            InstalledModule module = target.module();

            if (module.abilityCooldown() > 0) {
                continue;
            }

            Vec3 destination =
                    findDestination(
                            player,
                            target.properties().distance()
                    );

            if (destination == null) {
                continue;
            }

            EnergyOperations.EnergyConsumptionResult energyResult =
                    EnergyOperations.consumeEnergy(
                            data,
                            target.properties().activationEnergy()
                    );

            if (!energyResult.sufficient()) {
                continue;
            }

            ExoskeletonData updated =
                    ExoskeletonModules.update(
                            energyResult.data(),
                            target.reference(),
                            module.withAbilityCooldown(
                                    target.properties().cooldown()
                            )
                    );

            player.teleportTo(
                    destination.x,
                    destination.y,
                    destination.z
            );

            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.PLAYERS,
                    0.8F,
                    1.0F
            );

            return new ActivationResult(updated, true);
        }

        return new ActivationResult(data, false);
    }

    public static ExoskeletonData tick(ExoskeletonData data) {
        ExoskeletonData updated = data;

        for (Target target : collectTargets(updated)) {
            InstalledModule module =
                    ExoskeletonModules.get(
                            updated,
                            target.reference()
                    ).orElse(null);

            if (module == null) {
                continue;
            }

            // CloakingOperations already owns cooldown ticking for modules
            // that provide both abilities.
            if (target.definition().cloaking().isPresent()) {
                continue;
            }

            int cooldown =
                    Math.max(
                            0,
                            module.abilityCooldown() - 1
                    );

            if (cooldown != module.abilityCooldown()) {
                updated = ExoskeletonModules.update(
                        updated,
                        target.reference(),
                        module.withAbilityCooldown(cooldown)
                );
            }
        }

        return updated;
    }

    private static Vec3 findDestination(
            ServerPlayer player,
            double distance
    ) {
        Vec3 start = player.getEyePosition();
        Vec3 direction =
                player.getViewVector(1.0F).normalize();
        Vec3 requested =
                start.add(direction.scale(distance));

        BlockHitResult hit = player.level().clip(
                new ClipContext(
                        start,
                        requested,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        player
                )
        );

        // Blink follows the same basic rule as an Ender Pearl:
        // the ray starts at the player's eyes and, when it hits a block,
        // the hit position itself becomes the teleport target.
        //
        // Do not convert the hit position to the player's feet position
        // and do not shift it downward. This is important when the ray
        // hits the ground or a nearby block.
        Vec3 destination =
                hit.getType() == HitResult.Type.BLOCK
                        ? hit.getLocation()
                        : requested;

        double travelled = start.distanceTo(destination);

        // The exact hit position can intersect the player's bounding box.
        // If that happens, move the target back along the same ray until
        // the complete player bounding box fits.
        for (double offset = 0.0D;
             offset <= travelled;
             offset += SEARCH_STEP) {

            double actualOffset =
                    Math.min(offset, travelled);

            Vec3 candidate =
                    destination.subtract(
                            direction.scale(actualOffset)
                    );

            if (isSafe(player, candidate)
                    && candidate.distanceTo(player.position())
                    > COLLISION_EPSILON) {
                return candidate;
            }

            if (actualOffset >= travelled) {
                break;
            }
        }

        // If there is no collision-free position, still allow the blink.
        // Move the destination slightly back from the collision point so that
        // the player is not placed exactly on the block face.
        if (hit.getType() == HitResult.Type.BLOCK) {
            return destination.subtract(
                    direction.scale(COLLISION_EPSILON)
            );
        }

        return null;
    }

    private static boolean isSafe(
            ServerPlayer player,
            Vec3 position
    ) {
        Vec3 delta =
                position.subtract(player.position());

        AABB box =
                player.getBoundingBox().move(delta);

        return player.level().noCollision(player, box);
    }

    private static List<Target> collectTargets(
            ExoskeletonData data
    ) {
        return ExoskeletonModules.activeSupported(data)
                .stream()
                .filter(activeModule ->
                        activeModule.definition()
                                .blink()
                                .isPresent()
                )
                .map(activeModule ->
                        new Target(
                                activeModule.reference(),
                                activeModule.module(),
                                activeModule.definition(),
                                activeModule.definition()
                                        .blink()
                                        .orElseThrow()
                        )
                )
                .sorted(
                        Comparator.comparingInt(
                                        (Target target) ->
                                                target.reference()
                                                        .matrixSlot()
                                )
                                .thenComparingInt(
                                        target ->
                                                target.reference()
                                                        .moduleIndex()
                                )
                )
                .toList();
    }

    public record ActivationResult(
            ExoskeletonData data,
            boolean activated
    ) {}

    private record Target(
            InstalledModuleReference reference,
            InstalledModule module,
            ModuleDefinition definition,
            BlinkProperties properties
    ) {}
}
