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

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, path);
    }

}
