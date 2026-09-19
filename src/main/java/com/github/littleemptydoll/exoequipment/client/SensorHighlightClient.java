package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.network.SensorHighlightPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.util.HashMap;
import java.util.Map;

public final class SensorHighlightClient {
    private static final String HOSTILE_TEAM = "exoequipment_sensor_hostile";
    private static final String MOBS_TEAM = "exoequipment_sensor_mobs";
    private static final String PLAYERS_TEAM = "exoequipment_sensor_players";

    private static final Map<Integer, String> HIGHLIGHTED = new HashMap<>();

    private SensorHighlightClient() {}

    public static void apply(SensorHighlightPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return;
        }

        Scoreboard scoreboard = minecraft.level.getScoreboard();

        clearHighlights(scoreboard);

        applyTeam(
                scoreboard,
                payload.hostile(),
                HOSTILE_TEAM,
                ChatFormatting.RED
        );

        applyTeam(
                scoreboard,
                payload.mobs(),
                MOBS_TEAM,
                ChatFormatting.GREEN
        );

        applyTeam(
                scoreboard,
                payload.players(),
                PLAYERS_TEAM,
                ChatFormatting.BLUE
        );
    }

    private static void applyTeam(
            Scoreboard scoreboard,
            Iterable<Integer> entityIds,
            String teamName,
            ChatFormatting color
    ) {
        PlayerTeam team = getOrCreateTeam(scoreboard, teamName, color);

        for (int entityId : entityIds) {
            if (Minecraft.getInstance().level == null) {
                return;
            }

            var entity = Minecraft.getInstance().level.getEntity(entityId);

            if (entity == null) {
                continue;
            }

            String scoreboardName = entity.getScoreboardName();

            scoreboard.addPlayerToTeam(scoreboardName, team);
            HIGHLIGHTED.put(entityId, scoreboardName);
        }
    }

    private static void clearHighlights(Scoreboard scoreboard) {
        if (Minecraft.getInstance().level == null) {
            HIGHLIGHTED.clear();
            return;
        }

        for (Map.Entry<Integer, String> entry : HIGHLIGHTED.entrySet()) {
            PlayerTeam team = findSensorTeam(scoreboard, entry.getValue());

            if (team != null) {
                scoreboard.removePlayerFromTeam(entry.getValue(), team);
            }
        }

        HIGHLIGHTED.clear();
    }

    private static PlayerTeam findSensorTeam(
            Scoreboard scoreboard,
            String scoreboardName
    ) {
        for (String teamName : new String[]{
                HOSTILE_TEAM,
                MOBS_TEAM,
                PLAYERS_TEAM
        }) {
            PlayerTeam team = scoreboard.getPlayerTeam(teamName);

            if (team != null && team.getPlayers().contains(scoreboardName)) {
                return team;
            }
        }

        return null;
    }

    private static PlayerTeam getOrCreateTeam(
            Scoreboard scoreboard,
            String name,
            ChatFormatting color
    ) {
        PlayerTeam team = scoreboard.getPlayerTeam(name);

        if (team == null) {
            team = scoreboard.addPlayerTeam(name);
        }

        team.setColor(color);
        return team;
    }
}
