package com.github.littleemptydoll.exoequipment.frame;

import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.registry.EquipmentDefinition;
import com.github.littleemptydoll.exoequipment.registry.EquipmentProperties;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record FrameDefinition(
        ResourceLocation id,
        EquipmentProperties properties,
        ModuleSize maxModuleSize
) implements EquipmentDefinition {
    public static final Codec<FrameDefinition> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC
                                    .fieldOf("id")
                                    .forGetter(FrameDefinition::id),
                            EquipmentProperties.CODEC
                                    .fieldOf("properties")
                                    .forGetter(FrameDefinition::properties),
                            ModuleSize.CODEC
                                    .fieldOf("max_module_size")
                                    .forGetter(FrameDefinition::maxModuleSize)
                    ).apply(
                            instance,
                            FrameDefinition::new
                    )
            );
}
