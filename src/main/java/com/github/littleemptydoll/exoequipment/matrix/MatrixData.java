package com.github.littleemptydoll.exoequipment.matrix;

import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record MatrixData(
        ResourceLocation id,
        List<InstalledModule> modules
) {
    public static final Codec<MatrixData> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC
                                    .fieldOf("id")
                                    .forGetter(MatrixData::id),
                            InstalledModule.CODEC
                                    .listOf()
                                    .fieldOf("modules")
                                    .forGetter(MatrixData::modules)
                    ).apply(
                            instance,
                            MatrixData::new
                    )
            );

    public MatrixData {
        modules = List.copyOf(modules);
    }

    public static MatrixData empty(
            ResourceLocation id
    ) {
        return new MatrixData(
                id,
                List.of()
        );
    }

    public boolean isMatrix(
            ResourceLocation id
    ) {
        return this.id.equals(id);
    }
}
