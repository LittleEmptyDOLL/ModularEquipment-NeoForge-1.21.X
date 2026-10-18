package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;

import java.util.List;

/**
 * Exercises loaded generated-module datapack definitions in a real server.
 * The static JSON checker cannot validate runtime registries or record codecs.
 */
@GameTestHolder(ExoEquipment.MODID)
public final class GeneratedModuleGameTests {
    private GeneratedModuleGameTests() {}

    @GameTest(template = "empty")
    public static void generatedModulesCanBuild(GameTestHelper helper) {
        helper.assertTrue(!GeneratedModulePropertyRegistry.containers().isEmpty(),
                "Generated module rules did not load");

        long seed = 0;
        int isolated = 0;
        for (EquipmentTier tier : new EquipmentTier[]{
                EquipmentTier.ENGINEERING,
                EquipmentTier.MILITARY,
                EquipmentTier.EXPERIMENTAL
        }) {
            // Verify every available rule, even very rare property combinations.
            for (GeneratedModulePropertyRegistry.ContainerRule container
                    : GeneratedModulePropertyRegistry.containers()) {
                for (GeneratedModulePropertyRegistry.PropertyRule rule
                        : container.candidates(tier, 16, List.of())) {
                    ResourceLocation id = rule.id();
                    GeneratedModulePropertyPlan plan = new GeneratedModulePropertyPlan(
                            tier, new ModuleSize(4, 4), List.of(id)
                    );
                    ModuleDefinition definition =
                            GeneratedModuleDefinitionBuilderV2.build(plan, seed++);
                    helper.assertTrue(definition.category() == ModuleCategory.EXPERIMENTAL,
                            "Incorrect generated category for " + id);
                    isolated++;
                }
            }

            // Repeatedly combine independent container selections and ensure
            // their final ModuleDefinition is valid.
            for (long value = 0; value < 128; value++) {
                GeneratedModulePropertyPlan plan =
                        GeneratedModuleGenerator.propertyPlan(tier, value * 997L + 17L);
                ModuleDefinition definition =
                        GeneratedModuleDefinitionBuilderV2.build(plan, value * 997L + 17L);
                helper.assertTrue(definition.size().equals(plan.size()),
                        "Generated module size differs from its plan");
                helper.assertTrue(definition.properties().tier() == tier,
                        "Generated module tier differs from its plan");
            }
        }

        helper.assertTrue(isolated > 0, "No individual generated rules were exercised");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void directRegistryIdsResolve(GameTestHelper helper) {
        ModuleSize size = new ModuleSize(4, 4);
        ResourceLocation armor = id("attribute_properties/attributes/minecraft/generic.armor");
        ResourceLocation nightVision = id("effects_properties/effects/minecraft/night_vision");
        ResourceLocation ores = id("block_scanner_properties/scanner_targets/tag/c/ores");
        ResourceLocation projectile = id("damage_reduction_properties/damage_reductions/tag/minecraft/is_projectile/0");
        ResourceLocation harmful = id("status_protection_properties/status_protections/harmful");

        ModuleDefinition armorModule = GeneratedModuleDefinitionBuilderV2.build(
                new GeneratedModulePropertyPlan(EquipmentTier.MILITARY, size, List.of(armor)), 100L
        );
        helper.assertTrue(armorModule.attributes().isPresent()
                        && armorModule.attributes().get().attributes().containsKey(
                                ResourceLocation.parse("minecraft:generic.armor")),
                "Direct armor attribute registry ID was not applied");

        ModuleDefinition effectModule = GeneratedModuleDefinitionBuilderV2.build(
                new GeneratedModulePropertyPlan(EquipmentTier.EXPERIMENTAL, size, List.of(nightVision)), 101L
        );
        helper.assertTrue(effectModule.effects().isPresent()
                        && effectModule.effects().get().effects().containsKey(
                                ResourceLocation.parse("minecraft:night_vision")),
                "Direct night-vision effect registry ID was not applied");

        ModuleDefinition scannerModule = GeneratedModuleDefinitionBuilderV2.build(
                new GeneratedModulePropertyPlan(EquipmentTier.ENGINEERING, size, List.of(ores)), 102L
        );
        helper.assertTrue(scannerModule.blockScanner().isPresent()
                        && scannerModule.blockScanner().get().tags().contains(
                                ResourceLocation.parse("c:ores")),
                "Direct scanner block-tag registry ID was not applied");

        ModuleDefinition damageModule = GeneratedModuleDefinitionBuilderV2.build(
                new GeneratedModulePropertyPlan(EquipmentTier.MILITARY, size, List.of(projectile)), 103L
        );
        helper.assertTrue(damageModule.damageReduction().isPresent()
                        && damageModule.damageReduction().get().tagReductions().containsKey(
                                ResourceLocation.parse("minecraft:is_projectile")),
                "Direct damage tag registry ID was not applied");

        ModuleDefinition statusModule = GeneratedModuleDefinitionBuilderV2.build(
                new GeneratedModulePropertyPlan(EquipmentTier.ENGINEERING, size, List.of(harmful)), 104L
        );
        helper.assertTrue(statusModule.statusProtection().isPresent()
                        && statusModule.statusProtection().get().harmfulProtection() > 0.0D,
                "Direct harmful-effect protection was not applied");

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void generatedEffectVariantsApply(GameTestHelper helper) {
        ModuleSize size = new ModuleSize(4, 4);
        ResourceLocation haste = id("effects_properties/effects/minecraft/haste");
        ResourceLocation regeneration = id("effects_properties/effects/minecraft/regeneration");

        ModuleDefinition engineering = GeneratedModuleDefinitionBuilderV2.build(
                new GeneratedModulePropertyPlan(EquipmentTier.ENGINEERING, size, List.of(haste)), 201L
        );
        helper.assertTrue(engineering.effects().isPresent()
                        && engineering.effects().get().effects().get(
                                ResourceLocation.parse("minecraft:haste")) == 0,
                "Engineering Haste must be effect level I");

        ModuleDefinition experimental = GeneratedModuleDefinitionBuilderV2.build(
                new GeneratedModulePropertyPlan(EquipmentTier.EXPERIMENTAL, size,
                        List.of(haste, regeneration)), 202L
        );
        helper.assertTrue(experimental.effects().isPresent(),
                "Experimental effects were not generated");
        helper.assertTrue(Integer.valueOf(1).equals(experimental.effects().get().effects().get(
                        ResourceLocation.parse("minecraft:haste"))),
                "Experimental Haste must be effect level II");
        helper.assertTrue(Integer.valueOf(0).equals(experimental.effects().get().effects().get(
                        ResourceLocation.parse("minecraft:regeneration"))),
                "Experimental Regeneration must be effect level I");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void generatedScannerVariantsResolve(GameTestHelper helper) {
        ModuleSize size = new ModuleSize(4, 4);
        ResourceLocation oreSelection = id(
                "block_scanner_properties/scanner_targets/tag/c/ores/diamond");
        ResourceLocation spawnerSelection = id(
                "block_scanner_properties/scanner_targets/minecraft/spawner");
        ResourceLocation vaultSelection = id(
                "block_scanner_properties/scanner_targets/minecraft/vault");

        ModuleDefinition ores = GeneratedModuleDefinitionBuilderV2.build(
                new GeneratedModulePropertyPlan(EquipmentTier.EXPERIMENTAL, size,
                        List.of(oreSelection)), 301L
        );
        ModuleDefinition spawners = GeneratedModuleDefinitionBuilderV2.build(
                new GeneratedModulePropertyPlan(EquipmentTier.MILITARY, size,
                        List.of(spawnerSelection)), 302L
        );
        ModuleDefinition vaults = GeneratedModuleDefinitionBuilderV2.build(
                new GeneratedModulePropertyPlan(EquipmentTier.EXPERIMENTAL, size,
                        List.of(vaultSelection)), 303L
        );

        helper.assertTrue(ores.blockScanner().isPresent()
                        && ores.blockScanner().get().tags().contains(
                                ResourceLocation.parse("c:ores/diamond")),
                "Specialized ore scanner tag missing");
        helper.assertTrue(spawners.blockScanner().isPresent()
                        && spawners.blockScanner().get().blocks().contains(
                                ResourceLocation.parse("minecraft:spawner")),
                "Spawner scanner block missing");
        helper.assertTrue(vaults.blockScanner().isPresent()
                        && vaults.blockScanner().get().blocks().contains(
                                ResourceLocation.parse("minecraft:vault")),
                "Vault scanner block missing");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void generatedMultipleEffectsConsumeMoreEnergy(GameTestHelper helper) {
        ModuleSize size = new ModuleSize(4, 4);
        ResourceLocation haste = id("effects_properties/effects/minecraft/haste");
        ResourceLocation nightVision = id("effects_properties/effects/minecraft/night_vision");
        ResourceLocation waterBreathing = id("effects_properties/effects/minecraft/water_breathing");

        helper.assertTrue(GeneratedModulePropertyRegistry.containers().stream().anyMatch(
                        container -> container.id().equals(id("effects_properties"))
                                && container.maxRolls() == 3),
                "Expected a limit of three passive effects");

        ModuleDefinition single = GeneratedModuleDefinitionBuilderV2.build(
                new GeneratedModulePropertyPlan(EquipmentTier.EXPERIMENTAL, size,
                        List.of(haste)), 401L
        );
        ModuleDefinition combined = GeneratedModuleDefinitionBuilderV2.build(
                new GeneratedModulePropertyPlan(EquipmentTier.EXPERIMENTAL, size,
                        List.of(haste, nightVision, waterBreathing)), 401L
        );

        helper.assertTrue(combined.effects().isPresent()
                        && combined.effects().get().effects().size() == 3,
                "All three passive effects must be present");
        helper.assertTrue(combined.energy().isPresent() && single.energy().isPresent()
                        && combined.energy().get().consumption() > single.energy().get().consumption(),
                "Combining effects must increase passive energy consumption");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void generatedMultipleScannerTargetsConsumeMoreEnergy(GameTestHelper helper) {
        ModuleSize size = new ModuleSize(4, 4);
        ResourceLocation iron = id("block_scanner_properties/scanner_targets/tag/c/ores/iron");
        ResourceLocation gold = id("block_scanner_properties/scanner_targets/tag/c/ores/gold");
        ResourceLocation spawner = id("block_scanner_properties/scanner_targets/minecraft/spawner");
        ResourceLocation vault = id("block_scanner_properties/scanner_targets/minecraft/vault");

        helper.assertTrue(GeneratedModulePropertyRegistry.containers().stream().anyMatch(
                        container -> container.id().equals(id("block_scanner_properties"))
                                && container.maxRolls() == 4),
                "Expected a limit of four scanner targets");
        helper.assertTrue(GeneratedModulePropertyRegistry.containers().stream().anyMatch(
                        container -> container.id().equals(id("attribute_properties"))
                                && container.maxRolls() == 6),
                "Expected a limit of six attributes");

        ModuleDefinition single = GeneratedModuleDefinitionBuilderV2.build(
                new GeneratedModulePropertyPlan(EquipmentTier.EXPERIMENTAL, size,
                        List.of(iron)), 501L
        );
        ModuleDefinition combined = GeneratedModuleDefinitionBuilderV2.build(
                new GeneratedModulePropertyPlan(EquipmentTier.EXPERIMENTAL, size,
                        List.of(iron, gold, spawner, vault)), 501L
        );

        helper.assertTrue(combined.blockScanner().isPresent(),
                "Multi-target scanner was not generated");
        helper.assertTrue(combined.blockScanner().get().tags().size() == 2
                        && combined.blockScanner().get().blocks().size() == 2,
                "All four scanner targets must be present");
        helper.assertTrue(combined.blockScanner().get().activeConsumption()
                        > single.blockScanner().orElseThrow().activeConsumption(),
                "Combining scanner targets must increase active energy consumption");
        helper.assertTrue(combined.energy().isPresent() && single.energy().isPresent()
                        && combined.energy().get().consumption() > single.energy().get().consumption(),
                "Combining scanner targets must increase passive energy consumption");
        helper.succeed();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, path);
    }

}
