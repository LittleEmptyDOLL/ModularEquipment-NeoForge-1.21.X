package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record InstalledModule(
        ResourceLocation id,
        int x,
        int y,
        int rotation
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
                                    .forGetter(InstalledModule::rotation)
                    ).apply(
                            instance,
                            InstalledModule::new
                    )
            );

    public InstalledModule {
        rotation = MatrixOperations.normalizeRotation(rotation);
    }
}
