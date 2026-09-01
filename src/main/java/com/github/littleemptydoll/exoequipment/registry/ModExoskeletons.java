package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonDefinition;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModExoskeletons {
    private ModExoskeletons() {}

    public static final DeferredRegister<ExoskeletonDefinition> EXOSKELETONS =
            DeferredRegister.create(
                    ModRegistryKeys.EXOSKELETON_REGISTRY_KEY,
                    ExoEquipment.MODID
            );

    private static final EquipmentRegistry<
            ExoskeletonDefinition,
            ExoskeletonItem
            > REGISTRY =
            new EquipmentRegistry<>(
                    EXOSKELETONS,
                    ModItems.ITEMS,
                    "_exoskeleton",
                    ExoskeletonItem::new
            );

    public static ExoskeletonDefinition getDefinition(
            ResourceLocation id
    ) {
        return REGISTRY.getDefinition(id);
    }

    public static EquipmentEntry<
            ExoskeletonDefinition,
            ExoskeletonItem
    > find(
            ResourceLocation id
    ) {
        return REGISTRY.find(id);
    }

    public static final EquipmentEntry<
            ExoskeletonDefinition,
            ExoskeletonItem
    > BASIC = REGISTRY.register(
            "basic",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.UNCOMMON
            ),
            ExoskeletonDefinition::new
    );
}
