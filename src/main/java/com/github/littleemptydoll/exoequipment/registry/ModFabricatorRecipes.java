package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.fabricator.FabricatorRecipe;
import com.github.littleemptydoll.exoequipment.fabricator.FabricatorRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModFabricatorRecipes {
    private static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, "fabricator");
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, ExoEquipment.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ExoEquipment.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<FabricatorRecipe>> TYPE =
            TYPES.register("fabricator", () -> RecipeType.<FabricatorRecipe>simple(ID));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FabricatorRecipe>> SERIALIZER =
            SERIALIZERS.register("fabricator", FabricatorRecipeSerializer::new);

    private ModFabricatorRecipes() {}

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        SERIALIZERS.register(bus);
    }
}
