package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
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
 * V2 registry for procedurally generated module properties.
 *
 * <p>Unlike {@link GeneratedPropertyType}, property identity is a ResourceLocation.
 * Any datapack namespace may contribute a property under
 * {@code generated_module_properties}. Multiple files may contribute fragments to
 * the same property; object fields are merged recursively while arrays/scalars in
 * the later fragment replace earlier values.</p>
 */
@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class GeneratedModulePropertyRegistry extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final String DIRECTORY = "generated_module_properties";
    private static final GeneratedModulePropertyRegistry INSTANCE = new GeneratedModulePropertyRegistry();

    private static volatile Map<ResourceLocation, PropertyRule> rules = Map.of();

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

        Map<ResourceLocation, PropertyRule> loaded = new LinkedHashMap<>();
        fragments.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
                .forEach(entry -> {
                    try {
                        PropertyRule rule = PropertyRule.parse(entry.getKey(), entry.getValue());
                        if (!rule.tiers().isEmpty()) {
                            loaded.put(entry.getKey(), rule);
                        }
                    } catch (RuntimeException exception) {
                        LOGGER.error("Failed to load generated property {}", entry.getKey(), exception);
                    }
                });

        rules = Map.copyOf(loaded);
        LOGGER.info("Loaded {} extensible generated-module properties", rules.size());
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
            JsonObject properties = root.has("properties")
                    ? root.getAsJsonObject("properties")
                    : root;

            for (Map.Entry<String, JsonElement> entry : properties.entrySet()) {
                if (!entry.getValue().isJsonObject()) {
                    throw new IllegalArgumentException("Property " + entry.getKey() + " must be an object");
                }
                ResourceLocation id = parseId(sourceId.getNamespace(), entry.getKey());
                JsonObject merged = target.computeIfAbsent(id, ignored -> new JsonObject());
                deepMerge(merged, entry.getValue().getAsJsonObject());
            }
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to collect extensible generated properties from {}", sourceId, exception);
        }
    }

    private static void deepMerge(JsonObject target, JsonObject patch) {
        for (Map.Entry<String, JsonElement> entry : patch.entrySet()) {
            JsonElement existing = target.get(entry.getKey());
            JsonElement replacement = entry.getValue();
            if (existing != null && existing.isJsonObject() && replacement.isJsonObject()) {
                deepMerge(existing.getAsJsonObject(), replacement.getAsJsonObject());
            } else {
                target.add(entry.getKey(), replacement.deepCopy());
            }
        }
    }

    public static List<PropertyRule> candidates(EquipmentTier tier, int area) {
        return rules.values().stream()
                .filter(rule -> rule.isAvailableFor(tier) && rule.minArea() <= area)
                .sorted(Comparator.comparing(rule -> rule.id().toString()))
                .toList();
    }

    public static PropertyRule rule(ResourceLocation id) {
        PropertyRule rule = rules.get(id);
        if (rule == null) {
            throw new IllegalStateException("No generated-module property rule loaded for " + id);
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

    public record PropertyRule(
            ResourceLocation id,
            int minArea,
            int weight,
            Set<ResourceLocation> conflicts,
            Set<String> requirements,
            Map<EquipmentTier, TierRule> tiers,
            Optional<GeneratedPropertyType> legacyType,
            List<JsonObject> components,
            boolean replaceLegacy,
            boolean ignoreQuality
    ) {
        static PropertyRule parse(ResourceLocation id, JsonObject object) {
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

            Optional<GeneratedPropertyType> legacy = inferLegacy(id, object);
            LinkedHashSet<String> requirements = new LinkedHashSet<>(parseRequirements(object));
            legacy.ifPresent(type -> {
                String dependency = type.requiredModId();
                if (dependency != null) requirements.add(dependency);
            });

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

            boolean replaceLegacy = object.has("replace_legacy")
                    ? object.get("replace_legacy").getAsBoolean()
                    : !components.isEmpty();
            boolean ignoreQuality = object.has("ignore_quality")
                    && object.get("ignore_quality").getAsBoolean();

            return new PropertyRule(
                    id,
                    minArea,
                    weight,
                    Set.copyOf(conflicts),
                    Set.copyOf(requirements),
                    Map.copyOf(tiers),
                    legacy,
                    List.copyOf(components),
                    replaceLegacy,
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

        private static Optional<GeneratedPropertyType> inferLegacy(ResourceLocation id, JsonObject object) {
            String value = object.has("implementation")
                    ? object.get("implementation").getAsString()
                    : id.getNamespace().equals(ExoEquipment.MODID) ? id.getPath() : null;
            if (value == null) return Optional.empty();
            try {
                return Optional.of(GeneratedPropertyType.valueOf(value.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
                return Optional.empty();
            }
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
