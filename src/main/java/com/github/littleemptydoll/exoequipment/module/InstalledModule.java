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
        int revivalCooldown,
        int emergencyShieldCooldown
) {
    public static final Codec<InstalledModule> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC.fieldOf("id").forGetter(InstalledModule::id),
                            Codec.INT.fieldOf("x").forGetter(InstalledModule::x),
                            Codec.INT.fieldOf("y").forGetter(InstalledModule::y),
                            Codec.INT.optionalFieldOf("stored_energy", 0).forGetter(InstalledModule::storedEnergy),
                            Codec.DOUBLE.optionalFieldOf("shield_energy", 0.0D).forGetter(InstalledModule::shieldEnergy),
                            Codec.INT.optionalFieldOf("shield_recharge_cooldown", 0).forGetter(InstalledModule::shieldRechargeCooldown),
                            Codec.INT.optionalFieldOf("revival_cooldown", 0).forGetter(InstalledModule::revivalCooldown),
                            Codec.INT.optionalFieldOf("emergency_shield_cooldown", 0).forGetter(InstalledModule::emergencyShieldCooldown)
                    ).apply(instance, InstalledModule::new)
            );

    public InstalledModule(ResourceLocation id, int x, int y, int rotation) {
        this(id, x, y, rotation, 0, 0.0D, 0, 0, 0);
    }

    public InstalledModule(ResourceLocation id, int x, int y, int rotation, int storedEnergy) {
        this(id, x, y, rotation, storedEnergy, 0.0D, 0, 0, 0);
    }

    public InstalledModule {
        rotation = MatrixOperations.normalizeRotation(rotation);
        if (storedEnergy < 0) throw new IllegalArgumentException("Stored energy cannot be negative");
        if (!Double.isFinite(shieldEnergy) || shieldEnergy < 0.0D)
            throw new IllegalArgumentException("Shield energy must be finite and non-negative");
        if (shieldRechargeCooldown < 0)
            throw new IllegalArgumentException("Shield recharge cooldown cannot be negative");
        if (revivalCooldown < 0)
            throw new IllegalArgumentException("Revival cooldown cannot be negative");
        if (emergencyShieldCooldown < 0)
            throw new IllegalArgumentException("Emergency shield cooldown cannot be negative");
    }

    public InstalledModule withStoredEnergy(int value) {
        return new InstalledModule(id, x, y, rotation, value, shieldEnergy, shieldRechargeCooldown, revivalCooldown, emergencyShieldCooldown);
    }

    public InstalledModule withShieldEnergy(double value) {
        return new InstalledModule(id, x, y, rotation, storedEnergy, value, shieldRechargeCooldown, revivalCooldown, emergencyShieldCooldown);
    }

    public InstalledModule withShieldRechargeCooldown(int value) {
        return new InstalledModule(id, x, y, rotation, storedEnergy, shieldEnergy, value, revivalCooldown, emergencyShieldCooldown);
    }

    public InstalledModule withRevivalCooldown(int value) {
        return new InstalledModule(id, x, y, rotation, storedEnergy, shieldEnergy, shieldRechargeCooldown, value, emergencyShieldCooldown);
    }

    public InstalledModule withEmergencyShieldCooldown(int value) {
        return new InstalledModule(id, x, y, rotation, storedEnergy, shieldEnergy, shieldRechargeCooldown, revivalCooldown, value);
    }
}