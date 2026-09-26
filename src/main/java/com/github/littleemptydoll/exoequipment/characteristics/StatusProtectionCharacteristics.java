package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import com.github.littleemptydoll.exoequipment.module.StatusProtectionOperations;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class StatusProtectionCharacteristics {
    private StatusProtectionCharacteristics() {}

    static void add(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        Set<ResourceLocation> effectIds = new HashSet<>();
        var poweredModules =
                CharacteristicsSupport.poweredModules(context);

        for (var activeModule
                : CharacteristicsSupport.runtimeModules(context)) {
            if (!ExoskeletonModules.isPowered(
                    activeModule,
                    poweredModules
            )) {
                continue;
            }

            activeModule.definition()
                    .statusProtection()
                    .ifPresent(properties ->
                            effectIds.addAll(
                                    properties.protections().keySet()
                            )
                    );
        }

        for (ResourceLocation effectId : effectIds) {
            double protection =
                    context.isMatrixScope()
                            ? StatusProtectionOperations
                            .calculateProtection(
                                    context.data(),
                                    context.matrixSlot(),
                                    effectId,
                                    poweredModules
                            )
                            : StatusProtectionOperations
                            .calculateProtection(
                                    context.data(),
                                    effectId,
                                    poweredModules
                            );

            if (protection <= 0.0D) {
                continue;
            }

            result.add(
                    new Characteristic(
                            CharacteristicCategory.STATUS_PROTECTION,
                            "status_protection."
                                    + effectId
                                    + ".reduction",
                            CharacteristicType.CURRENT,
                            protection
                    )
            );
        }
    }
}
