package com.github.littleemptydoll.exoequipment.fabricator;

import com.github.littleemptydoll.exoequipment.item.ControllerItem;
import com.github.littleemptydoll.exoequipment.item.EnergySystemItem;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.item.FrameItem;
import com.github.littleemptydoll.exoequipment.item.MatrixItem;
import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.registry.ModItems;
import net.minecraft.world.item.Item;

/** Fabricator categories; modules keep their own subcategories. */
public enum FabricatorCategory {
    COMPONENTS, EXOSKELETON, FRAME, CONTROLLER, ENERGY_SYSTEM, MATRIX, MODULE;

    public static FabricatorCategory of(Item item) {
        if (item instanceof ExoskeletonItem) return EXOSKELETON;
        if (item instanceof FrameItem) return FRAME;
        if (item instanceof ControllerItem) return CONTROLLER;
        if (item instanceof EnergySystemItem) return ENERGY_SYSTEM;
        if (item instanceof MatrixItem) return MATRIX;
        if (item instanceof ModuleItem) return MODULE;
        if (ModItems.isFabricatorPart(item)) return COMPONENTS;
        throw new IllegalArgumentException("Not Fabricator equipment: " + item);
    }

    public String translationKey() {
        return "gui.exoequipment.fabricator.category." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
