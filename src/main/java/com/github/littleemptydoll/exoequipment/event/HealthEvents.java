package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.HealthOperations;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

import static net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class HealthEvents {
    private static final ResourceLocation ADDITIONAL_HEALTH_ID =
            ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "health_additional"
            );

    private HealthEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        Optional<ItemStack> exoskeletonStack = findExoskeleton(player);

        if (exoskeletonStack.isEmpty()) {
            updateAttribute(
                    player,
                    MAX_HEALTH,
                    ADDITIONAL_HEALTH_ID,
                    AttributeModifier.Operation.ADD_VALUE,
                    0.0D
            );
            clampHealth(player);
            return;
        }

        ItemStack stack = exoskeletonStack.get();
        ExoskeletonData data = ExoskeletonItem.getData(stack);
        ExoskeletonRuntimeState runtime = getRuntime(stack);

        double additionalHealth = HealthOperations.calculateAdditionalHealth(
                data,
                runtime.poweredModules()
        );

        updateAttribute(
                player,
                MAX_HEALTH,
                ADDITIONAL_HEALTH_ID,
                AttributeModifier.Operation.ADD_VALUE,
                additionalHealth
        );

        clampHealth(player);
    }

    private static void updateAttribute(
            Player player,
            net.minecraft.core.Holder<Attribute> attribute,
            ResourceLocation id,
            AttributeModifier.Operation operation,
            double amount
    ) {
        AttributeInstance instance = player.getAttribute(attribute);

        if (instance == null) {
            return;
        }

        AttributeModifier existing = instance.getModifier(id);

        if (existing != null
                && existing.amount() == amount
                && existing.operation() == operation) {
            return;
        }

        if (existing != null) {
            instance.removeModifier(id);
        }

        if (amount != 0.0D) {
            instance.addOrUpdateTransientModifier(
                    new AttributeModifier(id, amount, operation)
            );
        }
    }

    private static void clampHealth(Player player) {
        float maxHealth = player.getMaxHealth();

        if (player.getHealth() > maxHealth) {
            player.setHealth(maxHealth);
        }
    }

    private static ExoskeletonRuntimeState getRuntime(ItemStack stack) {
        ExoskeletonRuntimeState runtime =
                stack.get(ModDataComponents.EXOSKELETON_RUNTIME.get());

        return runtime != null
                ? runtime
                : ExoskeletonRuntimeState.empty();
    }

    private static Optional<ItemStack> findExoskeleton(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(curios ->
                        curios.findFirstCurio(
                                stack -> stack.getItem() instanceof ExoskeletonItem
                        )
                )
                .map(result -> result.stack());
    }
}
