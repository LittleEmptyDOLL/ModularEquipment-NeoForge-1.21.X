package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.AttributeOperations;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class AttributeEvents {
    private static final Map<Player, Set<AttributeOperations.AttributeKey>> APPLIED =
            Collections.synchronizedMap(new WeakHashMap<>());

    private AttributeEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        Map<AttributeOperations.AttributeKey, Double> desired =
                findAttributes(player);

        Set<AttributeOperations.AttributeKey> previous =
                APPLIED.computeIfAbsent(player, ignored -> new HashSet<>());

        Set<AttributeOperations.AttributeKey> previousSnapshot;

        synchronized (previous) {
            previousSnapshot = new HashSet<>(previous);
        }

        for (AttributeOperations.AttributeKey key : previousSnapshot) {
            if (!desired.containsKey(key)) {
                removeModifier(player, key);
            }
        }

        Set<AttributeOperations.AttributeKey> applied =
                new HashSet<>();

        for (Map.Entry<AttributeOperations.AttributeKey, Double> entry : desired.entrySet()) {
            AttributeOperations.AttributeKey key = entry.getKey();
            if (updateModifier(player, key, entry.getValue())) {
                applied.add(key);
            }
        }

        synchronized (previous) {
            previous.clear();
            previous.addAll(applied);
        }
    }

    private static Map<AttributeOperations.AttributeKey, Double> findAttributes(Player player) {
        Optional<ItemStack> exoskeletonStack = CuriosApi.getCuriosInventory(player)
                .flatMap(curios -> curios.findFirstCurio(
                        stack -> stack.getItem() instanceof ExoskeletonItem
                ))
                .map(result -> result.stack());

        if (exoskeletonStack.isEmpty()) {
            return Map.of();
        }

        ItemStack stack = exoskeletonStack.get();
        ExoskeletonData data = ExoskeletonItem.getData(stack);
        ExoskeletonRuntimeState runtime = stack.get(
                ModDataComponents.EXOSKELETON_RUNTIME.get()
        );

        if (runtime == null) {
            runtime = ExoskeletonRuntimeState.empty();
        }

        return AttributeOperations.calculate(
                data,
                runtime.poweredModules()
        );
    }

    private static boolean updateModifier(
            Player player,
            AttributeOperations.AttributeKey key,
            double amount
    ) {
        Holder<Attribute> attribute = resolve(key.attributeId());
        if (attribute == null) {
            return false;
        }

        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return false;
        }

        ResourceLocation modifierId = modifierId(key);
        AttributeModifier existing = instance.getModifier(modifierId);

        if (amount == 0.0D) {
            if (existing != null) {
                instance.removeModifier(modifierId);
            }
            return true;
        }

        if (existing != null
                && existing.amount() == amount
                && existing.operation() == key.operation()) {
            return true;
        }

        instance.addOrUpdateTransientModifier(
                new AttributeModifier(
                        modifierId,
                        amount,
                        key.operation()
                )
        );

        return true;
    }

    private static void removeModifier(
            Player player,
            AttributeOperations.AttributeKey key
    ) {
        Holder<Attribute> attribute = resolve(key.attributeId());
        if (attribute == null) {
            return;
        }

        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(modifierId(key));
        }
    }

    private static Holder<Attribute> resolve(ResourceLocation id) {
        return BuiltInRegistries.ATTRIBUTE.getHolder(id).orElse(null);
    }

    private static ResourceLocation modifierId(
            AttributeOperations.AttributeKey key
    ) {
        return ResourceLocation.fromNamespaceAndPath(
                ExoEquipment.MODID,
                "attribute/"
                        + key.attributeId().getNamespace()
                        + "/"
                        + key.attributeId().getPath()
                        + "/"
                        + key.operation().getSerializedName()
        );
    }
}
