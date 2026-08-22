package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.controller.ControllerDefinition;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModControllers {
    private ModControllers() {}

    public static final DeferredRegister<ControllerDefinition> CONTROLLERS =
            DeferredRegister.create(
                    ModRegistries.CONTROLLER_REGISTRY_KEY,
                    ExoEquipment.MODID
            );

    private static DeferredHolder<ControllerDefinition, ControllerDefinition> register(
            String id,
            EquipmentTier tier,
            int maxProfiles
    ) {
        ResourceLocation location =
                ResourceLocation.fromNamespaceAndPath(
                        ExoEquipment.MODID,
                        id
                );

        return CONTROLLERS.register(
                id,
                () -> new ControllerDefinition(
                        location,
                        tier,
                        maxProfiles
                )
        );
    }

    public static ControllerDefinition getDefinition(
            ResourceLocation id
    ) {
        return CONTROLLERS
                .getEntries()
                .stream()
                .filter(entry -> entry.getId().equals(id))
                .findFirst()
                .map(DeferredHolder::get)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unknown controller: " + id
                        )
                );
    }

    public static final DeferredHolder<ControllerDefinition, ControllerDefinition> CIVILIAN = register(
            "civilian",
            EquipmentTier.CIVILIAN,
            1
    );

    public static final DeferredHolder<ControllerDefinition, ControllerDefinition> MILITARY = register(
            "military",
            EquipmentTier.MILITARY,
            2
    );

    public static final DeferredHolder<ControllerDefinition, ControllerDefinition> ENGINEERING = register(
            "engineering",
            EquipmentTier.ENGINEERING,
            2
    );

    public static final DeferredHolder<ControllerDefinition, ControllerDefinition> EXPERIMENTAL = register(
            "experimental",
            EquipmentTier.EXPERIMENTAL,
            3
    );
}
