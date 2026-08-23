package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.github.littleemptydoll.exoequipment.controller.Controller;
import com.github.littleemptydoll.exoequipment.energy.EnergySystem;
import com.github.littleemptydoll.exoequipment.frame.Frame;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ExoskeletonData(
        Optional<Frame> frame,
        Optional<Controller> controller,
        Optional<EnergySystem> energySystem,
        List<MatrixSlot> matrices,
        List<ExoskeletonProfile> profiles,
        int activeProfile
) {
    public static final int MAX_MATRICES = 4;

    public ExoskeletonData {
        if (matrices.size() != MAX_MATRICES) {
            throw new IllegalArgumentException(
                    "Exoskeleton must contain exactly "
                            + MAX_MATRICES
                            + " matrix slots"
            );
        }
    }

    public static final Codec<ExoskeletonData> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Frame.CODEC
                                    .optionalFieldOf("frame")
                                    .forGetter(ExoskeletonData::frame),
                            Controller.CODEC
                                    .optionalFieldOf("controller")
                                    .forGetter(ExoskeletonData::controller),
                            EnergySystem.CODEC
                                    .optionalFieldOf("energy_system")
                                    .forGetter(ExoskeletonData::energySystem),
                            MatrixSlot.CODEC
                                    .listOf()
                                    .fieldOf("matrices")
                                    .forGetter(ExoskeletonData::matrices),
                            ExoskeletonProfile.CODEC
                                    .listOf()
                                    .fieldOf("profiles")
                                    .forGetter(ExoskeletonData::profiles),
                            Codec.INT
                                    .fieldOf("active_profile")
                                    .forGetter(ExoskeletonData::activeProfile)
                    ).apply(
                            instance,
                            ExoskeletonData::new
                    )
            );

    public static ExoskeletonData empty() {
        return new ExoskeletonData(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(
                        MatrixSlot.empty(),
                        MatrixSlot.empty(),
                        MatrixSlot.empty(),
                        MatrixSlot.empty()
                ),
                List.of(),
                -1
        );
    }

    public ExoskeletonData withFrame(Frame frame) {
        return new ExoskeletonData(
                Optional.of(frame),
                controller,
                energySystem,
                matrices,
                profiles,
                activeProfile
        );
    }

    public ExoskeletonData withoutFrame() {
        return new ExoskeletonData(
                Optional.empty(),
                controller,
                energySystem,
                matrices,
                profiles,
                activeProfile
        );
    }

    public ExoskeletonData withController(Controller controller) {
        return new ExoskeletonData(
                frame,
                Optional.of(controller),
                energySystem,
                matrices,
                profiles,
                activeProfile
        );
    }

    public ExoskeletonData withoutController() {
        return new ExoskeletonData(
                frame,
                Optional.empty(),
                energySystem,
                matrices,
                profiles,
                activeProfile
        );
    }

    public ExoskeletonData withEnergySystem(EnergySystem energySystem) {
        return new ExoskeletonData(
                frame,
                controller,
                Optional.of(energySystem),
                matrices,
                profiles,
                activeProfile
        );
    }

    public ExoskeletonData withoutEnergySystem() {
        return new ExoskeletonData(
                frame,
                controller,
                Optional.empty(),
                matrices,
                profiles,
                activeProfile
        );
    }

    public ExoskeletonData withMatrix(
            int slot,
            MatrixData matrix
    ) {
        List<MatrixSlot> newMatrices = new ArrayList<>(matrices);

        newMatrices.set(
                slot,
                new MatrixSlot(Optional.of(matrix))
        );

        return new ExoskeletonData(
                frame,
                controller,
                energySystem,
                List.copyOf(newMatrices),
                profiles,
                activeProfile
        );
    }

    public ExoskeletonData withoutMatrix(int slot) {
        List<MatrixSlot> newMatrices = new ArrayList<>(matrices);

        newMatrices.set(
                slot,
                MatrixSlot.empty()
        );

        return new ExoskeletonData(
                frame,
                controller,
                energySystem,
                List.copyOf(newMatrices),
                profiles,
                activeProfile
        );
    }

    public ExoskeletonData withProfiles(
            List<ExoskeletonProfile> profiles
    ) {
        return new ExoskeletonData(
                frame,
                controller,
                energySystem,
                matrices,
                List.copyOf(profiles),
                activeProfile
        );
    }

    public ExoskeletonData withProfiles(
            List<ExoskeletonProfile> profiles,
            int activeProfile
    ) {
        return new ExoskeletonData(
                frame,
                controller,
                energySystem,
                matrices,
                List.copyOf(profiles),
                activeProfile
        );
    }

    public ExoskeletonData withProfiles(
            int index,
            ExoskeletonProfile profile
    ) {
        List<ExoskeletonProfile> profiles = new ArrayList<>(this.profiles);

        profiles.set(index, profile);

        return new ExoskeletonData(
                frame,
                controller,
                energySystem,
                matrices,
                List.copyOf(profiles),
                activeProfile
        );
    }

    public ExoskeletonData withActiveProfile(
            int activeProfile
    ) {
        return new ExoskeletonData(
                frame,
                controller,
                energySystem,
                matrices,
                profiles,
                activeProfile
        );
    }
}
