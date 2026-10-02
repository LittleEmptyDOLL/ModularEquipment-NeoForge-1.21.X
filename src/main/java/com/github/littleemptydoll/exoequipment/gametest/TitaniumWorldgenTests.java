package com.github.littleemptydoll.exoequipment.gametest;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.registry.ModBlocks;
import com.github.littleemptydoll.exoequipment.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;

/** Checks datapack registries after a real server has loaded them. */
@GameTestHolder(ExoEquipment.MODID)
public final class TitaniumWorldgenTests {
    private TitaniumWorldgenTests() {}

    @GameTest(template = "empty")
    public static void titaniumLoads(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        var configured = registries.registryOrThrow(Registries.CONFIGURED_FEATURE);
        var placed = registries.registryOrThrow(Registries.PLACED_FEATURE);
        helper.assertTrue(configured.containsKey(id("titanium_ore")),
                "Missing titanium configured feature");
        helper.assertTrue(placed.containsKey(id("titanium_ore_deep")),
                "Missing deep titanium placement");
        helper.assertTrue(placed.containsKey(id("titanium_ore_high")),
                "Missing high titanium placement");
        helper.assertTrue(helper.getLevel().getRecipeManager()
                        .byKey(id("titanium_ingot_from_blasting")).isPresent(),
                "Missing titanium blasting recipe");
        helper.assertTrue(helper.getLevel().getRecipeManager()
                        .byKey(id("structural_component_titanium")).isPresent(),
                "Missing titanium Fabricator recipe");
        helper.assertTrue(ModBlocks.RAW_TITANIUM_BLOCK.get().asItem() == ModItems.RAW_TITANIUM_BLOCK.get(),
                "Raw titanium block item was not registered");
        helper.assertTrue(ModBlocks.TITANIUM_BLOCK.get().asItem() == ModItems.TITANIUM_BLOCK.get(),
                "Titanium block item was not registered");
        for (String recipe : new String[] {"raw_titanium_block", "raw_titanium_from_block",
                "titanium_block", "titanium_ingots_from_block", "titanium_nuggets_from_ingot",
                "titanium_ingot_from_nuggets"}) {
            helper.assertTrue(helper.getLevel().getRecipeManager().byKey(id(recipe)).isPresent(),
                    "Missing titanium crafting recipe: " + recipe);
        }
        helper.succeed();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, path);
    }
}
