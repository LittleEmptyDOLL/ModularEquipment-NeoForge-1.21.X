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
        Optional<EmergencyShieldProperties> emergencyShield,
        Optional<CloakingProperties> cloaking,
        Optional<BlinkProperties> blink,
        Optional<FlightProperties> flight,
        Optional<StatusProtectionProperties> statusProtection,
        Optional<NightVisionProperties> nightVision,
        Optional<EntityDetectionProperties> entityDetection,
        Optional<HungerProperties> hunger,
        Optional<RevivalProperties> revival,
        Optional<RegenerationProperties> regeneration,
        Optional<FallProtectionProperties> fallProtection,
        Optional<AttributeProperties> attributes,
        Optional<ConditionalAttributeProperties> conditionalAttributes,
        Optional<PickupMagnetProperties> pickupMagnet,
        Optional<BodyDamageProtectionProperties> bodyDamageProtection,
        Optional<BodyDamageRegenerationProperties> bodyDamageRegeneration,
        Optional<ThirstProperties> thirst,
        Optional<PainkillerProperties> painkiller,
        Optional<BlockScannerProperties> blockScanner
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
                            RecordCodecBuilder.of(
                                    ModuleDefinition::extendedProperties,
                                    ModuleExtendedProperties.CODEC
                            )
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
                emergencyShield,
                cloaking,
                statusProtection,
                nightVision,
                entityDetection,
                hunger,
                revival,
                regeneration,
                fallProtection
        );
    }

    private ModuleExtendedProperties extendedProperties() {
        return new ModuleExtendedProperties(
                blink,
                flight,
                attributes,
                conditionalAttributes,
                pickupMagnet,
                temperatureModifier,
                temperatureImpact,
                bodyDamageProtection,
                bodyDamageRegeneration,
                thirst,
                painkiller,
                blockScanner
        );
    }

    private static ModuleDefinition fromCodec(
            ResourceLocation id,
            EquipmentProperties properties,
            ModuleCategory category,
            ModuleSize size,
            ModuleOptionalProperties optional,
            ModuleExtendedProperties extended
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
                extended.temperatureModifier(),
                extended.temperatureImpact(),
                optional.damageReduction(),
                optional.shield(),
                optional.emergencyShield(),
                optional.cloaking(),
                extended.blink(),
                extended.flight(),
                optional.statusProtection(),
                optional.nightVision(),
                optional.entityDetection(),
                optional.hunger(),
                optional.revival(),
                optional.regeneration(),
                optional.fallProtection(),
                extended.attributes(),
                extended.conditionalAttributes(),
                extended.pickupMagnet(),
                extended.bodyDamageProtection(),
                extended.bodyDamageRegeneration(),
                extended.thirst(),
                extended.painkiller(),
                extended.blockScanner()
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
        private EmergencyShieldProperties emergencyShield;
        private CloakingProperties cloaking;
        private BlinkProperties blink;
        private FlightProperties flight;
        private StatusProtectionProperties statusProtection;
        private NightVisionProperties nightVision;
        private EntityDetectionProperties entityDetection;
        private HungerProperties hunger;
        private RevivalProperties revival;
        private RegenerationProperties regeneration;
        private FallProtectionProperties fallProtection;
        private AttributeProperties attributes;
        private ConditionalAttributeProperties conditionalAttributes;
        private PickupMagnetProperties pickupMagnet;
        private BodyDamageProtectionProperties bodyDamageProtection;
        private BodyDamageRegenerationProperties bodyDamageRegeneration;
        private ThirstProperties thirst;
        private PainkillerProperties painkiller;
        private BlockScannerProperties blockScanner;

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

        public Builder emergencyShield(EmergencyShieldProperties emergencyShield) {
            this.emergencyShield = emergencyShield;
            return this;
        }

        public Builder cloaking(CloakingProperties cloaking) {
            this.cloaking = cloaking;
            return this;
        }

        public Builder blink(BlinkProperties blink) {
            this.blink = blink;
            return this;
        }

        public Builder flight(FlightProperties flight) {
            this.flight = flight;
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

        public Builder conditionalAttributes(ConditionalAttributeProperties conditionalAttributes) {
            this.conditionalAttributes = conditionalAttributes;
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

        public Builder blockScanner(BlockScannerProperties blockScanner) {
            this.blockScanner = blockScanner;
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
                    Optional.ofNullable(emergencyShield),
                    Optional.ofNullable(cloaking),
                    Optional.ofNullable(blink),
                    Optional.ofNullable(flight),
                    Optional.ofNullable(statusProtection),
                    Optional.ofNullable(nightVision),
                    Optional.ofNullable(entityDetection),
                    Optional.ofNullable(hunger),
                    Optional.ofNullable(revival),
                    Optional.ofNullable(regeneration),
                    Optional.ofNullable(fallProtection),
                    Optional.ofNullable(attributes),
                    Optional.ofNullable(conditionalAttributes),
                    Optional.ofNullable(pickupMagnet),
                    Optional.ofNullable(bodyDamageProtection),
                    Optional.ofNullable(bodyDamageRegeneration),
                    Optional.ofNullable(thirst),
                    Optional.ofNullable(painkiller),
                    Optional.ofNullable(blockScanner)
            );
        }
    }
}
