package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.*;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

public class ModModules {
    private  ModModules() {}

    public static final DeferredRegister<ModuleDefinition> MODULES =
            DeferredRegister.create(
                    ModRegistryKeys.MODULE_REGISTRY,
                    ExoEquipment.MODID
            );

    private static final EquipmentRegistry<
            ModuleDefinition,
            ModuleItem
    > REGISTRY =
            new EquipmentRegistry<>(
                    MODULES,
                    ModItems.ITEMS,
                    "_module",
                    ModuleItem::new
            );

    public static ModuleDefinition getDefinition(
            ResourceLocation id
    ) {
        return REGISTRY.getDefinition(id);
    }

    public static EquipmentEntry<
            ModuleDefinition,
            ModuleItem
    > find(
            ResourceLocation id
    ) {
        return REGISTRY.find(id);
    }

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
    > TEST = REGISTRY.register(
            "test",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    new ModuleDefinition(
                            id,
                            properties,
                            ModuleCategory.UTILITY,
                            new ModuleSize(2,2),
                            Optional.empty(),
                            Optional.empty()
                    )
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
    > TEST_ENERGY = REGISTRY.register(
            "test_energy",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    new ModuleDefinition(
                            id,
                            properties,
                            ModuleCategory.UTILITY,
                            new ModuleSize(3,3),
                            Optional.of(new EnergyProperties(20)),
                            Optional.of(new ThermalProperties(10, 5))
                    )
    );
}
