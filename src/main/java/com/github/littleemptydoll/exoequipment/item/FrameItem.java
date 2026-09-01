package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.client.TooltipHelper;
import com.github.littleemptydoll.exoequipment.frame.Frame;
import com.github.littleemptydoll.exoequipment.frame.FrameDefinition;
import com.github.littleemptydoll.exoequipment.registry.EquipmentItem;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class FrameItem extends EquipmentItem<FrameDefinition> {

    public FrameItem(
            DeferredHolder<
                    FrameDefinition,
                    FrameDefinition
                    > definition,
            Properties properties
    ) {
        super(
                definition,
                properties
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
        appendEquipmentTooltip(tooltip);

        FrameDefinition definition = getDefinition();

        tooltip.add(
                TooltipHelper.maxModuleSize(
                        definition.maxModuleSize().width(),
                        definition.maxModuleSize().height()
                )
        );
    }
}
