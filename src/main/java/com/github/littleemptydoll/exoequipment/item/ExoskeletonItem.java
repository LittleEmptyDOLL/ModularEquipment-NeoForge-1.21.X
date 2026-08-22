package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.exoskeleton.Exoskeleton;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonDefinition;
import com.github.littleemptydoll.exoequipment.exoskeleton.MatrixSlot;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class ExoskeletonItem extends Item {
    private final DeferredHolder<
            ExoskeletonDefinition,
            ExoskeletonDefinition
    > definition;

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
                        .component(
                                ModDataComponents.EXOSKELETON_DATA.get(),
                                ExoskeletonData.empty()
                        )
        );

        this.definition = definition;
    }

    public ExoskeletonData getData(ItemStack stack) {
        ExoskeletonData data = stack.get(
                ModDataComponents.EXOSKELETON_DATA.get()
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

    public ExoskeletonDefinition getDefinition() {
        return definition.get();
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

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        Exoskeleton exoskeleton = getExoskeleton(stack);

        if (exoskeleton == null) {
            tooltip.add(
                    Component.literal("No exoskeleton data")
            );
            return;
        }

        ExoskeletonData data = getData(stack);

        tooltip.add(
                Component.literal(
                        "Frame: "
                                + (data.frame().isPresent() ? data.frame().get().definitionId() : "None")
                )
        );

        tooltip.add(
                Component.literal(
                        "Controller: "
                                + (data.controller().isPresent() ? data.controller().get().definitionId() : "None")
                )
        );

        tooltip.add(
                Component.literal(
                        "Energy system: "
                                + (data.energySystem().isPresent() ? data.energySystem().get().definitionId() : "None")
                )
        );

        tooltip.add(
                Component.literal(
                        "Matrices:"
                )
        );

        for (int i = 0; i < data.matrices().size(); i++) {
            MatrixSlot slot = data.matrices().get(i);

            tooltip.add(
                    Component.literal(
                            "  " + (i + 1) + ": " +
                                    (slot.matrix().isPresent() ? slot.matrix().toString() : "Empty")
                    )
            );
        }

        tooltip.add(
                Component.literal(
                        "Profiles: " + data.profiles().size()
                )
        );
    }
}
