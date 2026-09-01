package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.client.TooltipHelper;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixDefinition;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.registry.EquipmentItem;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.util.EquipmentItemUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class MatrixItem extends EquipmentItem<MatrixDefinition> {

    public MatrixItem(
            DeferredHolder<
                    MatrixDefinition,
                    MatrixDefinition
            > definition,
            Properties properties
    ) {
        super(
                definition,
                properties
                        .component(
                                ModDataComponents.MATRIX_DATA.get(),
                                MatrixData.empty(definition.getId())
                        )
        );
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

        tooltip.add(
                TooltipHelper.tier(
                        definition.tier()
                )
        );

        tooltip.add(
                TooltipHelper.size(
                        definition.width(),
                        definition.height()
                )
        );

        int moduleCount = data.modules().size();

        tooltip.add(
                TooltipHelper.modules(
                        moduleCount
                )
        );

        if (Screen.hasShiftDown()) {
            tooltip.add(
                    Component.translatable(
                            "tooltip.exoequipment.installed_modules"
                    )
            );

            for (InstalledModule module : data.modules()) {
                tooltip.add(
                        Component.translatable(
                                "tooltip.exoequipment.module_entry",
                                EquipmentItemUtils.moduleName(
                                        module.id()
                                )
                        )
                );
            }
        }
    }
}
