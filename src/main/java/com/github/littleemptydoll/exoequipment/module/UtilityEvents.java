package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;
import java.util.Optional;

public final class UtilityEvents {
    private static final ResourceLocation BLOCK_BREAK_SPEED_ID=id("utility_block_break_speed");
    private static final ResourceLocation BLOCK_INTERACTION_RANGE_ID=id("utility_block_interaction_range");
    private static final ResourceLocation BURNING_TIME_ID=id("utility_burning_time");
    private static final ResourceLocation LUCK_ID=id("utility_luck");
    private static final ResourceLocation MINING_EFFICIENCY_ID=id("utility_mining_efficiency");
    private static final ResourceLocation SUBMERGED_MINING_SPEED_ID=id("utility_submerged_mining_speed");
    private static final ResourceLocation MOVEMENT_EFFICIENCY_ID=id("utility_movement_efficiency");
    private static final ResourceLocation WATER_MOVEMENT_EFFICIENCY_ID=id("utility_water_movement_efficiency");
    private static final ResourceLocation OXYGEN_BONUS_ID=id("utility_oxygen_bonus");
    private static final ResourceLocation SAFE_FALL_DISTANCE_ID=id("utility_safe_fall_distance");
    private static final ResourceLocation STEP_HEIGHT_ID=id("utility_step_height");

    private UtilityEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player=event.getEntity();
        UtilityOperations.UtilityValues v=findValues(player);
        update(player,Attributes.BLOCK_BREAK_SPEED,BLOCK_BREAK_SPEED_ID,v.blockBreakSpeed(),AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        update(player,Attributes.BLOCK_INTERACTION_RANGE,BLOCK_INTERACTION_RANGE_ID,v.blockInteractionRange(),AttributeModifier.Operation.ADD_VALUE);
        update(player,Attributes.BURNING_TIME,BURNING_TIME_ID,v.burningTime(),AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        update(player,Attributes.LUCK,LUCK_ID,v.luck(),AttributeModifier.Operation.ADD_VALUE);
        update(player,Attributes.MINING_EFFICIENCY,MINING_EFFICIENCY_ID,v.miningEfficiency(),AttributeModifier.Operation.ADD_VALUE);
        update(player,Attributes.SUBMERGED_MINING_SPEED,SUBMERGED_MINING_SPEED_ID,v.submergedMiningSpeed(),AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        update(player,Attributes.MOVEMENT_EFFICIENCY,MOVEMENT_EFFICIENCY_ID,v.movementEfficiency(),AttributeModifier.Operation.ADD_VALUE);
        update(player,Attributes.WATER_MOVEMENT_EFFICIENCY,WATER_MOVEMENT_EFFICIENCY_ID,v.waterMovementEfficiency(),AttributeModifier.Operation.ADD_VALUE);
        update(player,Attributes.OXYGEN_BONUS,OXYGEN_BONUS_ID,v.oxygenBonus(),AttributeModifier.Operation.ADD_VALUE);
        update(player,Attributes.SAFE_FALL_DISTANCE,SAFE_FALL_DISTANCE_ID,v.safeFallDistance(),AttributeModifier.Operation.ADD_VALUE);
        update(player,Attributes.STEP_HEIGHT,STEP_HEIGHT_ID,v.stepHeight(),AttributeModifier.Operation.ADD_VALUE);
    }

    private static UtilityOperations.UtilityValues findValues(Player player) {
        Optional<ItemStack> stack=CuriosApi.getCuriosInventory(player)
                .flatMap(c->c.findFirstCurio(s->s.getItem() instanceof ExoskeletonItem))
                .map(r->r.stack());
        if (stack.isEmpty()) return new UtilityOperations.UtilityValues(0,0,0,0,0,0,0,0,0,0,0,0,0,0);
        ItemStack exoskeleton=stack.get();
        ExoskeletonData data=ExoskeletonItem.getData(exoskeleton);
        ExoskeletonRuntimeState runtime=exoskeleton.getOrDefault(
                ModDataComponents.EXOSKELETON_RUNTIME.get(),ExoskeletonRuntimeState.empty());
        return UtilityOperations.calculate(data,runtime.poweredModules());
    }

    private static void update(Player player,Holder<Attribute> attribute,ResourceLocation id,double amount,AttributeModifier.Operation operation) {
        AttributeInstance instance=player.getAttribute(attribute);
        if (instance==null) return;
        AttributeModifier existing=instance.getModifier(id);
        if (existing!=null && existing.amount()==amount && existing.operation()==operation) return;
        if (existing!=null) instance.removeModifier(id);
        if (amount!=0) instance.addOrUpdateTransientModifier(new AttributeModifier(id,amount,operation));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID,path);
    }
}
