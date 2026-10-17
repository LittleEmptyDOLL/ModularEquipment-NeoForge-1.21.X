package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GeneratedModuleGenerator {
    private GeneratedModuleGenerator() {}

    /**
     * Container-first generated-module plan used by actual prototype modules.
     * Each requested slot rolls a ModuleDefinition properties container first,
     * then one entry inside it. Repeatable containers (notably attributes) may
     * therefore fill several slots while still merging into one final field.
     */
    public static GeneratedModulePropertyPlan propertyPlan(
            EquipmentTier tier,
            long seed
    ) {
        validateTier(tier);

        RandomSource random = RandomSource.create(seed);
        ModuleSize size = rollSize(random);
        int area = size.width() * size.height();
        int requestedProperties = rollPropertyCount(area, random);

        List<ResourceLocation> selected = new ArrayList<>();
        Map<ResourceLocation, Integer> containerUses = new HashMap<>();

        while (selected.size() < requestedProperties) {
            List<ContainerChoice> availableContainers = new ArrayList<>();
            for (GeneratedModulePropertyRegistry.ContainerRule container
                    : GeneratedModulePropertyRegistry.containers()) {
                if (!container.isAvailable()) continue;
                int used = containerUses.getOrDefault(container.id(), 0);
                if (used >= container.maxRolls()) continue;

                List<GeneratedModulePropertyRegistry.PropertyRule> entries =
                        container.candidates(tier, area, selected);
                if (!entries.isEmpty()) {
                    availableContainers.add(new ContainerChoice(container, entries));
                }
            }

            if (availableContainers.isEmpty()) break;

            ContainerChoice containerChoice = weightedContainer(availableContainers, random);
            GeneratedModulePropertyRegistry.PropertyRule entry =
                    weightedEntry(containerChoice.entries(), random);

            selected.add(entry.id());
            containerUses.merge(containerChoice.container().id(), 1, Integer::sum);
        }

        if (selected.isEmpty()) {
            throw new IllegalStateException(
                    "No generated module parameter entries available for "
                            + tier + " at size " + size.width() + "x" + size.height()
            );
        }

        return new GeneratedModulePropertyPlan(tier, size, selected);
    }

    private static ContainerChoice weightedContainer(
            List<ContainerChoice> choices,
            RandomSource random
    ) {
        int totalWeight = choices.stream()
                .mapToInt(choice -> choice.container().weight())
                .sum();
        int roll = random.nextInt(totalWeight);
        for (ContainerChoice choice : choices) {
            roll -= choice.container().weight();
            if (roll < 0) return choice;
        }
        return choices.get(choices.size() - 1);
    }

    private static GeneratedModulePropertyRegistry.PropertyRule weightedEntry(
            List<GeneratedModulePropertyRegistry.PropertyRule> entries,
            RandomSource random
    ) {
        int totalWeight = entries.stream()
                .mapToInt(GeneratedModulePropertyRegistry.PropertyRule::weight)
                .sum();
        int roll = random.nextInt(totalWeight);
        for (GeneratedModulePropertyRegistry.PropertyRule entry : entries) {
            roll -= entry.weight();
            if (roll < 0) return entry;
        }
        return entries.get(entries.size() - 1);
    }

    public static GeneratedModuleData generate(
            EquipmentTier tier,
            long seed
    ) {
        GeneratedModulePropertyPlan plan = propertyPlan(tier, seed);
        ResourceLocation baseDefinitionId = prototypeDefinitionId(tier);

        ModuleDefinition definition = GeneratedModuleDefinitionBuilderV2.build(plan, seed);

        GeneratedModuleData generated = new GeneratedModuleData(
                seed,
                baseDefinitionId,
                definition
        );
        ModModules.registerGeneratedData(generated);
        return generated;
    }

    public static ItemStack createStack(
            EquipmentTier tier,
            long seed
    ) {
        GeneratedModuleData generated = generate(tier, seed);
        var entry = ModModules.find(generated.baseDefinitionId());
        if (entry == null) {
            throw new IllegalStateException(
                    "Missing prototype module base: " + generated.baseDefinitionId()
            );
        }

        ItemStack stack = entry.getItem().getDefaultInstance();
        stack.set(ModDataComponents.GENERATED_MODULE_DATA.get(), generated);
        return stack;
    }

    public static ItemStack createStack(
            EquipmentTier tier,
            RandomSource random
    ) {
        if (random == null) {
            throw new IllegalArgumentException("Random source cannot be null");
        }
        return createStack(tier, random.nextLong());
    }

    public static ResourceLocation prototypeDefinitionId(EquipmentTier tier) {
        validateTier(tier);
        String path = switch (tier) {
            case MILITARY -> "military_prototype";
            case ENGINEERING -> "engineering_prototype";
            case EXPERIMENTAL -> "experimental_prototype";
            default -> throw new IllegalArgumentException(
                    "Unsupported generated module tier: " + tier
            );
        };
        return ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, path);
    }

    public static double integrationMultiplier(int propertyCount) {
        return GeneratedModuleRules.settings().integrationMultiplier(propertyCount);
    }

    static ModuleSize rollSize(RandomSource random) {
        return GeneratedModuleRules.settings().rollSize(random);
    }

    static int rollPropertyCount(int area, RandomSource random) {
        return GeneratedModuleRules.settings().rollPropertyCount(area, random);
    }

    private static void validateTier(EquipmentTier tier) {
        if (tier != EquipmentTier.MILITARY
                && tier != EquipmentTier.ENGINEERING
                && tier != EquipmentTier.EXPERIMENTAL) {
            throw new IllegalArgumentException(
                    "Generated modules are only available for military, engineering and experimental tiers"
            );
        }
    }

    private record ContainerChoice(
            GeneratedModulePropertyRegistry.ContainerRule container,
            List<GeneratedModulePropertyRegistry.PropertyRule> entries
    ) {}
}
