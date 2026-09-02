package com.github.littleemptydoll.exoequipment.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

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
                new ModItemModelProvider(
                        output,
                        existingFileHelper
                )
        );
    }

    private DataGenerators() {
    }
}
