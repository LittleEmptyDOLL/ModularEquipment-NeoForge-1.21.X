package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record BlockScannerProperties(
        double range,
        List<ResourceLocation> blocks,
        List<ResourceLocation> tags,
        int activeConsumption,
        int color
) {
    public static final int DEFAULT_COLOR = 0x1AE6FF;

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
                                    .forGetter(BlockScannerProperties::tags),
                            Codec.INT
                                    .optionalFieldOf("active_consumption", 0)
                                    .forGetter(BlockScannerProperties::activeConsumption),
                            Codec.INT
                                    .optionalFieldOf("color", DEFAULT_COLOR)
                                    .forGetter(BlockScannerProperties::color)
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
        if (activeConsumption < 0) {
            throw new IllegalArgumentException(
                    "Block scanner active consumption cannot be negative"
            );
        }
        if (color < 0 || color > 0xFFFFFF) {
            throw new IllegalArgumentException("Block scanner color must be a 24-bit RGB value");
        }

        blocks = List.copyOf(blocks);
        tags = List.copyOf(tags);
    }
}
