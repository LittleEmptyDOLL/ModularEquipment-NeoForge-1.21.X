package com.github.littleemptydoll.exoequipment.fabricator;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class FabricatorRecipeSerializer implements RecipeSerializer<FabricatorRecipe> {
    @Override
    public MapCodec<FabricatorRecipe> codec() {
        return FabricatorRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FabricatorRecipe> streamCodec() {
        return FabricatorRecipe.STREAM_CODEC;
    }
}
