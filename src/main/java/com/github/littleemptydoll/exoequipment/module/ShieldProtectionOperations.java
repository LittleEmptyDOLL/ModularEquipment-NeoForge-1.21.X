package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations;

import java.util.List;
import java.util.Set;

public final class ShieldProtectionOperations {
    private ShieldProtectionOperations() {}

    public static double calculateTransfer(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        return calculateTransfer(
                data,
                ExoskeletonModules.activeSupported(data),
                poweredModules
        );
    }

    public static double calculateTransfer(
            ExoskeletonData data,
            int matrixSlot,
            Set<InstalledModuleReference> poweredModules
    ) {
        return calculateTransfer(
                data,
                ExoskeletonModules.supportedInMatrix(
                        data,
                        matrixSlot
                ),
                poweredModules
        );
    }

    private static double calculateTransfer(
            ExoskeletonData data,
            List<ExoskeletonModules.ActiveModule> modules,
            Set<InstalledModuleReference> poweredModules
    ) {
        double transfer = 0.0D;

        for (ExoskeletonModules.ActiveModule activeModule : modules) {
            if (!ExoskeletonModules.isPowered(
                    activeModule,
                    poweredModules
            )) {
                continue;
            }

            ShieldProtectionProperties properties =
                    activeModule.definition()
                            .shieldProtection()
                            .orElse(null);

            if (properties == null) {
                continue;
            }

            double efficiency =
                    TemperatureOperations.calculateModuleEfficiency(
                            activeModule.definition(),
                            data.temperature()
                    );

            transfer = Math.max(
                    transfer,
                    Math.min(
                            1.0D,
                            properties.transfer() * efficiency
                    )
            );
        }

        return Math.max(0.0D, transfer);
    }

    public static double calculateShieldDamageMultiplier(
            double damageMultiplier,
            double transfer
    ) {
        double clampedDamageMultiplier = Math.min(
                1.0D,
                Math.max(0.0D, damageMultiplier)
        );
        double clampedTransfer = Math.min(
                1.0D,
                Math.max(0.0D, transfer)
        );

        double reduction = 1.0D - clampedDamageMultiplier;
        return 1.0D - reduction * clampedTransfer;
    }
}
