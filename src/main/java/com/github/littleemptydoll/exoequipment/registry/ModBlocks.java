package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.fabricator.FabricatorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ExoEquipment.MODID);
    public static final DeferredHolder<Block, FabricatorBlock> FABRICATOR =
            BLOCKS.register("fabricator",
                    () -> new FabricatorBlock(BlockBehaviour.Properties.of().strength(3.0F)));
    public static final DeferredHolder<Block, Block> TITANIUM_ORE =
            BLOCKS.register("titanium_ore", () -> new Block(BlockBehaviour.Properties
                    .ofFullCopy(Blocks.IRON_ORE).requiresCorrectToolForDrops()));
    public static final DeferredHolder<Block, Block> DEEPSLATE_TITANIUM_ORE =
            BLOCKS.register("deepslate_titanium_ore", () -> new Block(BlockBehaviour.Properties
                    .ofFullCopy(Blocks.DEEPSLATE_IRON_ORE).requiresCorrectToolForDrops()));
    public static final DeferredHolder<Block, Block> RAW_TITANIUM_BLOCK =
            BLOCKS.register("raw_titanium_block", () -> new Block(BlockBehaviour.Properties
                    .ofFullCopy(Blocks.RAW_IRON_BLOCK).requiresCorrectToolForDrops()));
    public static final DeferredHolder<Block, Block> TITANIUM_BLOCK =
            BLOCKS.register("titanium_block", () -> new Block(BlockBehaviour.Properties
                    .ofFullCopy(Blocks.IRON_BLOCK).requiresCorrectToolForDrops()));

    private ModBlocks() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
