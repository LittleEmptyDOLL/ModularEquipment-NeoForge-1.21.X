package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.item.MatrixItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ExoEquipment.MODID);

    public static final Supplier<Item> EXOSKELETON = ITEMS.register(
            "exoskeleton",
            () -> new ExoskeletonItem(new Item.Properties())
    );

    public static final Supplier<Item> FRAME = ITEMS.register(
            "frame",
            () -> new Item(new Item.Properties())
    );

    public static final Supplier<Item> CIVILIAN_MATRIX = ITEMS.register(
            "civilian_matrix",
            () -> new MatrixItem(ModMatrices.CIVILIAN)
    );

    public static final Supplier<Item> MILITARY_MATRIX = ITEMS.register(
            "military_matrix",
            () -> new MatrixItem(ModMatrices.MILITARY)
    );

    public static final Supplier<Item> EXPERIMENTAL_MATRIX = ITEMS.register(
            "experimental_matrix",
            () -> new MatrixItem(ModMatrices.EXPERIMENTAL)
    );

    public static final Supplier<Item> ENERGY_SYSTEM = ITEMS.register(
            "energy_system",
            () -> new Item(new Item.Properties())
    );

    public static final Supplier<Item> CONTROLLER = ITEMS.register(
            "controller",
            () -> new Item(new Item.Properties())
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
