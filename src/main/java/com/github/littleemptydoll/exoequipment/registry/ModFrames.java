package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.frame.FrameDefinition;
import com.github.littleemptydoll.exoequipment.item.FrameItem;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ModFrames {
    private ModFrames() {}

    public static final DeferredRegister<FrameDefinition> FRAMES =
            DeferredRegister.create(
                    ModRegistryKeys.FRAME_REGISTRY_KEY,
                    ExoEquipment.MODID
            );

    private static final Map<ResourceLocation,
            EquipmentEntry<FrameDefinition, FrameItem>> BY_ID
            = new HashMap<>();

    private static EquipmentEntry<FrameDefinition, FrameItem> register(
            String id,
            EquipmentProperties properties,
            ModuleSize maxModuleSize
    ) {
        ResourceLocation resourceLocation =
                ResourceLocation.fromNamespaceAndPath(
                        ExoEquipment.MODID,
                        id
                );

        DeferredHolder<FrameDefinition, FrameDefinition> definition =
                FRAMES.register(
                        id,
                        () -> new FrameDefinition(
                                resourceLocation,
                                properties,
                                maxModuleSize
                        )
                );

        Item.Properties itemProperties = new Item.Properties().rarity(properties.rarity());

        DeferredHolder<Item, FrameItem> item =
                ModItems.ITEMS.register(
                        id + "_frame",
                        () -> new FrameItem(
                                definition,
                                itemProperties
                        )
                );

        EquipmentEntry<FrameDefinition, FrameItem> entry =
                new EquipmentEntry<>(
                        definition,
                        item
                );

        BY_ID.put(
                resourceLocation,
                entry
        );

        return entry;
    }

    public static EquipmentEntry<FrameDefinition, FrameItem> getEntry(ResourceLocation id) {
        return BY_ID.get(id);
    }

    public static FrameDefinition getDefinition(
            ResourceLocation id
    ) {
        return FRAMES
                .getEntries()
                .stream()
                .filter(entry -> entry.getId().equals(id))
                .findFirst()
                .map(DeferredHolder::get)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unknown frame: " + id
                        )
                );
    }

    public static Optional<
            EquipmentEntry<FrameDefinition, FrameItem>
            > find(ResourceLocation id) {
        return Optional.ofNullable(
                BY_ID.get(id)
        );
    }

    public static final EquipmentEntry<FrameDefinition, FrameItem> CIVILIAN = register(
            "civilian",
            new EquipmentProperties(
                    EquipmentTier.CIVILIAN,
                    Rarity.UNCOMMON
            ),
            new ModuleSize(2, 2)
    );

    public static final EquipmentEntry<FrameDefinition, FrameItem> MILITARY = register(
            "military",
            new EquipmentProperties(
                    EquipmentTier.MILITARY,
                    Rarity.RARE
            ),
            new ModuleSize(3, 3)
    );

    public static final EquipmentEntry<FrameDefinition, FrameItem> ENGINEERING = register(
            "engineering",
            new EquipmentProperties(
                    EquipmentTier.ENGINEERING,
                    Rarity.RARE
            ),
            new ModuleSize(3, 3)
    );

    public static final EquipmentEntry<FrameDefinition, FrameItem> EXPERIMENTAL = register(
            "experimental",
            new EquipmentProperties(
                    EquipmentTier.EXPERIMENTAL,
                    Rarity.EPIC
            ),
            new ModuleSize(4, 4)
    );
}
