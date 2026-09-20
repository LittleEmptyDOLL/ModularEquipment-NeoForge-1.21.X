package com.github.littleemptydoll.exoequipment.compat.lso;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import sfiomn.legendarysurvivaloverhaul.api.temperature.TemperatureUtil;
import top.theillusivec4.curios.api.CuriosApi;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class LsoTemperatureEvents {
    private static final UUID MODIFIER_UUID = UUID.nameUUIDFromBytes(
            "exoequipment:lso_temperature".getBytes(StandardCharsets.UTF_8)
    );

    private LsoTemperatureEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }

        Optional<ItemStack> exoskeletonStack =
                CuriosApi.getCuriosInventory(player)
                        .flatMap(curios ->
                                curios.findFirstCurio(
                                        stack -> stack.getItem() instanceof ExoskeletonItem
                                )
                        )
                        .map(result -> result.stack());

        if (exoskeletonStack.isEmpty()) {
            applyModifiers(player, 0.0D, 0.0D, 0.0D, 0.0D);
            return;
        }

        ItemStack stack = exoskeletonStack.get();
        var data = ExoskeletonItem.getData(stack);

        ExoskeletonRuntimeState runtime =
                stack.get(ModDataComponents.EXOSKELETON_RUNTIME.get());

        Set<com.github.littleemptydoll.exoequipment.module.InstalledModuleReference> poweredModules =
                runtime == null
                        ? Set.of()
                        : runtime.poweredModules();

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
                MODIFIER_UUID
        );
        TemperatureUtil.addHeatResistanceModifier(
                player,
                heatResistance,
                MODIFIER_UUID
        );
        TemperatureUtil.addColdResistanceModifier(
                player,
                coldResistance,
                MODIFIER_UUID
        );
        TemperatureUtil.addThermalResistanceModifier(
                player,
                thermalResistance,
                MODIFIER_UUID
        );
    }
}
