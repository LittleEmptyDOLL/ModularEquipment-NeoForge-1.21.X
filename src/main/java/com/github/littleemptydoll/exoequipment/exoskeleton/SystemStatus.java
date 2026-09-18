package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.github.littleemptydoll.exoequipment.energy.EnergyState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;

public record SystemStatus(int severity, int flags) {
    public static final int GREEN = 0;
    public static final int YELLOW = 1;
    public static final int RED = 2;
    public static final int GRAY = 3;

    public static final int FLAG_ENERGY_WARNING = 1;
    public static final int FLAG_ENERGY_CRITICAL = 1 << 1;
    public static final int FLAG_OVERSIZED_MODULE = 1 << 2;

    public static SystemStatus calculate(ExoskeletonData data) {
        EnergyState energy = EnergyState.calculate(data);

        int flags = 0;
        int severity = GREEN;

        if (energy.maxInput() == 0 && energy.maxOutput() == 0) {
            severity = GRAY;
        } else if (energy.generation() == 0
                && energy.storedEnergy() == 0
                && energy.consumption() > 0) {
            flags |= FLAG_ENERGY_CRITICAL;
            severity = RED;
        } else if (energy.netGeneration() < 0) {
            flags |= FLAG_ENERGY_WARNING;
            severity = YELLOW;
        }

        if (FrameOperations.hasOversizedModule(data)) {
            flags |= FLAG_OVERSIZED_MODULE;
            severity = maxSeverity(severity, YELLOW);
        }

        return new SystemStatus(severity, flags);
    }

    private static int maxSeverity(int first, int second) {
        if (first == RED || second == RED) {
            return RED;
        }
        if (first == YELLOW || second == YELLOW) {
            return YELLOW;
        }
        if (first == GREEN || second == GREEN) {
            return GREEN;
        }
        return GRAY;
    }
}
