package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.client.TooltipHelper;
import com.github.littleemptydoll.exoequipment.exoskeleton.*;
import com.github.littleemptydoll.exoequipment.registry.EquipmentItem;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.util.EquipmentItemUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class ExoskeletonItem extends EquipmentItem<ExoskeletonDefinition> {

    public ExoskeletonItem(
            DeferredHolder<
                    ExoskeletonDefinition,
                    ExoskeletonDefinition
            > definition,
            Properties properties
    ) {
        super(
                definition,
                properties
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

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        ExoskeletonData data = getData(stack);

        if (data.frame().isPresent()) {
            tooltip.add(
                    Component.translatable(
                            "tooltip.exoequipment.frame",
                            EquipmentItemUtils.frameName(
                                    data.frame().get().definitionId()
                            )
                    )
            );
        }

        if (data.controller().isPresent()) {
            tooltip.add(
                    Component.translatable(
                            "tooltip.exoequipment.controller",
                            EquipmentItemUtils.controllerName(
                                    data.controller().get().definitionId()
                            )
                    )
            );
        }

        if (data.energySystem().isPresent()) {
            tooltip.add(
                    Component.translatable(
                            "tooltip.exoequipment.energy_system",
                            EquipmentItemUtils.energySystemName(
                                    data.energySystem().get().definitionId()
                            )
                    )
            );
        }

        int installedMatrices = (int) data.matrices()
                .stream()
                .filter(slot -> slot.matrix().isPresent())
                .count();

        tooltip.add(
                TooltipHelper.matrices(
                        installedMatrices,
                        ExoskeletonData.MAX_MATRICES
                )
        );

        if (ExoskeletonState.hasActiveProfile(data)) {
            tooltip.add(
                    TooltipHelper.activeProfile(
                            data.activeProfile()
                    )
            );
        }

        if (Screen.hasShiftDown()) {
            tooltip.add(
                    Component.translatable(
                            "tooltip.exoequipment.installed_matrices"
                    )
            );

            for (int slot = 0;
                 slot < ExoskeletonData.MAX_MATRICES;
                 slot++) {
                final int currentSlot = slot;

                Component matrixName =
                        data.matrices()
                                .get(currentSlot)
                                .matrix()
                                .map(matrix ->
                                        EquipmentItemUtils.matrixName(
                                                matrix.id()
                                        )
                                )
                                .orElse(
                                        Component.translatable(
                                                "tooltip.exoequipment.empty"
                                        )
                                );

                tooltip.add(
                        Component.translatable(
                                "tooltip.exoequipment.matrix_slot",
                                currentSlot + 1,
                                matrixName
                        )
                );
            }
        }
    }
}
