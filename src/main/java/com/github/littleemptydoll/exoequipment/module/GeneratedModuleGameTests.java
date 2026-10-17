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
}
