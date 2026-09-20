package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.CloakingOperations;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.network.CloakingStatePayload;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class CloakingEvents {
    private static final String CLOAKING_MARKER = "exoequipment_cloaking";

    private CloakingEvents() {}

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !isCloakingActive(player)) {
            return;
        }

        deactivate(player);
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)
                || !(event.getNewAboutToBeSetTarget() instanceof Player player)
                || !isCloakingActive(player)) {
            return;
        }

        event.setNewAboutToBeSetTarget(null);
    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)
                || player.level().isClientSide()
                || !isCloakingActive(player)
                || event.getAmount() <= 0.0F) {
            return;
        }

        deactivate(player);
    }

    @SubscribeEvent
    public static void onBreakBlock(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof Player player)
                || player.level().isClientSide()
                || !isCloakingActive(player)) {
            return;
        }

        deactivate(player);
    }

    public static void deactivate(Player player) {
        findExoskeleton(player).ifPresent(stack -> {
            ExoskeletonData data = ExoskeletonItem.getData(stack);

            for (InstalledModuleReference reference : activeCloakingModules(data)) {
                data = CloakingOperations.deactivate(data, reference);
            }

            stack.set(
                    ModDataComponents.EXOSKELETON_DATA.get(),
                    data
            );
        });

        markInactive(player);
    }

    private static List<InstalledModuleReference> activeCloakingModules(
            ExoskeletonData data
    ) {
        List<InstalledModuleReference> references = new ArrayList<>();

        for (int slot = 0; slot < data.matrices().size(); slot++) {
            var matrix = data.matrices().get(slot).matrix().orElse(null);
            if (matrix == null) {
                continue;
            }

            for (int index = 0; index < matrix.modules().size(); index++) {
                var module = matrix.modules().get(index);
                if (module.active()
                        && com.github.littleemptydoll.exoequipment.registry.ModModules
                        .getDefinition(module.id()).cloaking().isPresent()) {
                    references.add(new InstalledModuleReference(slot, index));
                }
            }
        }

        return references;
    }

    public static void markActive(Player player) {
        player.getPersistentData().putBoolean(CLOAKING_MARKER, true);
        player.setInvisible(true);

        for (Mob mob : player.level().getEntitiesOfClass(
                Mob.class,
                player.getBoundingBox().inflate(32.0)
        )) {
            if (mob.getTarget() == player) {
                mob.setTarget(null);
            }
        }

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                player,
                new CloakingStatePayload(player.getId(), true)
        );
    }

    public static boolean isCloakingActive(Player player) {
        return player.getPersistentData().getBoolean(CLOAKING_MARKER);
    }

    public static void markInactive(Player player) {
        player.getPersistentData().remove(CLOAKING_MARKER);
        player.setInvisible(false);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                player,
                new CloakingStatePayload(player.getId(), false)
        );
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)
                || !(event.getTarget() instanceof Player target)
                || !isCloakingActive(target)) {
            return;
        }

        PacketDistributor.sendToPlayer(
                serverPlayer,
                new CloakingStatePayload(target.getId(), true)
        );
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !isCloakingActive(player)) {
            return;
        }

        Optional<ItemStack> exoskeleton = findExoskeleton(player);
        if (exoskeleton.isEmpty()) {
            markInactive(player);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.getOriginal().getPersistentData().getBoolean(CLOAKING_MARKER)) {
            event.getEntity().getPersistentData().remove(CLOAKING_MARKER);
            event.getEntity().setInvisible(false);
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
