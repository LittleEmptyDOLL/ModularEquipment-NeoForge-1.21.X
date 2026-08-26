package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.client.TooltipHelper;
import com.github.littleemptydoll.exoequipment.controller.Controller;
import com.github.littleemptydoll.exoequipment.controller.ControllerDefinition;
import com.github.littleemptydoll.exoequipment.registry.ModControllers;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class ControllerItem extends Item {

    private final DeferredHolder<
            ControllerDefinition,
            ControllerDefinition
            > definition;

    public ControllerItem(
            DeferredHolder<
                    ControllerDefinition,
                    ControllerDefinition
                    > definition
    ) {
        super(
                new Properties()
                        .component(
                                ModDataComponents.CONTROLLER.get(),
                                new Controller(
                                        definition.getId()
                                )
                        )
        );

        this.definition = definition;
    }

    public ControllerDefinition getDefinition() {
        return definition.get();
    }

    public ControllerDefinition getDefinition(ItemStack stack) {
        Controller controller = getController(stack);

        if (controller == null) {
            return null;
        }

        return ModControllers.getDefinition(
                controller.definitionId()
        );
    }

    public Controller getController(ItemStack stack) {
        Controller data = stack.get(
                ModDataComponents.CONTROLLER.get()
        );

        if (data == null) {
            throw new IllegalStateException(
                    "Controller item does not contain controller data"
            );
        }

        return data;
    }

    public static ControllerItem get(ItemStack stack) {
        if (!(stack.getItem() instanceof ControllerItem controllerItem)) {
            throw new IllegalArgumentException(
                    "ItemStack is not an controller"
            );
        }

        return controllerItem;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        Controller controller = getController(stack);

        if (controller == null) {
            tooltip.add(
                    Component.literal("No controller data")
            );
            return;
        }

        ControllerDefinition definition = getDefinition(stack);

        tooltip.add(
                TooltipHelper.tier(
                        definition.tier()
                )
        );

        tooltip.add(
                TooltipHelper.maxProfiles(
                        definition.maxProfiles()
                )
        );

        tooltip.add(
                TooltipHelper.maxActiveMatrices(
                        definition.maxActiveMatrices()
                )
        );
    }
}
