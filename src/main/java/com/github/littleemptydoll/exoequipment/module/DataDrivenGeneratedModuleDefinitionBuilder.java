package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.registry.EquipmentProperties;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Rarity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class DataDrivenGeneratedModuleDefinitionBuilder {
    private static final long VALUE_SEED_SALT = 0x19C476A2D53EB80FL;

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

    private static final ResourceLocation NIGHT_VISION = id("minecraft", "night_vision");
    private static final ResourceLocation ORES_TAG = id("c", "ores");
    private static final ResourceLocation PLACEHOLDER_DAMAGE_TAG = id("minecraft", "is_projectile");
    private static final ResourceLocation BYPASSES_EFFECTS = id("minecraft", "bypasses_effects");
    private static final ResourceLocation BYPASSES_ENCHANTMENTS = id("minecraft", "bypasses_enchantments");
    private static final ResourceLocation BYPASSES_INVULNERABILITY = id("minecraft", "bypasses_invulnerability");

    private DataDrivenGeneratedModuleDefinitionBuilder() {}

    static ModuleDefinition build(GeneratedModulePlan plan, long seed) {
        RandomSource random = RandomSource.create(seed ^ VALUE_SEED_SALT);
        Accumulator accumulator = new Accumulator(
                plan,
                seed,
                GeneratedModuleGenerator.integrationMultiplier(plan.properties().size())
        );

        for (GeneratedPropertyType property : plan.properties()) {
            GeneratedModuleRules.TierRule rule = GeneratedModuleRules.tierRule(property, plan.tier());
            double quality = GeneratedModuleRules.settings()
                    .rollComplexityScale(plan.properties().size(), random);
            accumulator.apply(property, rule, quality, random);
        }
        return accumulator.build();
    }

    private static final class Accumulator {
        private final GeneratedModulePlan plan;
        private final long seed;
        private final double integration;
        private double passiveEnergy;
        private int priority;

        private final Map<ResourceLocation, AttributeModifierProperties> attributes = new LinkedHashMap<>();
        private final Map<ResourceLocation, Double> damageReductions = new LinkedHashMap<>();
        private final Map<ResourceLocation, Double> damageTagReductions = new LinkedHashMap<>();
        private Optional<Double> defaultDamageReduction = Optional.empty();
        private ConditionalAttributeProperties conditionalAttributes;
        private StorageProperties storage;
        private GenerationProperties generation;
        private double heatGeneration;
        private double cooling;
        private double temperature;
        private double thermalResistance;
        private TemperatureImpactProperties temperatureImpact;
        private ShieldProperties shield;
        private EmergencyShieldProperties emergencyShield;
        private ShieldProtectionProperties shieldProtection;
        private StatusProtectionProperties statusProtection;
        private BodyDamageProtectionProperties bodyDamageProtection;
        private HungerProperties hunger;
        private ThirstProperties thirst;
        private RegenerationProperties regeneration;
        private RevivalProperties revival;
        private BodyDamageRegenerationProperties bodyDamageRegeneration;
        private JetpackProperties jetpack;
        private BlinkProperties blink;
        private FlightProperties flight;
        private EffectsProperties effects;
        private EntityDetectionProperties entityDetection;
        private BlockScannerProperties blockScanner;
        private ThermalVisionProperties thermalVision;
        private PickupMagnetProperties pickupMagnet;
        private CloakingProperties cloaking;
        private LaserDefenseProperties laserDefense;
        private DischargeDefenseProperties dischargeDefense;

        private Accumulator(GeneratedModulePlan plan, long seed, double integration) {
            this.plan = plan;
            this.seed = seed;
            this.integration = integration;
        }

        private void apply(
                GeneratedPropertyType property,
                GeneratedModuleRules.TierRule rule,
                double quality,
                RandomSource random
        ) {
            double q = switch (property) {
                case DAMAGE_IMMUNITY, ELYTRA, NIGHT_VISION -> 1.0D;
                default -> quality;
            };

            if (rule.has("energy")) {
                passiveEnergy += Math.max(0.0D, rule.roll("energy", random) * q);
                priority = Math.max(priority, rule.rollInt("priority", random, 0));
            }

            switch (property) {
                case STORAGE -> storage = new StorageProperties(
                        positiveInt(rule.roll("capacity", random) * q),
                        positiveInt(rule.roll("input", random) * q),
                        positiveInt(rule.roll("output", random) * q)
                );
                case GENERATION -> {
                    generation = new GenerationProperties(positiveInt(rule.roll("generation", random) * q));
                    heatGeneration += positive(rule.roll("heat", random, 0.0D) * q);
                }
                case COOLING -> cooling += positive(rule.roll("cooling", random) * q);
                case HEATING -> heatGeneration += positive(rule.roll("heat", random) * q);
                case PERSONAL_COOLING, PERSONAL_HEATING -> temperature += rule.roll("temperature", random) * q;
                case THERMAL_RESISTANCE -> thermalResistance += positive(rule.roll("thermal_resistance", random) * q);
                case TEMPERATURE_ISOLATION -> temperatureImpact = new TemperatureImpactProperties(
                        clamp01(rule.roll("resistance", random) * q)
                );
                case SHIELD -> shield = new ShieldProperties(
                        Math.max(1, positiveInt(rule.roll("capacity", random) * q)),
                        positive(rule.roll("recharge_rate", random) * q),
                        inverseInt(rule.rollInt("recharge_delay", random), q)
                );
                case GENERAL_DAMAGE_REDUCTION -> {
                    double reduction = clamp01(rule.roll("reduction", random) * q);
                    defaultDamageReduction = Optional.of(Math.max(defaultDamageReduction.orElse(0.0D), reduction));
                    damageTagReductions.put(BYPASSES_EFFECTS, 0.0D);
                    damageTagReductions.put(BYPASSES_ENCHANTMENTS, 0.0D);
                    damageTagReductions.put(BYPASSES_INVULNERABILITY, 0.0D);
                }
                case SPECIALIZED_DAMAGE_REDUCTION -> damageTagReductions.merge(
                        PLACEHOLDER_DAMAGE_TAG,
                        clamp01(rule.roll("reduction", random) * q),
                        Math::max
                );
                case DAMAGE_IMMUNITY -> damageTagReductions.put(
                        PLACEHOLDER_DAMAGE_TAG,
                        clamp01(rule.roll("reduction", random, 1.0D))
                );
                case EMERGENCY_SHIELD -> emergencyShield = new EmergencyShieldProperties(
                        clamp01(rule.roll("restore", random) * q),
                        inverseInt(rule.rollInt("cooldown", random), q)
                );
                case SHIELD_REINFORCEMENT -> shieldProtection = new ShieldProtectionProperties(
                        clamp01(rule.roll("transfer", random) * q)
                );
                case ARMOR -> {
                    mergeAttribute(ARMOR, addValue(rule.roll("armor", random) * q));
                    if (rule.has("toughness")) {
                        mergeAttribute(ARMOR_TOUGHNESS, addValue(rule.roll("toughness", random) * q));
                    }
                }
                case REACTIVE_ARMOR -> {
                    mergeAttribute(ARMOR, addValue(rule.roll("base_armor", random) * q));
                    Map<ResourceLocation, AttributeModifierProperties> conditional = new LinkedHashMap<>();
                    conditional.put(ARMOR, addValue(rule.roll("conditional_armor", random) * q));
                    conditional.put(ARMOR_TOUGHNESS, addValue(rule.roll("conditional_toughness", random) * q));
                    mergeConditional(conditional);
                }
                case FALL_PROTECTION -> {
                    if (rule.has("safe_fall_distance")) {
                        mergeAttribute(SAFE_FALL_DISTANCE, addValue(rule.roll("safe_fall_distance", random) * q));
                    }
                    mergeAttribute(
                            FALL_DAMAGE_MULTIPLIER,
                            multipliedBase(-clamp01(rule.roll("fall_reduction", random) * q))
                    );
                }
                case KNOCKBACK_STABILIZATION -> {
                    double amount = rule.roll("resistance", random) * q;
                    mergeAttribute(KNOCKBACK_RESISTANCE, multipliedBase(amount));
                    mergeAttribute(EXPLOSION_KNOCKBACK_RESISTANCE, multipliedBase(amount));
                }
                case DODGE -> mergeAttribute(DODGE_CHANCE, multipliedBase(rule.roll("amount", random) * q));
                case STATUS_PROTECTION -> statusProtection = new StatusProtectionProperties(
                        clamp01(rule.roll("protection", random) * q),
                        Map.of(),
                        Map.of()
                );
                case BODY_PROTECTION -> bodyDamageProtection = new BodyDamageProtectionProperties(
                        clamp01(rule.roll("chance", random) * q),
                        clamp01(rule.roll("reduction", random) * q),
                        Set.of()
                );
                case MAX_HEALTH -> mergeAttribute(MAX_HEALTH, addValue(rule.roll("amount", random) * q));
                case HUNGER_ASSIST -> hunger = new HungerProperties(clamp01(rule.roll("reduction", random) * q));
                case THIRST_ASSIST -> thirst = new ThirstProperties(clamp01(rule.roll("reduction", random) * q));
                case REGENERATION -> regeneration = new RegenerationProperties(
                        positive(rule.roll("health_per_second", random) * q)
                );
                case REVIVAL -> revival = new RevivalProperties(
                        positive(rule.roll("restore_health", random) * q),
                        inverseInt(rule.rollInt("cooldown", random), q),
                        positiveInt(rule.roll("invulnerability_ticks", random) * q)
                );
                case BODY_REGENERATION -> bodyDamageRegeneration = new BodyDamageRegenerationProperties(
                        positive(rule.roll("health_per_second", random) * q),
                        Set.of()
                );
                case OXYGEN_SUPPORT -> mergeAttribute(OXYGEN_BONUS, multipliedBase(rule.roll("amount", random) * q));
                case MOVEMENT_SPEED -> mergeAttribute(MOVEMENT_SPEED, multipliedBase(rule.roll("amount", random) * q));
                case JUMP_ASSIST -> {
                    mergeAttribute(JUMP_STRENGTH, multipliedBase(rule.roll("jump", random) * q));
                    mergeAttribute(STEP_HEIGHT, addValue(rule.roll("step_height", random) * q));
                }
                case SWIM_ASSIST -> mergeAttribute(
                        WATER_MOVEMENT_EFFICIENCY,
                        addValue(rule.roll("amount", random) * q)
                );
                case JETPACK -> {
                    Optional<ElytraBoostProperties> elytra = Optional.empty();
                    double elytraChance = rule.roll("elytra_chance", random, 0.0D);
                    if (elytraChance > 0.0D && random.nextDouble() < elytraChance) {
                        elytra = Optional.of(new ElytraBoostProperties(
                                positive(rule.roll("elytra_acceleration", random, 0.025D) * q),
                                positive(rule.roll("elytra_max_speed", random, 1.25D) * q)
                        ));
                    }
                    jetpack = new JetpackProperties(
                            Math.max(0.01D, rule.roll("vertical_thrust", random) * q),
                            positive(rule.roll("horizontal_speed", random) * q),
                            activeCost(rule.rollInt("active_energy", random), q),
                            elytra
                    );
                    heatGeneration += positive(rule.roll("heat", random, 0.0D) * q);
                }
                case BLINK -> blink = new BlinkProperties(
                        Math.max(0.1D, rule.roll("distance", random) * q),
                        activeCost(rule.rollInt("activation_energy", random), q),
                        inverseInt(rule.rollInt("cooldown", random), q)
                );
                case ELYTRA -> mergeAttribute(ELYTRA_FLIGHT, addValue(1.0D));
                case FLIGHT -> flight = new FlightProperties(activeCost(rule.rollInt("active_energy", random), q));
                case ATTACK_DAMAGE -> mergeAttribute(ATTACK_DAMAGE, multipliedBase(rule.roll("amount", random) * q));
                case ATTACK_SPEED -> mergeAttribute(ATTACK_SPEED, multipliedBase(rule.roll("amount", random) * q));
                case ATTACK_KNOCKBACK -> mergeAttribute(ATTACK_KNOCKBACK, multipliedBase(rule.roll("amount", random) * q));
                case INTERACTION_RANGE -> mergeAttribute(ENTITY_INTERACTION_RANGE, addValue(rule.roll("amount", random) * q));
                case CRIT_CHANCE -> mergeAttribute(CRIT_CHANCE, multipliedBase(rule.roll("amount", random) * q));
                case CRIT_DAMAGE -> mergeAttribute(CRIT_DAMAGE, multipliedBase(rule.roll("amount", random) * q));
                case CURRENT_HP_DAMAGE -> mergeAttribute(CURRENT_HP_DAMAGE, multipliedBase(rule.roll("amount", random) * q));
                case LIFE_STEAL -> mergeAttribute(LIFE_STEAL, multipliedBase(rule.roll("amount", random) * q));
                case OVERHEAL -> mergeAttribute(OVERHEAL, multipliedBase(rule.roll("amount", random) * q));
                case ARMOR_PIERCE -> {
                    mergeAttribute(ARMOR_PIERCE, multipliedBase(rule.roll("armor", random) * q));
                    mergeAttribute(PROTECTION_PIERCE, multipliedBase(rule.roll("protection", random) * q));
                }
                case ARMOR_SHRED -> {
                    mergeAttribute(ARMOR_SHRED, multipliedBase(rule.roll("armor", random) * q));
                    mergeAttribute(PROTECTION_SHRED, multipliedBase(rule.roll("protection", random) * q));
                }
                case RANGED_DAMAGE -> {
                    mergeAttribute(ARROW_DAMAGE, multipliedBase(rule.roll("damage", random) * q));
                    mergeAttribute(ARROW_VELOCITY, multipliedBase(rule.roll("velocity", random) * q));
                }
                case DRAW_SPEED -> mergeAttribute(DRAW_SPEED, multipliedBase(rule.roll("amount", random) * q));
                case FIRE_DAMAGE -> {
                    mergeAttribute(FIRE_DAMAGE, addValue(rule.roll("damage", random) * q));
                    heatGeneration += positive(rule.roll("heat", random, 0.0D) * q);
                }
                case COLD_DAMAGE -> {
                    mergeAttribute(COLD_DAMAGE, addValue(rule.roll("damage", random) * q));
                    cooling += positive(rule.roll("cooling", random, 0.0D) * q);
                }
                case ADRENALINE -> {
                    Map<ResourceLocation, AttributeModifierProperties> conditional = new LinkedHashMap<>();
                    conditional.put(ATTACK_DAMAGE, multipliedBase(rule.roll("attack_damage", random) * q));
                    conditional.put(ATTACK_SPEED, multipliedBase(rule.roll("attack_speed", random) * q));
                    mergeConditional(conditional);
                }
                case LASER_DEFENSE -> laserDefense = new LaserDefenseProperties(
                        Math.max(0.1D, rule.roll("range", random) * q),
                        (float) Math.max(0.1D, rule.roll("damage", random) * q),
                        inverseInt(rule.rollInt("cooldown", random), q),
                        activeCost(rule.rollInt("energy_cost", random), q),
                        rule.bool("target_players", false),
                        rule.bool("target_hostile", true),
                        rule.bool("target_aggressive", true)
                );
                case DISCHARGE_DEFENSE -> dischargeDefense = new DischargeDefenseProperties(
                        Math.max(0.1D, rule.roll("range", random) * q),
                        Math.max(0.1D, rule.roll("jump_range", random) * q),
                        (float) Math.max(0.1D, rule.roll("damage", random) * q),
                        (float) clamp(rule.roll("falloff", random), 0.01D, 1.0D),
                        Math.max(1, positiveInt(rule.roll("targets", random) * Math.sqrt(q))),
                        positiveInt(rule.roll("bounces", random) * Math.sqrt(q)),
                        inverseInt(rule.rollInt("cooldown", random), q),
                        activeCost(rule.rollInt("energy_cost", random), q),
                        rule.bool("target_players", false),
                        rule.bool("target_hostile", true),
                        rule.bool("target_aggressive", true)
                );
                case NIGHT_VISION -> effects = new EffectsProperties(Map.of(NIGHT_VISION, 0));
                case ENTITY_SENSOR -> {
                    boolean players = rule.bool("players", true);
                    boolean mobs = rule.bool("mobs", true);
                    boolean hostile = rule.bool("hostile", true);
                    if (!players && !mobs && !hostile) hostile = true;
                    entityDetection = new EntityDetectionProperties(
                            Math.max(1.0D, rule.roll("range", random) * q),
                            players,
                            mobs,
                            hostile,
                            activeCost(rule.rollInt("active_energy", random), q)
                    );
                }
                case BLOCK_SCANNER -> blockScanner = new BlockScannerProperties(
                        Math.max(1.0D, rule.roll("range", random) * q),
                        List.of(),
                        List.of(ORES_TAG),
                        activeCost(rule.rollInt("active_energy", random), q),
                        rule.rollInt("color", random, BlockScannerProperties.DEFAULT_COLOR)
                );
                case THERMAL_VISION -> thermalVision = new ThermalVisionProperties(
                        activeCost(rule.rollInt("active_energy", random), q)
                );
                case PICKUP_MAGNET -> pickupMagnet = new PickupMagnetProperties(
                        Math.max(0.1D, rule.roll("radius", random) * q),
                        switch (rule.string("mode", "both").toLowerCase(java.util.Locale.ROOT)) {
                            case "items" -> PickupMagnetProperties.Mode.ITEMS;
                            case "experience" -> PickupMagnetProperties.Mode.EXPERIENCE;
                            default -> PickupMagnetProperties.Mode.BOTH;
                        }
                );
                case CLOAKING -> cloaking = new CloakingProperties(
                        activeCost(rule.rollInt("activation_energy", random), q),
                        activeCost(rule.rollInt("active_energy", random), q),
                        inverseInt(rule.rollInt("cooldown", random), q)
                );
                case MINING_SPEED -> mergeAttribute(BLOCK_BREAK_SPEED, multipliedBase(rule.roll("amount", random) * q));
                case BLOCK_REACH -> mergeAttribute(BLOCK_INTERACTION_RANGE, addValue(rule.roll("amount", random) * q));
                case UNDERWATER_MINING -> mergeAttribute(SUBMERGED_MINING_SPEED, multipliedBase(rule.roll("amount", random) * q));
                case LUCK -> mergeAttribute(LUCK, addValue(rule.roll("amount", random) * q));
                case EXPERIENCE_GAIN -> mergeAttribute(EXPERIENCE_GAINED, multipliedBase(rule.roll("amount", random) * q));
            }
        }

        private void mergeConditional(Map<ResourceLocation, AttributeModifierProperties> values) {
            if (conditionalAttributes == null) {
                conditionalAttributes = new ConditionalAttributeProperties(
                        List.of(new AttributeCondition(AttributeCondition.Type.HEALTH_BELOW, Optional.of(0.30D))),
                        new AttributeProperties(values)
                );
                return;
            }

            Map<ResourceLocation, AttributeModifierProperties> merged = new LinkedHashMap<>(
                    conditionalAttributes.attributes().attributes()
            );
            values.forEach((key, value) -> merged.merge(
                    key,
                    value,
                    DataDrivenGeneratedModuleDefinitionBuilder::mergeModifiers
            ));
            conditionalAttributes = new ConditionalAttributeProperties(
                    conditionalAttributes.conditions(),
                    new AttributeProperties(merged)
            );
        }

        private void mergeAttribute(ResourceLocation key, AttributeModifierProperties value) {
            attributes.merge(key, value, DataDrivenGeneratedModuleDefinitionBuilder::mergeModifiers);
        }

        private int activeCost(int value, double quality) {
            if (value <= 0) return 0;
            return Math.max(1, (int) Math.ceil(value * quality * integration));
        }

        private ModuleDefinition build() {
            ResourceLocation generatedId = GeneratedModuleData.definitionId(plan.tier(), seed);
            Rarity rarity = plan.tier() == EquipmentTier.EXPERIMENTAL ? Rarity.EPIC : Rarity.RARE;
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
            if (heatGeneration > 0.0D || cooling > 0.0D) builder.thermal(new ThermalProperties(heatGeneration, cooling));
            if (temperature != 0.0D || thermalResistance > 0.0D) {
                builder.temperatureModifier(new TemperatureModifierProperties(
                        temperature, 0.0D, 0.0D, thermalResistance
                ));
            }
            if (temperatureImpact != null) builder.temperatureImpact(temperatureImpact);
            if (defaultDamageReduction.isPresent() || !damageReductions.isEmpty() || !damageTagReductions.isEmpty()) {
                builder.damageReduction(new DamageReductionProperties(
                        defaultDamageReduction,
                        damageReductions,
                        damageTagReductions
                ));
            }
            if (shield != null) builder.shield(shield);
            if (emergencyShield != null) builder.emergencyShield(emergencyShield);
            if (shieldProtection != null) builder.shieldProtection(shieldProtection);
            if (statusProtection != null) builder.statusProtection(statusProtection);
            if (bodyDamageProtection != null) builder.bodyDamageProtection(bodyDamageProtection);
            if (hunger != null) builder.hunger(hunger);
            if (thirst != null) builder.thirst(thirst);
            if (regeneration != null) builder.regeneration(regeneration);
            if (revival != null) builder.revival(revival);
            if (bodyDamageRegeneration != null) builder.bodyDamageRegeneration(bodyDamageRegeneration);
            if (!attributes.isEmpty()) builder.attributes(new AttributeProperties(attributes));
            if (conditionalAttributes != null) builder.conditionalAttributes(conditionalAttributes);
            if (jetpack != null) builder.jetpack(jetpack);
            if (blink != null) builder.blink(blink);
            if (flight != null) builder.flight(flight);
            if (effects != null) builder.effects(effects);
            if (entityDetection != null) builder.entityDetection(entityDetection);
            if (blockScanner != null) builder.blockScanner(blockScanner);
            if (thermalVision != null) builder.thermalVision(thermalVision);
            if (pickupMagnet != null) builder.pickupMagnet(pickupMagnet);
            if (cloaking != null) builder.cloaking(cloaking);
            if (laserDefense != null) builder.laserDefense(laserDefense);
            if (dischargeDefense != null) builder.dischargeDefense(dischargeDefense);
            return builder.build();
        }
    }

    private static AttributeModifierProperties addValue(double amount) {
        return new AttributeModifierProperties(amount, AttributeModifier.Operation.ADD_VALUE);
    }

    private static AttributeModifierProperties multipliedBase(double amount) {
        return new AttributeModifierProperties(amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    private static AttributeModifierProperties mergeModifiers(
            AttributeModifierProperties first,
            AttributeModifierProperties second
    ) {
        if (first.operation() != second.operation()) {
            return Math.abs(first.amount()) >= Math.abs(second.amount()) ? first : second;
        }
        return new AttributeModifierProperties(first.amount() + second.amount(), first.operation());
    }

    private static int positiveInt(double value) {
        return Math.max(0, (int) Math.round(value));
    }

    private static int inverseInt(int value, double scale) {
        if (value <= 0) return 0;
        return Math.max(1, (int) Math.round(value / Math.max(0.01D, scale)));
    }

    private static double positive(double value) {
        return Math.max(0.0D, value);
    }

    private static double clamp01(double value) {
        return clamp(value, 0.0D, 1.0D);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
