package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ExoEquipment.MODID);

    // These three components have no crafting recipes; loot integration follows the
    // structure pools when their locations are defined.
    public static final DeferredHolder<Item, Item> MILITARY_COMPONENT =
            ITEMS.register("military_component", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> ENGINEERING_COMPONENT =
            ITEMS.register("engineering_component", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> EXPERIMENTAL_COMPONENT =
            ITEMS.register("experimental_component", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));

    // Installed in the Fabricator's three dedicated upgrade slots.
    public static final DeferredHolder<Item, Item> MILITARY_FABRICATOR_UPGRADE =
            ITEMS.register("military_fabricator_upgrade", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> ENGINEERING_FABRICATOR_UPGRADE =
            ITEMS.register("engineering_fabricator_upgrade", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> EXPERIMENTAL_FABRICATOR_UPGRADE =
            ITEMS.register("experimental_fabricator_upgrade", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
