package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.module.EffectsOperations;

import java.util.List;
import java.util.Map;

final class EffectsCharacteristics {
    private EffectsCharacteristics() {}

    static void add(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        Map<net.minecraft.resources.ResourceLocation, Integer> effects =
                context.isMatrixScope()
                        ? EffectsOperations.collectEffects(
                                context.data(),
                                context.matrixSlot(),
                                CharacteristicsSupport
                                        .poweredModules(context)
                        )
                        : EffectsOperations.collectEffects(
                                context.data(),
                                CharacteristicsSupport
                                        .poweredModules(context)
                        );

        effects.forEach((effectId, amplifier) ->
                result.add(
                        new Characteristic(
                                CharacteristicCategory.EFFECTS,
                                "effect." + effectId,
                                CharacteristicType.CURRENT,
                                amplifier + 1.0D
                        )
                )
        );
    }
}
