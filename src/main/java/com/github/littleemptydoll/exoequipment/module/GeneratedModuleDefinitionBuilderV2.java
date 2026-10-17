package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.registry.EquipmentProperties;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Rarity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Builds generated modules from ResourceLocation keyed rules and generic descriptors. */
final class GeneratedModuleDefinitionBuilderV2 {
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

    private GeneratedModuleDefinitionBuilderV2() {}

    static ModuleDefinition build(GeneratedModulePropertyPlan plan, long seed) {
        RandomSource random = RandomSource.create(seed ^ VALUE_SEED_SALT);
        Accumulator accumulator = new Accumulator(plan, seed);
        for (ResourceLocation propertyId : plan.properties()) {
            GeneratedModulePropertyRegistry.PropertyRule property =
                    GeneratedModulePropertyRegistry.rule(propertyId);
            GeneratedModulePropertyRegistry.TierRule tierRule = property.tierRule(plan.tier());
            double quality = property.ignoreQuality()
                    ? 1.0D
                    : GeneratedModuleRules.settings().rollComplexityScale(plan.properties().size(), random);
            accumulator.apply(property, tierRule, quality, random);
        }
        return accumulator.build();
    }

    private static final class Accumulator {
        private final GeneratedModulePropertyPlan plan;
        private final long seed;
        private final double integration;
        private double passiveEnergy;
        private int priority;

        private final Map<ResourceLocation, AttributeModifierProperties> attributes = new LinkedHashMap<>();
        private final Map<ResourceLocation, Double> damageReductions = new LinkedHashMap<>();
        private final Map<ResourceLocation, Double> damageTagReductions = new LinkedHashMap<>();
        private Optional<Double> defaultDamageReduction = Optional.empty();
        private final Map<ResourceLocation, Double> statusProtections = new LinkedHashMap<>();
        private final Map<ResourceLocation, Double> statusTagProtections = new LinkedHashMap<>();
        private double harmfulStatusProtection;
        private final Map<ResourceLocation, Integer> effectLevels = new LinkedHashMap<>();
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
        private BodyDamageProtectionProperties bodyDamageProtection;
        private HungerProperties hunger;
        private ThirstProperties thirst;
        private RegenerationProperties regeneration;
        private RevivalProperties revival;
        private BodyDamageRegenerationProperties bodyDamageRegeneration;
        private JetpackProperties jetpack;
        private BlinkProperties blink;
        private FlightProperties flight;
        private EntityDetectionProperties entityDetection;
        private BlockScannerProperties blockScanner;
        private ThermalVisionProperties thermalVision;
        private PickupMagnetProperties pickupMagnet;
        private CloakingProperties cloaking;
        private LaserDefenseProperties laserDefense;
        private DischargeDefenseProperties dischargeDefense;

        private Accumulator(GeneratedModulePropertyPlan plan, long seed) {
            this.plan = plan;
            this.seed = seed;
            this.integration = GeneratedModuleGenerator.integrationMultiplier(plan.properties().size());
        }

        private void apply(
                GeneratedModulePropertyRegistry.PropertyRule property,
                GeneratedModulePropertyRegistry.TierRule rule,
                double quality,
                RandomSource random
        ) {
            if (rule.has("energy")) {
                passiveEnergy += Math.max(0.0D, rule.roll("energy", random) * quality);
                priority = Math.max(priority, rule.rollInt("priority", random, 0));
            }

            boolean appliedGeneric = false;
            for (JsonObject component : property.components()) {
                if (!GeneratedModulePropertyRegistry.entryAvailable(component)) continue;
                if (!passesChance(component, rule, random)) continue;
                applyComponent(component, rule, quality, random);
                appliedGeneric = true;
            }

            if (!property.replaceLegacy()) {
                GeneratedPropertyType legacy = property.legacyType().orElseThrow(() ->
                        new IllegalStateException(
                                "Generated property " + property.id()
                                        + " has neither generic components nor a legacy implementation"
                        )
                );
                applyLegacy(legacy, rule, quality, random);
            } else if (!appliedGeneric && property.components().isEmpty()) {
                throw new IllegalStateException(
                        "Generated property " + property.id() + " replaces legacy behavior but has no components"
                );
            }
        }

        private void applyComponent(
                JsonObject component,
                GeneratedModulePropertyRegistry.TierRule rule,
                double quality,
                RandomSource random
        ) {
            String type = component.get("type").getAsString().toLowerCase(Locale.ROOT);
            if (component.has("extra_energy")) {
                passiveEnergy += Math.max(0.0D, roll(component.get("extra_energy"), rule, random) * quality);
            }
            switch (type) {
                case "attribute" -> applyAttributeTargets(component, rule, quality, random, false);
                case "attributes" -> applyAttributeList(component, rule, quality, random, false);
                case "conditional_attribute" -> applyAttributeTargets(component, rule, quality, random, true);
                case "conditional_attributes" -> applyAttributeList(component, rule, quality, random, true);
                case "effect", "effects" -> applyEffects(component, rule, random);
                case "damage_reduction" -> applyDamageReduction(component, rule, quality, random);
                case "status_protection" -> applyStatusProtection(component, rule, quality, random);
                case "block_scanner" -> applyBlockScanner(component, rule, quality, random);
                case "body_protection" -> applyBodyProtection(component, rule, quality, random);
                case "body_regeneration" -> applyBodyRegeneration(component, rule, quality, random);
                case "entity_sensor" -> applyEntitySensor(component, rule, quality, random);
                case "thermal" -> applyThermal(component, rule, quality, random);
                default -> throw new IllegalArgumentException("Unknown generated component type: " + type);
            }
        }

        private void applyAttributeList(
                JsonObject component,
                GeneratedModulePropertyRegistry.TierRule rule,
                double quality,
                RandomSource random,
                boolean conditional
        ) {
            JsonArray entries = component.getAsJsonArray("attributes");
            if (entries == null) throw new IllegalArgumentException("attributes component requires an attributes array");
            List<AttributeCondition> conditions = conditional
                    ? rollConditions(component, rule, random)
                    : List.of();
            Map<ResourceLocation, AttributeModifierProperties> conditionalValues = new LinkedHashMap<>();
            for (JsonElement element : entries) {
                JsonObject merged = merge(component, element.getAsJsonObject());
                AttributeEntry entry = rollAttribute(merged, rule, quality, random);
                if (entry == null) continue;
                if (conditional) {
                    conditionalValues.merge(entry.id(), entry.modifier(), GeneratedModuleDefinitionBuilderV2::mergeModifiers);
                } else {
                    mergeAttribute(entry.id(), entry.modifier());
                }
            }
            if (conditional && !conditionalValues.isEmpty()) mergeConditional(conditions, conditionalValues);
        }

        private void applyAttributeTargets(
                JsonObject component,
                GeneratedModulePropertyRegistry.TierRule rule,
                double quality,
                RandomSource random,
                boolean conditional
        ) {
            List<JsonObject> targets = expandTargets(component, rule, random);
            List<AttributeCondition> conditions = conditional
                    ? rollConditions(component, rule, random)
                    : List.of();
            Map<ResourceLocation, AttributeModifierProperties> conditionalValues = new LinkedHashMap<>();
            for (JsonObject target : targets) {
                AttributeEntry entry = rollAttribute(target, rule, quality, random);
                if (entry == null) continue;
                if (conditional) {
                    conditionalValues.merge(entry.id(), entry.modifier(), GeneratedModuleDefinitionBuilderV2::mergeModifiers);
                } else {
                    mergeAttribute(entry.id(), entry.modifier());
                }
            }
            if (conditional && !conditionalValues.isEmpty()) mergeConditional(conditions, conditionalValues);
        }

        private AttributeEntry rollAttribute(
                JsonObject object,
                GeneratedModulePropertyRegistry.TierRule rule,
                double quality,
                RandomSource random
        ) {
            if (!numericExists(object, "amount", rule) && !"boolean".equalsIgnoreCase(string(object, "operation", "add_value"))) {
                if (bool(object, "optional", false)) return null;
                throw new IllegalArgumentException("Attribute descriptor is missing amount");
            }
            ResourceLocation attribute = parseRequiredId(object, "id");
            if (!BuiltInRegistries.ATTRIBUTE.containsKey(attribute)) return null;

            String operationName = string(object, "operation", "add_value").toLowerCase(Locale.ROOT);
            boolean booleanOperation = operationName.equals("boolean");
            AttributeModifier.Operation operation = switch (operationName) {
                case "add_value", "boolean" -> AttributeModifier.Operation.ADD_VALUE;
                case "add_multiplied_base" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                case "add_multiplied_total" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
                default -> throw new IllegalArgumentException("Unknown attribute operation: " + operationName);
            };

            double amount;
            if (booleanOperation) {
                amount = bool(object, "value", true) ? 1.0D : 0.0D;
            } else {
                amount = roll(object.get("amount"), rule, random);
                amount *= object.has("multiplier") ? object.get("multiplier").getAsDouble() : 1.0D;
                if (bool(object, "scale_quality", true)) amount *= quality;
            }
            return new AttributeEntry(attribute, new AttributeModifierProperties(amount, operation));
        }

        private void applyEffects(
                JsonObject component,
                GeneratedModulePropertyRegistry.TierRule rule,
                RandomSource random
        ) {
            for (JsonObject target : expandTargets(component, rule, random)) {
                ResourceLocation effect = parseRequiredId(target, "id");
                if (!BuiltInRegistries.MOB_EFFECT.containsKey(effect)) continue;
                int level = Math.max(1, (int) Math.round(roll(target.get("level"), rule, random)));
                effectLevels.merge(effect, level - 1, Math::max);
            }
        }

        private void applyDamageReduction(
                JsonObject component,
                GeneratedModulePropertyRegistry.TierRule rule,
                double quality,
                RandomSource random
        ) {
            for (JsonObject target : expandTargets(component, rule, random)) {
                double amount = clamp01(roll(target.get("amount"), rule, random)
                        * (bool(target, "scale_quality", true) ? quality : 1.0D));
                String targetType = string(target, "target_type", "damage_type").toLowerCase(Locale.ROOT);
                switch (targetType) {
                    case "default" -> defaultDamageReduction = Optional.of(
                            Math.max(defaultDamageReduction.orElse(0.0D), amount)
                    );
                    case "damage_type" -> damageReductions.merge(parseRequiredId(target, "id"), amount, Math::max);
                    case "tag" -> damageTagReductions.merge(parseRequiredId(target, "id"), amount, Math::max);
                    default -> throw new IllegalArgumentException("Unknown damage reduction target_type: " + targetType);
                }
            }
        }

        private void applyStatusProtection(
                JsonObject component,
                GeneratedModulePropertyRegistry.TierRule rule,
                double quality,
                RandomSource random
        ) {
            for (JsonObject target : expandTargets(component, rule, random)) {
                double amount = clamp01(roll(target.get("amount"), rule, random)
                        * (bool(target, "scale_quality", true) ? quality : 1.0D));
                String targetType = string(target, "target_type", "effect").toLowerCase(Locale.ROOT);
                switch (targetType) {
                    case "harmful" -> harmfulStatusProtection = Math.max(harmfulStatusProtection, amount);
                    case "effect" -> statusProtections.merge(parseRequiredId(target, "id"), amount, Math::max);
                    case "tag" -> statusTagProtections.merge(parseRequiredId(target, "id"), amount, Math::max);
                    default -> throw new IllegalArgumentException("Unknown status protection target_type: " + targetType);
                }
            }
        }

        private void applyBlockScanner(
                JsonObject component,
                GeneratedModulePropertyRegistry.TierRule rule,
                double quality,
                RandomSource random
        ) {
            LinkedHashSet<ResourceLocation> blocks = new LinkedHashSet<>();
            LinkedHashSet<ResourceLocation> tags = new LinkedHashSet<>();
            for (JsonObject target : expandTargets(component, rule, random)) {
                ResourceLocation id = parseRequiredId(target, "id");
                String targetType = string(target, "target_type", "block").toLowerCase(Locale.ROOT);
                if (targetType.equals("block")) {
                    if (BuiltInRegistries.BLOCK.containsKey(id)) blocks.add(id);
                } else if (targetType.equals("tag")) {
                    tags.add(id);
                } else {
                    throw new IllegalArgumentException("Unknown scanner target_type: " + targetType);
                }
            }
            if (blocks.isEmpty() && tags.isEmpty()) return;
            double range = Math.max(1.0D, roll(component.get("range"), rule, random) * quality);
            int active = activeCost((int) Math.round(roll(component.get("active_energy"), rule, random)), quality);
            int color = parseColor(component.has("color") ? component.get("color") : null, rule, random);
            blockScanner = new BlockScannerProperties(range, List.copyOf(blocks), List.copyOf(tags), active, color);
        }

        private void applyBodyProtection(
                JsonObject component,
                GeneratedModulePropertyRegistry.TierRule rule,
                double quality,
                RandomSource random
        ) {
            Set<BodyPart> parts = rollBodyParts(component, rule, random);
            bodyDamageProtection = new BodyDamageProtectionProperties(
                    clamp01(roll(component.get("chance"), rule, random) * quality),
                    clamp01(roll(component.get("reduction"), rule, random) * quality),
                    parts
            );
        }

        private void applyBodyRegeneration(
                JsonObject component,
                GeneratedModulePropertyRegistry.TierRule rule,
                double quality,
                RandomSource random
        ) {
            Set<BodyPart> parts = rollBodyParts(component, rule, random);
            bodyDamageRegeneration = new BodyDamageRegenerationProperties(
                    Math.max(0.0D, roll(component.get("health_per_second"), rule, random) * quality),
                    parts
            );
        }

        private Set<BodyPart> rollBodyParts(
                JsonObject component,
                GeneratedModulePropertyRegistry.TierRule rule,
                RandomSource random
        ) {
            LinkedHashSet<BodyPart> parts = new LinkedHashSet<>();
            for (JsonObject target : expandTargets(component, rule, random)) {
                String value = string(target, "id", "all");
                if (value.equalsIgnoreCase("all")) return Set.of();
                parts.add(BodyPart.valueOf(value.toUpperCase(Locale.ROOT)));
            }
            return Set.copyOf(parts);
        }

        private void applyEntitySensor(
                JsonObject component,
                GeneratedModulePropertyRegistry.TierRule rule,
                double quality,
                RandomSource random
        ) {
            boolean players = false;
            boolean mobs = false;
            boolean hostile = false;
            List<JsonObject> targets = expandTargets(component, rule, random);
            if (targets.size() == 1 && !targets.get(0).has("id")) {
                players = bool(component, "players", true);
                mobs = bool(component, "mobs", true);
                hostile = bool(component, "hostile", true);
            } else {
                for (JsonObject target : targets) {
                    switch (string(target, "id", "hostile").toLowerCase(Locale.ROOT)) {
                        case "players" -> players = true;
                        case "mobs" -> mobs = true;
                        case "hostile" -> hostile = true;
                        default -> throw new IllegalArgumentException("Unknown entity sensor target: " + target);
                    }
                }
            }
            if (!players && !mobs && !hostile) hostile = true;
            entityDetection = new EntityDetectionProperties(
                    Math.max(1.0D, roll(component.get("range"), rule, random) * quality),
                    players,
                    mobs,
                    hostile,
                    activeCost((int) Math.round(roll(component.get("active_energy"), rule, random)), quality)
            );
        }

        private void applyThermal(
                JsonObject component,
                GeneratedModulePropertyRegistry.TierRule rule,
                double quality,
                RandomSource random
        ) {
            double amount = Math.max(0.0D, roll(component.get("amount"), rule, random) * quality);
            switch (string(component, "mode", "heat").toLowerCase(Locale.ROOT)) {
                case "heat", "heating" -> heatGeneration += amount;
                case "cooling", "cool" -> cooling += amount;
                default -> throw new IllegalArgumentException("Unknown thermal mode");
            }
        }

        private List<JsonObject> expandTargets(
                JsonObject component,
                GeneratedModulePropertyRegistry.TierRule rule,
                RandomSource random
        ) {
            List<JsonObject> result = new ArrayList<>();
            if (component.has("targets")) {
                for (JsonElement element : component.getAsJsonArray("targets")) {
                    JsonObject target = normalizeTarget(element);
                    if (GeneratedModulePropertyRegistry.entryAvailable(target)) {
                        result.add(merge(component, target));
                    }
                }
            }
            if (component.has("target_pool")) {
                JsonObject pool = component.getAsJsonObject("target_pool");
                if (GeneratedModulePropertyRegistry.entryAvailable(pool)) {
                    int rolls = Math.max(0, (int) Math.round(pool.has("rolls")
                            ? roll(pool.get("rolls"), rule, random)
                            : 1.0D));
                    boolean unique = bool(pool, "unique", true);
                    List<JsonObject> available = new ArrayList<>();
                    for (JsonElement element : pool.getAsJsonArray("entries")) {
                        JsonObject entry = normalizeTarget(element);
                        if (GeneratedModulePropertyRegistry.entryAvailable(entry)) available.add(entry);
                    }
                    for (int i = 0; i < rolls && !available.isEmpty(); i++) {
                        int total = available.stream().mapToInt(value -> Math.max(1, intValue(value, "weight", 1))).sum();
                        int roll = random.nextInt(total);
                        JsonObject selected = available.get(available.size() - 1);
                        for (JsonObject candidate : available) {
                            roll -= Math.max(1, intValue(candidate, "weight", 1));
                            if (roll < 0) {
                                selected = candidate;
                                break;
                            }
                        }
                        result.add(merge(component, selected));
                        if (unique) available.remove(selected);
                    }
                }
            }
            if (result.isEmpty() && !component.has("targets") && !component.has("target_pool")) {
                result.add(component.deepCopy());
            }
            return result;
        }

        private List<AttributeCondition> rollConditions(
                JsonObject component,
                GeneratedModulePropertyRegistry.TierRule rule,
                RandomSource random
        ) {
            List<JsonObject> definitions = new ArrayList<>();
            if (component.has("conditions")) {
                for (JsonElement value : component.getAsJsonArray("conditions")) {
                    definitions.add(value.getAsJsonObject());
                }
            }
            if (component.has("condition_pool")) {
                JsonObject wrapper = new JsonObject();
                wrapper.add("target_pool", component.get("condition_pool"));
                definitions.addAll(expandTargets(wrapper, rule, random));
            }
            if (definitions.isEmpty()) {
                return List.of(new AttributeCondition(
                        AttributeCondition.Type.HEALTH_BELOW,
                        Optional.of(0.30D)
                ));
            }
            List<AttributeCondition> result = new ArrayList<>();
            for (JsonObject definition : definitions) {
                String typeName = definition.has("condition")
                        ? definition.get("condition").getAsString()
                        : string(definition, "type", "health_below");
                AttributeCondition.Type type = AttributeCondition.Type.valueOf(typeName.toUpperCase(Locale.ROOT));
                Optional<Double> value = switch (type) {
                    case HEALTH_BELOW, HEALTH_ABOVE -> Optional.of(
                            clamp(roll(definition.get("value"), rule, random), 0.0D, 1.0D)
                    );
                    default -> Optional.empty();
                };
                if (result.stream().noneMatch(existing -> existing.type() == type)) {
                    result.add(new AttributeCondition(type, value));
                }
            }
            return List.copyOf(result);
        }

        private void applyLegacy(
                GeneratedPropertyType property,
                GeneratedModulePropertyRegistry.TierRule rule,
                double quality,
                RandomSource random
        ) {
            double q = switch (property) {
                case DAMAGE_IMMUNITY, ELYTRA, NIGHT_VISION -> 1.0D;
                default -> quality;
            };
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
                        inverseInt(rule.rollInt("recharge_delay", random, 0), q)
                );
                case GENERAL_DAMAGE_REDUCTION -> {
                    double reduction = clamp01(rule.roll("reduction", random) * q);
                    defaultDamageReduction = Optional.of(Math.max(defaultDamageReduction.orElse(0.0D), reduction));
                    damageTagReductions.put(BYPASSES_EFFECTS, 0.0D);
                    damageTagReductions.put(BYPASSES_ENCHANTMENTS, 0.0D);
                    damageTagReductions.put(BYPASSES_INVULNERABILITY, 0.0D);
                }
                case SPECIALIZED_DAMAGE_REDUCTION -> damageTagReductions.merge(
                        PLACEHOLDER_DAMAGE_TAG, clamp01(rule.roll("reduction", random) * q), Math::max
                );
                case DAMAGE_IMMUNITY -> damageTagReductions.put(
                        PLACEHOLDER_DAMAGE_TAG, clamp01(rule.roll("reduction", random, 1.0D))
                );
                case EMERGENCY_SHIELD -> emergencyShield = new EmergencyShieldProperties(
                        clamp01(rule.roll("restore", random) * q),
                        inverseInt(rule.rollInt("cooldown", random, 0), q)
                );
                case SHIELD_REINFORCEMENT -> shieldProtection = new ShieldProtectionProperties(
                        clamp01(rule.roll("transfer", random) * q)
                );
                case ARMOR -> {
                    mergeAttribute(ARMOR, addValue(rule.roll("armor", random) * q));
                    if (rule.has("toughness")) mergeAttribute(ARMOR_TOUGHNESS, addValue(rule.roll("toughness", random) * q));
                }
                case REACTIVE_ARMOR -> {
                    mergeAttribute(ARMOR, addValue(rule.roll("base_armor", random) * q));
                    Map<ResourceLocation, AttributeModifierProperties> values = new LinkedHashMap<>();
                    values.put(ARMOR, addValue(rule.roll("conditional_armor", random) * q));
                    values.put(ARMOR_TOUGHNESS, addValue(rule.roll("conditional_toughness", random) * q));
                    mergeConditional(List.of(new AttributeCondition(AttributeCondition.Type.HEALTH_BELOW, Optional.of(0.30D))), values);
                }
                case FALL_PROTECTION -> {
                    if (rule.has("safe_fall_distance")) mergeAttribute(SAFE_FALL_DISTANCE, addValue(rule.roll("safe_fall_distance", random) * q));
                    mergeAttribute(FALL_DAMAGE_MULTIPLIER, multipliedBase(-clamp01(rule.roll("fall_reduction", random) * q)));
                }
                case KNOCKBACK_STABILIZATION -> {
                    double amount = rule.roll("resistance", random) * q;
                    mergeAttribute(KNOCKBACK_RESISTANCE, multipliedBase(amount));
                    mergeAttribute(EXPLOSION_KNOCKBACK_RESISTANCE, multipliedBase(amount));
                }
                case DODGE -> mergeAttribute(DODGE_CHANCE, multipliedBase(rule.roll("amount", random) * q));
                case STATUS_PROTECTION -> harmfulStatusProtection = Math.max(harmfulStatusProtection, clamp01(rule.roll("protection", random) * q));
                case BODY_PROTECTION -> bodyDamageProtection = new BodyDamageProtectionProperties(
                        clamp01(rule.roll("chance", random) * q), clamp01(rule.roll("reduction", random) * q), Set.of()
                );
                case MAX_HEALTH -> mergeAttribute(MAX_HEALTH, addValue(rule.roll("amount", random) * q));
                case HUNGER_ASSIST -> hunger = new HungerProperties(clamp01(rule.roll("reduction", random) * q));
                case THIRST_ASSIST -> thirst = new ThirstProperties(clamp01(rule.roll("reduction", random) * q));
                case REGENERATION -> regeneration = new RegenerationProperties(positive(rule.roll("health_per_second", random) * q));
                case REVIVAL -> revival = new RevivalProperties(
                        positive(rule.roll("restore_health", random) * q),
                        inverseInt(rule.rollInt("cooldown", random, 0), q),
                        positiveInt(rule.roll("invulnerability_ticks", random) * q)
                );
                case BODY_REGENERATION -> bodyDamageRegeneration = new BodyDamageRegenerationProperties(
                        positive(rule.roll("health_per_second", random) * q), Set.of()
                );
                case OXYGEN_SUPPORT -> mergeAttribute(OXYGEN_BONUS, multipliedBase(rule.roll("amount", random) * q));
                case MOVEMENT_SPEED -> mergeAttribute(MOVEMENT_SPEED, multipliedBase(rule.roll("amount", random) * q));
                case JUMP_ASSIST -> {
                    mergeAttribute(JUMP_STRENGTH, multipliedBase(rule.roll("jump", random) * q));
                    mergeAttribute(STEP_HEIGHT, addValue(rule.roll("step_height", random) * q));
                }
                case SWIM_ASSIST -> mergeAttribute(WATER_MOVEMENT_EFFICIENCY, addValue(rule.roll("amount", random) * q));
                case JETPACK -> {
                    Optional<ElytraBoostProperties> elytra = Optional.empty();
                    double chance = rule.roll("elytra_chance", random, 0.0D);
                    if (chance > 0.0D && random.nextDouble() < chance) {
                        elytra = Optional.of(new ElytraBoostProperties(
                                positive(rule.roll("elytra_acceleration", random, 0.025D) * q),
                                positive(rule.roll("elytra_max_speed", random, 1.25D) * q)
                        ));
                    }
                    jetpack = new JetpackProperties(
                            Math.max(0.01D, rule.roll("vertical_thrust", random) * q),
                            positive(rule.roll("horizontal_speed", random) * q),
                            activeCost(rule.rollInt("active_energy", random, 0), q), elytra
                    );
                    heatGeneration += positive(rule.roll("heat", random, 0.0D) * q);
                }
                case BLINK -> blink = new BlinkProperties(
                        Math.max(0.1D, rule.roll("distance", random) * q),
                        activeCost(rule.rollInt("activation_energy", random, 0), q),
                        inverseInt(rule.rollInt("cooldown", random, 0), q)
                );
                case ELYTRA -> mergeAttribute(ELYTRA_FLIGHT, addValue(1.0D));
                case FLIGHT -> flight = new FlightProperties(activeCost(rule.rollInt("active_energy", random, 0), q));
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
                    Map<ResourceLocation, AttributeModifierProperties> values = new LinkedHashMap<>();
                    values.put(ATTACK_DAMAGE, multipliedBase(rule.roll("attack_damage", random) * q));
                    values.put(ATTACK_SPEED, multipliedBase(rule.roll("attack_speed", random) * q));
                    mergeConditional(List.of(new AttributeCondition(AttributeCondition.Type.HEALTH_BELOW, Optional.of(0.30D))), values);
                }
                case LASER_DEFENSE -> laserDefense = new LaserDefenseProperties(
                        Math.max(0.1D, rule.roll("range", random) * q),
                        (float) Math.max(0.1D, rule.roll("damage", random) * q),
                        inverseInt(rule.rollInt("cooldown", random, 0), q),
                        activeCost(rule.rollInt("energy_cost", random, 0), q),
                        rule.bool("target_players", false), rule.bool("target_hostile", true), rule.bool("target_aggressive", true)
                );
                case DISCHARGE_DEFENSE -> dischargeDefense = new DischargeDefenseProperties(
                        Math.max(0.1D, rule.roll("range", random) * q),
                        Math.max(0.1D, rule.roll("jump_range", random) * q),
                        (float) Math.max(0.1D, rule.roll("damage", random) * q),
                        (float) clamp(rule.roll("falloff", random), 0.01D, 1.0D),
                        Math.max(1, positiveInt(rule.roll("targets", random) * Math.sqrt(q))),
                        positiveInt(rule.roll("bounces", random) * Math.sqrt(q)),
                        inverseInt(rule.rollInt("cooldown", random, 0), q),
                        activeCost(rule.rollInt("energy_cost", random, 0), q),
                        rule.bool("target_players", false), rule.bool("target_hostile", true), rule.bool("target_aggressive", true)
                );
                case NIGHT_VISION -> effectLevels.put(NIGHT_VISION, 0);
                case ENTITY_SENSOR -> {
                    boolean players = rule.bool("players", true);
                    boolean mobs = rule.bool("mobs", true);
                    boolean hostile = rule.bool("hostile", true);
                    if (!players && !mobs && !hostile) hostile = true;
                    entityDetection = new EntityDetectionProperties(
                            Math.max(1.0D, rule.roll("range", random) * q), players, mobs, hostile,
                            activeCost(rule.rollInt("active_energy", random, 0), q)
                    );
                }
                case BLOCK_SCANNER -> blockScanner = new BlockScannerProperties(
                        Math.max(1.0D, rule.roll("range", random) * q), List.of(), List.of(ORES_TAG),
                        activeCost(rule.rollInt("active_energy", random, 0), q),
                        rule.rollInt("color", random, BlockScannerProperties.DEFAULT_COLOR)
                );
                case THERMAL_VISION -> thermalVision = new ThermalVisionProperties(
                        activeCost(rule.rollInt("active_energy", random, 0), q)
                );
                case PICKUP_MAGNET -> pickupMagnet = new PickupMagnetProperties(
                        Math.max(0.1D, rule.roll("radius", random) * q),
                        switch (rule.string("mode", "both").toLowerCase(Locale.ROOT)) {
                            case "items" -> PickupMagnetProperties.Mode.ITEMS;
                            case "experience" -> PickupMagnetProperties.Mode.EXPERIENCE;
                            default -> PickupMagnetProperties.Mode.BOTH;
                        }
                );
                case CLOAKING -> cloaking = new CloakingProperties(
                        activeCost(rule.rollInt("activation_energy", random, 0), q),
                        activeCost(rule.rollInt("active_energy", random, 0), q),
                        inverseInt(rule.rollInt("cooldown", random, 0), q)
                );
                case MINING_SPEED -> mergeAttribute(BLOCK_BREAK_SPEED, multipliedBase(rule.roll("amount", random) * q));
                case BLOCK_REACH -> mergeAttribute(BLOCK_INTERACTION_RANGE, addValue(rule.roll("amount", random) * q));
                case UNDERWATER_MINING -> mergeAttribute(SUBMERGED_MINING_SPEED, multipliedBase(rule.roll("amount", random) * q));
                case LUCK -> mergeAttribute(LUCK, addValue(rule.roll("amount", random) * q));
                case EXPERIENCE_GAIN -> mergeAttribute(EXPERIENCE_GAINED, multipliedBase(rule.roll("amount", random) * q));
            }
        }

        private void mergeConditional(
                List<AttributeCondition> conditions,
                Map<ResourceLocation, AttributeModifierProperties> values
        ) {
            if (values.isEmpty()) return;
            if (conditionalAttributes == null) {
                conditionalAttributes = new ConditionalAttributeProperties(conditions, new AttributeProperties(values));
                return;
            }
            Map<ResourceLocation, AttributeModifierProperties> merged = new LinkedHashMap<>(
                    conditionalAttributes.attributes().attributes()
            );
            values.forEach((key, value) -> merged.merge(key, value, GeneratedModuleDefinitionBuilderV2::mergeModifiers));
            List<AttributeCondition> selectedConditions = conditionalAttributes.conditions();
            if (selectedConditions.isEmpty() && !conditions.isEmpty()) selectedConditions = conditions;
            conditionalAttributes = new ConditionalAttributeProperties(selectedConditions, new AttributeProperties(merged));
        }

        private void mergeAttribute(ResourceLocation key, AttributeModifierProperties value) {
            attributes.merge(key, value, GeneratedModuleDefinitionBuilderV2::mergeModifiers);
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
                builder.temperatureModifier(new TemperatureModifierProperties(temperature, 0.0D, 0.0D, thermalResistance));
            }
            if (temperatureImpact != null) builder.temperatureImpact(temperatureImpact);
            if (defaultDamageReduction.isPresent() || !damageReductions.isEmpty() || !damageTagReductions.isEmpty()) {
                builder.damageReduction(new DamageReductionProperties(defaultDamageReduction, damageReductions, damageTagReductions));
            }
            if (shield != null) builder.shield(shield);
            if (emergencyShield != null) builder.emergencyShield(emergencyShield);
            if (shieldProtection != null) builder.shieldProtection(shieldProtection);
            if (harmfulStatusProtection > 0.0D || !statusProtections.isEmpty() || !statusTagProtections.isEmpty()) {
                builder.statusProtection(new StatusProtectionProperties(harmfulStatusProtection, statusProtections, statusTagProtections));
            }
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
            if (!effectLevels.isEmpty()) builder.effects(new EffectsProperties(effectLevels));
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

    private record AttributeEntry(ResourceLocation id, AttributeModifierProperties modifier) {}

    private static boolean passesChance(
            JsonObject object,
            GeneratedModulePropertyRegistry.TierRule rule,
            RandomSource random
    ) {
        return !object.has("chance") || random.nextDouble() < clamp(roll(object.get("chance"), rule, random), 0.0D, 1.0D);
    }

    private static boolean numericExists(
            JsonObject object,
            String name,
            GeneratedModulePropertyRegistry.TierRule rule
    ) {
        if (!object.has(name)) return false;
        JsonElement value = object.get(name);
        return !(value.isJsonPrimitive() && value.getAsJsonPrimitive().isString())
                || rule.has(value.getAsString());
    }

    private static double roll(
            JsonElement specification,
            GeneratedModulePropertyRegistry.TierRule rule,
            RandomSource random
    ) {
        return GeneratedModulePropertyRegistry.rollNumeric(specification, rule.values(), random);
    }

    private static JsonObject normalizeTarget(JsonElement element) {
        if (element.isJsonObject()) return element.getAsJsonObject().deepCopy();
        JsonObject object = new JsonObject();
        object.addProperty("id", element.getAsString());
        return object;
    }

    private static JsonObject merge(JsonObject base, JsonObject override) {
        JsonObject result = base.deepCopy();
        for (Map.Entry<String, JsonElement> entry : override.entrySet()) {
            result.add(entry.getKey(), entry.getValue().deepCopy());
        }
        return result;
    }

    private static ResourceLocation parseRequiredId(JsonObject object, String name) {
        if (!object.has(name)) throw new IllegalArgumentException("Missing ResourceLocation field '" + name + "'");
        String value = object.get(name).getAsString();
        return GeneratedModulePropertyRegistry.parseId("minecraft", value);
    }

    private static int parseColor(
            JsonElement value,
            GeneratedModulePropertyRegistry.TierRule rule,
            RandomSource random
    ) {
        if (value == null) return BlockScannerProperties.DEFAULT_COLOR;
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
            String text = value.getAsString();
            if (rule.has(text)) return (int) Math.round(rule.roll(text, random));
            if (text.startsWith("#")) text = text.substring(1);
            return Integer.parseInt(text, 16);
        }
        return (int) Math.round(roll(value, rule, random));
    }

    private static String string(JsonObject object, String name, String fallback) {
        return object.has(name) ? object.get(name).getAsString() : fallback;
    }

    private static boolean bool(JsonObject object, String name, boolean fallback) {
        return object.has(name) ? object.get(name).getAsBoolean() : fallback;
    }

    private static int intValue(JsonObject object, String name, int fallback) {
        return object.has(name) ? object.get(name).getAsInt() : fallback;
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
