package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.github.littleemptydoll.exoequipment.controller.Controller;
import com.github.littleemptydoll.exoequipment.energy.EnergySystem;
import com.github.littleemptydoll.exoequipment.frame.Frame;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;

public final class ExoskeletonOperations {
    private ExoskeletonOperations() {}

    public static ExoskeletonData installFrame(
            ExoskeletonData data,
            Frame frame
    ) {
        if (data.frame().isPresent()) {
            throw new IllegalStateException(
                    "Exoskeleton already has a frame installed"
            );
        }

        return data.withFrame(frame);
    }

    public static ExoskeletonData removeFrame(
            ExoskeletonData data
    ) {
        if (data.frame().isEmpty()) {
            throw new IllegalStateException(
                    "Exoskeleton does not have a frame installed"
            );
        }

        return data.withoutFrame();
    }

    public static ExoskeletonData installController(
            ExoskeletonData data,
            Controller controller
    ) {
        if (data.controller().isPresent()) {
            throw new IllegalStateException(
                    "Exoskeleton already has a controller installed"
            );
        }

        return data.withController(controller);
    }

    public static ExoskeletonData removeController(
            ExoskeletonData data
    ) {
        ExoskeletonValidation.validateController(data);

        return data.withoutController();
    }

    public static ExoskeletonData installEnergySystem(
            ExoskeletonData data,
            EnergySystem energySystem
    ) {
        if (data.energySystem().isPresent()) {
            throw new IllegalStateException(
                    "Exoskeleton already has a energy system installed"
            );
        }

        return data.withEnergySystem(energySystem);
    }

    public static ExoskeletonData removeEnergySystem(
            ExoskeletonData data
    ) {
        if (data.energySystem().isEmpty()) {
            throw new IllegalStateException(
                    "Exoskeleton does not have a energy system installed"
            );
        }

        return data.withoutEnergySystem();
    }

    public static ExoskeletonData installMatrix(
            ExoskeletonData data,
            int slot,
            MatrixData matrix
    ) {
        ExoskeletonValidation.validateMatrixSlot(slot);

        if (data.matrices().get(slot).matrix().isPresent()) {
            throw new IllegalStateException(
                    "Matrix slot " + slot + " is already occupied"
            );
        }

        return data.withMatrix(slot, matrix);
    }

    public static ExoskeletonData removeMatrix(
            ExoskeletonData data,
            int slot
    ) {
        ExoskeletonValidation.validateMatrixSlot(slot);

        if (data.matrices().get(slot).matrix().isEmpty()) {
            throw new IllegalStateException(
                    "Matrix slot " + slot + " is already empty"
            );
        }

        return data.withoutMatrix(slot);
    }
}
