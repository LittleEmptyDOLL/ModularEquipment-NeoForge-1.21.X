package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class GeneratedModuleRules extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    private static final String DIRECTORY = "generated_module_properties";
    private static final ResourceLocation SETTINGS_ID = ResourceLocation.fromNamespaceAndPath(
            ExoEquipment.MODID,
            "generated_modules/settings.json"
    );
    private static final GeneratedModuleRules INSTANCE = new GeneratedModuleRules();

    private static volatile Map<GeneratedPropertyType, PropertyRule> rules = Map.of();
    private static volatile Settings settings = Settings.defaults();

    private GeneratedModuleRules() {
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
        EnumMap<GeneratedPropertyType, PropertyRule> loaded =
                new EnumMap<>(GeneratedPropertyType.class);

        resources.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
                .forEach(entry -> loadRuleFile(
                        entry.getKey(),
                        entry.getValue(),
                        loaded
                ));

        Settings loadedSettings = loadSettings(resourceManager)
                .orElseGet(Settings::defaults);

        rules = Map.copyOf(loaded);
        settings = loadedSettings;

        for (GeneratedPropertyType type : GeneratedPropertyType.values()) {
            if (!rules.containsKey(type)) {
                LOGGER.warn(
                        "No generated-module data rule was loaded for {}; "
                                + "legacy availability/min-area will be used, but generation of that property "
                                + "will fail until a tier rule exists",
                        type
                );
            }
        }

        LOGGER.info(
                "Loaded {} generated-module property rules and {} size entries",
                rules.size(),
                settings.sizes().size()
        );
    }

    private static void loadRuleFile(
            ResourceLocation sourceId,
            JsonElement element,
            EnumMap<GeneratedPropertyType, PropertyRule> target
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
                GeneratedPropertyType type = parseProperty(entry.getKey());
                if (!entry.getValue().isJsonObject()) {
                    throw new IllegalArgumentException(
                            "Property " + entry.getKey() + " must be a JSON object"
                    );
                }

                target.put(
                        type,
                        PropertyRule.parse(
                                type,
                                entry.getValue().getAsJsonObject()
                        )
                );
            }
        } catch (RuntimeException exception) {
            LOGGER.error(
                    "Failed to load generated-module rules from {}",
                    sourceId,
                    exception
            );
        }
    }

    private static Optional<Settings> loadSettings(ResourceManager resourceManager) {
        Optional<Resource> resource = resourceManager.getResource(SETTINGS_ID);
        if (resource.isEmpty()) {
            LOGGER.warn(
                    "Generated-module settings {} are missing; using built-in fallback settings",
                    SETTINGS_ID
            );
            return Optional.empty();
        }

        try (Reader reader = resource.get().openAsReader()) {
            JsonElement element = JsonParser.parseReader(reader);
            if (!element.isJsonObject()) {
                throw new IllegalArgumentException(
                        "Generated-module settings root must be an object"
                );
            }
            return Optional.of(Settings.parse(element.getAsJsonObject()));
        } catch (Exception exception) {
            LOGGER.error(
                    "Failed to load generated-module settings {}; using fallback settings",
                    SETTINGS_ID,
                    exception
            );
            return Optional.empty();
        }
    }

    public static int minArea(
            GeneratedPropertyType type,
            int legacyMinArea
    ) {
        PropertyRule rule = rules.get(type);
        return rule != null ? rule.minArea() : legacyMinArea;
    }

    public static boolean isAvailableFor(
            GeneratedPropertyType type,
            EquipmentTier tier,
            boolean legacyAvailable
    ) {
        PropertyRule rule = rules.get(type);
        if (rule == null) {
            return legacyAvailable;
        }
        return rule.tiers().containsKey(tier);
    }

    public static boolean conflictsWith(
            GeneratedPropertyType type,
            GeneratedPropertyType other,
            boolean legacyConflict
    ) {
        PropertyRule rule = rules.get(type);
        PropertyRule otherRule = rules.get(other);
        if (rule == null && otherRule == null) {
            return legacyConflict;
        }

        return (rule != null && rule.conflicts().contains(other))
                || (otherRule != null && otherRule.conflicts().contains(type));
    }

    public static TierRule tierRule(
            GeneratedPropertyType type,
            EquipmentTier tier
    ) {
        PropertyRule rule = rules.get(type);
        if (rule == null) {
            throw new IllegalStateException(
                    "No data-driven generated-module rule is loaded for " + type
            );
        }

        TierRule tierRule = rule.tiers().get(tier);
        if (tierRule == null) {
            throw new IllegalStateException(
                    "Generated-module rule " + type + " does not define tier " + tier
            );
        }
        return tierRule;
    }

    public static Settings settings() {
        return settings;
    }

    private static GeneratedPropertyType parseProperty(String value) {
        return GeneratedPropertyType.valueOf(value.toUpperCase(Locale.ROOT));
    }

    private static EquipmentTier parseTier(String value) {
        return EquipmentTier.valueOf(value.toUpperCase(Locale.ROOT));
    }

    public record PropertyRule(
            GeneratedPropertyType type,
            int minArea,
            Set<GeneratedPropertyType> conflicts,
            Map<EquipmentTier, TierRule> tiers
    ) {
        static PropertyRule parse(
                GeneratedPropertyType type,
                JsonObject object
        ) {
            int minArea = object.has("min_area")
                    ? object.get("min_area").getAsInt()
                    : 1;
            if (minArea < 1 || minArea > 16) {
                throw new IllegalArgumentException(
                        type + " min_area must be within 1..16"
                );
            }

            java.util.EnumSet<GeneratedPropertyType> conflicts =
                    java.util.EnumSet.noneOf(GeneratedPropertyType.class);
            if (object.has("conflicts")) {
                JsonArray array = object.getAsJsonArray("conflicts");
                for (JsonElement value : array) {
                    conflicts.add(parseProperty(value.getAsString()));
                }
            }

            JsonObject tierObject = object.getAsJsonObject("tiers");
            if (tierObject == null || tierObject.size() == 0) {
                throw new IllegalArgumentException(
                        type + " must define at least one tier"
                );
            }

            EnumMap<EquipmentTier, TierRule> tiers =
                    new EnumMap<>(EquipmentTier.class);
            for (Map.Entry<String, JsonElement> entry : tierObject.entrySet()) {
                if (!entry.getValue().isJsonObject()) {
                    throw new IllegalArgumentException(
                            type + " tier " + entry.getKey() + " must be an object"
                    );
                }
                tiers.put(
                        parseTier(entry.getKey()),
                        new TierRule(
                                entry.getValue().getAsJsonObject().deepCopy()
                        )
                );
            }

            return new PropertyRule(
                    type,
                    minArea,
                    Set.copyOf(conflicts),
                    Map.copyOf(tiers)
            );
        }
    }

    public static final class TierRule {
        private final JsonObject values;

        private TierRule(JsonObject values) {
            this.values = values;
        }

        public boolean has(String name) {
            return values.has(name);
        }

        public double roll(
                String name,
                RandomSource random
        ) {
            JsonElement element = values.get(name);
            if (element == null) {
                throw new IllegalStateException(
                        "Generated-module tier rule is missing numeric value '" + name + "'"
                );
            }
            return NumericRange.parse(name, element).roll(random);
        }

        public double roll(
                String name,
                RandomSource random,
                double fallback
        ) {
            JsonElement element = values.get(name);
            return element == null
                    ? fallback
                    : NumericRange.parse(name, element).roll(random);
        }

        public int rollInt(
                String name,
                RandomSource random
        ) {
            return (int) Math.round(roll(name, random));
        }

        public int rollInt(
                String name,
                RandomSource random,
                int fallback
        ) {
            return (int) Math.round(roll(name, random, fallback));
        }

        public boolean bool(
                String name,
                boolean fallback
        ) {
            JsonElement element = values.get(name);
            return element == null ? fallback : element.getAsBoolean();
        }

        public String string(
                String name,
                String fallback
        ) {
            JsonElement element = values.get(name);
            return element == null ? fallback : element.getAsString();
        }
    }

    private record NumericRange(
            double min,
            double max,
            double step,
            Distribution distribution
    ) {
        static NumericRange parse(
                String name,
                JsonElement element
        ) {
            if (element.isJsonPrimitive()) {
                double fixed = element.getAsDouble();
                validate(name, fixed, fixed, 0.0D);
                return new NumericRange(
                        fixed,
                        fixed,
                        0.0D,
                        Distribution.UNIFORM
                );
            }

            if (!element.isJsonObject()) {
                throw new IllegalArgumentException(
                        "Numeric value " + name + " must be a number or range object"
                );
            }

            JsonObject object = element.getAsJsonObject();
            double min = object.get("min").getAsDouble();
            double max = object.get("max").getAsDouble();
            double step = object.has("step")
                    ? object.get("step").getAsDouble()
                    : 0.0D;
            Distribution distribution = object.has("distribution")
                    ? Distribution.parse(object.get("distribution").getAsString())
                    : Distribution.UNIFORM;

            validate(name, min, max, step);
            return new NumericRange(min, max, step, distribution);
        }

        private static void validate(
                String name,
                double min,
                double max,
                double step
        ) {
            if (!Double.isFinite(min) || !Double.isFinite(max) || min > max) {
                throw new IllegalArgumentException(
                        "Invalid generated-module range " + name + ": " + min + ".." + max
                );
            }
            if (!Double.isFinite(step) || step < 0.0D) {
                throw new IllegalArgumentException(
                        "Invalid generated-module step " + name + ": " + step
                );
            }
        }

        double roll(RandomSource random) {
            if (Double.compare(min, max) == 0) {
                return min;
            }

            double progress = switch (distribution) {
                case UNIFORM -> random.nextDouble();
                case TRIANGULAR ->
                        (random.nextDouble() + random.nextDouble()) * 0.5D;
            };

            double value = min + (max - min) * progress;
            if (step > 0.0D) {
                value = min + Math.round((value - min) / step) * step;
            }
            return Math.max(min, Math.min(max, value));
        }
    }

    private enum Distribution {
        UNIFORM,
        TRIANGULAR;

        static Distribution parse(String value) {
            return switch (value.toLowerCase(Locale.ROOT)) {
                case "uniform" -> UNIFORM;
                case "triangular" -> TRIANGULAR;
                default -> throw new IllegalArgumentException(
                        "Unknown generated-module distribution: " + value
                );
            };
        }
    }

    public record Settings(
            List<WeightedSize> sizes,
            List<PropertyCountBand> propertyCounts,
            NavigableMap<Integer, Double> integrationMultipliers,
            double complexityChancePerExtraProperty,
            double complexityMaxChance,
            double complexityPenaltyPerExtraProperty,
            double complexityMaxPenalty
    ) {
        static Settings parse(JsonObject root) {
            List<WeightedSize> sizes = parseSizes(root.getAsJsonArray("sizes"));
            List<PropertyCountBand> propertyCounts =
                    parsePropertyCounts(root.getAsJsonArray("property_counts"));

            NavigableMap<Integer, Double> integration = new TreeMap<>();
            JsonObject integrationObject =
                    root.getAsJsonObject("integration_multipliers");
            for (Map.Entry<String, JsonElement> entry : integrationObject.entrySet()) {
                int count = Integer.parseInt(entry.getKey());
                double value = entry.getValue().getAsDouble();
                if (count < 1 || !Double.isFinite(value) || value <= 0.0D) {
                    throw new IllegalArgumentException(
                            "Invalid integration multiplier entry " + entry
                    );
                }
                integration.put(count, value);
            }

            JsonObject complexity = root.getAsJsonObject("complexity");
            return new Settings(
                    List.copyOf(sizes),
                    List.copyOf(propertyCounts),
                    java.util.Collections.unmodifiableNavigableMap(
                            new TreeMap<>(integration)
                    ),
                    complexity.get("chance_per_extra_property").getAsDouble(),
                    complexity.get("max_chance").getAsDouble(),
                    complexity.get("penalty_per_extra_property").getAsDouble(),
                    complexity.get("max_penalty").getAsDouble()
            );
        }

        static Settings defaults() {
            return new Settings(
                    List.of(
                            new WeightedSize(new ModuleSize(1, 1), 18),
                            new WeightedSize(new ModuleSize(1, 2), 16),
                            new WeightedSize(new ModuleSize(1, 3), 10),
                            new WeightedSize(new ModuleSize(2, 2), 14),
                            new WeightedSize(new ModuleSize(1, 4), 6),
                            new WeightedSize(new ModuleSize(2, 3), 12),
                            new WeightedSize(new ModuleSize(2, 4), 9),
                            new WeightedSize(new ModuleSize(3, 3), 8),
                            new WeightedSize(new ModuleSize(3, 4), 5),
                            new WeightedSize(new ModuleSize(4, 4), 2)
                    ),
                    List.of(
                            new PropertyCountBand(1, 1, Map.of(1, 85, 2, 15)),
                            new PropertyCountBand(2, 3, Map.of(2, 100)),
                            new PropertyCountBand(4, 5, Map.of(2, 50, 3, 50)),
                            new PropertyCountBand(6, 8, Map.of(3, 50, 4, 50)),
                            new PropertyCountBand(9, 12, Map.of(4, 50, 5, 50)),
                            new PropertyCountBand(13, 16, Map.of(5, 50, 6, 50))
                    ),
                    java.util.Collections.unmodifiableNavigableMap(
                            new TreeMap<>(Map.of(
                                    1, 1.0D,
                                    2, 1.20D,
                                    3, 1.45D,
                                    4, 1.75D,
                                    5, 2.15D,
                                    6, 2.65D
                            ))
                    ),
                    0.12D,
                    0.75D,
                    0.05D,
                    0.25D
            );
        }

        private static List<WeightedSize> parseSizes(JsonArray array) {
            if (array == null || array.size() == 0) {
                throw new IllegalArgumentException(
                        "Generated-module settings must define sizes"
                );
            }

            List<WeightedSize> result = new ArrayList<>();
            for (JsonElement element : array) {
                JsonObject object = element.getAsJsonObject();
                ModuleSize size = new ModuleSize(
                        object.get("width").getAsInt(),
                        object.get("height").getAsInt()
                );
                int weight = object.get("weight").getAsInt();
                if (weight <= 0) {
                    throw new IllegalArgumentException(
                            "Generated-module size weight must be positive"
                    );
                }
                result.add(new WeightedSize(size, weight));
            }
            return result;
        }

        private static List<PropertyCountBand> parsePropertyCounts(JsonArray array) {
            if (array == null || array.size() == 0) {
                throw new IllegalArgumentException(
                        "Generated-module settings must define property_counts"
                );
            }

            List<PropertyCountBand> result = new ArrayList<>();
            for (JsonElement element : array) {
                JsonObject object = element.getAsJsonObject();
                int minArea = object.get("min_area").getAsInt();
                int maxArea = object.get("max_area").getAsInt();
                JsonObject weightsObject = object.getAsJsonObject("weights");
                Map<Integer, Integer> weights = new LinkedHashMap<>();
                for (Map.Entry<String, JsonElement> entry : weightsObject.entrySet()) {
                    int count = Integer.parseInt(entry.getKey());
                    int weight = entry.getValue().getAsInt();
                    if (count < 1 || weight <= 0) {
                        throw new IllegalArgumentException(
                                "Invalid generated property-count weight " + entry
                        );
                    }
                    weights.put(count, weight);
                }
                result.add(new PropertyCountBand(
                        minArea,
                        maxArea,
                        Map.copyOf(weights)
                ));
            }
            return result;
        }

        public ModuleSize rollSize(RandomSource random) {
            int total = sizes.stream().mapToInt(WeightedSize::weight).sum();
            int roll = random.nextInt(total);
            for (WeightedSize entry : sizes) {
                roll -= entry.weight();
                if (roll < 0) {
                    return entry.size();
                }
            }
            return sizes.get(sizes.size() - 1).size();
        }

        public int rollPropertyCount(
                int area,
                RandomSource random
        ) {
            PropertyCountBand band = propertyCounts.stream()
                    .filter(value -> area >= value.minArea() && area <= value.maxArea())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "No generated property-count band for area " + area
                    ));
            return band.roll(random);
        }

        public double integrationMultiplier(int propertyCount) {
            Map.Entry<Integer, Double> entry = integrationMultipliers.floorEntry(
                    Math.max(1, propertyCount)
            );
            if (entry == null) {
                return integrationMultipliers.firstEntry().getValue();
            }
            return entry.getValue();
        }

        public double rollComplexityScale(
                int propertyCount,
                RandomSource random
        ) {
            int extra = Math.max(0, propertyCount - 1);
            if (extra == 0) {
                return 1.0D;
            }

            double chance = Math.min(
                    complexityMaxChance,
                    extra * complexityChancePerExtraProperty
            );
            if (random.nextDouble() >= chance) {
                return 1.0D;
            }

            double maxPenalty = Math.min(
                    complexityMaxPenalty,
                    extra * complexityPenaltyPerExtraProperty
            );
            return 1.0D - random.nextDouble() * maxPenalty;
        }
    }

    public record WeightedSize(
            ModuleSize size,
            int weight
    ) {}

    public record PropertyCountBand(
            int minArea,
            int maxArea,
            Map<Integer, Integer> weights
    ) {
        int roll(RandomSource random) {
            int total = weights.values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
            int roll = random.nextInt(total);
            for (Map.Entry<Integer, Integer> entry : weights.entrySet()) {
                roll -= entry.getValue();
                if (roll < 0) {
                    return entry.getKey();
                }
            }
            return weights.keySet().iterator().next();
        }
    }
}
