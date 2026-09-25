package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private ModCreativeTabs() {}

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(
                    Registries.CREATIVE_MODE_TAB,
                    ExoEquipment.MODID
            );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EQUIPMENT =
            CREATIVE_MODE_TABS.register(
                    "equipment",
                    () -> CreativeModeTab.builder()
                            .title(
                                    Component.translatable(
                                            "itemGroup.exoequipment"
                                    )
                            )
                            .icon(
                                    () -> ModExoskeletons.BASIC
                                            .getItem()
                                            .getDefaultInstance()
                            )
                            .displayItems(
                                    (parameters, output) ->
                                            ModItems.ITEMS.getEntries()
                                                    .forEach(holder ->
                                                            output.accept(holder.get())
                                                    )
                            )
                            .build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
