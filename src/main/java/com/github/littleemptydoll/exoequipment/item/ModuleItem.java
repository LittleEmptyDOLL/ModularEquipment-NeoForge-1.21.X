package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.module.Module;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModuleItem extends Item {

    private final DeferredHolder<
            ModuleDefinition,
            ModuleDefinition
            > definition;

    public ModuleItem(
            DeferredHolder<
                    ModuleDefinition,
                    ModuleDefinition
                    > definition
    ) {
        super(
                new Properties()
                        .component(
                                ModDataComponents.MODULE.get(),
                                new Module(
                                        definition.getId()
                                )
                        )
        );

        this.definition = definition;
    }

    public ModuleDefinition getDefinition() {
        return definition.get();
    }
}
