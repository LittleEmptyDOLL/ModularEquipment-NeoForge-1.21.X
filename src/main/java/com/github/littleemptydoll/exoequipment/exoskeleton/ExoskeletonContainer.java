package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.github.littleemptydoll.exoequipment.controller.Controller;
import com.github.littleemptydoll.exoequipment.energy.EnergySystem;
import com.github.littleemptydoll.exoequipment.frame.Frame;
import com.github.littleemptydoll.exoequipment.item.*;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.registry.*;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ExoskeletonContainer implements Container {

    public static final int FRAME_SLOT = 0;
    public static final int CONTROLLER_SLOT = 1;
    public static final int ENERGY_SYSTEM_SLOT = 2;

    public static final int MATRIX_START_SLOT = 3;
    public static final int MATRIX_COUNT = ExoskeletonData.MAX_MATRICES;

    public static final int SLOT_COUNT = MATRIX_START_SLOT + MATRIX_COUNT;

    private final ItemStack exoskeleton;

    public ExoskeletonContainer(ItemStack exoskeleton) {
        if (!(exoskeleton.getItem() instanceof ExoskeletonItem)) {
            throw new IllegalArgumentException(
                    "ItemStack is not an exoskeleton"
            );
        }

        this.exoskeleton = exoskeleton;
    }

    private ItemStack getExoskeleton() {
        return exoskeleton;
    }

    public ExoskeletonData getData() {
        return ExoskeletonItem
                .get(exoskeleton)
                .getData(exoskeleton);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (!getItem(slot).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return switch (slot) {
            case FRAME_SLOT -> getFrameStack();
            case CONTROLLER_SLOT -> getControllerStack();
            case ENERGY_SYSTEM_SLOT -> getEnergySystemStack();
            default -> getMatrixStack(slot);
        };
    }

    private ItemStack getFrameStack() {
        return getData()
                .frame()
                .map(frame ->
                        ModFrames.find(frame.definitionId())
                                .getItem()
                                .getDefaultInstance()
                )
                .orElse(ItemStack.EMPTY);
    }

    private ItemStack getControllerStack() {
        return getData()
                .controller()
                .map(controller ->
                        ModControllers.find(controller.definitionId())
                                .getItem()
                                .getDefaultInstance()
                )
                .orElse(ItemStack.EMPTY);
    }

    private ItemStack getEnergySystemStack() {
        return getData()
                .energySystem()
                .map(energySystem ->
                        ModEnergySystems.find(energySystem.definitionId())
                                .getItem()
                                .getDefaultInstance()
                )
                .orElse(ItemStack.EMPTY);
    }

    private ItemStack getMatrixStack(int slot) {
        int matrixSlot = slot - MATRIX_START_SLOT;

        if (matrixSlot < 0 || matrixSlot >= MATRIX_COUNT) {
            return ItemStack.EMPTY;
        }

        return getData()
                .matrices()
                .get(matrixSlot)
                .matrix()
                .map(matrix -> {
                    ItemStack stack =
                            ModMatrices.find(matrix.id())
                                    .getItem()
                                    .getDefaultInstance();
                    stack.set(
                            ModDataComponents.MATRIX_DATA.get(),
                            matrix
                    );

                    return stack;
                })
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack removeItem(
            int slot,
            int amount
    ) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(
            int slot,
            ItemStack stack
    ) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            throw new IndexOutOfBoundsException(
                    "Invalid exoskeleton slot: " + slot
            );
        }

        ExoskeletonData data = getData();

        ExoskeletonData newData = switch (slot) {
            case FRAME_SLOT -> setFrame(data, stack);

            case CONTROLLER_SLOT -> setController(data, stack);

            case ENERGY_SYSTEM_SLOT -> setEnergySystem(data, stack);

            default -> setMatrix(
                    data,
                    slot - MATRIX_START_SLOT,
                    stack
            );
        };

        exoskeleton.set(
                ModDataComponents.EXOSKELETON_DATA.get(),
                newData
        );

        setChanged();
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
    }

    private ExoskeletonData setFrame(
            ExoskeletonData data,
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            return ExoskeletonOperations.removeFrame(data);
        }

        FrameItem item = FrameItem.get(stack);

        return ExoskeletonOperations.installFrame(
                data,
                new Frame(
                        item.getDefinition().id()
                )
        );
    }

    private ExoskeletonData setController(
            ExoskeletonData data,
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            return ExoskeletonOperations.removeController(data);
        }

        ControllerItem item = ControllerItem.get(stack);

        return ExoskeletonOperations.installController(
                data,
                new Controller(
                        item.getDefinition().id()
                )
        );
    }

    private ExoskeletonData setEnergySystem(
            ExoskeletonData data,
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            return ExoskeletonOperations.removeEnergySystem(data);
        }

        EnergySystemItem item = EnergySystemItem.get(stack);

        return ExoskeletonOperations.installEnergySystem(
                data,
                new EnergySystem(
                        item.getDefinition().id()
                )
        );
    }

    private ExoskeletonData setMatrix(
            ExoskeletonData data,
            int matrixSlot,
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            return ExoskeletonOperations.removeMatrix(
                    data,
                    matrixSlot
            );
        }

        MatrixItem item = MatrixItem.get(stack);

        return ExoskeletonOperations.installMatrix(
                data,
                matrixSlot,
                item.getMatrixData(stack)
        );
    }
}
