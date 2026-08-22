package com.github.littleemptydoll.exoequipment.frame;

import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record FrameDefinition(
        ResourceLocation id,
        EquipmentTier tier,
        ModuleSize maxModuleSize
) {
    public static final Codec<FrameDefinition> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC
                                    .fieldOf("id")
                                    .forGetter(FrameDefinition::id),
                            EquipmentTier.CODEC
                                    .fieldOf("tier")
                                    .forGetter(FrameDefinition::tier),
                            ModuleSize.CODEC
                                    .fieldOf("max_module_size")
                                    .forGetter(FrameDefinition::maxModuleSize)
                    ).apply(
                            instance,
                            FrameDefinition::new
                    )
            );

    public FrameDefinition {}
}
