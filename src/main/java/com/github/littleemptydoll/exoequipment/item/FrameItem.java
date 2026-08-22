package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.frame.Frame;
import com.github.littleemptydoll.exoequipment.frame.FrameDefinition;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.registry.ModFrames;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

public class FrameItem extends Item {

    public FrameItem(
            DeferredHolder<
                    FrameDefinition,
                    FrameDefinition
                    > definition
    ) {
        super(
                new Properties()
                        .component(
                                ModDataComponents.FRAME.get(),
                                new Frame(
                                        definition.getId()
                                )
                        )
        );
    }

    public FrameDefinition getDefinition(ItemStack stack) {
        Frame frame = getFrame(stack);

        if (frame == null) {
            return null;
        }

        return ModFrames.getDefinition(
                frame.definitionId()
        );
    }

    public Frame getFrame(ItemStack stack) {
        Frame data = stack.get(
                ModDataComponents.FRAME.get()
        );

        if (data == null) {
            throw new IllegalStateException(
                    "Frame item does not contain frame data"
            );
        }

        return data;
    }

    public static FrameItem get(ItemStack stack) {
        if (!(stack.getItem() instanceof FrameItem frameItem)) {
            throw new IllegalArgumentException(
                    "ItemStack is not an frame"
            );
        }

        return frameItem;
    }
}
