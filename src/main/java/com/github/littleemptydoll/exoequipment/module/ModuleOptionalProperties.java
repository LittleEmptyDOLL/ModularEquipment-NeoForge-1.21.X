package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record ModuleOptionalProperties(
        Optional<EnergyProperties> energy,
        Optional<GenerationProperties> generation,
        Optional<StorageProperties> storage,
        Optional<ThermalProperties> thermal,
        Optional<TemperatureProperties> temperature,
        Optional<DamageReductionProperties> damageReduction,
        Optional<ShieldProperties> shield,
        Optional<StatusProtectionProperties> statusProtection,
        Optional<MobilityProperties> mobility,
        Optional<NightVisionProperties> nightVision,
        Optional<EntityDetectionProperties> entityDetection,
        Optional<HungerProperties> hunger,
        Optional<HealthProperties> health,
        Optional<RevivalProperties> revival,
        Optional<RegenerationProperties> regeneration,
        Optional<FallProtectionProperties> fallProtection
) {
    public static final MapCodec<ModuleOptionalProperties> CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            EnergyProperties.CODEC
                                    .optionalFieldOf("energy")
                                    .forGetter(ModuleOptionalProperties::energy),
                            GenerationProperties.CODEC
                                    .optionalFieldOf("generation")
                                    .forGetter(ModuleOptionalProperties::generation),
                            StorageProperties.CODEC
                                    .optionalFieldOf("storage")
                                    .forGetter(ModuleOptionalProperties::storage),
                            ThermalProperties.CODEC
                                    .optionalFieldOf("thermal")
                                    .forGetter(ModuleOptionalProperties::thermal),
                            TemperatureProperties.CODEC
                                    .optionalFieldOf("temperature")
                                    .forGetter(ModuleOptionalProperties::temperature),
                            DamageReductionProperties.CODEC
                                    .optionalFieldOf("damage_reduction")
                                    .forGetter(ModuleOptionalProperties::damageReduction),
                            ShieldProperties.CODEC
                                    .optionalFieldOf("shield")
                                    .forGetter(ModuleOptionalProperties::shield),
                            StatusProtectionProperties.CODEC
                                    .optionalFieldOf("status_protection")
                                    .forGetter(ModuleOptionalProperties::statusProtection),
                            MobilityProperties.CODEC
                                    .optionalFieldOf("mobility")
                                    .forGetter(ModuleOptionalProperties::mobility),
                            NightVisionProperties.CODEC
                                    .optionalFieldOf("night_vision")
                                    .forGetter(ModuleOptionalProperties::nightVision),
                            EntityDetectionProperties.CODEC
                                    .optionalFieldOf("entity_detection")
                                    .forGetter(ModuleOptionalProperties::entityDetection),
                            HungerProperties.CODEC
                                    .optionalFieldOf("hunger")
                                    .forGetter(ModuleOptionalProperties::hunger),
                            HealthProperties.CODEC
                                    .optionalFieldOf("health")
                                    .forGetter(ModuleOptionalProperties::health),
                            RevivalProperties.CODEC
                                    .optionalFieldOf("revival")
                                    .forGetter(ModuleOptionalProperties::revival),
                            RegenerationProperties.CODEC
                                    .optionalFieldOf("regeneration")
                                    .forGetter(ModuleOptionalProperties::regeneration),
                            FallProtectionProperties.CODEC
                                    .optionalFieldOf("fall_protection")
                                    .forGetter(ModuleOptionalProperties::fallProtection)
                    ).apply(instance, ModuleOptionalProperties::new)
            );
}
