package com.github.littleemptydoll.exoequipment;

import com.github.littleemptydoll.exoequipment.network.ModNetworking;
import com.github.littleemptydoll.exoequipment.registry.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ExoEquipment.MODID)
public class ExoEquipment {
    public static final String MODID = "exoequipment";
    public static final String LOCALE = "en_us";

    public ExoEquipment(IEventBus eventBus) {
        ModRegistries.register(eventBus);
        eventBus.addListener(ModRegistryKeys::register);

        ModItems.register(eventBus);
        ModDataComponents.register(eventBus);
        ModMenus.register(eventBus);
        ModNetworking.register(eventBus);

        ModMatrices.MATRICES.register(eventBus);
        ModModules.MODULES.register(eventBus);
        ModEnergySystems.ENERGY_SYSTEMS.register(eventBus);
        ModFrames.FRAMES.register(eventBus);
        ModControllers.CONTROLLERS.register(eventBus);
        ModExoskeletons.EXOSKELETONS.register(eventBus);
    }
}
