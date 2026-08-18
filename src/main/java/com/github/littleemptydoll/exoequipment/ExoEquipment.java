package com.github.littleemptydoll.exoequipment;

import com.github.littleemptydoll.exoequipment.command.ModCommands;
import com.github.littleemptydoll.exoequipment.registry.*;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ExoEquipment.MODID)
public class ExoEquipment {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "exoequipment";

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ExoEquipment(IEventBus eventBus) {
        eventBus.addListener(ModRegistries::register);

        ModItems.register(eventBus);
        ModDataComponents.register(eventBus);

        ModMatrices.MATRICES.register(eventBus);
        ModModules.MODULES.register(eventBus);
    }
}
