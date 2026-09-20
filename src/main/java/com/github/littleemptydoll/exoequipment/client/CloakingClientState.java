package com.github.littleemptydoll.exoequipment.client;

import java.util.HashSet;
import java.util.Set;

public final class CloakingClientState {
    private static final Set<Integer> ACTIVE_ENTITIES = new HashSet<>();

    private CloakingClientState() {}

    public static void setActive(int entityId, boolean active) {
        if (active) {
            ACTIVE_ENTITIES.add(entityId);
        } else {
            ACTIVE_ENTITIES.remove(entityId);
        }
    }

    public static boolean isActive(int entityId) {
        return ACTIVE_ENTITIES.contains(entityId);
    }

    public static Set<Integer> activeEntities() {
        return Set.copyOf(ACTIVE_ENTITIES);
    }

    public static void clear() {
        ACTIVE_ENTITIES.clear();
    }
}
