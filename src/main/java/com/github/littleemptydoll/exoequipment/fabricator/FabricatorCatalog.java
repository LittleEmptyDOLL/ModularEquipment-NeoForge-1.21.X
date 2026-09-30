package com.github.littleemptydoll.exoequipment.fabricator;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.registry.EquipmentItem;
import com.github.littleemptydoll.exoequipment.registry.ModItems;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

/** Derives the Fabricator's choices from registered items, without duplicating item lists. */
public final class FabricatorCatalog {
    private FabricatorCatalog() {}

    public static List<Item> entries(FabricatorCategory category) {
        List<Item> result = new ArrayList<>();
        for (var holder : ModItems.ITEMS.getEntries()) {
            Item item = holder.get();
            if ((category == FabricatorCategory.COMPONENTS && ModItems.isFabricatorPart(item))
                    || (item instanceof EquipmentItem<?> equipment
                    && equipment.getDefinition().tier() != EquipmentTier.CREATIVE
                    && FabricatorCategory.of(item) == category)) {
                result.add(item);
            }
        }
        return List.copyOf(result);
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
