package com.github.littleemptydoll.exoequipment.characteristics;

public record Characteristic(
        CharacteristicCategory category,
        String key,
        CharacteristicType type,
        double value
) {
    public Characteristic {
        if (category == null) {
            throw new IllegalArgumentException("Characteristic category cannot be null");
        }
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Characteristic key cannot be blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("Characteristic type cannot be null");
        }
    }
}
