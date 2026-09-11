package com.github.littleemptydoll.exoequipment.matrix;

public record MatrixState(
        int energyConsumption,
        int energyGeneration,
        int energyStorageCapacity,
        int energyStorageInput,
        int energyStorageOutput,
        int heatGeneration,
        int cooling
) {
    public int thermalBalance() {
        return heatGeneration - cooling;
    }
}
