package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.FlightOperations;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class FlightEvents {
    private static final String FLIGHT_MARKER = "exoequipment_flight";

    private FlightEvents() {}

    public static void markActive(Player player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        player.getPersistentData().putBoolean(FLIGHT_MARKER, true);
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
    }

    public static boolean isActive(Player player) {
        return player.getPersistentData().getBoolean(FLIGHT_MARKER);
    }

    public static void markInactive(Player player) {
        player.getPersistentData().remove(FLIGHT_MARKER);

        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().flying = false;
            player.getAbilities().mayfly = false;
            player.onUpdateAbilities();
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !isActive(player)) {
            return;
        }

        if (player.isCreative() || player.isSpectator()) {
            player.getPersistentData().remove(FLIGHT_MARKER);
            return;
        }

        Optional<ItemStack> exoskeleton = findExoskeleton(player);
        if (exoskeleton.isEmpty()) {
            markInactive(player);
            return;
        }

        ExoskeletonData data = ExoskeletonItem.getData(exoskeleton.get());
        if (!FlightOperations.hasActive(data)
                || !ExoskeletonState.canActivate(data)) {
            markInactive(player);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.getOriginal().getPersistentData().getBoolean(FLIGHT_MARKER)) {
            event.getEntity().getPersistentData().remove(FLIGHT_MARKER);
            if (!event.getEntity().isCreative() && !event.getEntity().isSpectator()) {
                event.getEntity().getAbilities().flying = false;
                event.getEntity().getAbilities().mayfly = false;
            }
        }
    }

    private static Optional<ItemStack> findExoskeleton(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(curios -> curios.findFirstCurio(
                        stack -> stack.getItem() instanceof ExoskeletonItem
                ))
                .map(result -> result.stack());
    }
}
