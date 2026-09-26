package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.energy.EnergySystemDefinition;
import com.github.littleemptydoll.exoequipment.item.EnergySystemItem;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEnergySystems {
    private ModEnergySystems() {}

    public static final DeferredRegister<EnergySystemDefinition> ENERGY_SYSTEMS =
            DeferredRegister.create(
                    ModRegistryKeys.ENERGY_SYSTEM_REGISTRY_KEY,
                    ExoEquipment.MODID
            );

    private static final EquipmentRegistry<
            EnergySystemDefinition,
            EnergySystemItem
    > REGISTRY = new EquipmentRegistry<>(
            ENERGY_SYSTEMS,
            ModItems.ITEMS,
            "_energy_system",
            EnergySystemItem::new
    );

    public static EnergySystemDefinition getDefinition(
            ResourceLocation id
    ) {
        return REGISTRY.getDefinition(id);
    }

    public static EquipmentEntry<
            EnergySystemDefinition,
            EnergySystemItem
    > find(
            ResourceLocation id
    ) {
        return REGISTRY.find(id);
    }

    public static final EquipmentEntry<
            EnergySystemDefinition,
            EnergySystemItem
    > CIVILIAN = REGISTRY.register(
            "civilian",
            new EquipmentProperties(
                    EquipmentTier.CIVILIAN,
                    Rarity.UNCOMMON
            ),
            (id, properties) -> new EnergySystemDefinition(
                    id,
                    properties,
                    250,
                    250,
                    1.0
            )
    );

    public static final EquipmentEntry<
            EnergySystemDefinition,
            EnergySystemItem
    > MILITARY = REGISTRY.register(
            "military",
            new EquipmentProperties(
                    EquipmentTier.MILITARY,
                    Rarity.RARE
            ),
            (id, properties) -> new EnergySystemDefinition(
                    id,
                    properties,
                    750,
                    1_000,
                    1.0
            )
    );

    public static final EquipmentEntry<
            EnergySystemDefinition,
            EnergySystemItem
    > ENGINEERING = REGISTRY.register(
            "engineering",
            new EquipmentProperties(
                    EquipmentTier.ENGINEERING,
                    Rarity.RARE
            ),
            (id, properties) -> new EnergySystemDefinition(
                    id,
                    properties,
                    1_000,
                    750,
                    1.0
            )
    );

    public static final EquipmentEntry<
            EnergySystemDefinition,
            EnergySystemItem
    > EXPERIMENTAL = REGISTRY.register(
            "experimental",
            new EquipmentProperties(
                    EquipmentTier.EXPERIMENTAL,
                    Rarity.EPIC
            ),
            (id, properties) -> new EnergySystemDefinition(
                    id,
                    properties,
                    2_500,
                    2_500,
                    1.1
            )
    );
}
