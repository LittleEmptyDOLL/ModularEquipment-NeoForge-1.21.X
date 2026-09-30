package com.github.littleemptydoll.exoequipment.datagen;

import com.github.littleemptydoll.exoequipment.registry.ModBlocks;
import com.github.littleemptydoll.exoequipment.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Set;

public final class ModBlockLootProvider extends BlockLootSubProvider {
    public ModBlockLootProvider(HolderLookup.Provider lookupProvider) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, lookupProvider);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        // The Fabricator's loot table is maintained in src/main/resources.
        return List.of(ModBlocks.TITANIUM_ORE.get(), ModBlocks.DEEPSLATE_TITANIUM_ORE.get());
    }

    @Override
    protected void generate() {
        add(ModBlocks.TITANIUM_ORE.get(),
                createOreDrop(ModBlocks.TITANIUM_ORE.get(), ModItems.RAW_TITANIUM.get()));
        add(ModBlocks.DEEPSLATE_TITANIUM_ORE.get(),
                createOreDrop(ModBlocks.DEEPSLATE_TITANIUM_ORE.get(), ModItems.RAW_TITANIUM.get()));
    }
}
