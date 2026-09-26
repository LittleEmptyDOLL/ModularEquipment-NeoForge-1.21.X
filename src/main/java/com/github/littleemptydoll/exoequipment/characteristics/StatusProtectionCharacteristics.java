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
        Set<ResourceLocation> tagIds = new HashSet<>();
        var poweredModules =
                CharacteristicsSupport.poweredModules(context);
        boolean hasHarmfulProtection = false;

        for (var activeModule
                : CharacteristicsSupport.runtimeModules(context)) {
            if (!ExoskeletonModules.isPowered(
                    activeModule,
                    poweredModules
            )) {
                continue;
            }

            var properties = activeModule.definition()
                    .statusProtection()
                    .orElse(null);

            if (properties == null) {
                continue;
            }

            effectIds.addAll(properties.protections().keySet());
            tagIds.addAll(properties.tagProtections().keySet());
            hasHarmfulProtection |=
                    properties.harmfulProtection() > 0.0D;
        }

        if (hasHarmfulProtection) {
            double protection =
                    context.isMatrixScope()
                            ? StatusProtectionOperations
                            .calculateHarmfulProtection(
                                    context.data(),
                                    context.matrixSlot(),
                                    poweredModules
                            )
                            : StatusProtectionOperations
                            .calculateHarmfulProtection(
                                    context.data(),
                                    poweredModules
                            );

            if (protection > 0.0D) {
                result.add(
                        new Characteristic(
                                CharacteristicCategory.STATUS_PROTECTION,
                                "status_protection.harmful.reduction",
                                CharacteristicType.CURRENT,
                                protection
                        )
                );
            }
        }

        for (ResourceLocation tagId : tagIds) {
            double protection =
                    context.isMatrixScope()
                            ? StatusProtectionOperations
                            .calculateTagProtection(
                                    context.data(),
                                    context.matrixSlot(),
                                    tagId,
                                    poweredModules
                            )
                            : StatusProtectionOperations
                            .calculateTagProtection(
                                    context.data(),
                                    tagId,
                                    poweredModules
                            );

            if (protection <= 0.0D) {
                continue;
            }

            result.add(
                    new Characteristic(
                            CharacteristicCategory.STATUS_PROTECTION,
                            "status_protection."
                                    + tagName(tagId)
                                    + ".reduction",
                            CharacteristicType.CURRENT,
                            protection
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

    private static String tagName(ResourceLocation tagId) {
        String path = tagId.getPath();
        int separator = path.lastIndexOf('/');

        return separator >= 0 && separator + 1 < path.length()
                ? path.substring(separator + 1)
                : path;
    }
}
