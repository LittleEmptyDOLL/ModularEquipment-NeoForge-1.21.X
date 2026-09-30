package com.github.littleemptydoll.exoequipment.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;

public class DataGenerators {
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(DataGenerators::gatherData);
    }

    private static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(
                event.includeClient(),
                new ModLanguageProvider(
                        output
                )
        );
        generator.addProvider(
                event.includeClient(),
                new ModBlockStateProvider(output, existingFileHelper)
        );
        generator.addProvider(
                event.includeClient(),
                new ModItemModelProvider(
                        output,
                        existingFileHelper
                )
        );
        generator.addProvider(event.includeServer(),
                new LootTableProvider(output, Set.of(),
                        List.of(new LootTableProvider.SubProviderEntry(
                                ModBlockLootProvider::new, LootContextParamSets.BLOCK)),
                        event.getLookupProvider()));
    }

    private DataGenerators() {
    }
}
