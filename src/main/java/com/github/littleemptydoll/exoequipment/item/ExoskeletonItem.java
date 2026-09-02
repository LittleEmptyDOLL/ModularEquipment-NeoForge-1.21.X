package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.client.TooltipHelper;
import com.github.littleemptydoll.exoequipment.exoskeleton.*;
import com.github.littleemptydoll.exoequipment.gui.ExoskeletonMenu;
import com.github.littleemptydoll.exoequipment.registry.EquipmentItem;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.util.EquipmentItemUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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

    private static int getInventorySlot(
            Player player,
            InteractionHand hand
    ) {
        if (hand == InteractionHand.MAIN_HAND) {
            return player.getInventory().selected;
        }

        return 40;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()
                && player instanceof ServerPlayer serverPlayer) {
            int slot = getInventorySlot(
                    player,
                    hand
            );

            serverPlayer.openMenu(
                    new SimpleMenuProvider(
                            (containerId, inventory, menuPlayer) ->
                                    new ExoskeletonMenu(
                                            containerId,
                                            inventory,
                                            slot
                                    ),
                            Component.translatable(
                                    "menu.exoequipment.exoskeleton"
                            )
                    ),
                    buffer -> buffer.writeInt(slot)
            );
        }

        return InteractionResultHolder.sidedSuccess(
                stack,
                level.isClientSide()
        );
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        appendEquipmentTooltip(tooltip);

        ExoskeletonData data = getData(stack);

        if (data.frame().isPresent()) {
            tooltip.add(
                    TooltipHelper.frame(
                            EquipmentItemUtils.frameName(
                                    data.frame().get().definitionId()
                            )
                    )
            );
        }

        if (data.controller().isPresent()) {
            tooltip.add(
                    TooltipHelper.controller(
                            EquipmentItemUtils.controllerName(
                                    data.controller().get().definitionId()
                            )
                    )
            );
        }

        if (data.energySystem().isPresent()) {
            tooltip.add(
                    TooltipHelper.energySystem(
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

        if (TooltipHelper.isShiftDown()) {
            tooltip.add(
                    TooltipHelper.installedMatrices()
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
                                        TooltipHelper.empty()
                                );

                tooltip.add(
                        TooltipHelper.matrixSlot(
                                currentSlot + 1,
                                matrixName
                        )
                );
            }
        }
    }
}
