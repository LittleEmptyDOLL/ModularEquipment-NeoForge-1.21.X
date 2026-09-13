package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.client.TooltipHelper;
import com.github.littleemptydoll.exoequipment.gui.MatrixMenuProvider;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixDefinition;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.registry.EquipmentItem;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.util.EquipmentItemUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class MatrixItem extends EquipmentItem<MatrixDefinition> {

    public MatrixItem(
            DeferredHolder<MatrixDefinition, MatrixDefinition> definition,
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
    public InteractionResult use(
            net.minecraft.world.level.Level level,
            Player player,
            InteractionHand hand
    ) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            int inventorySlot = hand == InteractionHand.MAIN_HAND
                    ? player.getInventory().selected
                    : 40;

            MatrixMenuProvider.openFromHand(
                    serverPlayer,
                    inventorySlot
            );
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        appendEquipmentTooltip(tooltip);

        MatrixData data = getMatrixData(stack);
        MatrixDefinition definition = getDefinition();

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

        if (TooltipHelper.isShiftDown()) {
            tooltip.add(
                    TooltipHelper.installedModules()
            );

            for (InstalledModule module : data.modules()) {
                tooltip.add(
                        TooltipHelper.moduleEntry(
                                EquipmentItemUtils.moduleName(
                                        module.id()
                                )
                        )
                );
            }
        }
    }
}
