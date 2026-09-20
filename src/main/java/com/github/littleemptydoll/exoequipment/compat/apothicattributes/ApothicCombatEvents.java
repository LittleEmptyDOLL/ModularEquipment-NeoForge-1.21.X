package com.github.littleemptydoll.exoequipment.compat.apothicattributes;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.CombatOperations;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

public final class ApothicCombatEvents {
    private static final ResourceLocation CRIT_CHANCE_ID = id("combat_crit_chance");
    private static final ResourceLocation CRIT_DAMAGE_ID = id("combat_crit_damage");
    private static final ResourceLocation ARMOR_PIERCE_ID = id("combat_armor_pierce");
    private static final ResourceLocation ARMOR_SHRED_ID = id("combat_armor_shred");
    private static final ResourceLocation PROT_PIERCE_ID = id("combat_prot_pierce");
    private static final ResourceLocation PROT_SHRED_ID = id("combat_prot_shred");
    private static final ResourceLocation CURRENT_HP_DAMAGE_ID = id("combat_current_hp_damage");
    private static final ResourceLocation LIFE_STEAL_ID = id("combat_life_steal");
    private static final ResourceLocation OVERHEAL_ID = id("combat_overheal");
    private static final ResourceLocation FIRE_DAMAGE_ID = id("combat_fire_damage");
    private static final ResourceLocation COLD_DAMAGE_ID = id("combat_cold_damage");
    private static final ResourceLocation PROJECTILE_DAMAGE_ID = id("combat_projectile_damage");
    private static final ResourceLocation ARROW_DAMAGE_ID = id("combat_arrow_damage");
    private static final ResourceLocation ARROW_VELOCITY_ID = id("combat_arrow_velocity");
    private static final ResourceLocation DRAW_SPEED_ID = id("combat_draw_speed");

    private ApothicCombatEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        updateAttribute(player, ALObjects.Attributes.CRIT_CHANCE, CRIT_CHANCE_ID, 0.0D);
        updateAttribute(player, ALObjects.Attributes.CRIT_DAMAGE, CRIT_DAMAGE_ID, 0.0D);
        updateAttribute(player, ALObjects.Attributes.ARMOR_PIERCE, ARMOR_PIERCE_ID, 0.0D);
        updateAttribute(player, ALObjects.Attributes.ARMOR_SHRED, ARMOR_SHRED_ID, 0.0D);
        updateAttribute(player, ALObjects.Attributes.PROT_PIERCE, PROT_PIERCE_ID, 0.0D);
        updateAttribute(player, ALObjects.Attributes.PROT_SHRED, PROT_SHRED_ID, 0.0D);
        updateAttribute(player, ALObjects.Attributes.CURRENT_HP_DAMAGE, CURRENT_HP_DAMAGE_ID, 0.0D);
        updateAttribute(player, ALObjects.Attributes.LIFE_STEAL, LIFE_STEAL_ID, 0.0D);
        updateAttribute(player, ALObjects.Attributes.OVERHEAL, OVERHEAL_ID, 0.0D);
        updateAttribute(player, ALObjects.Attributes.FIRE_DAMAGE, FIRE_DAMAGE_ID, 0.0D);
        updateAttribute(player, ALObjects.Attributes.COLD_DAMAGE, COLD_DAMAGE_ID, 0.0D);
        updateAttribute(player, ALObjects.Attributes.PROJECTILE_DAMAGE, PROJECTILE_DAMAGE_ID, 0.0D);
        updateAttribute(player, ALObjects.Attributes.ARROW_DAMAGE, ARROW_DAMAGE_ID, 0.0D);
        updateAttribute(player, ALObjects.Attributes.ARROW_VELOCITY, ARROW_VELOCITY_ID, 0.0D);
        updateAttribute(player, ALObjects.Attributes.DRAW_SPEED, DRAW_SPEED_ID, 0.0D);

        Optional<ItemStack> exoskeletonStack = CuriosApi.getCuriosInventory(player)
                .flatMap(curios -> curios.findFirstCurio(
                        stack -> stack.getItem() instanceof ExoskeletonItem
                ))
                .map(result -> result.stack());

        if (exoskeletonStack.isEmpty()) return;

        ItemStack stack = exoskeletonStack.get();
        ExoskeletonData data = ExoskeletonItem.getData(stack);
        ExoskeletonRuntimeState runtime = stack.get(ModDataComponents.EXOSKELETON_RUNTIME.get());
        if (runtime == null) runtime = ExoskeletonRuntimeState.empty();

        CombatOperations.CombatValues values =
                CombatOperations.calculate(data, runtime.poweredModules());

        updateAttribute(player, ALObjects.Attributes.CRIT_CHANCE, CRIT_CHANCE_ID, values.critChance());
        updateAttribute(player, ALObjects.Attributes.CRIT_DAMAGE, CRIT_DAMAGE_ID, values.critDamage());
        updateAttribute(player, ALObjects.Attributes.ARMOR_PIERCE, ARMOR_PIERCE_ID, values.armorPierce());
        updateAttribute(player, ALObjects.Attributes.ARMOR_SHRED, ARMOR_SHRED_ID, values.armorShred());
        updateAttribute(player, ALObjects.Attributes.PROT_PIERCE, PROT_PIERCE_ID, values.protPierce());
        updateAttribute(player, ALObjects.Attributes.PROT_SHRED, PROT_SHRED_ID, values.protShred());
        updateAttribute(player, ALObjects.Attributes.CURRENT_HP_DAMAGE, CURRENT_HP_DAMAGE_ID, values.currentHpDamage());
        updateAttribute(player, ALObjects.Attributes.LIFE_STEAL, LIFE_STEAL_ID, values.lifeSteal());
        updateAttribute(player, ALObjects.Attributes.OVERHEAL, OVERHEAL_ID, values.overheal());
        updateAttribute(player, ALObjects.Attributes.FIRE_DAMAGE, FIRE_DAMAGE_ID, values.fireDamage());
        updateAttribute(player, ALObjects.Attributes.COLD_DAMAGE, COLD_DAMAGE_ID, values.coldDamage());
        updateAttribute(player, ALObjects.Attributes.PROJECTILE_DAMAGE, PROJECTILE_DAMAGE_ID, values.projectileDamage());
        updateAttribute(player, ALObjects.Attributes.ARROW_DAMAGE, ARROW_DAMAGE_ID, values.arrowDamage());
        updateAttribute(player, ALObjects.Attributes.ARROW_VELOCITY, ARROW_VELOCITY_ID, values.arrowVelocity());
        updateAttribute(player, ALObjects.Attributes.DRAW_SPEED, DRAW_SPEED_ID, values.drawSpeed());
    }

    private static void updateAttribute(
            Player player,
            Holder<Attribute> attribute,
            ResourceLocation id,
            double amount
    ) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;

        AttributeModifier existing = instance.getModifier(id);
        if (existing != null && existing.amount() == amount
                && existing.operation() == AttributeModifier.Operation.ADD_VALUE) return;

        if (existing != null) instance.removeModifier(id);

        if (amount != 0.0D) {
            instance.addOrUpdateTransientModifier(
                    new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE)
            );
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, path);
    }
}
