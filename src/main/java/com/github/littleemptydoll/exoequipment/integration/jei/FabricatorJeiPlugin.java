package com.github.littleemptydoll.exoequipment.integration.jei;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.fabricator.FabricatorRecipe;
import com.github.littleemptydoll.exoequipment.registry.ModBlocks;
import com.github.littleemptydoll.exoequipment.registry.ModFabricatorRecipes;
import com.github.littleemptydoll.exoequipment.util.NumberFormatter;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

@JeiPlugin
public final class FabricatorJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, "fabricator");
    private static final RecipeType<FabricatorRecipe> TYPE =
            RecipeType.create(ExoEquipment.MODID, "fabricator", FabricatorRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IDrawable icon = registration.getJeiHelpers().getGuiHelper()
                .createDrawableItemStack(new ItemStack(ModBlocks.FABRICATOR.get()));
        registration.addRecipeCategories(new Category(icon));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level != null) {
            registration.addRecipes(TYPE, level.getRecipeManager()
                    .getAllRecipesFor(ModFabricatorRecipes.TYPE.get())
                    .stream().map(holder -> holder.value()).toList());
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModBlocks.FABRICATOR.get(), TYPE);
    }

    private record Category(IDrawable icon) implements IRecipeCategory<FabricatorRecipe> {
        @Override
        public RecipeType<FabricatorRecipe> getRecipeType() { return TYPE; }

        @Override
        public Component getTitle() { return Component.translatable("block.exoequipment.fabricator"); }

        @Override
        public int getWidth() { return 140; }

        @Override
        public int getHeight() { return 82; }

        @Override
        public IDrawable getIcon() { return icon; }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, FabricatorRecipe recipe, IFocusGroup focuses) {
            for (int i = 0; i < recipe.requirements().size(); i++) {
                var entry = recipe.requirements().get(i);
                builder.addInputSlot(4 + (i % 4) * 20, 5 + (i / 4) * 20)
                        .addItemStacks(Arrays.stream(entry.ingredient().getItems())
                                .map(stack -> stack.copyWithCount(entry.count())).toList());
            }
            builder.addOutputSlot(110, 15).addItemStack(recipe.result());
        }

        @Override
        public void draw(FabricatorRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                         double mouseX, double mouseY) {
            graphics.drawString(Minecraft.getInstance().font,
                    Component.translatable("gui.exoequipment.fabricator.cost",
                            NumberFormatter.format(recipe.energyCost())),
                    4, 66, 0xFFDEE6E9, false);
        }
    }
}
