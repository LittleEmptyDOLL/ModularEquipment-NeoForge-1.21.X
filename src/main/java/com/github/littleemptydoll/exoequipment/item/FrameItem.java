package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.client.TooltipHelper;
import com.github.littleemptydoll.exoequipment.frame.Frame;
import com.github.littleemptydoll.exoequipment.frame.FrameDefinition;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.registry.ModFrames;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class FrameItem extends Item {

    private final DeferredHolder<
            FrameDefinition,
            FrameDefinition
            > definition;

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

        this.definition = definition;
    }

    public FrameDefinition getDefinition() {
        return definition.get();
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

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        Frame frame = getFrame(stack);

        if (frame == null) {
            tooltip.add(
                    Component.literal("No frame data")
            );
            return;
        }

        FrameDefinition definition = getDefinition(stack);

        tooltip.add(
                TooltipHelper.tier(
                        definition.tier()
                )
        );

        tooltip.add(
                TooltipHelper.maxModuleSize(
                        definition.maxModuleSize().width(),
                        definition.maxModuleSize().height()
                )
        );
    }
}
