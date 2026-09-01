package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public final class EquipmentRegistry<
        D extends EquipmentDefinition,
        I extends  EquipmentItem<D>
> {

    private final DeferredRegister<D> definitions;
    private final DeferredRegister.Items items;
    private final String itemSuffix;

    private final BiFunction<
            DeferredHolder<D, D>,
            Item.Properties,
            I
    > itemFactory;

    private final Map<
            ResourceLocation,
            EquipmentEntry<D, I>
    > entries = new HashMap<>();

    public EquipmentRegistry(
            DeferredRegister<D> definitions,
            DeferredRegister.Items items,
            String itemSuffix,
            BiFunction<
                    DeferredHolder<D, D>,
                    Item.Properties,
                    I
            > itemFactory
    ) {
        this.definitions = definitions;
        this.items = items;
        this.itemSuffix = itemSuffix;
        this.itemFactory = itemFactory;
    }

    public EquipmentEntry<D, I> register(
            String id,
            EquipmentProperties properties,
            BiFunction<
                    ResourceLocation,
                    EquipmentProperties,
                    D
            > definitionFactory
    ) {
        ResourceLocation resourceLocation =
                ResourceLocation.fromNamespaceAndPath(
                        ExoEquipment.MODID,
                        id
                );

        DeferredHolder<D, D> definition =
                definitions.register(
                        id,
                        () -> definitionFactory.apply(
                                resourceLocation,
                                properties
                        )
                );

        Item.Properties itemProperties =
                new Item.Properties()
                        .rarity(properties.rarity());

        DeferredHolder<Item, I> item =
                items.register(
                        id + itemSuffix,
                        () -> itemFactory.apply(
                                definition,
                                itemProperties
                        )
                );

        EquipmentEntry<D, I> entry =
                new EquipmentEntry<>(
                        definition,
                        item
                );

        entries.put(
                resourceLocation,
                entry
        );

        return entry;
    }

    public EquipmentEntry<D, I> find(
            ResourceLocation id
    ) {
        return entries.get(id);
    }

    private EquipmentEntry<D, I> require(
            ResourceLocation id
    ) {
        EquipmentEntry<D, I> entry = find(id);

        if (entry == null) {
            throw new IllegalArgumentException(
                    "Unknown equipment: " + id
            );
        }

        return entry;
    }

    public D getDefinition(
            ResourceLocation id
    ) {
        return require(id).getDefinition();
    }

    public I getItem(
            ResourceLocation id
    ) {
        return require(id).getItem();
    }
}
