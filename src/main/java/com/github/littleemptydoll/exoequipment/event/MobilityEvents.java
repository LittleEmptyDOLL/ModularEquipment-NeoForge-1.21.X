package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.github.littleemptydoll.exoequipment.module.MobilityOperations;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class MobilityEvents {
    private static final ResourceLocation MOVEMENT_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "mobility_movement_speed"
            );

    private static final ResourceLocation SWIM_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "mobility_swim_speed"
            );

    private static final ResourceLocation JUMP_STRENGTH_ID =
            ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "mobility_jump_strength"
            );

    private static final ResourceLocation STEP_HEIGHT_ID =
            ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "mobility_step_height"
            );

    private MobilityEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        updateAttribute(
                player,
                Attributes.MOVEMENT_SPEED,
                MOVEMENT_SPEED_ID,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0D
        );

        updateAttribute(
                player,
                Attributes.WATER_MOVEMENT_EFFICIENCY,
                SWIM_SPEED_ID,
                AttributeModifier.Operation.ADD_VALUE,
                0.0D
        );

        updateAttribute(
                player,
                Attributes.JUMP_STRENGTH,
                JUMP_STRENGTH_ID,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0D
        );

        updateAttribute(
                player,
                Attributes.STEP_HEIGHT,
                STEP_HEIGHT_ID,
                AttributeModifier.Operation.ADD_VALUE,
                0.0D
        );

        Optional<ItemStack> stack = findExoskeleton(player);

        if (stack.isEmpty()) {
            return;
        }

        ExoskeletonData data = ExoskeletonItem.getData(stack.get());
        ExoskeletonRuntimeState runtime = getRuntime(stack.get());

        MobilityOperations.MobilityValues values =
                MobilityOperations.calculate(
                        data,
                        runtime.poweredModules()
                );

        updateAttribute(
                player,
                Attributes.MOVEMENT_SPEED,
                MOVEMENT_SPEED_ID,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                values.movementSpeed()
        );

        updateAttribute(
                player,
                Attributes.WATER_MOVEMENT_EFFICIENCY,
                SWIM_SPEED_ID,
                AttributeModifier.Operation.ADD_VALUE,
                values.swimSpeed()
        );

        updateAttribute(
                player,
                Attributes.JUMP_STRENGTH,
                JUMP_STRENGTH_ID,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                values.jumpStrength()
        );

        updateAttribute(
                player,
                Attributes.STEP_HEIGHT,
                STEP_HEIGHT_ID,
                AttributeModifier.Operation.ADD_VALUE,
                values.stepHeight()
        );
    }

    private static void updateAttribute(
            Player player,
            net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
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
