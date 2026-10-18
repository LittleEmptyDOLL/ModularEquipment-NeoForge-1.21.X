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

            if (property.components().isEmpty()) {
                throw new IllegalStateException(
                        "Generated property " + property.id() + " has no components; "
                                + "all generated rules must use data-driven descriptors"
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
                case "storage" -> storage = new StorageProperties(
                        positiveInt(read(component, "capacity", rule, random) * quality),
                        positiveInt(read(component, "input", rule, random) * quality),
                        positiveInt(read(component, "output", rule, random) * quality)
                );
                case "generation" -> {
                    generation = new GenerationProperties(positiveInt(read(component, "amount", rule, random) * quality));
                    heatGeneration += positive(read(component, "heat", rule, random, 0.0D) * quality);
                }
                case "temperature_modifier" -> {
                    temperature += read(component, "temperature", rule, random, 0.0D) * quality;
                    thermalResistance += positive(read(component, "thermal_resistance", rule, random, 0.0D) * quality);
                }
                case "temperature_impact" -> temperatureImpact = new TemperatureImpactProperties(
                        clamp01(read(component, "resistance", rule, random) * quality)
                );
                case "shield" -> shield = new ShieldProperties(
                        Math.max(1, positiveInt(read(component, "capacity", rule, random) * quality)),
                        positive(read(component, "recharge_rate", rule, random) * quality),
                        inverseInt((int) Math.round(read(component, "recharge_delay", rule, random)), quality)
                );
                case "emergency_shield" -> emergencyShield = new EmergencyShieldProperties(
                        clamp01(read(component, "restore", rule, random) * quality),
                        inverseInt((int) Math.round(read(component, "cooldown", rule, random)), quality)
                );
                case "shield_protection" -> shieldProtection = new ShieldProtectionProperties(
                        clamp01(read(component, "transfer", rule, random) * quality)
                );
                case "hunger" -> hunger = new HungerProperties(
                        clamp01(read(component, "reduction", rule, random) * quality)
                );
                case "thirst" -> thirst = new ThirstProperties(
                        clamp01(read(component, "reduction", rule, random) * quality)
                );
                case "regeneration" -> regeneration = new RegenerationProperties(
                        positive(read(component, "health_per_second", rule, random) * quality)
                );
                case "revival" -> revival = new RevivalProperties(
                        positive(read(component, "restore_health", rule, random) * quality),
                        inverseInt((int) Math.round(read(component, "cooldown", rule, random)), quality),
                        positiveInt(read(component, "invulnerability_ticks", rule, random) * quality)
                );
                case "jetpack" -> applyJetpack(component, rule, quality, random);
                case "blink" -> blink = new BlinkProperties(
                        Math.max(0.1D, read(component, "distance", rule, random) * quality),
                        activeCost((int) Math.round(read(component, "activation_energy", rule, random)), quality),
                        inverseInt((int) Math.round(read(component, "cooldown", rule, random)), quality)
                );
                case "flight" -> flight = new FlightProperties(
                        activeCost((int) Math.round(read(component, "active_energy", rule, random)), quality)
                );
                case "thermal_vision" -> thermalVision = new ThermalVisionProperties(
                        activeCost((int) Math.round(read(component, "active_energy", rule, random)), quality)
                );
                case "pickup_magnet" -> applyPickupMagnet(component, rule, quality, random);
                case "cloaking" -> cloaking = new CloakingProperties(
                        activeCost((int) Math.round(read(component, "activation_energy", rule, random)), quality),
                        activeCost((int) Math.round(read(component, "active_energy", rule, random)), quality),
                        inverseInt((int) Math.round(read(component, "cooldown", rule, random)), quality)
                );
                case "laser_defense" -> laserDefense = new LaserDefenseProperties(
                        Math.max(0.1D, read(component, "range", rule, random) * quality),
                        (float) Math.max(0.1D, read(component, "damage", rule, random) * quality),
                        inverseInt((int) Math.round(read(component, "cooldown", rule, random)), quality),
                        activeCost((int) Math.round(read(component, "energy_cost", rule, random)), quality),
                        rule.bool("target_players", false),
                        rule.bool("target_hostile", true),
                        rule.bool("target_aggressive", true)
                );
                case "discharge_defense" -> dischargeDefense = new DischargeDefenseProperties(
                        Math.max(0.1D, read(component, "range", rule, random) * quality),
                        Math.max(0.1D, read(component, "jump_range", rule, random) * quality),
                        (float) Math.max(0.1D, read(component, "damage", rule, random) * quality),
                        (float) clamp(read(component, "falloff", rule, random), 0.01D, 1.0D),
                        Math.max(1, positiveInt(read(component, "targets", rule, random) * Math.sqrt(quality))),
                        positiveInt(read(component, "bounces", rule, random) * Math.sqrt(quality)),
                        inverseInt((int) Math.round(read(component, "cooldown", rule, random)), quality),
                        activeCost((int) Math.round(read(component, "energy_cost", rule, random)), quality),
                        rule.bool("target_players", false),
                        rule.bool("target_hostile", true),
                        rule.bool("target_aggressive", true)
                );
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
            if (blockScanner != null) {
                blocks.addAll(blockScanner.blocks());
                tags.addAll(blockScanner.tags());
                range = Math.max(range, blockScanner.range());
                // Multiple scanner targets are separate detection workloads:
                // charge each target's active energy, not only the most expensive one.
                active = Math.addExact(active, blockScanner.activeConsumption());
            }
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
                players = bool(component, "players", rule.bool("players", true));
                mobs = bool(component, "mobs", rule.bool("mobs", true));
                hostile = bool(component, "hostile", rule.bool("hostile", true));
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


        private static double read(
                JsonObject component,
                String name,
                GeneratedModulePropertyRegistry.TierRule rule,
                RandomSource random
        ) {
            if (!component.has(name)) {
                throw new IllegalArgumentException("Missing generated component field: " + name);
            }
            return roll(component.get(name), rule, random);
        }

        private static double read(
                JsonObject component,
                String name,
                GeneratedModulePropertyRegistry.TierRule rule,
                RandomSource random,
                double fallback
        ) {
            if (!component.has(name)) return fallback;
            JsonElement specification = component.get(name);
            if (specification.isJsonPrimitive() && specification.getAsJsonPrimitive().isString()
                    && !rule.has(specification.getAsString())) {
                return fallback;
            }
            return roll(specification, rule, random);
        }

        private void applyJetpack(
                JsonObject component,
                GeneratedModulePropertyRegistry.TierRule rule,
                double quality,
                RandomSource random
        ) {
            Optional<ElytraBoostProperties> elytra = Optional.empty();
            double chance = read(component, "elytra_chance", rule, random, 0.0D);
            if (chance > 0.0D && random.nextDouble() < chance) {
                elytra = Optional.of(new ElytraBoostProperties(
                        positive(read(component, "elytra_acceleration", rule, random, 0.025D) * quality),
                        positive(read(component, "elytra_max_speed", rule, random, 1.25D) * quality)
                ));
            }
            jetpack = new JetpackProperties(
                    Math.max(0.01D, read(component, "vertical_thrust", rule, random) * quality),
                    positive(read(component, "horizontal_speed", rule, random) * quality),
                    activeCost((int) Math.round(read(component, "active_energy", rule, random)), quality),
                    elytra
            );
            heatGeneration += positive(read(component, "heat", rule, random, 0.0D) * quality);
        }

        private void applyPickupMagnet(
                JsonObject component,
                GeneratedModulePropertyRegistry.TierRule rule,
                double quality,
                RandomSource random
        ) {
            String mode = component.has("mode")
                    ? component.get("mode").getAsString() : "both";
            if (rule.has(mode)) mode = rule.string(mode, "both");
            PickupMagnetProperties.Mode selected = switch (mode.toLowerCase(Locale.ROOT)) {
                case "items" -> PickupMagnetProperties.Mode.ITEMS;
                case "experience" -> PickupMagnetProperties.Mode.EXPERIENCE;
                default -> PickupMagnetProperties.Mode.BOTH;
            };
            pickupMagnet = new PickupMagnetProperties(
                    Math.max(0.1D, read(component, "radius", rule, random) * quality), selected
            );
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
