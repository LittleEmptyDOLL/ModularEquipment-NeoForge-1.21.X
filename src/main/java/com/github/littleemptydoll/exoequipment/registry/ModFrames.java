package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.frame.FrameDefinition;
import com.github.littleemptydoll.exoequipment.item.FrameItem;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFrames {
    private ModFrames() {}

    public static final DeferredRegister<FrameDefinition> FRAMES =
            DeferredRegister.create(
                    ModRegistryKeys.FRAME_REGISTRY_KEY,
                    ExoEquipment.MODID
            );

    private static final EquipmentRegistry<
            FrameDefinition,
            FrameItem
            > REGISTRY =
            new EquipmentRegistry<>(
                    FRAMES,
                    ModItems.ITEMS,
                    "_frame",
                    FrameItem::new
            );

    public static FrameDefinition getDefinition(
            ResourceLocation id
    ) {
        return REGISTRY.getDefinition(id);
    }

    public static EquipmentEntry<
            FrameDefinition,
            FrameItem
    > find(
            ResourceLocation id
    ) {
        return REGISTRY.find(id);
    }

    public static final EquipmentEntry<
            FrameDefinition,
            FrameItem
    > CIVILIAN = REGISTRY.register(
            "civilian",
            new EquipmentProperties(
                    EquipmentTier.CIVILIAN,
                    Rarity.UNCOMMON
            ),
            (id, properties) ->
                    new FrameDefinition(
                            id,
                            properties,
                            new ModuleSize(
                                    2,
                                    2
                            )
                    )
    );

    public static final EquipmentEntry<
            FrameDefinition,
            FrameItem
    > MILITARY = REGISTRY.register(
            "military",
            new EquipmentProperties(
                    EquipmentTier.MILITARY,
                    Rarity.RARE
            ),
            (id, properties) ->
                    new FrameDefinition(
                            id,
                            properties,
                            new ModuleSize(
                                    3,
                                    3
                            )
                    )
    );

    public static final EquipmentEntry<
            FrameDefinition,
            FrameItem
    > ENGINEERING = REGISTRY.register(
            "engineering",
            new EquipmentProperties(
                    EquipmentTier.ENGINEERING,
                    Rarity.RARE
            ),
            (id, properties) ->
                    new FrameDefinition(
                            id,
                            properties,
                            new ModuleSize(
                                    3,
                                    3
                            )
                    )
    );

    public static final EquipmentEntry<
            FrameDefinition,
            FrameItem
    > EXPERIMENTAL = REGISTRY.register(
            "experimental",
            new EquipmentProperties(
                    EquipmentTier.EXPERIMENTAL,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    new FrameDefinition(
                            id,
                            properties,
                            new ModuleSize(
                                    4,
                                    4
                            )
                    )
    );
}
