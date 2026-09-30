package com.github.littleemptydoll.exoequipment.fabricator;

import com.github.littleemptydoll.exoequipment.registry.ModFabricatorRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/** Instant recipe drawn directly from the player's inventory. */
public record FabricatorRecipe(List<FabricatorIngredient> requirements, ItemStack result, int energyCost)
        implements Recipe<CraftingInput> {
    public static final MapCodec<FabricatorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    FabricatorIngredient.CODEC.listOf().fieldOf("ingredients")
                            .forGetter(FabricatorRecipe::requirements),
                    ItemStack.CODEC.fieldOf("result").forGetter(FabricatorRecipe::result),
                    Codec.intRange(1, 1_000_000).fieldOf("energy").forGetter(FabricatorRecipe::energyCost)
            ).apply(instance, FabricatorRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FabricatorRecipe> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, recipe) -> {
                        buffer.writeVarInt(recipe.requirements().size());
                        for (FabricatorIngredient ingredient : recipe.requirements()) {
                            FabricatorIngredient.STREAM_CODEC.encode(buffer, ingredient);
                        }
                        ItemStack.STREAM_CODEC.encode(buffer, recipe.result());
                        buffer.writeVarInt(recipe.energyCost());
                    },
                    buffer -> {
                        int count = buffer.readVarInt();
                        if (count < 0 || count > 32) throw new IllegalArgumentException("Too many Fabricator ingredients");
                        List<FabricatorIngredient> entries = new ArrayList<>(count);
                        for (int i = 0; i < count; i++) {
                            entries.add(FabricatorIngredient.STREAM_CODEC.decode(buffer));
                        }
                        return new FabricatorRecipe(entries, ItemStack.STREAM_CODEC.decode(buffer), buffer.readVarInt());
                    }
            );

    public FabricatorRecipe {
        requirements = List.copyOf(requirements);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        // Fabricator inputs live in the player's inventory, not in a crafting grid.
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        for (FabricatorIngredient entry : requirements) list.add(entry.ingredient());
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModFabricatorRecipes.SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModFabricatorRecipes.TYPE.get();
    }
}
