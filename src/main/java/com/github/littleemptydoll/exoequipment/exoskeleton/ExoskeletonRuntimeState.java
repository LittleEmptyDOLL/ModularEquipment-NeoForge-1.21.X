package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Set;

public record ExoskeletonRuntimeState(
        Set<InstalledModuleReference> poweredModules
) {
    public static final Codec<ExoskeletonRuntimeState> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            InstalledModuleReference.CODEC
                                    .listOf()
                                    .optionalFieldOf("powered_modules", List.of())
                                    .xmap(Set::copyOf, list -> List.copyOf(list))
                                    .forGetter(ExoskeletonRuntimeState::poweredModules)
                    ).apply(
                            instance,
                            ExoskeletonRuntimeState::new
                    )
            );

    public ExoskeletonRuntimeState {
        poweredModules = Set.copyOf(poweredModules);
    }

    public static ExoskeletonRuntimeState empty() {
        return new ExoskeletonRuntimeState(Set.of());
    }

    public ExoskeletonRuntimeState withPoweredModules(
            Set<InstalledModuleReference> poweredModules
    ) {
        return new ExoskeletonRuntimeState(poweredModules);
    }
}
