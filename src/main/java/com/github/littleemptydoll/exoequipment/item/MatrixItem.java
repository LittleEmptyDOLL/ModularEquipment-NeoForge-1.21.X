package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixDefinition;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class MatrixItem extends Item {

    private final DeferredHolder<
            MatrixDefinition,
            MatrixDefinition
    > definition;

    public MatrixItem(
            DeferredHolder<
                    MatrixDefinition,
                    MatrixDefinition
            > definition
    ) {
        super(
                new Properties()
                        .component(
                                ModDataComponents.MATRIX_DATA.get(),
                                MatrixData.empty()
                        )
        );

        this.definition = definition;
    }

    public MatrixDefinition getDefinition() {
        return definition.get();
    }

    public MatrixData getMatrixData(ItemStack stack) {
        MatrixData data = stack.get(
                ModDataComponents.MATRIX_DATA.get()
        );

        if (data == null) {
            throw new IllegalStateException("Matrix item does not contain matrix data");
        }

        return data;
    }

    public static MatrixItem get(ItemStack stack) {
        if (!(stack.getItem() instanceof MatrixItem matrixItem)) {
            throw new IllegalArgumentException(
                    "ItemStack is not a matrix"
            );
        }

        return matrixItem;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        MatrixData data = getMatrixData(stack);
        MatrixDefinition definition = getDefinition();

        if (data == null) {
            tooltip.add(
                    Component.literal("No matrix data")
            );
            return;
        }

        tooltip.add(
                Component.literal(
                        "Type: " + definition.type()
                )
        );

        tooltip.add(
                Component.literal(
                        "Size: "
                                + definition.width()
                                + "x"
                                + definition.height()
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
