package com.github.littleemptydoll.exoequipment.compat.apothicattributes;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.UtilityOperations;
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

public final class ApothicUtilityEvents {
    private static final ResourceLocation EXPERIENCE_GAINED_ID=id("utility_experience_gained");
    private static final ResourceLocation HEALING_RECEIVED_ID=id("utility_healing_received");
    private static final ResourceLocation DODGE_CHANCE_ID=id("utility_dodge_chance");

    private ApothicUtilityEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player=event.getEntity();
        UtilityOperations.UtilityValues v=findValues(player);
        update(player,ALObjects.Attributes.EXPERIENCE_GAINED,EXPERIENCE_GAINED_ID,v.experienceGained());
        update(player,ALObjects.Attributes.HEALING_RECEIVED,HEALING_RECEIVED_ID,v.healingReceived());
        update(player,ALObjects.Attributes.DODGE_CHANCE,DODGE_CHANCE_ID,v.dodgeChance());
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

    private static void update(Player player,Holder<Attribute> attribute,ResourceLocation id,double amount) {
        AttributeInstance instance=player.getAttribute(attribute);
        if (instance==null) return;
        AttributeModifier existing=instance.getModifier(id);
        if (existing!=null && existing.amount()==amount) return;
        if (existing!=null) instance.removeModifier(id);
        if (amount!=0) {
            instance.addOrUpdateTransientModifier(
                    new AttributeModifier(id,amount,AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID,path);
    }
}
