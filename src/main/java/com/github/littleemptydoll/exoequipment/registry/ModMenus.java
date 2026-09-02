package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.gui.ExoskeletonMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {

    private ModMenus() {}

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(
                    Registries.MENU,
                    ExoEquipment.MODID
            );

    public static final DeferredHolder<
            MenuType<?>,
            MenuType<ExoskeletonMenu>
    > EXOSKELETON = MENUS.register(
            "exoskeleton",
            () -> IMenuTypeExtension.create(
                    ExoskeletonMenu::new
            )
    );

    public static void register(
            IEventBus eventBus
    ) {
        MENUS.register(eventBus);
    }
}
