package com.github.littleemptydoll.exoequipment.registry;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

import java.util.function.Supplier;

public final class ModItemComponents {
    private ModItemComponents() {}

    @SubscribeEvent
    public static void modifyDefaultComponent(
            ModifyDefaultComponentsEvent event
    ) {
        // Матрицы
        applyRarity(
                event,
                ModItems.CIVILIAN_MATRIX,
                ModMatrices.CIVILIAN
        );
        applyRarity(
                event,
                ModItems.MILITARY_MATRIX,
                ModMatrices.MILITARY
        );
        applyRarity(
                event,
                ModItems.ENGINEERING_MATRIX,
                ModMatrices.ENGINEERING
        );
        applyRarity(
                event,
                ModItems.EXPERIMENTAL_MATRIX,
                ModMatrices.EXPERIMENTAL
        );
        // Контроллеры
        applyRarity(
                event,
                ModItems.CIVILIAN_CONTROLLER,
                ModControllers.CIVILIAN
        );
        applyRarity(
                event,
                ModItems.MILITARY_CONTROLLER,
                ModControllers.MILITARY
        );
        applyRarity(
                event,
                ModItems.ENGINEERING_CONTROLLER,
                ModControllers.ENGINEERING
        );
        applyRarity(
                event,
                ModItems.EXPERIMENTAL_CONTROLLER,
                ModControllers.EXPERIMENTAL
        );
        // Энергосистемы
        applyRarity(
                event,
                ModItems.CIVILIAN_ENERGY_SYSTEM,
                ModEnergySystems.CIVILIAN
        );
        applyRarity(
                event,
                ModItems.MILITARY_ENERGY_SYSTEM,
                ModEnergySystems.MILITARY
        );
        applyRarity(
                event,
                ModItems.ENGINEERING_ENERGY_SYSTEM,
                ModEnergySystems.ENGINEERING
        );
        applyRarity(
                event,
                ModItems.EXPERIMENTAL_ENERGY_SYSTEM,
                ModEnergySystems.EXPERIMENTAL
        );
        // Рамы
        applyRarity(
                event,
                ModItems.CIVILIAN_FRAME,
                ModFrames.CIVILIAN
        );
        applyRarity(
                event,
                ModItems.MILITARY_FRAME,
                ModFrames.MILITARY
        );
        applyRarity(
                event,
                ModItems.ENGINEERING_FRAME,
                ModFrames.ENGINEERING
        );
        applyRarity(
                event,
                ModItems.EXPERIMENTAL_FRAME,
                ModFrames.EXPERIMENTAL
        );
    }

    private static void applyRarity(
            ModifyDefaultComponentsEvent event,
            Supplier<Item> item,
            Supplier<? extends EquipmentDefinition> definition
    ) {
        event.modify(
                item.get(),
                builder -> builder.set(
                        DataComponents.RARITY,
                        definition.get().rarity()
                )
        );
    }
}
