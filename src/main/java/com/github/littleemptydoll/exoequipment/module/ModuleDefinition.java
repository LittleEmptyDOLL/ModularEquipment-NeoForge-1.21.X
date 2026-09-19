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
        Optional<HealthProperties> health
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
                            GenerationProperties.CODEC
                                    .optionalFieldOf("generation")
                                    .forGetter(ModuleDefinition::generation),
                            StorageProperties.CODEC
                                    .optionalFieldOf("storage")
                                    .forGetter(ModuleDefinition::storage),
                            ThermalProperties.CODEC
                                    .optionalFieldOf("thermal")
                                    .forGetter(ModuleDefinition::thermal),
                            TemperatureProperties.CODEC
                                    .optionalFieldOf("temperature")
                                    .forGetter(ModuleDefinition::temperature),
                            DamageReductionProperties.CODEC
                                    .optionalFieldOf("damage_reduction")
                                    .forGetter(ModuleDefinition::damageReduction),
                            ShieldProperties.CODEC
                                    .optionalFieldOf("shield")
                                    .forGetter(ModuleDefinition::shield),
                            StatusProtectionProperties.CODEC
                                    .optionalFieldOf("status_protection")
                                    .forGetter(ModuleDefinition::statusProtection),
                            MobilityProperties.CODEC
                                    .optionalFieldOf("mobility")
                                    .forGetter(ModuleDefinition::mobility),
                            NightVisionProperties.CODEC
                                    .optionalFieldOf("night_vision")
                                    .forGetter(ModuleDefinition::nightVision),
                            EntityDetectionProperties.CODEC
                                    .optionalFieldOf("entity_detection")
                                    .forGetter(ModuleDefinition::entityDetection),
                            HungerProperties.CODEC
                                    .optionalFieldOf("hunger")
                                    .forGetter(ModuleDefinition::hunger),
                            HealthProperties.CODEC
                                    .optionalFieldOf("health")
                                    .forGetter(ModuleDefinition::health)
                    ).apply(
                            instance,
                            ModuleDefinition::new
                    )
            );

    public static Builder builder(
            ResourceLocation id,
            EquipmentProperties properties,
            ModuleCategory category,
            ModuleSize size
    ) {
        return new Builder(id, properties, category, size);
    }

    public static final class Builder {
        private final ResourceLocation id;
        private final EquipmentProperties properties;
        private final ModuleCategory category;
        private final ModuleSize size;

        private EnergyProperties energy;
        private GenerationProperties generation;
        private StorageProperties storage;
        private ThermalProperties thermal;
        private TemperatureProperties temperature;
        private DamageReductionProperties damageReduction;
        private ShieldProperties shield;
        private StatusProtectionProperties statusProtection;
        private MobilityProperties mobility;
        private NightVisionProperties nightVision;
        private EntityDetectionProperties entityDetection;
        private HungerProperties hunger;
        private HealthProperties health;

        private Builder(
                ResourceLocation id,
                EquipmentProperties properties,
                ModuleCategory category,
                ModuleSize size
        ) {
            this.id = id;
            this.properties = properties;
            this.category = category;
            this.size = size;
        }

        public Builder energy(EnergyProperties energy) {
            this.energy = energy;
            return this;
        }

        public Builder generation(GenerationProperties generation) {
            this.generation = generation;
            return this;
        }

        public Builder storage(StorageProperties storage) {
            this.storage = storage;
            return this;
        }

        public Builder thermal(ThermalProperties thermal) {
            this.thermal = thermal;
            return this;
        }

        public Builder temperature(TemperatureProperties temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder damageReduction(DamageReductionProperties damageReduction) {
            this.damageReduction = damageReduction;
            return this;
        }

        public Builder shield(ShieldProperties shield) {
            this.shield = shield;
            return this;
        }

        public Builder statusProtection(StatusProtectionProperties statusProtection) {
            this.statusProtection = statusProtection;
            return this;
        }

        public Builder mobility(MobilityProperties mobility) {
            this.mobility = mobility;
            return this;
        }

        public Builder nightVision(NightVisionProperties nightVision) {
            this.nightVision = nightVision;
            return this;
        }

        public Builder entityDetection(EntityDetectionProperties entityDetection) {
            this.entityDetection = entityDetection;
            return this;
        }

        public Builder hunger(HungerProperties hunger) {
            this.hunger = hunger;
            return this;
        }

        public Builder health(HealthProperties health) {
            this.health = health;
            return this;
        }

        public ModuleDefinition build() {
            return new ModuleDefinition(
                    id,
                    properties,
                    category,
                    size,
                    Optional.ofNullable(energy),
                    Optional.ofNullable(generation),
                    Optional.ofNullable(storage),
                    Optional.ofNullable(thermal),
                    Optional.ofNullable(temperature),
                    Optional.ofNullable(damageReduction),
                    Optional.ofNullable(shield),
                    Optional.ofNullable(statusProtection),
                    Optional.ofNullable(mobility),
                    Optional.ofNullable(nightVision),
                    Optional.ofNullable(entityDetection),
                    Optional.ofNullable(hunger),
                    Optional.ofNullable(health)
            );
        }
    }
}
