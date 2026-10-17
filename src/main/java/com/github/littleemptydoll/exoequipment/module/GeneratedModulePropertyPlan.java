package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record GeneratedModulePropertyPlan(
        EquipmentTier tier,
        ModuleSize size,
        List<ResourceLocation> properties
) {
    public GeneratedModulePropertyPlan {
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
            ResourceLocation propertyId = properties.get(index);
            GeneratedModulePropertyRegistry.PropertyRule rule =
                    GeneratedModulePropertyRegistry.rule(propertyId);
            if (!rule.isAvailableFor(tier) || rule.minArea() > area) {
                throw new IllegalArgumentException(
                        "Generated property is not valid for module tier/size: " + propertyId
                );
            }
            for (int other = index + 1; other < properties.size(); other++) {
                if (rule.conflictsWith(properties.get(other))) {
                    throw new IllegalArgumentException(
                            "Generated module contains conflicting properties: "
                                    + propertyId + " / " + properties.get(other)
                    );
                }
            }
        }
    }

    public int area() {
        return size.width() * size.height();
    }
}
