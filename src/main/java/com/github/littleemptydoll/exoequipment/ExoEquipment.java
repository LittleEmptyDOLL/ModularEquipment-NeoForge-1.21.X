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
    public static final String LOCALE = "en_us";

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ExoEquipment(IEventBus eventBus) {
        // Регистрируем абсолютно всё именно тут, что бы не грузить главный класс
        ModRegistries.register(eventBus);
        eventBus.addListener(ModRegistryKeys::register);
        eventBus.register(ModItemComponents.class);

        ModItems.register(eventBus);
        ModDataComponents.register(eventBus);

        ModMatrices.MATRICES.register(eventBus);
        ModModules.MODULES.register(eventBus);
        ModEnergySystems.ENERGY_SYSTEMS.register(eventBus);
        ModFrames.FRAMES.register(eventBus);
        ModControllers.CONTROLLERS.register(eventBus);
        ModExoskeletons.EXOSKELETONS.register(eventBus);
    }
}
