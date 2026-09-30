package com.github.littleemptydoll.exoequipment.fabricator;

import com.github.littleemptydoll.exoequipment.registry.ModItems;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.world.item.ItemStack;

/** Server-side tier rule shared by the future recipe menu and its upgrade slots. */
public final class FabricatorUnlocks {
    public static final int MILITARY_SLOT = 0;
    public static final int ENGINEERING_SLOT = 1;
    public static final int EXPERIMENTAL_SLOT = 2;
    public static final int UPGRADE_SLOT_COUNT = 3;

    private FabricatorUnlocks() {}

    public static boolean accepts(int slot, ItemStack stack) {
        if (stack.isEmpty()) return slot >= 0 && slot < UPGRADE_SLOT_COUNT;
        return switch (slot) {
            case MILITARY_SLOT -> stack.is(ModItems.MILITARY_FABRICATOR_UPGRADE.get());
            case ENGINEERING_SLOT -> stack.is(ModItems.ENGINEERING_FABRICATOR_UPGRADE.get());
            case EXPERIMENTAL_SLOT -> stack.is(ModItems.EXPERIMENTAL_FABRICATOR_UPGRADE.get());
            default -> false;
        };
    }

    public static boolean unlocked(EquipmentTier tier, ItemStack military,
                                   ItemStack engineering, ItemStack experimental) {
        boolean hasMilitary = military.is(ModItems.MILITARY_FABRICATOR_UPGRADE.get());
        boolean hasEngineering = engineering.is(ModItems.ENGINEERING_FABRICATOR_UPGRADE.get());
        return switch (tier) {
            case BASIC, CIVILIAN -> true;
            case MILITARY -> hasMilitary;
            case ENGINEERING -> hasEngineering;
            case EXPERIMENTAL -> hasMilitary && hasEngineering
                    && experimental.is(ModItems.EXPERIMENTAL_FABRICATOR_UPGRADE.get());
            case CREATIVE -> false;
        };
    }
}
