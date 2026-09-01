package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.controller.ControllerDefinition;
import com.github.littleemptydoll.exoequipment.item.ControllerItem;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModControllers {
    private ModControllers() {}

    public static final DeferredRegister<ControllerDefinition> CONTROLLERS =
            DeferredRegister.create(
                    ModRegistryKeys.CONTROLLER_REGISTRY_KEY,
                    ExoEquipment.MODID
            );

    private static final EquipmentRegistry<
            ControllerDefinition,
            ControllerItem
            > REGISTRY =
            new EquipmentRegistry<>(
                    CONTROLLERS,
                    ModItems.ITEMS,
                    "_controller",
                    ControllerItem::new
            );

    public static ControllerDefinition getDefinition(
            ResourceLocation id
    ) {
        return REGISTRY.getDefinition(id);
    }

    public static EquipmentEntry<
            ControllerDefinition,
            ControllerItem
    > find(
            ResourceLocation id
    ) {
        return REGISTRY.find(id);
    }

    public static final EquipmentEntry<
            ControllerDefinition,
            ControllerItem
    > CIVILIAN = REGISTRY.register(
            "civilian",
            new EquipmentProperties(
                    EquipmentTier.CIVILIAN,
                    Rarity.UNCOMMON
            ),
            (id, properties) ->
                    new ControllerDefinition(
                            id,
                            properties,
                            1,
                            1
                    )
    );

    public static final EquipmentEntry<
            ControllerDefinition,
            ControllerItem
    > MILITARY = REGISTRY.register(
            "military",
            new EquipmentProperties(
                    EquipmentTier.MILITARY,
                    Rarity.RARE
            ),
            (id, properties) ->
                    new ControllerDefinition(
                            id,
                            properties,
                            2,
                            2
                    )
    );

    public static final EquipmentEntry<
            ControllerDefinition,
            ControllerItem
    > ENGINEERING = REGISTRY.register(
            "engineering",
            new EquipmentProperties(
                    EquipmentTier.ENGINEERING,
                    Rarity.RARE
            ),
            (id, properties) ->
                    new ControllerDefinition(
                            id,
                            properties,
                            2,
                            2
                    )
    );

    public static final EquipmentEntry<
            ControllerDefinition,
            ControllerItem
    > EXPERIMENTAL = REGISTRY.register(
            "experimental",
            new EquipmentProperties(
                    EquipmentTier.EXPERIMENTAL,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    new ControllerDefinition(
                            id,
                            properties,
                            3,
                            3
                    )
    );
}
