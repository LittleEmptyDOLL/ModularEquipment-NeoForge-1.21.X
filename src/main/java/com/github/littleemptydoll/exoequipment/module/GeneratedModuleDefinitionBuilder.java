package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.registry.EquipmentProperties;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Rarity;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class GeneratedModuleDefinitionBuilder {
    private static final long VALUE_SEED_SALT = 0x57E17A9B3D21C4A5L;

    private static final ResourceLocation MOVEMENT_SPEED = id("minecraft", "generic.movement_speed");
    private static final ResourceLocation JUMP_STRENGTH = id("minecraft", "generic.jump_strength");
    private static final ResourceLocation STEP_HEIGHT = id("minecraft", "generic.step_height");
    private static final ResourceLocation WATER_MOVEMENT_EFFICIENCY = id("minecraft", "generic.water_movement_efficiency");
    private static final ResourceLocation ATTACK_DAMAGE = id("minecraft", "generic.attack_damage");
    private static final ResourceLocation ATTACK_SPEED = id("minecraft", "generic.attack_speed");
    private static final ResourceLocation ATTACK_KNOCKBACK = id("minecraft", "generic.attack_knockback");
    private static final ResourceLocation ENTITY_INTERACTION_RANGE = id("minecraft", "generic.entity_interaction_range");
    private static final ResourceLocation MAX_HEALTH = id("minecraft", "generic.max_health");
    private static final ResourceLocation OXYGEN_BONUS = id("minecraft", "generic.oxygen_bonus");
    private static final ResourceLocation ARMOR = id("minecraft", "generic.armor");
    private static final ResourceLocation ARMOR_TOUGHNESS = id("minecraft", "generic.armor_toughness");
    private static final ResourceLocation SAFE_FALL_DISTANCE = id("minecraft", "generic.safe_fall_distance");
    private static final ResourceLocation FALL_DAMAGE_MULTIPLIER = id("minecraft", "generic.fall_damage_multiplier");
    private static final ResourceLocation KNOCKBACK_RESISTANCE = id("minecraft", "generic.knockback_resistance");
    private static final ResourceLocation EXPLOSION_KNOCKBACK_RESISTANCE = id("minecraft", "generic.explosion_knockback_resistance");
    private static final ResourceLocation BLOCK_BREAK_SPEED = id("minecraft", "generic.block_break_speed");
    private static final ResourceLocation BLOCK_INTERACTION_RANGE = id("minecraft", "generic.block_interaction_range");
    private static final ResourceLocation SUBMERGED_MINING_SPEED = id("minecraft", "generic.submerged_mining_speed");
    private static final ResourceLocation LUCK = id("minecraft", "generic.luck");

    private static final ResourceLocation ELYTRA_FLIGHT = id("apothic_attributes", "elytra_flight");
    private static final ResourceLocation DODGE_CHANCE = id("apothic_attributes", "dodge_chance");
    private static final ResourceLocation CRIT_CHANCE = id("apothic_attributes", "crit_chance");
    private static final ResourceLocation CRIT_DAMAGE = id("apothic_attributes", "crit_damage");
    private static final ResourceLocation CURRENT_HP_DAMAGE = id("apothic_attributes", "current_hp_damage");
    private static final ResourceLocation LIFE_STEAL = id("apothic_attributes", "life_steal");
    private static final ResourceLocation OVERHEAL = id("apothic_attributes", "overheal");
    private static final ResourceLocation ARMOR_PIERCE = id("apothic_attributes", "armor_pierce");
    private static final ResourceLocation PROTECTION_PIERCE = id("apothic_attributes", "protection_pierce");
    private static final ResourceLocation ARMOR_SHRED = id("apothic_attributes", "armor_shred");
    private static final ResourceLocation PROTECTION_SHRED = id("apothic_attributes", "protection_shred");
    private static final ResourceLocation ARROW_DAMAGE = id("apothic_attributes", "arrow_damage");
    private static final ResourceLocation ARROW_VELOCITY = id("apothic_attributes", "arrow_velocity");
    private static final ResourceLocation DRAW_SPEED = id("apothic_attributes", "draw_speed");
    private static final ResourceLocation FIRE_DAMAGE = id("apothic_attributes", "fire_damage");
    private static final ResourceLocation COLD_DAMAGE = id("apothic_attributes", "cold_damage");
    private static final ResourceLocation EXPERIENCE_GAINED = id("apothic_attributes", "experience_gained");

    private GeneratedModuleDefinitionBuilder() {}

    public static ModuleDefinition build(
            GeneratedModulePlan plan,
            long seed
    ) {
        RandomSource random = RandomSource.create(seed ^ VALUE_SEED_SALT);
        Accumulator accumulator = new Accumulator(
                plan,
                seed,
                GeneratedModuleGenerator.integrationMultiplier(plan.properties().size())
        );

        for (GeneratedPropertyType property : plan.properties()) {
            ModuleDefinition source = sourceFor(property, plan.tier(), random);
            double scale = rollEffectScale(plan.tier(), source.tier(), random);
            accumulator.apply(property, source, scale);
        }

        return accumulator.build();
    }

    private static ModuleDefinition sourceFor(
            GeneratedPropertyType property,
            EquipmentTier tier,
            RandomSource random
    ) {
        return source(switch (property) {
            case STORAGE -> switch (tier) {
                case ENGINEERING -> "high_rate_battery";
                case MILITARY -> random.nextBoolean() ? "high_capacity_battery" : "discharge_battery";
                case EXPERIMENTAL -> "experimental_battery";
                default -> throw unsupported(tier, property);
            };
            case GENERATION -> byTier(tier, "advanced_generator", "high_output_generator", "experimental_generator");
            case COOLING -> tier == EquipmentTier.EXPERIMENTAL ? "experimental_cooler" : "advanced_cooler";
            case HEATING -> tier == EquipmentTier.EXPERIMENTAL ? "experimental_heater" : "advanced_heater";
            case PERSONAL_COOLING -> "personal_cooler";
            case PERSONAL_HEATING -> "personal_heater";
            case THERMAL_RESISTANCE -> "thermal_protection";
            case TEMPERATURE_ISOLATION -> tier == EquipmentTier.EXPERIMENTAL
                    ? "experimental_thermal_isolation" : "thermal_isolation";
            case SHIELD -> byTier(tier, "engineering_shield", "military_shield", "experimental_shield");
            case GENERAL_DAMAGE_REDUCTION -> tier == EquipmentTier.EXPERIMENTAL
                    ? "experimental_protection" : "military_protection";
            case SPECIALIZED_DAMAGE_REDUCTION -> switch (tier) {
                case ENGINEERING -> "fire_suppression";
                case MILITARY -> random.nextBoolean() ? "ballistic_protection" : "blast_protection";
                case EXPERIMENTAL -> switch (random.nextInt(3)) {
                    case 0 -> "fire_suppression";
                    case 1 -> "ballistic_protection";
                    default -> "blast_protection";
                };
                default -> throw unsupported(tier, property);
            };
            case DAMAGE_IMMUNITY -> switch (random.nextInt(4)) {
                case 0 -> "experimental_fall_immunity";
                case 1 -> "experimental_drowning_immunity";
                case 2 -> "experimental_freezing_immunity";
                default -> "experimental_lightning_immunity";
            };
            case EMERGENCY_SHIELD -> tier == EquipmentTier.EXPERIMENTAL
                    ? "experimental_emergency_shield" : "military_emergency_shield";
            case SHIELD_REINFORCEMENT -> byTier(tier, "engineering_shield_reinforcement",
                    "military_shield_reinforcement", "advanced_shield_reinforcement");
            case ARMOR -> "military_reinforced_plating";
            case REACTIVE_ARMOR -> "military_reactive_armor";
            case FALL_PROTECTION -> tier == EquipmentTier.ENGINEERING
                    ? "impact_protection_system" : "kinetic_stabilization";
            case KNOCKBACK_STABILIZATION -> "kinetic_stabilization";
            case DODGE -> "evasion_system";
            case STATUS_PROTECTION -> switch (tier) {
                case ENGINEERING -> random.nextBoolean() ? "biological_protection" : "mobility_protection";
                case MILITARY -> random.nextBoolean() ? "sensory_protection" : "combat_status_protection";
                case EXPERIMENTAL -> "experimental_status_protection";
                default -> throw unsupported(tier, property);
            };
            case BODY_PROTECTION -> switch (tier) {
                case ENGINEERING -> "engineering_body_protection";
                case MILITARY -> random.nextBoolean() ? "military_vital_protection" : "military_limb_protection";
                case EXPERIMENTAL -> "experimental_body_protection";
                default -> throw unsupported(tier, property);
            };
            case MAX_HEALTH -> tier == EquipmentTier.EXPERIMENTAL
                    ? "experimental_vitality_support" : "combat_vitality_support";
            case HUNGER_ASSIST -> tier == EquipmentTier.EXPERIMENTAL
                    ? "advanced_life_support" : "life_support_system";
            case THIRST_ASSIST -> tier == EquipmentTier.EXPERIMENTAL
                    ? "advanced_life_support" : "life_support_system";
            case REGENERATION -> tier == EquipmentTier.ENGINEERING
                    ? "engineering_regeneration" : "military_combat_regeneration";
            case REVIVAL -> tier == EquipmentTier.EXPERIMENTAL
                    ? "experimental_revival_system" : "emergency_revival_system";
            case BODY_REGENERATION -> tier == EquipmentTier.ENGINEERING
                    ? "medical_recovery_system" : "advanced_recovery_system";
            case OXYGEN_SUPPORT -> "respiratory_support";
            case MOVEMENT_SPEED -> tier == EquipmentTier.EXPERIMENTAL
                    ? "experimental_actuator" : "military_high_power_actuator";
            case JUMP_ASSIST -> tier == EquipmentTier.EXPERIMENTAL
                    ? "experimental_mobility_control" : "engineering_mobility_control";
            case SWIM_ASSIST -> tier == EquipmentTier.ENGINEERING
                    ? "engineering_aquatic_mobility" : "military_swim_assist";
            case JETPACK -> byTier(tier, "engineering_jetpack", "military_jetpack", "experimental_jetpack");
            case BLINK -> byTier(tier, "engineering_blink", "military_blink", "experimental_blink");
            case ELYTRA -> "engineering_elytra_system";
            case FLIGHT -> "experimental_flight";
            case ATTACK_DAMAGE -> switch (tier) {
                case ENGINEERING -> "combat_processor";
                case MILITARY -> "high_power_actuator";
                case EXPERIMENTAL -> "experimental_damage_actuator";
                default -> throw unsupported(tier, property);
            };
            case ATTACK_SPEED -> tier == EquipmentTier.EXPERIMENTAL
                    ? "experimental_attack_servo" : "advanced_attack_servo";
            case ATTACK_KNOCKBACK -> "impact_amplifier";
            case INTERACTION_RANGE -> "long_sword";
            case CRIT_CHANCE -> switch (tier) {
                case ENGINEERING -> "precision_targeting";
                case MILITARY -> "critical_targeting";
                case EXPERIMENTAL -> "critical_strike_system";
                default -> throw unsupported(tier, property);
            };
            case CRIT_DAMAGE -> switch (tier) {
                case ENGINEERING -> "precision_targeting";
                case MILITARY -> "critical_amplifier";
                case EXPERIMENTAL -> "critical_strike_system";
                default -> throw unsupported(tier, property);
            };
            case CURRENT_HP_DAMAGE -> "heavy_target_system";
            case LIFE_STEAL -> tier == EquipmentTier.EXPERIMENTAL ? "predator_system" : "combat_recovery";
            case OVERHEAL -> "predator_system";
            case ARMOR_PIERCE -> tier == EquipmentTier.EXPERIMENTAL ? "adaptive_penetrator" : "armor_penetrator";
            case ARMOR_SHRED -> tier == EquipmentTier.EXPERIMENTAL ? "adaptive_penetrator" : "defense_disruptor";
            case RANGED_DAMAGE -> "marksman_system";
            case DRAW_SPEED -> "draw_speed";
            case FIRE_DAMAGE -> tier == EquipmentTier.EXPERIMENTAL
                    ? "burning_cold_attack_system" : "incendiary_attack_system";
            case COLD_DAMAGE -> tier == EquipmentTier.EXPERIMENTAL
                    ? "burning_cold_attack_system" : "cryogenic_attack_system";
            case ADRENALINE -> "adrenaline";
            case LASER_DEFENSE -> switch (tier) {
                case ENGINEERING -> "rapid_laser_defense";
                case MILITARY -> random.nextBoolean() ? "personal_laser_defense" : "heavy_laser_defense";
                case EXPERIMENTAL -> "experimental_laser_defense";
                default -> throw unsupported(tier, property);
            };
            case DISCHARGE_DEFENSE -> byTier(tier, "crowd_discharge_system",
                    "discharge_defense", "experimental_arc_defense");
            case NIGHT_VISION -> "night_vision";
            case ENTITY_SENSOR -> byTier(tier, "lifeform_scanner", "threat_sensor", "multispectral_sensor");
            case BLOCK_SCANNER -> tier == EquipmentTier.EXPERIMENTAL
                    ? "advanced_resource_scanner" : "resource_scanner";
            case THERMAL_VISION -> "thermal_vision";
            case PICKUP_MAGNET -> "advanced_pickup_magnet";
            case CLOAKING -> tier == EquipmentTier.EXPERIMENTAL ? "experimental_cloaking" : "cloaking_system";
            case MINING_SPEED, BLOCK_REACH -> "advanced_mining_assist";
            case UNDERWATER_MINING -> "underwater_work_system";
            case LUCK -> "fortune_analysis_system";
            case EXPERIENCE_GAIN -> "ancient_knowledge";
        });
    }

    private static String byTier(
            EquipmentTier tier,
            String engineering,
            String military,
            String experimental
    ) {
        return switch (tier) {
            case ENGINEERING -> engineering;
            case MILITARY -> military;
            case EXPERIMENTAL -> experimental;
            default -> throw new IllegalArgumentException("Unsupported generated module tier: " + tier);
        };
    }

    private static IllegalArgumentException unsupported(
            EquipmentTier tier,
            GeneratedPropertyType property
    ) {
        return new IllegalArgumentException(
                "No generated source for " + property + " at tier " + tier
        );
    }

    private static ModuleDefinition source(String path) {
        return ModModules.getDefinition(
                ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, path)
        );
    }

    private static double rollEffectScale(
            EquipmentTier target,
            EquipmentTier source,
            RandomSource random
    ) {
        double min = target == EquipmentTier.EXPERIMENTAL ? 0.90D : 0.90D;
        double max = target == EquipmentTier.EXPERIMENTAL ? 1.35D : 1.20D;
        double roll = min + random.nextDouble() * (max - min);

        if (target != EquipmentTier.EXPERIMENTAL || source == EquipmentTier.EXPERIMENTAL) {
            return roll;
        }
        if (source == EquipmentTier.CIVILIAN) {
            return roll * 2.0D;
        }
        return roll * 1.35D;
    }

    private static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    private static double clamp01(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }

    private static int scaleInt(int value, double scale) {
        return Math.max(0, (int) Math.round(value * scale));
    }

    private static int inverseInt(int value, double scale) {
        if (value <= 0) return 0;
        return Math.max(1, (int) Math.round(value / Math.max(0.01D, scale)));
    }

    private static double energyShare(
            GeneratedPropertyType property,
            ModuleDefinition source
    ) {
        return switch (property) {
            case HUNGER_ASSIST, THIRST_ASSIST,
                 MINING_SPEED, BLOCK_REACH -> 0.5D;
            case ATTACK_DAMAGE -> source.id().getPath().equals("combat_processor") ? 0.5D : 1.0D;
            case CRIT_CHANCE, CRIT_DAMAGE -> source.id().getPath().equals("precision_targeting")
                    || source.id().getPath().equals("critical_strike_system") ? 0.5D : 1.0D;
            default -> 1.0D;
        };
    }

    private static Set<ResourceLocation> attributeKeys(GeneratedPropertyType property) {
        return switch (property) {
            case MOVEMENT_SPEED -> Set.of(MOVEMENT_SPEED);
            case JUMP_ASSIST -> Set.of(JUMP_STRENGTH, STEP_HEIGHT);
            case SWIM_ASSIST -> Set.of(WATER_MOVEMENT_EFFICIENCY);
            case ELYTRA -> Set.of(ELYTRA_FLIGHT);
            case ATTACK_DAMAGE -> Set.of(ATTACK_DAMAGE);
            case ATTACK_SPEED -> Set.of(ATTACK_SPEED);
            case ATTACK_KNOCKBACK -> Set.of(ATTACK_KNOCKBACK);
            case INTERACTION_RANGE -> Set.of(ENTITY_INTERACTION_RANGE);
            case CRIT_CHANCE -> Set.of(CRIT_CHANCE);
            case CRIT_DAMAGE -> Set.of(CRIT_DAMAGE);
            case CURRENT_HP_DAMAGE -> Set.of(CURRENT_HP_DAMAGE);
            case LIFE_STEAL -> Set.of(LIFE_STEAL);
            case OVERHEAL -> Set.of(OVERHEAL);
            case ARMOR_PIERCE -> Set.of(ARMOR_PIERCE, PROTECTION_PIERCE);
            case ARMOR_SHRED -> Set.of(ARMOR_SHRED, PROTECTION_SHRED);
            case RANGED_DAMAGE -> Set.of(ARROW_DAMAGE, ARROW_VELOCITY);
            case DRAW_SPEED -> Set.of(DRAW_SPEED);
            case FIRE_DAMAGE -> Set.of(FIRE_DAMAGE);
            case COLD_DAMAGE -> Set.of(COLD_DAMAGE);
            case MAX_HEALTH -> Set.of(MAX_HEALTH);
            case OXYGEN_SUPPORT -> Set.of(OXYGEN_BONUS);
            case ARMOR -> Set.of(ARMOR, ARMOR_TOUGHNESS);
            case FALL_PROTECTION -> Set.of(SAFE_FALL_DISTANCE, FALL_DAMAGE_MULTIPLIER);
            case KNOCKBACK_STABILIZATION -> Set.of(KNOCKBACK_RESISTANCE, EXPLOSION_KNOCKBACK_RESISTANCE);
            case DODGE -> Set.of(DODGE_CHANCE);
            case MINING_SPEED -> Set.of(BLOCK_BREAK_SPEED);
            case BLOCK_REACH -> Set.of(BLOCK_INTERACTION_RANGE);
            case UNDERWATER_MINING -> Set.of(SUBMERGED_MINING_SPEED);
            case LUCK -> Set.of(LUCK);
            case EXPERIENCE_GAIN -> Set.of(EXPERIENCE_GAINED);
            default -> Set.of();
        };
    }

    private static final class Accumulator {
        private final GeneratedModulePlan plan;
        private final long seed;
        private final double integration;
        private double passiveEnergy;
        private int priority;

        private final Map<ResourceLocation, AttributeModifierProperties> attributes = new LinkedHashMap<>();
        private ConditionalAttributeProperties conditionalAttributes;
        private double heatGeneration;
        private double cooling;
        private double temperature;
        private double heatResistance;
        private double coldResistance;
        private double thermalResistance;
        private TemperatureImpactProperties temperatureImpact;
        private Optional<Double> defaultDamageReduction = Optional.empty();
        private final Map<ResourceLocation, Double> damageReductions = new HashMap<>();
        private final Map<ResourceLocation, Double> damageTagReductions = new HashMap<>();

        private StorageProperties storage;
        private GenerationProperties generation;
        private TemperatureProperties temperatureProperties;
        private ShieldProperties shield;
        private EmergencyShieldProperties emergencyShield;
        private CloakingProperties cloaking;
        private BlinkProperties blink;
        private FlightProperties flight;
        private JetpackProperties jetpack;
        private StatusProtectionProperties statusProtection;
        private EffectsProperties effects;
        private EntityDetectionProperties entityDetection;
        private HungerProperties hunger;
        private RevivalProperties revival;
        private RegenerationProperties regeneration;
        private PickupMagnetProperties pickupMagnet;
        private BodyDamageProtectionProperties bodyDamageProtection;
        private BodyDamageRegenerationProperties bodyDamageRegeneration;
        private ThirstProperties thirst;
        private BlockScannerProperties blockScanner;
        private ShieldProtectionProperties shieldProtection;
        private ThermalVisionProperties thermalVision;
        private LaserDefenseProperties laserDefense;
        private DischargeDefenseProperties dischargeDefense;

        private Accumulator(
                GeneratedModulePlan plan,
                long seed,
                double integration
        ) {
            this.plan = plan;
            this.seed = seed;
            this.integration = integration;
        }

        private void apply(
                GeneratedPropertyType property,
                ModuleDefinition source,
                double scale
        ) {
            source.energy().ifPresent(energy -> {
                passiveEnergy += energy.consumption()
                        * energyShare(property, source)
                        * scale;
                priority = Math.max(priority, energy.priority());
            });

            switch (property) {
                case STORAGE -> source.storage().ifPresent(value -> storage = new StorageProperties(
                        scaleInt(value.capacity(), scale),
                        scaleInt(value.maxInput(), scale),
                        scaleInt(value.maxOutput(), scale)
                ));
                case GENERATION -> {
                    source.generation().ifPresent(value -> generation =
                            new GenerationProperties(scaleInt(value.generation(), scale)));
                    mergeThermal(source, scale);
                    source.temperature().ifPresent(value -> temperatureProperties = value);
                }
                case COOLING, HEATING -> mergeThermal(source, scale);
                case PERSONAL_COOLING, PERSONAL_HEATING, THERMAL_RESISTANCE ->
                        source.temperatureModifier().ifPresent(value -> mergeTemperatureModifier(value, scale));
                case TEMPERATURE_ISOLATION -> source.temperatureImpact().ifPresent(value ->
                        temperatureImpact = new TemperatureImpactProperties(
                                clamp01(value.resistance() * scale)
                        ));
                case SHIELD -> source.shield().ifPresent(value -> shield = new ShieldProperties(
                        Math.max(1, scaleInt(value.capacity(), scale)),
                        value.rechargeRate() * scale,
                        inverseInt(value.rechargeDelay(), scale)
                ));
                case GENERAL_DAMAGE_REDUCTION, SPECIALIZED_DAMAGE_REDUCTION, DAMAGE_IMMUNITY ->
                        source.damageReduction().ifPresent(value -> mergeDamageReduction(value, scale));
                case EMERGENCY_SHIELD -> source.emergencyShield().ifPresent(value ->
                        emergencyShield = new EmergencyShieldProperties(
                                clamp01(value.restore() * scale),
                                inverseInt(value.cooldown(), scale)
                        ));
                case SHIELD_REINFORCEMENT -> source.shieldProtection().ifPresent(value ->
                        shieldProtection = new ShieldProtectionProperties(
                                clamp01(value.transfer() * scale)
                        ));
                case ARMOR, FALL_PROTECTION, KNOCKBACK_STABILIZATION, DODGE,
                     MAX_HEALTH, OXYGEN_SUPPORT, MOVEMENT_SPEED, JUMP_ASSIST,
                     SWIM_ASSIST, ELYTRA, ATTACK_DAMAGE, ATTACK_SPEED,
                     ATTACK_KNOCKBACK, INTERACTION_RANGE, CRIT_CHANCE,
                     CRIT_DAMAGE, CURRENT_HP_DAMAGE, LIFE_STEAL, OVERHEAL,
                     ARMOR_PIERCE, ARMOR_SHRED, RANGED_DAMAGE, DRAW_SPEED,
                     MINING_SPEED, BLOCK_REACH, UNDERWATER_MINING, LUCK,
                     EXPERIENCE_GAIN -> mergeAttributes(source, property, scale);
                case REACTIVE_ARMOR -> {
                    mergeAttributes(source, null, scale);
                    mergeConditional(source, scale);
                }
                case FIRE_DAMAGE, COLD_DAMAGE -> {
                    mergeAttributes(source, property, scale);
                    mergeThermal(source, scale);
                }
                case ADRENALINE -> mergeConditional(source, scale);
                case STATUS_PROTECTION -> source.statusProtection().ifPresent(value ->
                        statusProtection = scaleStatusProtection(value, scale));
                case BODY_PROTECTION -> source.bodyDamageProtection().ifPresent(value ->
                        bodyDamageProtection = new BodyDamageProtectionProperties(
                                clamp01(value.chance() * scale),
                                clamp01(value.damageReduction() * scale),
                                value.bodyParts()
                        ));
                case HUNGER_ASSIST -> source.hunger().ifPresent(value ->
                        hunger = new HungerProperties(
                                clamp01(value.exhaustionReduction() * scale)
                        ));
                case THIRST_ASSIST -> source.thirst().ifPresent(value ->
                        thirst = new ThirstProperties(
                                clamp01(value.exhaustionReduction() * scale)
                        ));
                case REGENERATION -> source.regeneration().ifPresent(value ->
                        regeneration = new RegenerationProperties(
                                value.healthPerSecond() * scale
                        ));
                case REVIVAL -> source.revival().ifPresent(value ->
                        revival = new RevivalProperties(
                                value.restoreHealth() * scale,
                                inverseInt(value.cooldown(), scale),
                                scaleInt(value.invulnerabilityTicks(), scale)
                        ));
                case BODY_REGENERATION -> source.bodyDamageRegeneration().ifPresent(value ->
                        bodyDamageRegeneration = new BodyDamageRegenerationProperties(
                                value.healthPerSecond() * scale,
                                value.bodyParts()
                        ));
                case JETPACK -> source.jetpack().ifPresent(value -> jetpack = new JetpackProperties(
                        value.verticalThrust() * scale,
                        value.horizontalSpeed() * scale,
                        scaleActiveCost(value.energyConsumption(), scale),
                        value.elytra().map(elytra -> new ElytraBoostProperties(
                                elytra.acceleration() * scale,
                                elytra.maxSpeed() * scale
                        ))
                ));
                case BLINK -> source.blink().ifPresent(value -> blink = new BlinkProperties(
                        value.distance() * scale,
                        scaleActiveCost(value.activationEnergy(), scale),
                        inverseInt(value.cooldown(), scale)
                ));
                case FLIGHT -> source.flight().ifPresent(value -> flight = new FlightProperties(
                        scaleActiveCost(value.activeConsumption(), scale)
                ));
                case NIGHT_VISION -> source.effects().ifPresent(value -> effects = value);
                case ENTITY_SENSOR -> source.entityDetection().ifPresent(value ->
                        entityDetection = new EntityDetectionProperties(
                                value.range() * scale,
                                value.players(),
                                value.mobs(),
                                value.hostile(),
                                scaleActiveCost(value.activeConsumption(), scale)
                        ));
                case BLOCK_SCANNER -> source.blockScanner().ifPresent(value ->
                        blockScanner = new BlockScannerProperties(
                                value.range() * scale,
                                value.blocks(),
                                value.tags(),
                                scaleActiveCost(value.activeConsumption(), scale),
                                value.color()
                        ));
                case THERMAL_VISION -> source.thermalVision().ifPresent(value ->
                        thermalVision = new ThermalVisionProperties(
                                scaleActiveCost(value.activeConsumption(), scale)
                        ));
                case PICKUP_MAGNET -> source.pickupMagnet().ifPresent(value ->
                        pickupMagnet = new PickupMagnetProperties(
                                value.radius() * scale,
                                value.mode()
                        ));
                case CLOAKING -> source.cloaking().ifPresent(value -> cloaking = new CloakingProperties(
                        scaleActiveCost(value.activationEnergy(), scale),
                        scaleActiveCost(value.activeConsumption(), scale),
                        inverseInt(value.cooldown(), scale)
                ));
                case LASER_DEFENSE -> source.laserDefense().ifPresent(value ->
                        laserDefense = new LaserDefenseProperties(
                                value.range() * scale,
                                (float) (value.damage() * scale),
                                inverseInt(value.cooldown(), scale),
                                scaleActiveCost(value.energyCost(), scale),
                                value.targetPlayers(), value.targetHostile(), value.targetAggressive()
                        ));
                case DISCHARGE_DEFENSE -> source.dischargeDefense().ifPresent(value ->
                        dischargeDefense = new DischargeDefenseProperties(
                                value.range() * scale,
                                value.jumpRange() * scale,
                                (float) (value.damage() * scale),
                                value.falloff(),
                                Math.max(1, scaleInt(value.targets(), Math.sqrt(scale))),
                                Math.max(0, scaleInt(value.bounces(), Math.sqrt(scale))),
                                inverseInt(value.cooldown(), scale),
                                scaleActiveCost(value.energyCost(), scale),
                                value.targetPlayers(), value.targetHostile(), value.targetAggressive()
                        ));
            }
        }

        private int scaleActiveCost(int value, double scale) {
            if (value <= 0) return 0;
            return Math.max(1, (int) Math.ceil(value * scale * integration));
        }

        private void mergeThermal(ModuleDefinition source, double scale) {
            source.thermal().ifPresent(value -> {
                heatGeneration += value.heatGeneration() * scale;
                cooling += value.cooling() * scale;
            });
        }

        private void mergeTemperatureModifier(
                TemperatureModifierProperties value,
                double scale
        ) {
            temperature += value.temperature() * scale;
            heatResistance += value.heatResistance() * scale;
            coldResistance += value.coldResistance() * scale;
            thermalResistance += value.thermalResistance() * scale;
        }

        private void mergeDamageReduction(
                DamageReductionProperties value,
                double scale
        ) {
            value.defaultReduction().ifPresent(reduction -> {
                double scaled = clamp01(reduction * scale);
                defaultDamageReduction = Optional.of(Math.max(
                        defaultDamageReduction.orElse(0.0D),
                        scaled
                ));
            });
            value.reductions().forEach((key, reduction) ->
                    damageReductions.merge(key, clamp01(reduction * scale), Math::max));
            value.tagReductions().forEach((key, reduction) ->
                    damageTagReductions.merge(key, clamp01(reduction * scale), Math::max));
        }

        private StatusProtectionProperties scaleStatusProtection(
                StatusProtectionProperties value,
                double scale
        ) {
            Map<ResourceLocation, Double> direct = new HashMap<>();
            value.protections().forEach((key, protection) ->
                    direct.put(key, clamp01(protection * scale)));
            Map<ResourceLocation, Double> tags = new HashMap<>();
            value.tagProtections().forEach((key, protection) ->
                    tags.put(key, clamp01(protection * scale)));
            return new StatusProtectionProperties(
                    clamp01(value.harmfulProtection() * scale),
                    direct,
                    tags
            );
        }

        private void mergeAttributes(
                ModuleDefinition source,
                GeneratedPropertyType property,
                double scale
        ) {
            Set<ResourceLocation> filter = property == null
                    ? Set.of()
                    : attributeKeys(property);
            source.attributes().ifPresent(value -> value.attributes().forEach((key, modifier) -> {
                if (!filter.isEmpty() && !filter.contains(key)) {
                    return;
                }
                mergeAttribute(key, modifier, scale);
            }));
        }

        private void mergeConditional(
                ModuleDefinition source,
                double scale
        ) {
            source.conditionalAttributes().ifPresent(value -> {
                Map<ResourceLocation, AttributeModifierProperties> rolled = new LinkedHashMap<>();
                value.attributes().attributes().forEach((key, modifier) ->
                        rolled.put(key, scaleModifier(modifier, scale)));

                if (conditionalAttributes == null) {
                    conditionalAttributes = new ConditionalAttributeProperties(
                            value.conditions(),
                            new AttributeProperties(rolled)
                    );
                    return;
                }

                if (!conditionalAttributes.conditions().equals(value.conditions())) {
                    throw new IllegalStateException(
                            "Generated module selected incompatible conditional attribute groups"
                    );
                }

                Map<ResourceLocation, AttributeModifierProperties> merged =
                        new LinkedHashMap<>(conditionalAttributes.attributes().attributes());
                rolled.forEach((key, modifier) -> merged.merge(
                        key,
                        modifier,
                        GeneratedModuleDefinitionBuilder::mergeModifiers
                ));
                conditionalAttributes = new ConditionalAttributeProperties(
                        value.conditions(),
                        new AttributeProperties(merged)
                );
            });
        }

        private void mergeAttribute(
                ResourceLocation key,
                AttributeModifierProperties modifier,
                double scale
        ) {
            attributes.merge(
                    key,
                    scaleModifier(modifier, scale),
                    GeneratedModuleDefinitionBuilder::mergeModifiers
            );
        }

        private ModuleDefinition build() {
            ResourceLocation generatedId = GeneratedModuleData.definitionId(plan.tier(), seed);
            Rarity rarity = plan.tier() == EquipmentTier.EXPERIMENTAL
                    ? Rarity.EPIC : Rarity.RARE;

            ModuleDefinition.Builder builder = ModuleDefinition.builder(
                    generatedId,
                    new EquipmentProperties(plan.tier(), rarity),
                    ModuleCategory.EXPERIMENTAL,
                    plan.size()
            );

            int finalPassive = (int) Math.ceil(passiveEnergy * integration);
            if (finalPassive > 0) builder.energy(new EnergyProperties(finalPassive, priority));
            if (generation != null) builder.generation(generation);
            if (storage != null) builder.storage(storage);
            if (heatGeneration > 0.0D || cooling > 0.0D)
                builder.thermal(new ThermalProperties(heatGeneration, cooling));
            if (temperatureProperties != null) builder.temperature(temperatureProperties);
            if (temperature != 0.0D || heatResistance > 0.0D
                    || coldResistance > 0.0D || thermalResistance > 0.0D)
                builder.temperatureModifier(new TemperatureModifierProperties(
                        temperature, heatResistance, coldResistance, thermalResistance));
            if (temperatureImpact != null) builder.temperatureImpact(temperatureImpact);
            if (defaultDamageReduction.isPresent() || !damageReductions.isEmpty() || !damageTagReductions.isEmpty())
                builder.damageReduction(new DamageReductionProperties(
                        defaultDamageReduction, damageReductions, damageTagReductions));
            if (shield != null) builder.shield(shield);
            if (emergencyShield != null) builder.emergencyShield(emergencyShield);
            if (cloaking != null) builder.cloaking(cloaking);
            if (blink != null) builder.blink(blink);
            if (flight != null) builder.flight(flight);
            if (jetpack != null) builder.jetpack(jetpack);
            if (statusProtection != null) builder.statusProtection(statusProtection);
            if (effects != null) builder.effects(effects);
            if (entityDetection != null) builder.entityDetection(entityDetection);
            if (hunger != null) builder.hunger(hunger);
            if (revival != null) builder.revival(revival);
            if (regeneration != null) builder.regeneration(regeneration);
            if (!attributes.isEmpty()) builder.attributes(new AttributeProperties(attributes));
            if (conditionalAttributes != null) builder.conditionalAttributes(conditionalAttributes);
            if (pickupMagnet != null) builder.pickupMagnet(pickupMagnet);
            if (bodyDamageProtection != null) builder.bodyDamageProtection(bodyDamageProtection);
            if (bodyDamageRegeneration != null) builder.bodyDamageRegeneration(bodyDamageRegeneration);
            if (thirst != null) builder.thirst(thirst);
            if (blockScanner != null) builder.blockScanner(blockScanner);
            if (shieldProtection != null) builder.shieldProtection(shieldProtection);
            if (thermalVision != null) builder.thermalVision(thermalVision);
            if (laserDefense != null) builder.laserDefense(laserDefense);
            if (dischargeDefense != null) builder.dischargeDefense(dischargeDefense);
            return builder.build();
        }
    }

    private static AttributeModifierProperties scaleModifier(
            AttributeModifierProperties modifier,
            double scale
    ) {
        return new AttributeModifierProperties(
                modifier.amount() * scale,
                modifier.operation()
        );
    }

    private static AttributeModifierProperties mergeModifiers(
            AttributeModifierProperties first,
            AttributeModifierProperties second
    ) {
        if (first.operation() != second.operation()) {
            return Math.abs(first.amount()) >= Math.abs(second.amount()) ? first : second;
        }
        return new AttributeModifierProperties(
                first.amount() + second.amount(),
                first.operation()
        );
    }
}
