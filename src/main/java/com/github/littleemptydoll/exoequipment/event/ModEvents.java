package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.command.ModCommands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(
        modid = ExoEquipment.MODID
)
public final class ModEvents {
    private ModEvents() {}

    @SubscribeEvent
    public static void registerCommands(
            RegisterCommandsEvent event
    ) {
        ModCommands.register(event);
    }
}
