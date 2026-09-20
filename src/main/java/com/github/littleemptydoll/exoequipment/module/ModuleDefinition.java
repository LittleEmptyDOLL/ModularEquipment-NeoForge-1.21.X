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
        Optional<TemperatureModifierProperties> temperatureModifier,
        Optional<TemperatureImpactProperties> temperatureImpact,
        Optional<DamageReductionProperties> damageReduction,
        Optional<ShieldProperties> shield,
        Optional<StatusProtectionProperties> statusProtection,
        Optional<NightVisionProperties> nightVision,
        Optional<EntityDetectionProperties> entityDetection,
        Optional<HungerProperties> hunger,
        Optional<RevivalProperties> revival,
        Optional<RegenerationProperties> regeneration,
        Optional<FallProtectionProperties> fallProtection,
        Optional<AttributeProperties> attributes,
        Optional<PickupMagnetProperties> pickupMagnet,
        Optional<BodyDamageProtectionProperties> bodyDamageProtection,
        Optional<BodyDamageRegenerationProperties> bodyDamageRegeneration,
        Optional<ThirstProperties> thirst,
        Optional<PainkillerProperties> painkiller
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
                            RecordCodecBuilder.of(
                                    ModuleDefinition::optionalProperties,
                                    ModuleOptionalProperties.CODEC
                            ),
                            AttributeProperties.CODEC
                                    .optionalFieldOf("attributes")
                                    .forGetter(ModuleDefinition::attributes),
                            PickupMagnetProperties.CODEC
                                    .optionalFieldOf("pickup_magnet")
                                    .forGetter(ModuleDefinition::pickupMagnet),
                            TemperatureModifierProperties.CODEC
                                    .optionalFieldOf("temperature_modifier")
                                    .forGetter(ModuleDefinition::temperatureModifier),
                            TemperatureImpactProperties.CODEC
                                    .optionalFieldOf("temperature_impact")
                                    .forGetter(ModuleDefinition::temperatureImpact),
                            BodyDamageProtectionProperties.CODEC
                                    .optionalFieldOf("body_damage_protection")
                                    .forGetter(ModuleDefinition::bodyDamageProtection),
                            BodyDamageRegenerationProperties.CODEC
                                    .optionalFieldOf("body_damage_regeneration")
                                    .forGetter(ModuleDefinition::bodyDamageRegeneration),
                            ThirstProperties.CODEC
                                    .optionalFieldOf("thirst")
                                    .forGetter(ModuleDefinition::thirst),
                            PainkillerProperties.CODEC
                                    .optionalFieldOf("painkiller")
                                    .forGetter(ModuleDefinition::painkiller)
                    ).apply(
                            instance,
                            ModuleDefinition::fromCodec
                    )
            );

    private ModuleOptionalProperties optionalProperties() {
        return new ModuleOptionalProperties(
                energy,
                generation,
                storage,
                thermal,
                temperature,
                damageReduction,
                shield,
                statusProtection,
                nightVision,
                entityDetection,
                hunger,
                revival,
                regeneration,
                fallProtection
        );
    }

    private static ModuleDefinition fromCodec(
            ResourceLocation id,
            EquipmentProperties properties,
            ModuleCategory category,
            ModuleSize size,
            ModuleOptionalProperties optional,
            Optional<AttributeProperties> attributes,
            Optional<PickupMagnetProperties> pickupMagnet,
            Optional<TemperatureModifierProperties> temperatureModifier,
            Optional<TemperatureImpactProperties> temperatureImpact,
            Optional<BodyDamageProtectionProperties> bodyDamageProtection,
            Optional<BodyDamageRegenerationProperties> bodyDamageRegeneration,
            Optional<ThirstProperties> thirst,
            Optional<PainkillerProperties> painkiller
    ) {
        return new ModuleDefinition(
                id,
                properties,
                category,
                size,
                optional.energy(),
                optional.generation(),
                optional.storage(),
                optional.thermal(),
                optional.temperature(),
                temperatureModifier,
                temperatureImpact,
                optional.damageReduction(),
                optional.shield(),
                optional.statusProtection(),
                optional.nightVision(),
                optional.entityDetection(),
                optional.hunger(),
                optional.revival(),
                optional.regeneration(),
                optional.fallProtection(),
                attributes,
                pickupMagnet,
                bodyDamageProtection,
                bodyDamageRegeneration,
                thirst,
                painkiller
        );
    }

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
        private TemperatureModifierProperties temperatureModifier;
        private TemperatureImpactProperties temperatureImpact;
        private DamageReductionProperties damageReduction;
        private ShieldProperties shield;
        private StatusProtectionProperties statusProtection;
        private NightVisionProperties nightVision;
        private EntityDetectionProperties entityDetection;
        private HungerProperties hunger;
        private RevivalProperties revival;
        private RegenerationProperties regeneration;
        private FallProtectionProperties fallProtection;
        private AttributeProperties attributes;
        private PickupMagnetProperties pickupMagnet;
        private BodyDamageProtectionProperties bodyDamageProtection;
        private BodyDamageRegenerationProperties bodyDamageRegeneration;
        private ThirstProperties thirst;
        private PainkillerProperties painkiller;

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

        public Builder temperatureModifier(TemperatureModifierProperties temperatureModifier) {
            this.temperatureModifier = temperatureModifier;
            return this;
        }

        public Builder temperatureImpact(TemperatureImpactProperties temperatureImpact) {
            this.temperatureImpact = temperatureImpact;
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

        public Builder revival(RevivalProperties revival) {
            this.revival = revival;
            return this;
        }

        public Builder regeneration(RegenerationProperties regeneration) {
            this.regeneration = regeneration;
            return this;
        }

        public Builder fallProtection(FallProtectionProperties fallProtection) {
            this.fallProtection = fallProtection;
            return this;
        }

        public Builder attributes(AttributeProperties attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder pickupMagnet(PickupMagnetProperties pickupMagnet) {
            this.pickupMagnet = pickupMagnet;
            return this;
        }

        public Builder bodyDamageProtection(BodyDamageProtectionProperties bodyDamageProtection) {
            this.bodyDamageProtection = bodyDamageProtection;
            return this;
        }

        public Builder bodyDamageRegeneration(BodyDamageRegenerationProperties bodyDamageRegeneration) {
            this.bodyDamageRegeneration = bodyDamageRegeneration;
            return this;
        }

        public Builder thirst(ThirstProperties thirst) {
            this.thirst = thirst;
            return this;
        }

        public Builder painkiller(PainkillerProperties painkiller) {
            this.painkiller = painkiller;
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
                    Optional.ofNullable(temperatureModifier),
                    Optional.ofNullable(temperatureImpact),
                    Optional.ofNullable(damageReduction),
                    Optional.ofNullable(shield),
                    Optional.ofNullable(statusProtection),
                    Optional.ofNullable(nightVision),
                    Optional.ofNullable(entityDetection),
                    Optional.ofNullable(hunger),
                    Optional.ofNullable(revival),
                    Optional.ofNullable(regeneration),
                    Optional.ofNullable(fallProtection),
                    Optional.ofNullable(attributes),
                    Optional.ofNullable(pickupMagnet),
                    Optional.ofNullable(bodyDamageProtection),
                    Optional.ofNullable(bodyDamageRegeneration),
                    Optional.ofNullable(thirst),
                    Optional.ofNullable(painkiller)
            );
        }
    }
}
