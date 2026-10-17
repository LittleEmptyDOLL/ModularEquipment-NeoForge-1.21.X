package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

/** Shared data-driven generation settings; individual property rules are loaded
 *  exclusively by {@link GeneratedModulePropertyRegistry}. */
@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class GeneratedModuleRules extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final ResourceLocation SETTINGS_ID = ResourceLocation.fromNamespaceAndPath(
            ExoEquipment.MODID, "generated_modules/settings.json"
    );
    private static final GeneratedModuleRules INSTANCE = new GeneratedModuleRules();
    private static volatile Settings settings = Settings.defaults();

    private GeneratedModuleRules() {
        super(GSON, "generated_modules");
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
        settings = loadSettings(resourceManager).orElseGet(Settings::defaults);
        LOGGER.info("Loaded generated-module settings with {} size entries",
                settings.sizes().size());
    }

    private static Optional<Settings> loadSettings(ResourceManager resourceManager) {
        Optional<Resource> resource = resourceManager.getResource(SETTINGS_ID);
        if (resource.isEmpty()) {
            LOGGER.warn("Generated-module settings {} are missing; using defaults", SETTINGS_ID);
            return Optional.empty();
        }
        try (Reader reader = resource.get().openAsReader()) {
            JsonElement value = JsonParser.parseReader(reader);
            if (!value.isJsonObject()) {
                throw new IllegalArgumentException("Settings must be a JSON object");
            }
            return Optional.of(Settings.parse(value.getAsJsonObject()));
        } catch (Exception exception) {
            LOGGER.error("Failed to load generated-module settings {}; using defaults",
                    SETTINGS_ID, exception);
            return Optional.empty();
        }
    }

    public static Settings settings() {
        return settings;
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
