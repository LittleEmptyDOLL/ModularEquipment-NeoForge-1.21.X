package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.exoskeleton.Exoskeleton;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonDefinition;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.registry.ModExoskeletons;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ExoskeletonItem extends Item {

    public ExoskeletonItem(
            DeferredHolder<
                    ExoskeletonDefinition,
                    ExoskeletonDefinition
                    > definition
    ) {
        super(
                new Properties()
                        .component(
                                ModDataComponents.EXOSKELETON.get(),
                                new Exoskeleton(
                                        definition.getId()
                                )
                        )
        );
    }

    public ExoskeletonDefinition getDefinition(ItemStack stack) {
        Exoskeleton exoskeleton = getExoskeleton(stack);

        if (exoskeleton == null) {
            return null;
        }

        return ModExoskeletons.getDefinition(
                exoskeleton.definitionId()
        );
    }

    public Exoskeleton getExoskeleton(ItemStack stack) {
        Exoskeleton data = stack.get(
                ModDataComponents.EXOSKELETON.get()
        );

        if (data == null) {
            throw new IllegalStateException(
                    "Exoskeleton item does not contain exoskeleton data"
            );
        }

        return data;
    }

    public static ExoskeletonItem get(ItemStack stack) {
        if (!(stack.getItem() instanceof ExoskeletonItem exoskeletonItem)) {
            throw new IllegalArgumentException(
                    "ItemStack is not an exoskeleton"
            );
        }

        return exoskeletonItem;
    }
}
