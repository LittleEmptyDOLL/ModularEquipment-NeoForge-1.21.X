package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ShieldState(
        InstalledModuleReference reference,
        double currentEnergy,
        int rechargeCooldown
) {
    public static final Codec<ShieldState> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            InstalledModuleReference.CODEC
                                    .fieldOf("reference")
                                    .forGetter(ShieldState::reference),
                            Codec.DOUBLE
                                    .fieldOf("current_energy")
                                    .forGetter(ShieldState::currentEnergy),
                            Codec.INT
                                    .fieldOf("recharge_cooldown")
                                    .forGetter(ShieldState::rechargeCooldown)
                    ).apply(
                            instance,
                            ShieldState::new
                    )
            );

    public ShieldState {
        if (!Double.isFinite(currentEnergy) || currentEnergy < 0.0D) {
            throw new IllegalArgumentException(
                    "Shield energy must be finite and non-negative"
            );
        }

        if (rechargeCooldown < 0) {
            throw new IllegalArgumentException(
                    "Shield recharge cooldown cannot be negative"
            );
        }
    }

    public ShieldState withCurrentEnergy(double currentEnergy) {
        return new ShieldState(reference, currentEnergy, rechargeCooldown);
    }

    public ShieldState withRechargeCooldown(int rechargeCooldown) {
        return new ShieldState(reference, currentEnergy, rechargeCooldown);
    }
}
