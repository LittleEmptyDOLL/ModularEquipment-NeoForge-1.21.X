package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.client.TooltipHelper;
import com.github.littleemptydoll.exoequipment.exoskeleton.*;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.util.EquipmentItemUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

        long installedMatrices = (int) data.matrices()
                .stream()
                .filter(slot -> slot.matrix().isPresent())
                .count();

        tooltip.add(
                TooltipHelper.matrices(
                        (int) installedMatrices,
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
                                                matrix.definition()
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
