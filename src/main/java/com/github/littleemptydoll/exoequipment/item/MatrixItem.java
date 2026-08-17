package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class MatrixItem extends Item {
    public MatrixItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        MatrixData data = stack.get(
                ModDataComponents.MATRIX_DATA.get()
        );

        if (data == null) {
            tooltip.add(
                    Component.literal("No matrix data")
            );
            return;
        }

        tooltip.add(
                Component.literal(
                        "Size: "
                                + data.width()
                                + "x"
                                + data.height()
                )
        );

        tooltip.add(
                Component.literal(
                        "Modules: "
                                + data.modules().size()
                )
        );

        for (var module : data.modules()) {
            tooltip.add(
                    Component.literal(
                            module.id()
                                    + " @ "
                                    + module.x()
                                    + ","
                                    + module.y()
                                    + " rot="
                                    + module.rotation()
                    )
            );
        }
    }
}
