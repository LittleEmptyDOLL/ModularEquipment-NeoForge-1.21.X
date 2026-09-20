package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record BlockScannerProperties(
        double range,
        List<ResourceLocation> blocks,
        List<ResourceLocation> tags
) {
    public static final Codec<BlockScannerProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .fieldOf("range")
                                    .forGetter(BlockScannerProperties::range),
                            ResourceLocation.CODEC.listOf()
                                    .optionalFieldOf("blocks", List.of())
                                    .forGetter(BlockScannerProperties::blocks),
                            ResourceLocation.CODEC.listOf()
                                    .optionalFieldOf("tags", List.of())
                                    .forGetter(BlockScannerProperties::tags)
                    ).apply(instance, BlockScannerProperties::new)
            );

    public BlockScannerProperties {
        if (!Double.isFinite(range) || range <= 0.0D) {
            throw new IllegalArgumentException(
                    "Block scanner range must be finite and positive"
            );
        }

        if (blocks.isEmpty() && tags.isEmpty()) {
            throw new IllegalArgumentException(
                    "Block scanner must define at least one block or tag"
            );
        }

        blocks = List.copyOf(blocks);
        tags = List.copyOf(tags);
    }
}
