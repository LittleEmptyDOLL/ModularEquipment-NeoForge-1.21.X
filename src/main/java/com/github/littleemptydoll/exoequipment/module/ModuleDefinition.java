package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record ModuleDefinition(
        ResourceLocation id,
        ModuleSize size,
        ModuleCategory category,
        ModuleBranch branch,
        Optional<EnergyProperties> energy,
        Optional<ThermalProperties> thermal
) {
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
                            ModuleBranch.CODEC
                                    .fieldOf("branch")
                                    .forGetter(ModuleDefinition::branch),
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
            ModuleBranch branch
    ) {
        this(
                id,
                size,
                category,
                branch,
                Optional.empty(),
                Optional.empty()
        );
    }
}
