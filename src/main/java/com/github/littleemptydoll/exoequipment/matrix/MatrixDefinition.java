package com.github.littleemptydoll.exoequipment.matrix;

public record MatrixDefinition(
        MatrixType type,
        int width,
        int height
) {
    public MatrixDefinition {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Matrix size must be positive");
        }
    }
}
