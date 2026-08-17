package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.module.ModuleBranch;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class ModModules {
    private  ModModules() {}

    private static final Map<ResourceLocation, ModuleDefinition> MODULES = new HashMap<>();

    public static ModuleDefinition register(
            String id,
            ModuleSize size,
            ModuleCategory category,
            ModuleBranch branch
    ) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(
                ExoEquipment.MODID,
                id
        );

        ModuleDefinition definition = new ModuleDefinition(
                location,
                size,
                category,
                branch
        );

        if (MODULES.putIfAbsent(location, definition) != null) {
            throw new IllegalStateException("Duplicate module id: " + location);
        }

        return definition;
    }

    public static ModuleDefinition getDefinition(
            ResourceLocation id
    ) {
        ModuleDefinition definition = MODULES.get(id);

        if (definition == null) {
            throw new IllegalArgumentException("Unknown module: " + id);
        }

        return definition;
    }

    public static final ModuleDefinition TEST_MODULE = register(
            "test_module",
            new ModuleSize(2,2),
            ModuleCategory.UTILITY,
            ModuleBranch.BASIC
    );

    public static final ModuleDefinition TEST_RECT_MODULE = register(
            "test_rect_module",
            new ModuleSize(2,3),
            ModuleCategory.UTILITY,
            ModuleBranch.BASIC
    );
}
