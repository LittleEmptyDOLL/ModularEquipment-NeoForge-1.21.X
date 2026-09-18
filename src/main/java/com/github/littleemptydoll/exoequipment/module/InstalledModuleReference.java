package com.github.littleemptydoll.exoequipment.module;

public record InstalledModuleReference(
        int matrixSlot,
        int moduleIndex
) {
    public InstalledModuleReference {
        if (matrixSlot < 0) {
            throw new IllegalArgumentException("Matrix slot cannot be negative");
        }
        if (moduleIndex < 0) {
            throw new IllegalArgumentException("Module index cannot be negative");
        }
    }
}