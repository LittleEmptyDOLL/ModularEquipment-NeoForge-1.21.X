package com.github.littleemptydoll.exoequipment.fabricator;

import com.github.littleemptydoll.exoequipment.item.ControllerItem;
import com.github.littleemptydoll.exoequipment.item.EnergySystemItem;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.item.FrameItem;
import com.github.littleemptydoll.exoequipment.item.MatrixItem;
import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import net.minecraft.world.item.Item;

/** The six top-level Fabricator categories; modules keep their own subcategories. */
public enum FabricatorCategory {
    EXOSKELETON, FRAME, CONTROLLER, ENERGY_SYSTEM, MATRIX, MODULE;

    public static FabricatorCategory of(Item item) {
        if (item instanceof ExoskeletonItem) return EXOSKELETON;
        if (item instanceof FrameItem) return FRAME;
        if (item instanceof ControllerItem) return CONTROLLER;
        if (item instanceof EnergySystemItem) return ENERGY_SYSTEM;
        if (item instanceof MatrixItem) return MATRIX;
        if (item instanceof ModuleItem) return MODULE;
        throw new IllegalArgumentException("Not Fabricator equipment: " + item);
    }

    public String translationKey() {
        return "gui.exoequipment.fabricator.category." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
