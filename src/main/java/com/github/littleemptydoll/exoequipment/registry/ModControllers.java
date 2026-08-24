package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.controller.ControllerDefinition;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModControllers {
    private ModControllers() {}

    public static final DeferredRegister<ControllerDefinition> CONTROLLERS =
            DeferredRegister.create(
                    ModRegistryKeys.CONTROLLER_REGISTRY_KEY,
                    ExoEquipment.MODID
            );

    private static DeferredHolder<ControllerDefinition, ControllerDefinition> register(
            String id,
            EquipmentTier tier,
            Rarity rarity,
            int maxProfiles,
            int maxActiveMatrices
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
                        rarity,
                        maxProfiles,
                        maxActiveMatrices
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
            Rarity.UNCOMMON,
            1,
            1
    );

    public static final DeferredHolder<ControllerDefinition, ControllerDefinition> MILITARY = register(
            "military",
            EquipmentTier.MILITARY,
            Rarity.RARE,
            2,
            2
    );

    public static final DeferredHolder<ControllerDefinition, ControllerDefinition> ENGINEERING = register(
            "engineering",
            EquipmentTier.ENGINEERING,
            Rarity.RARE,
            2,
            2
    );

    public static final DeferredHolder<ControllerDefinition, ControllerDefinition> EXPERIMENTAL = register(
            "experimental",
            EquipmentTier.EXPERIMENTAL,
            Rarity.EPIC,
            3,
            3
    );
}
