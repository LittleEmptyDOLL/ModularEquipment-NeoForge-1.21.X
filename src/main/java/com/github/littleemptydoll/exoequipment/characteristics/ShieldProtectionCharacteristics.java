package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.module.ShieldProtectionOperations;

import java.util.List;

final class ShieldProtectionCharacteristics {
    private ShieldProtectionCharacteristics() {}

    static void add(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        double transfer = context.isMatrixScope()
                ? ShieldProtectionOperations.calculateTransfer(
                        context.data(),
                        context.matrixSlot(),
                        CharacteristicsSupport.poweredModules(context)
                )
                : ShieldProtectionOperations.calculateTransfer(
                        context.data(),
                        CharacteristicsSupport.poweredModules(context)
                );

        if (transfer <= 0.0D) {
            return;
        }

        result.add(
                new Characteristic(
                        CharacteristicCategory.SHIELD,
                        "protection_transfer.reduction",
                        CharacteristicType.CURRENT,
                        transfer
                )
        );
    }
}
