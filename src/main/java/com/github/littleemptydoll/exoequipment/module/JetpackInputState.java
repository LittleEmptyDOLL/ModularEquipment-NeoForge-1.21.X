package com.github.littleemptydoll.exoequipment.module;

import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class JetpackInputState {
    public static final int FORWARD = 1;
    public static final int BACK = 1 << 1;
    public static final int LEFT = 1 << 2;
    public static final int RIGHT = 1 << 3;
    public static final int UP = 1 << 4;
    public static final int DOWN = 1 << 5;

    private static final Map<UUID, Byte> INPUTS = new ConcurrentHashMap<>();

    private JetpackInputState() {}

    public static void set(Player player, byte input) {
        INPUTS.put(player.getUUID(), input);
    }

    public static byte get(Player player) {
        return INPUTS.getOrDefault(player.getUUID(), (byte) 0);
    }

    public static boolean isThrusting(Player player) {
        return isEnergyActive(player);
    }

    public static boolean isEnergyActive(Player player) {
        byte input = get(player);
        if (player.isFallFlying()) {
            return has(input, UP);
        }
        return input != 0;
    }

    public static boolean has(byte input, int flag) {
        return (input & flag) != 0;
    }
}
