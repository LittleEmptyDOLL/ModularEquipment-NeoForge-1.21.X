package com.github.littleemptydoll.exoequipment.datagen;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ExoEquipment.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Temporary appearance until the Fabricator receives its own texture/model.
        simpleBlockWithItem(ModBlocks.FABRICATOR.get(),
                models().cubeAll("fabricator", mcLoc("block/iron_block")));
        simpleBlockWithItem(ModBlocks.TITANIUM_ORE.get(),
                models().cubeAll("titanium_ore", modLoc("block/titanium_ore")));
        simpleBlockWithItem(ModBlocks.DEEPSLATE_TITANIUM_ORE.get(),
                models().cubeAll("deepslate_titanium_ore", modLoc("block/deepslate_titanium_ore")));
        simpleBlockWithItem(ModBlocks.RAW_TITANIUM_BLOCK.get(),
                models().cubeAll("raw_titanium_block", modLoc("block/raw_titanium_block")));
        simpleBlockWithItem(ModBlocks.TITANIUM_BLOCK.get(),
                models().cubeAll("titanium_block", modLoc("block/titanium_block")));
    }
}
