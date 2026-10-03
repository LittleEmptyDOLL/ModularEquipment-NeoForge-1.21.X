package com.github.littleemptydoll.exoequipment.module;

import net.minecraft.core.BlockPos;

/** A scanner result with the outline color of the module that detected it. */
public record ScannedBlock(BlockPos position, int color) {
    public ScannedBlock {
        position = position.immutable();
        if (color < 0 || color > 0xFFFFFF) {
            throw new IllegalArgumentException("Block scan color must be a 24-bit RGB value");
        }
    }
}
