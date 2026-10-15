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
    private static final List<ModuleSize> SIZES = List.of(
            new ModuleSize(1, 1),
            new ModuleSize(1, 2),
            new ModuleSize(1, 3),
            new ModuleSize(2, 2),
            new ModuleSize(1, 4),
            new ModuleSize(2, 3),
            new ModuleSize(2, 4),
            new ModuleSize(3, 3),
            new ModuleSize(3, 4),
            new ModuleSize(4, 4)
    );

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
            if (property.isAvailableFor(tier) && property.minArea() <= area) {
                candidates.add(property);
            }
        }

        List<GeneratedPropertyType> selected = new ArrayList<>();
        while (!candidates.isEmpty() && selected.size() < requestedProperties) {
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
        ModuleDefinition definition = GeneratedModuleDefinitionBuilder.build(plan, seed);
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

    public static ResourceLocation prototypeDefinitionId(
            EquipmentTier tier
    ) {
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
        return switch (Math.max(1, propertyCount)) {
            case 1 -> 1.0D;
            case 2 -> 1.20D;
            case 3 -> 1.45D;
            case 4 -> 1.75D;
            case 5 -> 2.15D;
            default -> 2.65D;
        };
    }

    static ModuleSize rollSize(RandomSource random) {
        // Smaller prototypes remain more common, while 3x4 and 4x4 are
        // deliberately uncommon because their property budget is much larger.
        int roll = random.nextInt(100);
        if (roll < 18) return SIZES.get(0); // 1x1
        if (roll < 34) return SIZES.get(1); // 1x2
        if (roll < 44) return SIZES.get(2); // 1x3
        if (roll < 58) return SIZES.get(3); // 2x2
        if (roll < 64) return SIZES.get(4); // 1x4
        if (roll < 76) return SIZES.get(5); // 2x3
        if (roll < 85) return SIZES.get(6); // 2x4
        if (roll < 93) return SIZES.get(7); // 3x3
        if (roll < 98) return SIZES.get(8); // 3x4
        return SIZES.get(9);                // 4x4
    }

    static int rollPropertyCount(
            int area,
            RandomSource random
    ) {
        if (area <= 1) {
            return random.nextFloat() < 0.15F ? 2 : 1;
        }
        if (area <= 3) {
            return 2;
        }
        if (area <= 5) {
            return 2 + random.nextInt(2);
        }
        if (area <= 8) {
            return 3 + random.nextInt(2);
        }
        if (area <= 12) {
            return 4 + random.nextInt(2);
        }
        return 5 + random.nextInt(2);
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
