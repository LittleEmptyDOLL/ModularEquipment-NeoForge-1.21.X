package com.github.littleemptydoll.exoequipment.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Short-lived handoff between the clientbound shield-hit packet and the player
 * render layer. Entries are removed as soon as the matching player is rendered.
 */
public final class ShieldHitClientState {
    private static final Map<Integer, List<Hit>> PENDING = new HashMap<>();

    private ShieldHitClientState() {}

    public static void add(
            int entityId,
            int seed,
            int partIndex,
            float directionX,
            float directionZ,
            float energyLoss
    ) {
        PENDING.computeIfAbsent(entityId, ignored -> new ArrayList<>())
                .add(new Hit(
                        seed,
                        partIndex,
                        directionX,
                        directionZ,
                        energyLoss
                ));
    }

    public static List<Hit> drain(int entityId) {
        List<Hit> hits = PENDING.remove(entityId);
        return hits == null ? List.of() : List.copyOf(hits);
    }

    public record Hit(
            int seed,
            int partIndex,
            float directionX,
            float directionZ,
            float energyLoss
    ) {}
}
