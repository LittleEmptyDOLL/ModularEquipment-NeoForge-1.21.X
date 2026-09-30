package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ExoEquipment.MODID);

    public static final DeferredHolder<Item, BlockItem> TITANIUM_ORE =
            ITEMS.register("titanium_ore", () ->
                    new BlockItem(ModBlocks.TITANIUM_ORE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> DEEPSLATE_TITANIUM_ORE =
            ITEMS.register("deepslate_titanium_ore", () ->
                    new BlockItem(ModBlocks.DEEPSLATE_TITANIUM_ORE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> RAW_TITANIUM = part("raw_titanium");
    public static final DeferredHolder<Item, Item> TITANIUM_INGOT = part("titanium_ingot");

    // Craftable intermediates. Recipes are processed by the Fabricator from the player's inventory.
    public static final DeferredHolder<Item, Item> CARBON_FIBER = part("carbon_fiber");
    public static final DeferredHolder<Item, Item> COMPOSITE = part("composite");
    public static final DeferredHolder<Item, Item> ELECTRONIC_COMPONENT = part("electronic_component");
    public static final DeferredHolder<Item, Item> MECHANICAL_COMPONENT = part("mechanical_component");
    public static final DeferredHolder<Item, Item> ENERGY_COMPONENT = part("energy_component");
    public static final DeferredHolder<Item, Item> STRUCTURAL_COMPONENT = part("structural_component");
    public static final DeferredHolder<Item, Item> SHIELD_EMITTER = part("shield_emitter");
    public static final DeferredHolder<Item, Item> THRUSTER = part("thruster");
    public static final DeferredHolder<Item, Item> SENSOR_ARRAY = part("sensor_array");
    public static final DeferredHolder<Item, Item> THERMAL_REGULATOR = part("thermal_regulator");

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

    public static final DeferredHolder<Item, BlockItem> FABRICATOR =
            ITEMS.register("fabricator", () ->
                    new BlockItem(ModBlocks.FABRICATOR.get(), new Item.Properties()));

    private static DeferredHolder<Item, Item> part(String id) {
        return ITEMS.register(id, () -> new Item(new Item.Properties()));
    }

    public static boolean isFabricatorPart(Item item) {
        return item == CARBON_FIBER.get() || item == COMPOSITE.get()
                || item == ELECTRONIC_COMPONENT.get() || item == MECHANICAL_COMPONENT.get()
                || item == ENERGY_COMPONENT.get() || item == STRUCTURAL_COMPONENT.get()
                || item == SHIELD_EMITTER.get() || item == THRUSTER.get()
                || item == SENSOR_ARRAY.get() || item == THERMAL_REGULATOR.get();
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
