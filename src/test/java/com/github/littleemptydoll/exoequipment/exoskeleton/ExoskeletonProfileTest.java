package com.github.littleemptydoll.exoequipment.exoskeleton;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExoskeletonProfileTest {

    @Test
    void copiesActiveMatrices() {
        List<Integer> matrices =
                new ArrayList<>(List.of(0, 2));

        ExoskeletonProfile profile =
                new ExoskeletonProfile(
                        "Combat",
                        matrices
                );

        matrices.clear();

        assertEquals(
                List.of(0, 2),
                profile.activeMatrices()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> profile.activeMatrices().add(1)
        );
    }

    @Test
    void rejectsBlankName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExoskeletonProfile(
                        " ",
                        List.of()
                )
        );
    }

    @Test
    void rejectsTooLongName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExoskeletonProfile(
                        "x".repeat(33),
                        List.of()
                )
        );
    }

    @Test
    void withNameKeepsMatrices() {
        ExoskeletonProfile original =
                new ExoskeletonProfile(
                        "Profile",
                        List.of(1, 3)
                );

        ExoskeletonProfile renamed =
                original.withName("Exploration");

        assertEquals(
                "Exploration",
                renamed.name()
        );
        assertEquals(
                original.activeMatrices(),
                renamed.activeMatrices()
        );
    }
}
