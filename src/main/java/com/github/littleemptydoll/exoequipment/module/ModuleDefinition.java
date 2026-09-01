package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.registry.EquipmentDefinition;
import com.github.littleemptydoll.exoequipment.registry.EquipmentProperties;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record ModuleDefinition(
        ResourceLocation id,
        EquipmentProperties properties,
        ModuleCategory category,
        ModuleSize size,
        Optional<EnergyProperties> energy,
        Optional<ThermalProperties> thermal
) implements EquipmentDefinition {
    public static final Codec<ModuleDefinition> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC
                                    .fieldOf("id")
                                    .forGetter(ModuleDefinition::id),
                            EquipmentProperties.CODEC
                                    .fieldOf("properties")
                                    .forGetter(ModuleDefinition::properties),
                            ModuleCategory.CODEC
                                    .fieldOf("category")
                                    .forGetter(ModuleDefinition::category),
                            ModuleSize.CODEC
                                    .fieldOf("size")
                                    .forGetter(ModuleDefinition::size),
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

    public ModuleDefinition {}
}
