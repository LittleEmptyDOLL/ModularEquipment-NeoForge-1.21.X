package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.datagen.DataGenerators;
import net.neoforged.bus.api.IEventBus;

public final class ModRegistries {
    private ModRegistries() {}

    public static void register(IEventBus event) {
        // Регистрируем генераторы ресурсов мода
        DataGenerators.register(event);
    }
}
