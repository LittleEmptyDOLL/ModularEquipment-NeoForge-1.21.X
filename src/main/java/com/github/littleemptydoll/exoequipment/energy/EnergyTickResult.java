package com.github.littleemptydoll.exoequipment.energy;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;

import java.util.Set;

public record EnergyTickResult(
        ExoskeletonData data,
        int generated,
        int externalInput,
        int consumed,
        int charged,
        int discharged,
        int deficit,
        int wasted,
        Set<InstalledModuleReference> poweredModules
) {
    public EnergyTickResult {
        if (generated < 0) throw new IllegalArgumentException("Generated energy cannot be negative");
        if (externalInput < 0) throw new IllegalArgumentException("External input cannot be negative");
        if (consumed < 0) throw new IllegalArgumentException("Consumed energy cannot be negative");
        if (charged < 0) throw new IllegalArgumentException("Charged energy cannot be negative");
        if (discharged < 0) throw new IllegalArgumentException("Discharged energy cannot be negative");
        if (deficit < 0) throw new IllegalArgumentException("Energy deficit cannot be negative");
        if (wasted < 0) throw new IllegalArgumentException("Wasted energy cannot be negative");
        poweredModules = Set.copyOf(poweredModules);
    }

    public boolean isPowered(InstalledModuleReference reference) {
        return poweredModules.contains(reference);
    }
}
