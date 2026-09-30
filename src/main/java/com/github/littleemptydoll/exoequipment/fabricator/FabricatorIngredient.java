package com.github.littleemptydoll.exoequipment.fabricator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;

/** One ingredient alternative (item or tag) and the number of items to consume. */
public record FabricatorIngredient(Ingredient ingredient, int count) {
    public static final Codec<FabricatorIngredient> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(FabricatorIngredient::ingredient),
                    Codec.intRange(1, 64).fieldOf("count").forGetter(FabricatorIngredient::count)
            ).apply(instance, FabricatorIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FabricatorIngredient> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, entry) -> {
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, entry.ingredient());
                        buffer.writeVarInt(entry.count());
                    },
                    buffer -> new FabricatorIngredient(
                            Ingredient.CONTENTS_STREAM_CODEC.decode(buffer), buffer.readVarInt())
            );
}
