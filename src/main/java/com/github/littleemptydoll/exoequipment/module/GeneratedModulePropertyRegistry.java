package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import com.google.gson.JsonArray;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Registry for procedurally generated module parameters.
 *
 * <p>The top-level unit is a {@link ModuleDefinition} properties container
 * (for example {@code attribute_properties} or {@code shield_properties}).
 * A generated module first rolls a container and only then an entry inside that
 * container. Repeatable containers can therefore contribute several entries to
 * the same final ModuleDefinition field.</p>
 */
@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class GeneratedModulePropertyRegistry extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final String DIRECTORY = "generated_module_properties";
    private static final GeneratedModulePropertyRegistry INSTANCE = new GeneratedModulePropertyRegistry();

    private static volatile Map<ResourceLocation, PropertyRule> rules = Map.of();
    private static volatile Map<ResourceLocation, ContainerRule> containers = Map.of();

    private GeneratedModulePropertyRegistry() {
        super(GSON, DIRECTORY);
    }

    @SubscribeEvent
    public static void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> resources,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        Map<ResourceLocation, JsonObject> fragments = new LinkedHashMap<>();

        resources.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
                .forEach(entry -> collect(entry.getKey(), entry.getValue(), fragments));

        Map<ResourceLocation, PropertyRule> loadedRules = new LinkedHashMap<>();
        Map<ResourceLocation, ContainerRule> loadedContainers = new LinkedHashMap<>();

        fragments.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
                .forEach(entry -> {
                    try {
                        ContainerRule container = ContainerRule.parse(entry.getKey(), entry.getValue(), loadedRules);
                        loadedContainers.put(container.id(), container);
                    } catch (RuntimeException exception) {
                        LOGGER.error("Failed to load generated parameter container {}", entry.getKey(), exception);
                    }
                });

        rules = Map.copyOf(loadedRules);
        containers = Map.copyOf(loadedContainers);
        LOGGER.info(
                "Loaded {} generated-module parameter containers with {} entries",
                containers.size(), rules.size()
        );
    }

    private static void collect(
            ResourceLocation sourceId,
            JsonElement element,
            Map<ResourceLocation, JsonObject> target
    ) {
        try {
            if (!element.isJsonObject()) {
                throw new IllegalArgumentException("Root must be a JSON object");
            }
            JsonObject root = element.getAsJsonObject();
            ResourceLocation containerId = root.has("container")
                    ? parseId(sourceId.getNamespace(), root.get("container").getAsString())
                    : sourceId;
            JsonObject merged = target.computeIfAbsent(containerId, ignored -> new JsonObject());
            deepMerge(merged, root);
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to collect generated parameter container from {}", sourceId, exception);
        }
    }

    private static void deepMerge(JsonObject target, JsonObject patch) {
        for (Map.Entry<String, JsonElement> entry : patch.entrySet()) {
            if (entry.getKey().equals("container")) continue;
            JsonElement existing = target.get(entry.getKey());
            JsonElement replacement = entry.getValue();
            if (existing != null && existing.isJsonObject() && replacement.isJsonObject()) {
                deepMerge(existing.getAsJsonObject(), replacement.getAsJsonObject());
            } else {
                target.add(entry.getKey(), replacement.deepCopy());
            }
        }
    }

    /** Flat entry view retained for the definition builder and debugging. */
    public static List<PropertyRule> candidates(EquipmentTier tier, int area) {
        return rules.values().stream()
                .filter(rule -> rule.isAvailableFor(tier) && rule.minArea() <= area)
                .sorted(Comparator.comparing(rule -> rule.id().toString()))
                .toList();
    }

    public static List<ContainerRule> containers() {
        return containers.values().stream()
                .sorted(Comparator.comparing(container -> container.id().toString()))
                .toList();
    }

    public static PropertyRule rule(ResourceLocation id) {
        PropertyRule rule = rules.get(id);
        if (rule == null) {
            throw new IllegalStateException("No generated-module entry rule loaded for " + id);
        }
        return rule;
    }

    public static Optional<PropertyRule> find(ResourceLocation id) {
        return Optional.ofNullable(rules.get(id));
    }

    public static ResourceLocation builtInId(GeneratedPropertyType type) {
        return ResourceLocation.fromNamespaceAndPath(
                ExoEquipment.MODID,
                type.name().toLowerCase(Locale.ROOT)
        );
    }

    static ResourceLocation parseId(String defaultNamespace, String value) {
        int separator = value.indexOf(':');
        return separator >= 0
                ? ResourceLocation.fromNamespaceAndPath(
                        value.substring(0, separator),
                        value.substring(separator + 1)
                )
                : ResourceLocation.fromNamespaceAndPath(defaultNamespace, value);
    }

    private static boolean requirementsLoaded(Set<String> requirements) {
        if (requirements.isEmpty()) return true;
        ModList modList = ModList.get();
        if (modList == null) return false;
        for (String modId : requirements) {
            if (!modList.isLoaded(modId)) return false;
        }
        return true;
    }

    static Set<String> parseRequirements(JsonObject object) {
        if (!object.has("requires")) return Set.of();
        JsonElement element = object.get("requires");
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (element.isJsonArray()) {
            for (JsonElement value : element.getAsJsonArray()) {
                result.add(value.getAsString());
            }
        } else {
            result.add(element.getAsString());
        }
        return Set.copyOf(result);
    }

    static boolean entryAvailable(JsonObject object) {
        return requirementsLoaded(parseRequirements(object));
    }

    public record ContainerRule(
            ResourceLocation id,
            int weight,
            int maxRolls,
            boolean selectable,
            Set<String> requirements,
            List<ResourceLocation> entries
    ) {
        static ContainerRule parse(
                ResourceLocation id,
                JsonObject object,
                Map<ResourceLocation, PropertyRule> loadedRules
        ) {
            int weight = object.has("weight") ? object.get("weight").getAsInt() : 1;
            if (weight <= 0) throw new IllegalArgumentException(id + " weight must be positive");

            boolean selectable = !object.has("selectable") || object.get("selectable").getAsBoolean();
            int maxRolls = object.has("max_rolls") ? object.get("max_rolls").getAsInt() : 1;
            if (maxRolls < 0 || (selectable && maxRolls == 0)) {
                throw new IllegalArgumentException(id + " max_rolls must be positive for selectable containers");
            }

            Set<String> requirements = parseRequirements(object);
            List<ResourceLocation> entryIds = new ArrayList<>();
            JsonObject entries = object.getAsJsonObject("entries");
            if (entries != null) {
                for (Map.Entry<String, JsonElement> entry : entries.entrySet()) {
                    if (!entry.getValue().isJsonObject()) {
                        throw new IllegalArgumentException("Entry " + entry.getKey() + " must be an object");
                    }
                    ResourceLocation entryId = parseId(id.getNamespace(), entry.getKey());
                    PropertyRule rule = PropertyRule.parse(entryId, id, entry.getValue().getAsJsonObject());
                    if (!rule.tiers().isEmpty()) {
                        if (loadedRules.put(entryId, rule) != null) {
                            throw new IllegalArgumentException("Duplicate generated-module entry " + entryId);
                        }
                        entryIds.add(entryId);
                    }
                }
            }


            // Resource-id keyed choices: the JSON key IS the registry id, not a
            // hard-coded alias interpreted by the definition builder.
            parseDirectSelections(id, object, loadedRules, entryIds);

            if (selectable && entryIds.isEmpty()) {
                throw new IllegalArgumentException(id + " is selectable but has no entries");
            }

            return new ContainerRule(
                    id,
                    weight,
                    maxRolls,
                    selectable,
                    requirements,
                    List.copyOf(entryIds)
            );
        }


        /**
         * Each ModuleDefinition field can have a direct selection map. Keys are
         * ResourceLocations (prefix '#' for tags); duplicate target variants may
         * be supplied as arrays. The generated rule id is scoped to its
         * container so the same attribute can appear in conditional attributes.
         */
        private static void parseDirectSelections(
                ResourceLocation containerId,
                JsonObject root,
                Map<ResourceLocation, PropertyRule> loadedRules,
                List<ResourceLocation> entryIds
        ) {
            Map<String, String> fields = Map.of(
                    "attributes", "attribute",
                    "conditional_attributes", "conditional_attribute",
                    "effects", "effect",
                    "damage_reductions", "damage_reduction",
                    "status_protections", "status_protection",
                    "scanner_targets", "block_scanner"
            );
            for (Map.Entry<String, String> field : fields.entrySet()) {
                JsonObject targets = root.getAsJsonObject(field.getKey());
                if (targets == null) continue;
                for (Map.Entry<String, JsonElement> target : targets.entrySet()) {
                    JsonArray variants = new JsonArray();
                    if (target.getValue().isJsonArray()) {
                        for (JsonElement variant : target.getValue().getAsJsonArray()) {
                            variants.add(variant);
                        }
                    } else {
                        variants.add(target.getValue());
                    }
                    for (int index = 0; index < variants.size(); index++) {
                        if (!variants.get(index).isJsonObject()) {
                            throw new IllegalArgumentException("Target " + target.getKey() + " must be an object");
                        }
                        String key = target.getKey();
                        JsonObject descriptor = variants.get(index).getAsJsonObject();
                        String type = field.getValue();
                        boolean tag = key.startsWith("#");
                        boolean special = key.equals("default") || key.equals("harmful");
                        String rawId = tag ? key.substring(1) : key;
                        String resolvedId = descriptor.has("id")
                                ? descriptor.get("id").getAsString()
                                : rawId;
                        if (!special) parseId("minecraft", resolvedId);

                        if (type.equals("attribute")
                                || type.equals("conditional_attribute")) {
                            if (!BuiltInRegistries.ATTRIBUTE.containsKey(parseId("minecraft", resolvedId))) {
                                continue;
                            }
                        } else if (type.equals("effect")) {
                            if (!BuiltInRegistries.MOB_EFFECT.containsKey(parseId("minecraft", resolvedId))) {
                                continue;
                            }
                        }

                        JsonObject ruleObject = descriptor.deepCopy();
                        JsonArray components = new JsonArray();
                        JsonObject component = new JsonObject();
                        component.addProperty("type", type);
                        if (!special) component.addProperty("id", resolvedId);

                        // The descriptor may customize the amount, operation,
                        // condition, target type, scaling and scanner options.
                        for (String fieldName : List.of(
                                "operation", "amount", "level", "multiplier",
                                "scale_quality", "optional", "value", "conditions",
                                "condition_pool", "chance", "range", "active_energy",
                                "color", "target_type", "extra_energy"
                        )) {
                            if (descriptor.has(fieldName)) {
                                component.add(fieldName, descriptor.get(fieldName).deepCopy());
                            }
                        }

                        if (type.equals("attribute") || type.equals("conditional_attribute")) {
                            if (!component.has("amount") && !("boolean".equalsIgnoreCase(
                                    component.has("operation") ? component.get("operation").getAsString() : ""))) {
                                component.addProperty("amount", "amount");
                            }
                        } else if (type.equals("effect")) {
                            if (!component.has("level")) component.addProperty("level", 1);
                        } else if (type.equals("damage_reduction") || type.equals("status_protection")) {
                            if (!component.has("amount")) {
                                component.addProperty("amount", type.equals("damage_reduction") ? "reduction" : "protection");
                            }
                            if (!component.has("target_type")) {
                                component.addProperty("target_type",
                                        special ? key : tag ? "tag"
                                                : type.equals("damage_reduction") ? "damage_type" : "effect");
                            }
                        } else if (type.equals("block_scanner")) {
                            if (!component.has("target_type")) {
                                component.addProperty("target_type", tag ? "tag" : "block");
                            }
                            if (!component.has("range")) component.addProperty("range", "range");
                            if (!component.has("active_energy")) component.addProperty("active_energy", "active_energy");
                            if (!component.has("color")) component.addProperty("color", "color");
                        }
                        components.add(component);
                        if (descriptor.has("components")) {
                            for (JsonElement extra : descriptor.getAsJsonArray("components")) {
                                components.add(extra.deepCopy());
                            }
                        }
                        ruleObject.add("components", components);

                        String suffix = field.getKey() + "/" + (tag ? "tag/" : "")
                                + rawId.replace(':', '/') + (variants.size() > 1 ? "/" + index : "");
                        ResourceLocation selectionId = ResourceLocation.fromNamespaceAndPath(
                                containerId.getNamespace(),
                                containerId.getPath() + "/" + suffix
                        );
                        PropertyRule rule = PropertyRule.parse(selectionId, containerId, ruleObject);
                        if (!rule.tiers().isEmpty()) {
                            if (loadedRules.putIfAbsent(selectionId, rule) != null) {
                                throw new IllegalArgumentException("Duplicate generated target " + selectionId);
                            }
                            entryIds.add(selectionId);
                        }
                    }
                }
            }
        }

        public boolean isAvailable() {
            return selectable && requirementsLoaded(requirements);
        }

        public List<PropertyRule> candidates(
                EquipmentTier tier,
                int area,
                List<ResourceLocation> selected
        ) {
            return entries.stream()
                    .map(rules::get)
                    .filter(rule -> rule != null)
                    .filter(rule -> rule.isAvailableFor(tier) && rule.minArea() <= area)
                    .filter(rule -> !selected.contains(rule.id()))
                    .filter(rule -> selected.stream().noneMatch(rule::conflictsWith))
                    .sorted(Comparator.comparing(rule -> rule.id().toString()))
                    .toList();
        }
    }

    public record PropertyRule(
            ResourceLocation id,
            ResourceLocation containerId,
            int minArea,
            int weight,
            Set<ResourceLocation> conflicts,
            Set<String> requirements,
            Map<EquipmentTier, TierRule> tiers,
            List<JsonObject> components,
            boolean ignoreQuality
    ) {
        static PropertyRule parse(ResourceLocation id, ResourceLocation containerId, JsonObject object) {
            int minArea = object.has("min_area") ? object.get("min_area").getAsInt() : 1;
            if (minArea < 1 || minArea > 16) {
                throw new IllegalArgumentException(id + " min_area must be within 1..16");
            }
            int weight = object.has("weight") ? object.get("weight").getAsInt() : 1;
            if (weight <= 0) throw new IllegalArgumentException(id + " weight must be positive");

            LinkedHashSet<ResourceLocation> conflicts = new LinkedHashSet<>();
            if (object.has("conflicts")) {
                for (JsonElement value : object.getAsJsonArray("conflicts")) {
                    conflicts.add(parseId(id.getNamespace(), value.getAsString()));
                }
            }

            Set<String> requirements = parseRequirements(object);

            EnumMap<EquipmentTier, TierRule> tiers = new EnumMap<>(EquipmentTier.class);
            JsonObject tierObject = object.getAsJsonObject("tiers");
            if (tierObject != null) {
                for (Map.Entry<String, JsonElement> entry : tierObject.entrySet()) {
                    EquipmentTier tier = EquipmentTier.valueOf(entry.getKey().toUpperCase(Locale.ROOT));
                    tiers.put(tier, new TierRule(entry.getValue().getAsJsonObject().deepCopy()));
                }
            }

            List<JsonObject> components = new ArrayList<>();
            if (object.has("components")) {
                for (JsonElement component : object.getAsJsonArray("components")) {
                    components.add(component.getAsJsonObject().deepCopy());
                }
            }

            boolean ignoreQuality = object.has("ignore_quality")
                    && object.get("ignore_quality").getAsBoolean();

            return new PropertyRule(
                    id,
                    containerId,
                    minArea,
                    weight,
                    Set.copyOf(conflicts),
                    Set.copyOf(requirements),
                    Map.copyOf(tiers),
                    List.copyOf(components),
                    ignoreQuality
            );
        }

        public boolean isAvailableFor(EquipmentTier tier) {
            return tiers.containsKey(tier) && requirementsLoaded(requirements);
        }

        public boolean conflictsWith(ResourceLocation other) {
            PropertyRule otherRule = rules.get(other);
            return conflicts.contains(other)
                    || (otherRule != null && otherRule.conflicts().contains(id));
        }

        public TierRule tierRule(EquipmentTier tier) {
            TierRule rule = tiers.get(tier);
            if (rule == null) {
                throw new IllegalStateException(id + " does not define tier " + tier);
            }
            return rule;
        }

    }

    public static final class TierRule {
        private final JsonObject values;

        private TierRule(JsonObject values) {
            this.values = values;
        }

        public JsonObject values() {
            return values;
        }

        public boolean has(String name) {
            return values.has(name);
        }

        public JsonElement element(String name) {
            return values.get(name);
        }

        public double roll(String name, net.minecraft.util.RandomSource random) {
            JsonElement element = values.get(name);
            if (element == null) {
                throw new IllegalStateException("Generated property tier is missing numeric value '" + name + "'");
            }
            return rollNumeric(element, values, random);
        }

        public double roll(String name, net.minecraft.util.RandomSource random, double fallback) {
            JsonElement element = values.get(name);
            return element == null ? fallback : rollNumeric(element, values, random);
        }

        public int rollInt(String name, net.minecraft.util.RandomSource random, int fallback) {
            return (int) Math.round(roll(name, random, fallback));
        }

        public boolean bool(String name, boolean fallback) {
            JsonElement element = values.get(name);
            return element == null ? fallback : element.getAsBoolean();
        }

        public String string(String name, String fallback) {
            JsonElement element = values.get(name);
            return element == null ? fallback : element.getAsString();
        }
    }

    static double rollNumeric(JsonElement element, JsonObject tierValues, net.minecraft.util.RandomSource random) {
        if (element == null) {
            throw new IllegalArgumentException("Numeric specification is missing");
        }
        if (element.isJsonPrimitive()) {
            if (element.getAsJsonPrimitive().isString()) {
                String reference = element.getAsString();
                JsonElement referenced = tierValues.get(reference);
                if (referenced == null) {
                    throw new IllegalArgumentException("Unknown tier numeric reference: " + reference);
                }
                return rollNumeric(referenced, tierValues, random);
            }
            return element.getAsDouble();
        }
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("Numeric specification must be a number, field reference, or range object");
        }
        JsonObject range = element.getAsJsonObject();
        double min = range.get("min").getAsDouble();
        double max = range.get("max").getAsDouble();
        if (!Double.isFinite(min) || !Double.isFinite(max) || min > max) {
            throw new IllegalArgumentException("Invalid numeric range " + min + ".." + max);
        }
        if (Double.compare(min, max) == 0) return min;
        String distribution = range.has("distribution")
                ? range.get("distribution").getAsString().toLowerCase(Locale.ROOT)
                : "uniform";
        double progress = switch (distribution) {
            case "uniform" -> random.nextDouble();
            case "triangular" -> (random.nextDouble() + random.nextDouble()) * 0.5D;
            default -> throw new IllegalArgumentException("Unknown distribution: " + distribution);
        };
        double result = min + (max - min) * progress;
        double step = range.has("step") ? range.get("step").getAsDouble() : 0.0D;
        if (step < 0.0D || !Double.isFinite(step)) {
            throw new IllegalArgumentException("Invalid numeric step: " + step);
        }
        if (step > 0.0D) {
            result = min + Math.round((result - min) / step) * step;
        }
        return Math.max(min, Math.min(max, result));
    }
}
