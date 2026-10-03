package com.github.littleemptydoll.exoequipment.energy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Either;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record EnergySystem(
        ResourceLocation definitionId,
        int bufferStored
) {
    public EnergySystem(ResourceLocation definitionId) {
        this(definitionId, 0);
    }

    public EnergySystem {
        if (bufferStored < 0) throw new IllegalArgumentException("Buffer energy cannot be negative");
    }

    private static final Codec<EnergySystem> OBJECT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(EnergySystem::definitionId),
                    Codec.INT.optionalFieldOf("buffer_stored", 0).forGetter(EnergySystem::bufferStored)
            ).apply(instance, EnergySystem::new));

    // Existing saves encoded the system as a bare resource location.
    public static final Codec<EnergySystem> CODEC =
            Codec.either(OBJECT_CODEC, ResourceLocation.CODEC).xmap(
                    value -> value.map(system -> system, EnergySystem::new),
                    system -> Either.left(system));

    public EnergySystem withBufferStored(int amount) {
        return new EnergySystem(definitionId, amount);
    }
}
