package com.github.littleemptydoll.exoequipment.datagen;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(
            PackOutput output,
            ExistingFileHelper existingFileHelper
    ) {
        super(
                output,
                ExoEquipment.MODID,
                existingFileHelper
        );
    }

    @Override
    protected void registerModels() {
        for (DeferredHolder<Item, ? extends Item> holder :
                ModItems.ITEMS.getEntries()) {
            ResourceLocation id = holder.getId();

            withExistingParent(
                    id.getPath(),
                    mcLoc("item/generated")
            ).texture(
                    "layer0",
                    modLoc("item/" + id.getPath())
            );
        }
    }
}
