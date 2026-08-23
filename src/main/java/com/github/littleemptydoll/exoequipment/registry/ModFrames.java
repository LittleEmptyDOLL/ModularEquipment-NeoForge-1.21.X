package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.frame.FrameDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFrames {
    private ModFrames() {}

    public static final DeferredRegister<FrameDefinition> FRAMES =
            DeferredRegister.create(
                    ModRegistryKeys.FRAME_REGISTRY_KEY,
                    ExoEquipment.MODID
            );

    private static DeferredHolder<FrameDefinition, FrameDefinition> register(
            String id,
            EquipmentTier tier,
            ModuleSize maxModuleSize
    ) {
        ResourceLocation location =
                ResourceLocation.fromNamespaceAndPath(
                        ExoEquipment.MODID,
                        id
                );

        return FRAMES.register(
                id,
                () -> new FrameDefinition(
                        location,
                        tier,
                        maxModuleSize
                )
        );
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

    public static final DeferredHolder<FrameDefinition, FrameDefinition> CIVILIAN = register(
            "civilian",
            EquipmentTier.CIVILIAN,
            new ModuleSize(2, 2)
    );

    public static final DeferredHolder<FrameDefinition, FrameDefinition> MILITARY = register(
            "military",
            EquipmentTier.MILITARY,
            new ModuleSize(3, 3)
    );

    public static final DeferredHolder<FrameDefinition, FrameDefinition> ENGINEERING = register(
            "engineering",
            EquipmentTier.ENGINEERING,
            new ModuleSize(3, 3)
    );

    public static final DeferredHolder<FrameDefinition, FrameDefinition> EXPERIMENTAL = register(
            "experimental",
            EquipmentTier.EXPERIMENTAL,
            new ModuleSize(4, 4)
    );
}
