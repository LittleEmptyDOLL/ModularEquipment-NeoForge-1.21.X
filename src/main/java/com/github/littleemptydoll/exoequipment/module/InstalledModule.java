package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record InstalledModule(
        ResourceLocation id,
        int x,
        int y,
        int rotation,
        int storedEnergy,
        double shieldEnergy,
        int shieldRechargeCooldown,
        int revivalCooldown
) {
    public static final Codec<InstalledModule> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC
                                    .fieldOf("id")
                                    .forGetter(InstalledModule::id),

                            Codec.INT.fieldOf("x")
                                    .forGetter(InstalledModule::x),

                            Codec.INT.fieldOf("y")
                                    .forGetter(InstalledModule::y),

                            Codec.INT.fieldOf("rotation")
                                    .forGetter(InstalledModule::rotation),

                            Codec.INT.optionalFieldOf("stored_energy", 0)
                                    .forGetter(InstalledModule::storedEnergy),

                            Codec.DOUBLE.optionalFieldOf("shield_energy", 0.0D)
                                    .forGetter(InstalledModule::shieldEnergy),

                            Codec.INT.optionalFieldOf("shield_recharge_cooldown", 0)
                                    .forGetter(InstalledModule::shieldRechargeCooldown),

                            Codec.INT.optionalFieldOf("revival_cooldown", 0)
                                    .forGetter(InstalledModule::revivalCooldown)
                    ).apply(
                            instance,
                            InstalledModule::new
                    )
            );

    public InstalledModule(
            ResourceLocation id,
            int x,
            int y,
            int rotation
    ) {
        this(id, x, y, rotation, 0, 0.0D, 0, 0);
    }

    public InstalledModule(
            ResourceLocation id,
            int x,
            int y,
            int rotation,
            int storedEnergy
    ) {
        this(id, x, y, rotation, storedEnergy, 0.0D, 0, 0);
    }

    public InstalledModule {
        rotation = MatrixOperations.normalizeRotation(rotation);

        if (storedEnergy < 0) {
            throw new IllegalArgumentException("Stored energy cannot be negative");
        }

        if (!Double.isFinite(shieldEnergy) || shieldEnergy < 0.0D) {
            throw new IllegalArgumentException(
                    "Shield energy must be finite and non-negative"
            );
        }

        if (shieldRechargeCooldown < 0) {
            throw new IllegalArgumentException(
                    "Shield recharge cooldown cannot be negative"
            );
        }

        if (revivalCooldown < 0) {
            throw new IllegalArgumentException(
                    "Revival cooldown cannot be negative"
            );
        }
    }

    public InstalledModule withStoredEnergy(int storedEnergy) {
        return new InstalledModule(
                id,
                x,
                y,
                rotation,
                storedEnergy,
                shieldEnergy,
                shieldRechargeCooldown,
                revivalCooldown
        );
    }

    public InstalledModule withShieldEnergy(double shieldEnergy) {
        return new InstalledModule(
                id,
                x,
                y,
                rotation,
                storedEnergy,
                shieldEnergy,
                shieldRechargeCooldown,
                revivalCooldown
        );
    }

    public InstalledModule withShieldRechargeCooldown(int cooldown) {
        return new InstalledModule(
                id,
                x,
                y,
                rotation,
                storedEnergy,
                shieldEnergy,
                cooldown,
                revivalCooldown
        );
    }

    public InstalledModule withRevivalCooldown(int cooldown) {
        return new InstalledModule(
                id,
                x,
                y,
                rotation,
                storedEnergy,
                shieldEnergy,
                shieldRechargeCooldown,
                cooldown
        );
    }
}
