package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record ModuleExtendedProperties(
        Optional<BlinkProperties> blink,
        Optional<FlightProperties> flight,
        Optional<AttributeProperties> attributes,
        Optional<ConditionalAttributeProperties> conditionalAttributes,
        Optional<PickupMagnetProperties> pickupMagnet,
        Optional<TemperatureModifierProperties> temperatureModifier,
        Optional<TemperatureImpactProperties> temperatureImpact,
        Optional<BodyDamageProtectionProperties> bodyDamageProtection,
        Optional<BodyDamageRegenerationProperties> bodyDamageRegeneration,
        Optional<ThirstProperties> thirst,
        Optional<PainkillerProperties> painkiller,
        Optional<BlockScannerProperties> blockScanner
) {
    public static final MapCodec<ModuleExtendedProperties> CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            BlinkProperties.CODEC.optionalFieldOf("blink")
                                    .forGetter(ModuleExtendedProperties::blink),
                            FlightProperties.CODEC.optionalFieldOf("flight")
                                    .forGetter(ModuleExtendedProperties::flight),
                            AttributeProperties.CODEC.optionalFieldOf("attributes")
                                    .forGetter(ModuleExtendedProperties::attributes),
                            ConditionalAttributeProperties.CODEC.optionalFieldOf("conditional_attributes")
                                    .forGetter(ModuleExtendedProperties::conditionalAttributes),
                            PickupMagnetProperties.CODEC.optionalFieldOf("pickup_magnet")
                                    .forGetter(ModuleExtendedProperties::pickupMagnet),
                            TemperatureModifierProperties.CODEC.optionalFieldOf("temperature_modifier")
                                    .forGetter(ModuleExtendedProperties::temperatureModifier),
                            TemperatureImpactProperties.CODEC.optionalFieldOf("temperature_impact")
                                    .forGetter(ModuleExtendedProperties::temperatureImpact),
                            BodyDamageProtectionProperties.CODEC.optionalFieldOf("body_damage_protection")
                                    .forGetter(ModuleExtendedProperties::bodyDamageProtection),
                            BodyDamageRegenerationProperties.CODEC.optionalFieldOf("body_damage_regeneration")
                                    .forGetter(ModuleExtendedProperties::bodyDamageRegeneration),
                            ThirstProperties.CODEC.optionalFieldOf("thirst")
                                    .forGetter(ModuleExtendedProperties::thirst),
                            PainkillerProperties.CODEC.optionalFieldOf("painkiller")
                                    .forGetter(ModuleExtendedProperties::painkiller),
                            BlockScannerProperties.CODEC.optionalFieldOf("block_scanner")
                                    .forGetter(ModuleExtendedProperties::blockScanner)
                    ).apply(instance, ModuleExtendedProperties::new)
            );
}
