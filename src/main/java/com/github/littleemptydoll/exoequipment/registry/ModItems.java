package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.item.MatrixItem;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
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

    public static final Supplier<Item> MATRIX = ITEMS.register(
            "matrix",
            () -> new MatrixItem(
                    new Item.Properties()
                            .component(
                                    ModDataComponents.MATRIX_DATA.get(),
                                    MatrixData.empty(6, 6)
                            )
            )
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
