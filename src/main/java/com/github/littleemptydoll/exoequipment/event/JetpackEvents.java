package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.JetpackOperations;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class JetpackEvents {

    private JetpackEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        Optional<ItemStack> exoskeleton = findExoskeleton(player);
        if (exoskeleton.isEmpty()) {
            return;
        }

        ItemStack stack = exoskeleton.get();
        var data = ExoskeletonItem.getData(stack);
        ExoskeletonRuntimeState runtime = stack.get(
                ModDataComponents.EXOSKELETON_RUNTIME.get()
        );

        if (runtime == null || runtime.poweredModules().isEmpty()) {
            return;
        }

        var updatedData = JetpackOperations.tick(
                player,
                data,
                runtime.poweredModules()
        );

        if (!updatedData.equals(data)) {
            stack.set(
                    ModDataComponents.EXOSKELETON_DATA.get(),
                    updatedData
            );
        }

        if (!player.level().isClientSide()
                && player instanceof ServerPlayer serverPlayer
                && !updatedData.equals(data)) {
            serverPlayer.connection.send(
                    new ClientboundSetEntityMotionPacket(serverPlayer)
            );
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
