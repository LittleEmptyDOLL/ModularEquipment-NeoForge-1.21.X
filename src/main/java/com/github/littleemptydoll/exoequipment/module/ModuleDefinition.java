package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.registry.EquipmentDefinition;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;

import java.util.Optional;

public record ModuleDefinition(
        ResourceLocation id,
        ModuleSize size,
        ModuleCategory category,
        EquipmentTier tier,
        Rarity rarity,
        Optional<EnergyProperties> energy,
        Optional<ThermalProperties> thermal
) implements EquipmentDefinition {
    public static final Codec<ModuleDefinition> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC
                                    .fieldOf("id")
                                    .forGetter(ModuleDefinition::id),
                            ModuleSize.CODEC
                                    .fieldOf("size")
                                    .forGetter(ModuleDefinition::size),
                            ModuleCategory.CODEC
                                    .fieldOf("category")
                                    .forGetter(ModuleDefinition::category),
                            EquipmentTier.CODEC
                                    .fieldOf("tier")
                                    .forGetter(ModuleDefinition::tier),
                            Rarity.CODEC
                                    .fieldOf("rarity")
                                    .forGetter(ModuleDefinition::rarity),
                            EnergyProperties.CODEC
                                    .optionalFieldOf("energy")
                                    .forGetter(ModuleDefinition::energy),
                            ThermalProperties.CODEC
                                    .optionalFieldOf("thermal")
                                    .forGetter(ModuleDefinition::thermal)
                    ).apply(
                            instance,
                            ModuleDefinition::new
                    )
            );

    public ModuleDefinition(
            ResourceLocation id,
            ModuleSize size,
            ModuleCategory category,
            EquipmentTier tier,
            Rarity rarity
    ) {
        this(
                id,
                size,
                category,
                tier,
                rarity,
                Optional.empty(),
                Optional.empty()
        );
    }
}
