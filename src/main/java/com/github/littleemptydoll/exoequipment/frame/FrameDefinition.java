package com.github.littleemptydoll.exoequipment.frame;

import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;

public record FrameDefinition(
        ResourceLocation id,
        EquipmentTier tier,
        Rarity rarity,
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
                            Rarity.CODEC
                                    .fieldOf("rarity")
                                    .forGetter(FrameDefinition::rarity),
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
