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

import java.util.Optional;

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
        checkSlot(slot);

        return switch (slot) {
            case FRAME_SLOT -> getFrameStack();
            case CONTROLLER_SLOT -> getControllerStack();
            case ENERGY_SYSTEM_SLOT -> getEnergySystemStack();
            default -> getMatrixStack(slot - MATRIX_START_SLOT);
        };
    }

    private ItemStack getFrameStack() {
        Optional<Frame> frame = getData().frame();

        if (frame.isEmpty()) {
            return ItemStack.EMPTY;
        }

        var entry = ModFrames.find(frame.get().definitionId());

        if (entry == null) {
            return ItemStack.EMPTY;
        }

        return entry.getItem().getDefaultInstance();
    }

    private ItemStack getControllerStack() {
        Optional<Controller> controller = getData().controller();

        if (controller.isEmpty()) {
            return ItemStack.EMPTY;
        }

        var entry = ModControllers.find(controller.get().definitionId());

        if (entry == null) {
            return ItemStack.EMPTY;
        }

        return entry.getItem().getDefaultInstance();
    }

    private ItemStack getEnergySystemStack() {
        Optional<EnergySystem> energySystem = getData().energySystem();

        if (energySystem.isEmpty()) {
            return ItemStack.EMPTY;
        }

        var entry = ModEnergySystems.find(energySystem.get().definitionId());

        if (entry == null) {
            return ItemStack.EMPTY;
        }

        return entry.getItem().getDefaultInstance();
    }

    private ItemStack getMatrixStack(int slot) {
        MatrixData matrix = null;
        if (getData().matrices().get(slot).matrix().isPresent()) {
            matrix = getData().matrices().get(slot).matrix().get();
        }

        if (matrix == null) {
            return ItemStack.EMPTY;
        }

        var entry = ModMatrices.find(matrix.id());

        if (entry == null) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = entry.getItem().getDefaultInstance();

        stack.set(
                ModDataComponents.MATRIX_DATA.get(),
                matrix
        );

        return stack;
    }

    @Override
    public ItemStack removeItem(
            int slot,
            int amount
    ) {
        checkSlot(slot);

        ItemStack current = getItem(slot);

        if (current.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }

        setItem(slot, ItemStack.EMPTY);

        return current;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        checkSlot(slot);

        ItemStack current = getItem(slot);

        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ExoskeletonData data = getData();

        ExoskeletonData newData = switch (slot) {
            case FRAME_SLOT -> ExoskeletonOperations.removeFrame(data);
            case CONTROLLER_SLOT -> ExoskeletonOperations.removeController(data);
            case ENERGY_SYSTEM_SLOT -> ExoskeletonOperations.removeEnergySystem(data);
            default -> ExoskeletonOperations.removeMatrix(
                    data,
                    slot - MATRIX_START_SLOT
            );
        };

        exoskeleton.set(
                ModDataComponents.EXOSKELETON_DATA.get(),
                newData
        );

        setChanged();

        return current;
    }

    @Override
    public void setItem(
            int slot,
            ItemStack stack
    ) {
        checkSlot(slot);

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
        return player.isAlive();
    }

    @Override
    public void clearContent() {
        ExoskeletonData data = getData();

        if (data.frame().isPresent()) {
            data = ExoskeletonOperations.removeFrame(data);
        }

        if (data.controller().isPresent()) {
            data = ExoskeletonOperations.removeController(data);
        }

        if (data.energySystem().isPresent()) {
            data = ExoskeletonOperations.removeEnergySystem(data);
        }

        for (int i = 0; i < MATRIX_COUNT; i++) {
            if (data.matrices().get(i).matrix().isPresent()) {
                data = ExoskeletonOperations.removeMatrix(data, i);
            }
        }

        exoskeleton.set(
                ModDataComponents.EXOSKELETON_DATA.get(),
                data
        );

        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        checkSlot(slot);

        return switch (slot) {
            case FRAME_SLOT -> stack.getItem() instanceof FrameItem;
            case CONTROLLER_SLOT -> stack.getItem() instanceof ControllerItem;
            case ENERGY_SYSTEM_SLOT -> stack.getItem() instanceof EnergySystemItem;
            default -> stack.getItem() instanceof MatrixItem;
        };
    }

    private void checkSlot(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            throw new IndexOutOfBoundsException(
                    "Invalid exoskeleton slot: " + slot
            );
        }
    }

    private ExoskeletonData setFrame(
            ExoskeletonData data,
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            if (data.frame().isEmpty()) {
                return data;
            }

            return ExoskeletonOperations.removeFrame(data);
        }

        FrameItem item = FrameItem.get(stack);

        if (data.frame().isPresent()) {
            data = ExoskeletonOperations.removeFrame(data);
        }

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
            if (data.controller().isEmpty()) {
                return data;
            }

            return ExoskeletonOperations.removeController(data);
        }

        ControllerItem item = ControllerItem.get(stack);

        if (data.controller().isPresent()) {
            data = ExoskeletonOperations.removeController(data);
        }

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
            if (data.energySystem().isEmpty()) {
                return data;
            }

            return ExoskeletonOperations.removeEnergySystem(data);
        }

        EnergySystemItem item = EnergySystemItem.get(stack);

        if (data.energySystem().isPresent()) {
            data = ExoskeletonOperations.removeEnergySystem(data);
        }

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
        MatrixSlot current = data.matrices().get(matrixSlot);

        if (stack.isEmpty()) {
            if (current.matrix().isEmpty()) {
                return data;
            }

            return ExoskeletonOperations.removeMatrix(
                    data,
                    matrixSlot
            );
        }

        MatrixItem item = MatrixItem.get(stack);

        if (current.matrix().isPresent()) {
            data = ExoskeletonOperations.removeMatrix(
                    data,
                    matrixSlot
            );
        }

        return ExoskeletonOperations.installMatrix(
                data,
                matrixSlot,
                item.getMatrixData(stack)
        );
    }
}
