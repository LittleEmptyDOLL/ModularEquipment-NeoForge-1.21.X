package com.github.littleemptydoll.exoequipment.matrix;

import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;

public record MatrixDefinition(
        ResourceLocation id,
        EquipmentTier tier,
        int width,
        int height
) {
    public MatrixDefinition {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Matrix size must be positive");
        }
    }

    public int area() {
        return width * height;
    }

    public boolean contains(int x, int y) {
        return x >= 0
                && y >= 0
                && x < width
                && y < height;
    }

    public boolean contains(
            int x,
            int y,
            int objectWidth,
            int objectHeight
    ) {
        return x >= 0
                && y >= 0
                && x + objectWidth <= width
                && y + objectHeight <= height;
    }

    public boolean contains(
            ModuleSize size,
            int x,
            int y
    ) {
        return contains(
                x,
                y,
                size.width(),
                size.height()
        );
    }
}
