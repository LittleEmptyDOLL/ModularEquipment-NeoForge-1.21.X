package com.github.littleemptydoll.exoequipment.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ActionIdTest {

    @Test
    void matrixActionIdsRemainStable() {
        assertEquals(
                MatrixActionPayload.Action.PLACE,
                MatrixActionPayload.Action.fromId(0)
        );
        assertEquals(
                MatrixActionPayload.Action.REMOVE,
                MatrixActionPayload.Action.fromId(1)
        );
        assertEquals(
                MatrixActionPayload.Action.ROTATE,
                MatrixActionPayload.Action.fromId(2)
        );
        assertEquals(
                MatrixActionPayload.Action.MOVE,
                MatrixActionPayload.Action.fromId(3)
        );
        assertNull(
                MatrixActionPayload.Action.fromId(99)
        );
    }

    @Test
    void profileActionIdsRemainStable() {
        assertEquals(
                ProfileActionPayload.Action.SELECT,
                ProfileActionPayload.Action.fromId(0)
        );
        assertEquals(
                ProfileActionPayload.Action.CREATE,
                ProfileActionPayload.Action.fromId(1)
        );
        assertEquals(
                ProfileActionPayload.Action.REMOVE,
                ProfileActionPayload.Action.fromId(2)
        );
        assertEquals(
                ProfileActionPayload.Action.TOGGLE_MATRIX,
                ProfileActionPayload.Action.fromId(3)
        );
        assertEquals(
                ProfileActionPayload.Action.RENAME,
                ProfileActionPayload.Action.fromId(4)
        );
        assertNull(
                ProfileActionPayload.Action.fromId(99)
        );
    }
}
