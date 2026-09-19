package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.network.SensorHighlightPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterClientPayloadHandlersEvent;

@EventBusSubscriber(
        modid = ExoEquipment.MODID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD
)
public final class SensorClientNetworking {

    private SensorClientNetworking() {}

    @SubscribeEvent
    public static void register(RegisterClientPayloadHandlersEvent event) {
        event.register(
                SensorHighlightPayload.TYPE,
                (payload, context) ->
                        context.enqueueWork(
                                () -> SensorHighlightClient.apply(payload)
                        )
        );
    }
}
