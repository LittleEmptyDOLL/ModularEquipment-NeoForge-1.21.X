package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.CombatOperations;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
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
public final class CombatEvents {
    private static final ResourceLocation ATTACK_DAMAGE_ID =
            ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "combat_attack_damage"
            );

    private static final ResourceLocation ATTACK_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "combat_attack_speed"
            );

    private static final ResourceLocation ENTITY_INTERACTION_RANGE_ID =
            ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "combat_entity_interaction_range"
            );

    private CombatEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        updateAttribute(
                player,
                Attributes.ATTACK_DAMAGE,
                ATTACK_DAMAGE_ID,
                AttributeModifier.Operation.ADD_VALUE,
                0.0D
        );

        updateAttribute(
                player,
                Attributes.ATTACK_SPEED,
                ATTACK_SPEED_ID,
                AttributeModifier.Operation.ADD_VALUE,
                0.0D
        );

        updateAttribute(
                player,
                Attributes.ENTITY_INTERACTION_RANGE,
                ENTITY_INTERACTION_RANGE_ID,
                AttributeModifier.Operation.ADD_VALUE,
                0.0D
        );

        Optional<ItemStack> stack = findExoskeleton(player);

        if (stack.isEmpty()) {
            return;
        }

        ExoskeletonData data = ExoskeletonItem.getData(stack.get());
        ExoskeletonRuntimeState runtime = stack.get(
                ModDataComponents.EXOSKELETON_RUNTIME.get()
        );

        if (runtime == null) {
            runtime = ExoskeletonRuntimeState.empty();
        }

        CombatOperations.CombatValues values =
                CombatOperations.calculate(
                        data,
                        runtime.poweredModules()
                );

        updateAttribute(
                player,
                Attributes.ATTACK_DAMAGE,
                ATTACK_DAMAGE_ID,
                AttributeModifier.Operation.ADD_VALUE,
                values.attackDamage()
        );

        updateAttribute(
                player,
                Attributes.ATTACK_SPEED,
                ATTACK_SPEED_ID,
                AttributeModifier.Operation.ADD_VALUE,
                values.attackSpeed()
        );

        updateAttribute(
                player,
                Attributes.ENTITY_INTERACTION_RANGE,
                ENTITY_INTERACTION_RANGE_ID,
                AttributeModifier.Operation.ADD_VALUE,
                values.entityInteractionRange()
        );
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
