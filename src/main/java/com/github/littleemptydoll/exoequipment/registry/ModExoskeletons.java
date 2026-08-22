package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonDefinition;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModExoskeletons {
    private ModExoskeletons() {}

    public static final DeferredRegister<ExoskeletonDefinition> EXOSKELETONS =
            DeferredRegister.create(
                    ModRegistries.EXOSKELETON_REGISTRY_KEY,
                    ExoEquipment.MODID
            );

    private static DeferredHolder<ExoskeletonDefinition, ExoskeletonDefinition> register(
            String id
    ) {
        ResourceLocation location =
                ResourceLocation.fromNamespaceAndPath(
                        ExoEquipment.MODID,
                        id
                );

        return EXOSKELETONS.register(
                id,
                () -> new ExoskeletonDefinition(
                        location
                )
        );
    }

    public static ExoskeletonDefinition getDefinition(
            ResourceLocation id
    ) {
        return EXOSKELETONS
                .getEntries()
                .stream()
                .filter(entry -> entry.getId().equals(id))
                .findFirst()
                .map(DeferredHolder::get)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unknown exoskeleton: " + id
                        )
                );
    }

    public static final DeferredHolder<ExoskeletonDefinition, ExoskeletonDefinition> BASIC = register(
            "basic"
    );
}
