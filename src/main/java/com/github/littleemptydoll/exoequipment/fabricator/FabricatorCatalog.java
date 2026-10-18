package com.github.littleemptydoll.exoequipment.fabricator;

import com.github.littleemptydoll.exoequipment.item.GeneratedModuleItem;
import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.registry.EquipmentItem;
import com.github.littleemptydoll.exoequipment.registry.ModItems;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/** Derives the Fabricator's choices from registered items, without duplicating item lists. */
public final class FabricatorCatalog {
    private FabricatorCatalog() {}

    public static List<Item> entries(FabricatorCategory category) {
        List<Item> result = new ArrayList<>();
        for (var holder : ModItems.ITEMS.getEntries()) {
            Item item = holder.get();
            if (item instanceof GeneratedModuleItem) {
                continue;
            }
            if ((category == FabricatorCategory.COMPONENTS && ModItems.isFabricatorPart(item))
                    || (item instanceof EquipmentItem<?> equipment
                    && equipment.getDefinition().tier() != EquipmentTier.CREATIVE
                    && FabricatorCategory.of(item) == category)) {
                result.add(item);
            }
        }
        return List.copyOf(result);
    }

    /**
     * Only show module subcategories that actually have a Fabricator recipe.
     * The generated-prototype category is not craftable, but its enum value
     * must remain registered for generated modules and other systems.
     */
    public static List<ModuleCategory> craftableModuleCategories(
            Iterable<RecipeHolder<FabricatorRecipe>> recipes
    ) {
        EnumSet<ModuleCategory> categories = EnumSet.noneOf(ModuleCategory.class);
        for (RecipeHolder<FabricatorRecipe> holder : recipes) {
            Item item = holder.value().result().getItem();
            if (item instanceof ModuleItem module && !(item instanceof GeneratedModuleItem)) {
                categories.add(module.getDefinition().category());
            }
        }
        return List.copyOf(categories);
    }

    public static List<ModuleItem> modules(ModuleCategory category) {
        List<ModuleItem> result = new ArrayList<>();
        for (Item item : entries(FabricatorCategory.MODULE)) {
            ModuleItem module = (ModuleItem) item;
            if (module.getDefinition().category() == category) {
                result.add(module);
            }
        }
        return List.copyOf(result);
    }
}
