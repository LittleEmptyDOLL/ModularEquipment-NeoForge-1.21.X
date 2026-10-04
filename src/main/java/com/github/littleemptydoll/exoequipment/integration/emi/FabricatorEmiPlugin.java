package com.github.littleemptydoll.exoequipment.integration.emi;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.fabricator.FabricatorRecipe;
import com.github.littleemptydoll.exoequipment.registry.ModBlocks;
import com.github.littleemptydoll.exoequipment.registry.ModFabricatorRecipes;
import com.github.littleemptydoll.exoequipment.util.NumberFormatter;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@EmiEntrypoint
public final class FabricatorEmiPlugin implements EmiPlugin {
    private static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, "fabricator"),
            EmiStack.of(ModBlocks.FABRICATOR.get()));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(ModBlocks.FABRICATOR.get()));
        registry.getRecipeManager().getAllRecipesFor(ModFabricatorRecipes.TYPE.get())
                .forEach(holder -> registry.addRecipe(new Entry(holder.id(), holder.value())));
    }

    private record Entry(ResourceLocation id, FabricatorRecipe recipe) implements EmiRecipe {
        @Override
        public EmiRecipeCategory getCategory() { return CATEGORY; }

        @Override
        public ResourceLocation getId() { return id; }

        @Override
        public List<EmiIngredient> getInputs() {
            return recipe.requirements().stream()
                    .map(entry -> EmiIngredient.of(entry.ingredient(), entry.count())).toList();
        }

        @Override
        public List<EmiStack> getOutputs() { return List.of(EmiStack.of(recipe.result())); }

        @Override
        public int getDisplayWidth() { return 140; }

        @Override
        public int getDisplayHeight() { return 82; }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            List<EmiIngredient> inputs = getInputs();
            for (int i = 0; i < inputs.size(); i++) {
                widgets.addSlot(inputs.get(i), 4 + (i % 4) * 20, 5 + (i / 4) * 20);
            }
            widgets.addSlot(getOutputs().getFirst(), 110, 15).recipeContext(this);
            widgets.addText(Component.translatable("gui.exoequipment.fabricator.cost",
                            NumberFormatter.format(recipe.energyCost())),
                    4, 66, 0xFFDEE6E9, false);
        }
    }
}
