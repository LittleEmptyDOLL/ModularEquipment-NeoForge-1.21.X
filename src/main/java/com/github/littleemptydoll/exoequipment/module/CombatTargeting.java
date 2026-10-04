package com.github.littleemptydoll.exoequipment.module;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

final class CombatTargeting {
    private static final FtbTeamsAccess FTB_TEAMS = FtbTeamsAccess.create();
    private static final ResourceLocation TARGET_DUMMY =
            ResourceLocation.fromNamespaceAndPath("dummmmmmy", "target_dummy");

    private CombatTargeting() {}

    static boolean eligible(ServerPlayer owner, LivingEntity target,
                            boolean players, boolean hostile, boolean aggressive) {
        if (target == owner || !target.isAlive() || target.isSpectator()
                || owner.isAlliedTo(target)) return false;
        if (trainingDummy(target)) return true;
        if (target instanceof Player other) {
            return players && other instanceof ServerPlayer serverPlayer
                    && owner.canHarmPlayer(other) && !FTB_TEAMS.allied(owner, serverPlayer);
        }
        return (hostile && target instanceof Enemy)
                || (aggressive && target instanceof Mob mob && mob.getTarget() == owner);
    }

    static boolean trainingDummy(LivingEntity target) {
        return TARGET_DUMMY.equals(BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()));
    }

    /** Access the optional 1.21.1 FTB Teams API without requiring it in standalone packs. */
    private record FtbTeamsAccess(boolean installed, Method api, Method isManagerLoaded, Method getManager,
                                  Method sameTeam, Method teamForPlayer, Method getRank,
                                  Method isAllyOrBetter) {
        static FtbTeamsAccess create() {
            if (ModList.get() == null || !ModList.get().isLoaded("ftbteams")) return nullAccess();
            try {
                Class<?> apiType = Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI");
                Class<?> apiInterface = Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI$API");
                Class<?> managerType = Class.forName("dev.ftb.mods.ftbteams.api.TeamManager");
                Class<?> teamType = Class.forName("dev.ftb.mods.ftbteams.api.Team");
                Class<?> rankType = Class.forName("dev.ftb.mods.ftbteams.api.TeamRank");
                return new FtbTeamsAccess(true, apiType.getMethod("api"),
                        apiInterface.getMethod("isManagerLoaded"), apiInterface.getMethod("getManager"),
                        managerType.getMethod("arePlayersInSameTeam", UUID.class, UUID.class),
                        managerType.getMethod("getTeamForPlayerID", UUID.class),
                        teamType.getMethod("getRankForPlayer", UUID.class),
                        rankType.getMethod("isAllyOrBetter"));
            } catch (ReflectiveOperationException | LinkageError ignored) {
                return new FtbTeamsAccess(true, null, null, null, null, null, null, null);
            }
        }

        private static FtbTeamsAccess nullAccess() {
            return new FtbTeamsAccess(false, null, null, null, null, null, null, null);
        }

        boolean allied(ServerPlayer owner, ServerPlayer target) {
            // Fail closed when the optional mod is present but its API is unavailable.
            if (api == null) return installed;
            try {
                Object instance = api.invoke(null);
                if (!(boolean) isManagerLoaded.invoke(instance)) return true;
                Object manager = getManager.invoke(instance);
                UUID ownerId = owner.getUUID();
                UUID targetId = target.getUUID();
                if ((boolean) sameTeam.invoke(manager, ownerId, targetId)) return true;
                Optional<?> ownerTeam = (Optional<?>) teamForPlayer.invoke(manager, ownerId);
                Optional<?> targetTeam = (Optional<?>) teamForPlayer.invoke(manager, targetId);
                if (ownerTeam.isEmpty() || targetTeam.isEmpty()) return true;
                return (boolean) isAllyOrBetter.invoke(getRank.invoke(ownerTeam.get(), targetId))
                        || (boolean) isAllyOrBetter.invoke(getRank.invoke(targetTeam.get(), ownerId));
            } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
                return true;
            }
        }
    }
}
