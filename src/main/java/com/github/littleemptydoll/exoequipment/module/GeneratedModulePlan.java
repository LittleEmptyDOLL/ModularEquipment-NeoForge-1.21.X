package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;

import java.util.List;

public record GeneratedModulePlan(
        EquipmentTier tier,
        ModuleSize size,
        List<GeneratedPropertyType> properties
) {
    public GeneratedModulePlan {
        if (tier != EquipmentTier.MILITARY
                && tier != EquipmentTier.ENGINEERING
                && tier != EquipmentTier.EXPERIMENTAL) {
            throw new IllegalArgumentException(
                    "Generated module tier must be military, engineering or experimental"
            );
        }
        if (size == null || size.width() < 1 || size.height() < 1
                || size.width() > 4 || size.height() > 4) {
            throw new IllegalArgumentException("Generated module size must be within 1x1..4x4");
        }

        properties = List.copyOf(properties);
        int area = size.width() * size.height();
        for (int index = 0; index < properties.size(); index++) {
            GeneratedPropertyType property = properties.get(index);
            if (!property.isAvailableFor(tier) || property.minArea() > area) {
                throw new IllegalArgumentException(
                        "Generated property is not valid for module tier/size: " + property
                );
            }
            for (int other = index + 1; other < properties.size(); other++) {
                if (property.conflictsWith(properties.get(other))) {
                    throw new IllegalArgumentException(
                            "Generated module contains conflicting properties"
                    );
                }
            }
        }
    }

    public int area() {
        return size.width() * size.height();
    }
}
