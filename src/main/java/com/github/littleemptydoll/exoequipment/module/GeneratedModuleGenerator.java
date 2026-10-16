package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class GeneratedModuleGenerator {
    private GeneratedModuleGenerator() {}

    public static GeneratedModulePlan plan(
            EquipmentTier tier,
            long seed
    ) {
        validateTier(tier);

        RandomSource random = RandomSource.create(seed);
        ModuleSize size = rollSize(random);
        int area = size.width() * size.height();
        int requestedProperties = rollPropertyCount(area, random);

        List<GeneratedPropertyType> candidates = new ArrayList<>();
        for (GeneratedPropertyType property : GeneratedPropertyType.values()) {
            if (property.isAvailableFor(tier)
                    && property.minArea() <= area) {
                candidates.add(property);
            }
        }

        List<GeneratedPropertyType> selected = new ArrayList<>();
        while (!candidates.isEmpty()
                && selected.size() < requestedProperties) {
            int selectedIndex = random.nextInt(candidates.size());
            GeneratedPropertyType property = candidates.remove(selectedIndex);

            boolean conflicts = selected.stream()
                    .anyMatch(existing -> existing.conflictsWith(property));
            if (conflicts) {
                continue;
            }

            selected.add(property);
            candidates.removeIf(candidate ->
                    candidate == property || candidate.conflictsWith(property));
        }

        if (selected.isEmpty()) {
            throw new IllegalStateException(
                    "No generated module properties available for "
                            + tier + " at size " + size.width() + "x" + size.height()
            );
        }

        return new GeneratedModulePlan(tier, size, selected);
    }

    public static GeneratedModuleData generate(
            EquipmentTier tier,
            long seed
    ) {
        GeneratedModulePlan plan = plan(tier, seed);
        ResourceLocation baseDefinitionId = prototypeDefinitionId(tier);

        ModuleDefinition definition = DataDrivenGeneratedModuleDefinitionBuilder.build(plan, seed);
        definition = GeneratedModuleVariantRandomizer.apply(definition, plan, seed);

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
}
