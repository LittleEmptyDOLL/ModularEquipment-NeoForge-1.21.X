package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.github.littleemptydoll.exoequipment.module.RevivalState;
import com.github.littleemptydoll.exoequipment.module.ShieldState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Set;

public record ExoskeletonRuntimeState(
        List<ShieldState> shields,
        List<RevivalState> revivals,
        Set<InstalledModuleReference> poweredModules
) {
    public static final Codec<ExoskeletonRuntimeState> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ShieldState.CODEC
                                    .listOf()
                                    .optionalFieldOf("shields", List.of())
                                    .forGetter(ExoskeletonRuntimeState::shields),
                            RevivalState.CODEC
                                    .listOf()
                                    .optionalFieldOf("revivals", List.of())
                                    .forGetter(ExoskeletonRuntimeState::revivals),
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
        shields = List.copyOf(shields);
        revivals = List.copyOf(revivals);
        poweredModules = Set.copyOf(poweredModules);
    }

    public static ExoskeletonRuntimeState empty() {
        return new ExoskeletonRuntimeState(
                List.of(),
                List.of(),
                Set.of()
        );
    }

    public ExoskeletonRuntimeState withShields(
            List<ShieldState> shields
    ) {
        return new ExoskeletonRuntimeState(
                shields,
                revivals,
                poweredModules
        );
    }

    public ExoskeletonRuntimeState withRevivals(
            List<RevivalState> revivals
    ) {
        return new ExoskeletonRuntimeState(
                shields,
                revivals,
                poweredModules
        );
    }

    public ExoskeletonRuntimeState withPoweredModules(
            Set<InstalledModuleReference> poweredModules
    ) {
        return new ExoskeletonRuntimeState(
                shields,
                revivals,
                poweredModules
        );
    }
}
