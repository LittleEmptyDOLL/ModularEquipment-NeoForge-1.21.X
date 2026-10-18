package com.github.littleemptydoll.exoequipment.fabricator;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.GeneratedModuleItem;
import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.registry.ModFabricatorRecipes;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;

import java.util.List;

/** Fabricator tabs must represent actual craftable recipes, not every module kind. */
@GameTestHolder(ExoEquipment.MODID)
public final class FabricatorGameTests {
    private FabricatorGameTests() {}

    @GameTest(template = "empty")
    public static void moduleTabsOnlyShowCraftableCategories(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager()
                .getAllRecipesFor(ModFabricatorRecipes.TYPE.get());
        List<ModuleCategory> categories =
                FabricatorCatalog.craftableModuleCategories(recipes);

        helper.assertTrue(!categories.isEmpty(),
                "The Fabricator must expose craftable module categories");
        helper.assertTrue(!categories.contains(ModuleCategory.EXPERIMENTAL),
                "Generated prototypes must not create an empty Fabricator category");

        for (ModuleCategory category : categories) {
            helper.assertTrue(recipes.stream().anyMatch(holder -> {
                var item = holder.value().result().getItem();
                return item instanceof ModuleItem module
                        && !(item instanceof GeneratedModuleItem)
                        && module.getDefinition().category() == category;
            }), "Visible Fabricator module category has no recipe: " + category);
        }
        helper.succeed();
    }
}
