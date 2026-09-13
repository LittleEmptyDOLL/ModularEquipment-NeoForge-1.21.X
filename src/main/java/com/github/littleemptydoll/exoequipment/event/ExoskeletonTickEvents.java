package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.energy.EnergyOperations;
import com.github.littleemptydoll.exoequipment.energy.EnergyTickResult;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.gui.ExoskeletonMenuProvider;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class ExoskeletonTickEvents {
    private ExoskeletonTickEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        ExoskeletonMenuProvider.findBodyExoskeleton(player)
                .ifPresent(ExoskeletonTickEvents::tickEnergy);
    }

    private static void tickEnergy(ItemStack exoskeleton) {
        ExoskeletonData data = ExoskeletonItem.getData(exoskeleton);
        EnergyTickResult result = EnergyOperations.tick(data);

        if (!result.data().equals(data)) {
            exoskeleton.set(
                    ModDataComponents.EXOSKELETON_DATA.get(),
                    result.data()
            );
        }
    }
}
