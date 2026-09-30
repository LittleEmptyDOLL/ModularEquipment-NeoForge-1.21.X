package com.github.littleemptydoll.exoequipment.fabricator;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.registry.EquipmentItem;
import com.github.littleemptydoll.exoequipment.registry.ModItems;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;

import java.util.ArrayList;
import java.util.List;

/** Derives the Fabricator's choices from registered equipment, without duplicating item lists. */
public final class FabricatorCatalog {
    private FabricatorCatalog() {}

    public static List<EquipmentItem<?>> entries(FabricatorCategory category) {
        List<EquipmentItem<?>> result = new ArrayList<>();
        for (var holder : ModItems.ITEMS.getEntries()) {
            if (holder.get() instanceof EquipmentItem<?> item
                    && item.getDefinition().tier() != EquipmentTier.CREATIVE
                    && FabricatorCategory.of(item) == category) {
                result.add(item);
            }
        }
        return List.copyOf(result);
    }

    public static List<ModuleItem> modules(ModuleCategory category) {
        List<ModuleItem> result = new ArrayList<>();
        for (EquipmentItem<?> item : entries(FabricatorCategory.MODULE)) {
            ModuleItem module = (ModuleItem) item;
            if (module.getDefinition().category() == category) {
                result.add(module);
            }
        }
        return List.copyOf(result);
    }
}
