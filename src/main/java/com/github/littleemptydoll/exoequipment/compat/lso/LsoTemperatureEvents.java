package com.github.littleemptydoll.exoequipment.compat.lso;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonTemperatureState;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import sfiomn.legendarysurvivaloverhaul.api.temperature.TemperatureUtil;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class LsoTemperatureEvents {
    private static final UUID TEMPERATURE_MODIFIER_UUID = UUID.nameUUIDFromBytes(
            "exoequipment:lso_temperature_modifier".getBytes(StandardCharsets.UTF_8)
    );
    private static final UUID EXOSKELETON_TEMPERATURE_UUID = UUID.nameUUIDFromBytes(
            "exoequipment:exoskeleton_temperature".getBytes(StandardCharsets.UTF_8)
    );

    private LsoTemperatureEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }

        var context = ExoskeletonAccess.findContext(player).orElse(null);

        if (context == null) {
            applyModifiers(player, 0.0D, 0.0D, 0.0D, 0.0D);
            TemperatureUtil.addTemperatureModifier(
                    player,
                    0.0D,
                    EXOSKELETON_TEMPERATURE_UUID
            );
            return;
        }

        var data = context.data();
        var poweredModules = context.poweredModules();

        var modifiers = LsoTemperatureOperations.calculateModifiers(
                data,
                poweredModules
        );

        applyModifiers(
                player,
                modifiers.temperature(),
                modifiers.heatResistance(),
                modifiers.coldResistance(),
                modifiers.thermalResistance()
        );

        double resistance = LsoTemperatureOperations
                .calculateTemperatureImpactResistance(data, poweredModules);
        double temperatureImpact =
                (data.temperature()
                        - ExoskeletonTemperatureState.INITIAL_TEMPERATURE)
                        * ExoskeletonTemperatureState.TEMPERATURE_IMPACT_PER_DEGREE
                        * (1.0D - resistance);

        TemperatureUtil.addTemperatureModifier(
                player,
                temperatureImpact,
                EXOSKELETON_TEMPERATURE_UUID
        );
    }

    private static void applyModifiers(
            Player player,
            double temperature,
            double heatResistance,
            double coldResistance,
            double thermalResistance
    ) {
        TemperatureUtil.addTemperatureModifier(
                player,
                temperature,
                TEMPERATURE_MODIFIER_UUID
        );
        TemperatureUtil.addHeatResistanceModifier(
                player,
                heatResistance,
                TEMPERATURE_MODIFIER_UUID
        );
        TemperatureUtil.addColdResistanceModifier(
                player,
                coldResistance,
                TEMPERATURE_MODIFIER_UUID
        );
        TemperatureUtil.addThermalResistanceModifier(
                player,
                thermalResistance,
                TEMPERATURE_MODIFIER_UUID
        );
    }
}
