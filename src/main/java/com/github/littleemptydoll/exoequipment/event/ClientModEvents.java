package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.gui.ExoskeletonScreen;
import com.github.littleemptydoll.exoequipment.gui.MatrixScreen;
import com.github.littleemptydoll.exoequipment.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(
        modid = ExoEquipment.MODID,
        value = Dist.CLIENT
)
public final class ClientModEvents {

    private ClientModEvents() {}

    @SubscribeEvent
    public static void registerScreens(
            RegisterMenuScreensEvent event
    ) {
        event.register(
                ModMenus.EXOSKELETON.get(),
                ExoskeletonScreen::new
        );

        event.register(
                ModMenus.MATRIX.get(),
                MatrixScreen::new
        );
    }
}
