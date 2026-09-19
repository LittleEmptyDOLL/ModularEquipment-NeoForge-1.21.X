package com.github.littleemptydoll.exoequipment.compat.lso;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import sfiomn.legendarysurvivaloverhaul.api.thirst.ThirstUtil;
import sfiomn.legendarysurvivaloverhaul.common.attachments.thirst.ThirstAttachment;
import sfiomn.legendarysurvivaloverhaul.util.AttachmentUtil;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class LsoThirstEvents {
    private static final Map<Player, Float> PREVIOUS_EXHAUSTION =
            new WeakHashMap<>();

    private LsoThirstEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }

        if (!ThirstUtil.isThirstActive(player)) {
            PREVIOUS_EXHAUSTION.remove(player);
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
            PREVIOUS_EXHAUSTION.remove(player);
            return;
        }

        ItemStack stack = exoskeletonStack.get();
        var data = ExoskeletonItem.getData(stack);

        ExoskeletonRuntimeState runtime =
                stack.get(ModDataComponents.EXOSKELETON_RUNTIME.get());

        if (runtime == null) {
            runtime = ExoskeletonRuntimeState.empty();
        }

        ThirstAttachment thirst = AttachmentUtil.getThirstAttachment(player);
        float currentExhaustion = thirst.getThirstExhaustion();

        Float previousExhaustion = PREVIOUS_EXHAUSTION.get(player);

        if (previousExhaustion == null) {
            PREVIOUS_EXHAUSTION.put(player, currentExhaustion);
            return;
        }

        float reducedExhaustion = LsoThirstOperations.applyExhaustionReduction(
                previousExhaustion,
                currentExhaustion,
                data,
                runtime.poweredModules()
        );

        if (reducedExhaustion != currentExhaustion) {
            thirst.setExhaustion(reducedExhaustion);
        }

        PREVIOUS_EXHAUSTION.put(player, reducedExhaustion);
    }
}
