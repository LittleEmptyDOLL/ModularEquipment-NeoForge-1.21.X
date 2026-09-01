package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.Rarity;

public record EquipmentProperties(
        EquipmentTier tier,
        Rarity rarity
) {
    public static final Codec<EquipmentProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            EquipmentTier.CODEC
                                    .fieldOf("tier")
                                    .forGetter(EquipmentProperties::tier),
                            Rarity.CODEC
                                    .fieldOf("rarity")
                                    .forGetter(EquipmentProperties::rarity)
                    ).apply(
                            instance,
                            EquipmentProperties::new
                    )
            );
}
