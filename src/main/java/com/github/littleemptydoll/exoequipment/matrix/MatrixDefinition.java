package com.github.littleemptydoll.exoequipment.matrix;

import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.registry.EquipmentDefinition;
import com.github.littleemptydoll.exoequipment.registry.EquipmentProperties;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record MatrixDefinition(
        ResourceLocation id,
        EquipmentProperties properties,
        int width,
        int height
) implements EquipmentDefinition {

    public static final Codec<MatrixDefinition> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC
                                    .fieldOf("id")
                                    .forGetter(MatrixDefinition::id),
                            EquipmentProperties.CODEC
                                    .fieldOf("properties")
                                    .forGetter(MatrixDefinition::properties),
                            Codec.INT
                                    .fieldOf("width")
                                    .forGetter(MatrixDefinition::width),
                            Codec.INT
                                    .fieldOf("height")
                                    .forGetter(MatrixDefinition::height)
                    ).apply(
                            instance,
                            MatrixDefinition::new
                    )
            );

    public MatrixDefinition {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Matrix size must be positive");
        }
    }

    public boolean contains(
            ModuleSize size,
            int x,
            int y
    ) {
        return x >= 0
                && y >= 0
                && x + size.width() <= width
                && y + size.height() <= height;
    }
}
