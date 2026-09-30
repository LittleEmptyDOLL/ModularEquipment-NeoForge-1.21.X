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
        // Temporary vanilla textures until the titanium artwork is finalized.
        simpleBlockWithItem(ModBlocks.TITANIUM_ORE.get(),
                models().cubeAll("titanium_ore", mcLoc("block/iron_ore")));
        simpleBlockWithItem(ModBlocks.DEEPSLATE_TITANIUM_ORE.get(),
                models().cubeAll("deepslate_titanium_ore", mcLoc("block/deepslate_iron_ore")));
    }
}
