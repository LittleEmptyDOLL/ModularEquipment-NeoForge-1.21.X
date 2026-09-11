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
        int storedEnergy
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
                                    .forGetter(InstalledModule::storedEnergy)
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
        this(id, x, y, rotation, 0);
    }

    public InstalledModule {
        rotation = MatrixOperations.normalizeRotation(rotation);

        if (storedEnergy < 0) {
            throw new IllegalArgumentException("Stored energy cannot be negative");
        }
    }

    public InstalledModule withStoredEnergy(int storedEnergy) {
        return new InstalledModule(id, x, y, rotation, storedEnergy);
    }
}
