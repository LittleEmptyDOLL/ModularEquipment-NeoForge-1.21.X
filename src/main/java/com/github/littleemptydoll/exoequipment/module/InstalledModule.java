package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record InstalledModule(
        ResourceLocation id,
        int x,
        int y,
        int rotation,
        int storedEnergy,
        double shieldEnergy,
        int shieldRechargeCooldown,
        int revivalCooldown,
        int emergencyShieldCooldown,
        boolean active,
        int abilityCooldown,
        boolean flightActive,
        Optional<GeneratedModuleData> generated
) {
    public static final Codec<InstalledModule> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC.fieldOf("id").forGetter(InstalledModule::id),
                            Codec.INT.fieldOf("x").forGetter(InstalledModule::x),
                            Codec.INT.fieldOf("y").forGetter(InstalledModule::y),
                            Codec.INT.fieldOf("rotation").forGetter(InstalledModule::rotation),
                            Codec.INT.optionalFieldOf("stored_energy", 0).forGetter(InstalledModule::storedEnergy),
                            Codec.DOUBLE.optionalFieldOf("shield_energy", 0.0D).forGetter(InstalledModule::shieldEnergy),
                            Codec.INT.optionalFieldOf("shield_recharge_cooldown", 0).forGetter(InstalledModule::shieldRechargeCooldown),
                            Codec.INT.optionalFieldOf("revival_cooldown", 0).forGetter(InstalledModule::revivalCooldown),
                            Codec.INT.optionalFieldOf("emergency_shield_cooldown", 0).forGetter(InstalledModule::emergencyShieldCooldown),
                            Codec.BOOL.optionalFieldOf("active", false).forGetter(InstalledModule::active),
                            Codec.INT.optionalFieldOf("ability_cooldown", 0).forGetter(InstalledModule::abilityCooldown),
                            Codec.BOOL.optionalFieldOf("flight_active", false).forGetter(InstalledModule::flightActive),
                            GeneratedModuleData.CODEC.optionalFieldOf("generated").forGetter(InstalledModule::generated)
                    ).apply(instance, InstalledModule::new)
            );

    public InstalledModule(ResourceLocation id, int x, int y, int rotation) {
        this(id, x, y, rotation, 0, 0.0D, 0, 0, 0, false, 0, false, Optional.empty());
    }

    public InstalledModule(ResourceLocation id, int x, int y, int rotation, int storedEnergy) {
        this(id, x, y, rotation, storedEnergy, 0.0D, 0, 0, 0, false, 0, false, Optional.empty());
    }

    public InstalledModule(
            ResourceLocation id,
            int x,
            int y,
            int rotation,
            int storedEnergy,
            double shieldEnergy,
            int shieldRechargeCooldown,
            int revivalCooldown,
            int emergencyShieldCooldown,
            boolean active,
            int abilityCooldown
    ) {
        this(
                id,
                x,
                y,
                rotation,
                storedEnergy,
                shieldEnergy,
                shieldRechargeCooldown,
                revivalCooldown,
                emergencyShieldCooldown,
                active,
                abilityCooldown,
                false,
                Optional.empty()
        );
    }

    public InstalledModule(
            ResourceLocation id,
            int x,
            int y,
            int rotation,
            int storedEnergy,
            double shieldEnergy,
            int shieldRechargeCooldown,
            int revivalCooldown,
            int emergencyShieldCooldown,
            boolean active,
            int abilityCooldown,
            boolean flightActive
    ) {
        this(
                id,
                x,
                y,
                rotation,
                storedEnergy,
                shieldEnergy,
                shieldRechargeCooldown,
                revivalCooldown,
                emergencyShieldCooldown,
                active,
                abilityCooldown,
                flightActive,
                Optional.empty()
        );
    }

    public InstalledModule {
        rotation = MatrixOperations.normalizeRotation(rotation);
        generated = generated == null ? Optional.empty() : generated;

        if (storedEnergy < 0) throw new IllegalArgumentException("Stored energy cannot be negative");
        if (!Double.isFinite(shieldEnergy) || shieldEnergy < 0.0D)
            throw new IllegalArgumentException("Shield energy must be finite and non-negative");
        if (shieldRechargeCooldown < 0)
            throw new IllegalArgumentException("Shield recharge cooldown cannot be negative");
        if (revivalCooldown < 0)
            throw new IllegalArgumentException("Revival cooldown cannot be negative");
        if (emergencyShieldCooldown < 0)
            throw new IllegalArgumentException("Emergency shield cooldown cannot be negative");
        if (abilityCooldown < 0)
            throw new IllegalArgumentException("Ability cooldown cannot be negative");

        generated.ifPresent(value -> {
            if (!value.definition().id().equals(id)) {
                throw new IllegalArgumentException(
                        "Generated module definition id does not match installed module id"
                );
            }
            ModModules.registerTransientDefinition(value.definition());
        });
    }

    public InstalledModule withStoredEnergy(int value) {
        return new InstalledModule(id, x, y, rotation, value, shieldEnergy, shieldRechargeCooldown, revivalCooldown, emergencyShieldCooldown, active, abilityCooldown, flightActive, generated);
    }

    public InstalledModule withShieldEnergy(double value) {
        return new InstalledModule(id, x, y, rotation, storedEnergy, value, shieldRechargeCooldown, revivalCooldown, emergencyShieldCooldown, active, abilityCooldown, flightActive, generated);
    }

    public InstalledModule withShieldRechargeCooldown(int value) {
        return new InstalledModule(id, x, y, rotation, storedEnergy, shieldEnergy, value, revivalCooldown, emergencyShieldCooldown, active, abilityCooldown, flightActive, generated);
    }

    public InstalledModule withRevivalCooldown(int value) {
        return new InstalledModule(id, x, y, rotation, storedEnergy, shieldEnergy, shieldRechargeCooldown, value, emergencyShieldCooldown, active, abilityCooldown, flightActive, generated);
    }

    public InstalledModule withEmergencyShieldCooldown(int value) {
        return new InstalledModule(id, x, y, rotation, storedEnergy, shieldEnergy, shieldRechargeCooldown, revivalCooldown, value, active, abilityCooldown, flightActive, generated);
    }

    public InstalledModule withActive(boolean value) {
        return new InstalledModule(id, x, y, rotation, storedEnergy, shieldEnergy, shieldRechargeCooldown, revivalCooldown, emergencyShieldCooldown, value, abilityCooldown, flightActive, generated);
    }

    public InstalledModule withAbilityCooldown(int value) {
        return new InstalledModule(id, x, y, rotation, storedEnergy, shieldEnergy, shieldRechargeCooldown, revivalCooldown, emergencyShieldCooldown, active, value, flightActive, generated);
    }

    public InstalledModule withFlightActive(boolean value) {
        return new InstalledModule(id, x, y, rotation, storedEnergy, shieldEnergy, shieldRechargeCooldown, revivalCooldown, emergencyShieldCooldown, active, abilityCooldown, value, generated);
    }

    public InstalledModule withGenerated(GeneratedModuleData value) {
        return new InstalledModule(id, x, y, rotation, storedEnergy, shieldEnergy, shieldRechargeCooldown, revivalCooldown, emergencyShieldCooldown, active, abilityCooldown, flightActive, Optional.ofNullable(value));
    }
}
