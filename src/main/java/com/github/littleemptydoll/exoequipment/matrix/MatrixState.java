package com.github.littleemptydoll.exoequipment.matrix;

public record MatrixState(
        int energyConsumption,
        int heatGeneration,
        int cooling
) {
    public int thermalBalance() {
        return heatGeneration - cooling;
    }
}
