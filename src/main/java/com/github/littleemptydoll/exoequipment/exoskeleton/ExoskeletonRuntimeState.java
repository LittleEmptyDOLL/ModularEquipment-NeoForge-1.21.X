package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Set;

public record ExoskeletonRuntimeState(
        Set<InstalledModuleReference> poweredModules,
        long inputTick,
        int inputUsed
) {
    public static final Codec<ExoskeletonRuntimeState> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            InstalledModuleReference.CODEC
                                    .listOf()
                                    .optionalFieldOf("powered_modules", List.of())
                                    .xmap(Set::copyOf, list -> List.copyOf(list))
                                    .forGetter(ExoskeletonRuntimeState::poweredModules),
                            Codec.LONG.optionalFieldOf("input_tick", -1L)
                                    .forGetter(ExoskeletonRuntimeState::inputTick),
                            Codec.INT.optionalFieldOf("input_used", 0)
                                    .forGetter(ExoskeletonRuntimeState::inputUsed)
                    ).apply(
                            instance,
                            ExoskeletonRuntimeState::new
                    )
            );

    public ExoskeletonRuntimeState {
        poweredModules = Set.copyOf(poweredModules);
        if (inputUsed < 0) {
            throw new IllegalArgumentException("Input used cannot be negative");
        }
    }

    public static ExoskeletonRuntimeState empty() {
        return new ExoskeletonRuntimeState(Set.of(), -1L, 0);
    }

    public ExoskeletonRuntimeState withPoweredModules(
            Set<InstalledModuleReference> poweredModules
    ) {
        return new ExoskeletonRuntimeState(poweredModules, inputTick, inputUsed);
    }

    public int remainingInput(long tick, int maxInput) {
        return Math.max(0, maxInput - (inputTick == tick ? inputUsed : 0));
    }

    public ExoskeletonRuntimeState withInput(long tick, int amount) {
        if (amount <= 0) {
            return this;
        }
        int used = inputTick == tick ? inputUsed : 0;
        return new ExoskeletonRuntimeState(poweredModules, tick, used + amount);
    }
}
