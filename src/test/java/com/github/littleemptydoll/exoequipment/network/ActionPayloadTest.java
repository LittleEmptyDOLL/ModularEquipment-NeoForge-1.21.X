package com.github.littleemptydoll.exoequipment.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ActionPayloadTest {
    @Test
    void matrixActionIdsStayStable() {
        assertEquals(0, MatrixActionPayload.Action.PLACE.id());
        assertEquals(1, MatrixActionPayload.Action.REMOVE.id());
        assertEquals(2, MatrixActionPayload.Action.ROTATE.id());
        assertEquals(3, MatrixActionPayload.Action.MOVE.id());

        for (MatrixActionPayload.Action action
                : MatrixActionPayload.Action.values()) {
            assertEquals(
                    action,
                    MatrixActionPayload.Action.fromId(action.id())
            );
        }

        assertNull(MatrixActionPayload.Action.fromId(-1));
        assertNull(MatrixActionPayload.Action.fromId(4));
    }

    @Test
    void profileActionIdsStayStable() {
        assertEquals(0, ProfileActionPayload.Action.SELECT.id());
        assertEquals(1, ProfileActionPayload.Action.CREATE.id());
        assertEquals(2, ProfileActionPayload.Action.REMOVE.id());
        assertEquals(3, ProfileActionPayload.Action.TOGGLE_MATRIX.id());
        assertEquals(4, ProfileActionPayload.Action.RENAME.id());

        for (ProfileActionPayload.Action action
                : ProfileActionPayload.Action.values()) {
            assertEquals(
                    action,
                    ProfileActionPayload.Action.fromId(action.id())
            );
        }

        assertNull(ProfileActionPayload.Action.fromId(-1));
        assertNull(ProfileActionPayload.Action.fromId(5));
    }
}
