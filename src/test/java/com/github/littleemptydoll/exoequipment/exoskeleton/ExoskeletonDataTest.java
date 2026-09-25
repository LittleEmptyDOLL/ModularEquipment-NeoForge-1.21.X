package com.github.littleemptydoll.exoequipment.exoskeleton;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExoskeletonDataTest {

    @Test
    void copiesStateLists() {
        List<MatrixSlot> matrices =
                new ArrayList<>(
                        List.of(
                                MatrixSlot.empty(),
                                MatrixSlot.empty(),
                                MatrixSlot.empty(),
                                MatrixSlot.empty()
                        )
                );

        List<ExoskeletonProfile> profiles =
                new ArrayList<>(
                        List.of(
                                new ExoskeletonProfile(
                                        "Profile",
                                        List.of()
                                )
                        )
                );

        ExoskeletonData data =
                new ExoskeletonData(
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        matrices,
                        profiles,
                        0,
                        20.0D
                );

        matrices.clear();
        profiles.clear();

        assertEquals(
                ExoskeletonData.MAX_MATRICES,
                data.matrices().size()
        );
        assertEquals(
                1,
                data.profiles().size()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> data.profiles().clear()
        );
    }

    @Test
    void requiresExactlyFourMatrixSlots() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExoskeletonData(
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        List.of(),
                        List.of(),
                        -1,
                        20.0D
                )
        );
    }
}
