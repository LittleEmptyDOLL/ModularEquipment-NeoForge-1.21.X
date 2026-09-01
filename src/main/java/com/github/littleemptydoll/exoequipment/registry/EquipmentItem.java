package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.client.TooltipHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public abstract class EquipmentItem<D extends EquipmentDefinition> extends Item {

    private final DeferredHolder<D, D> definition;

    protected EquipmentItem(
            DeferredHolder<D, D> definition,
            Properties properties
    ) {
        super(properties);
        this.definition = definition;
    }

    public D getDefinition() {
        return definition.get();
    }

    public DeferredHolder<D, D> getDefinitionHolder() {
        return definition;
    }

    protected void appendEquipmentTooltip(
            List<Component> tooltip
    ) {
        tooltip.add(
                TooltipHelper.tier(
                        getDefinition().tier()
                )
        );
    }
}
