package com.github.littleemptydoll.exoequipment.command;

import com.github.littleemptydoll.exoequipment.controller.Controller;
import com.github.littleemptydoll.exoequipment.controller.ControllerDefinition;
import com.github.littleemptydoll.exoequipment.energy.EnergySystem;
import com.github.littleemptydoll.exoequipment.energy.EnergySystemDefinition;
import com.github.littleemptydoll.exoequipment.exoskeleton.*;
import com.github.littleemptydoll.exoequipment.frame.Frame;
import com.github.littleemptydoll.exoequipment.frame.FrameDefinition;
import com.github.littleemptydoll.exoequipment.item.*;
import com.github.littleemptydoll.exoequipment.matrix.MatrixState;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.registry.*;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;

public final class ModCommands {
    private ModCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("exo_matrix")
                        .then(
                                Commands.literal("add")
                                        .then(
                                                Commands.argument(
                                                        "x",
                                                        IntegerArgumentType.integer()
                                                ).then(
                                                        Commands.argument(
                                                                "y",
                                                                IntegerArgumentType.integer()
                                                        )
                                                        .executes(context ->
                                                                addModule(
                                                                        context.getSource()
                                                                                .getPlayerOrException()
                                                                                .getMainHandItem(),
                                                                        IntegerArgumentType.getInteger(
                                                                                context,
                                                                                "x"
                                                                        ),
                                                                        IntegerArgumentType.getInteger(
                                                                                context,
                                                                                "y"
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                        )
                        .then(
                                Commands.literal("remove")
                                        .then(
                                                Commands.argument(
                                                        "x",
                                                        IntegerArgumentType.integer()
                                                ).then(
                                                        Commands.argument(
                                                                "y",
                                                                IntegerArgumentType.integer()
                                                        )
                                                        .executes(context ->
                                                                removeModule(
                                                                        context.getSource()
                                                                                .getPlayerOrException()
                                                                                .getMainHandItem(),
                                                                        IntegerArgumentType.getInteger(
                                                                                context,
                                                                                "x"
                                                                        ),
                                                                        IntegerArgumentType.getInteger(
                                                                                context,
                                                                                "y"
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                        )
                        .then(
                                Commands.literal("rotate")
                                        .then(
                                                Commands.argument(
                                                        "x",
                                                        IntegerArgumentType.integer()
                                                ).then(
                                                        Commands.argument(
                                                                "y",
                                                                IntegerArgumentType.integer()
                                                        )
                                                        .executes(context ->
                                                                rotateModule(
                                                                        context.getSource()
                                                                                .getPlayerOrException()
                                                                                .getMainHandItem(),
                                                                        IntegerArgumentType.getInteger(
                                                                                context,
                                                                                "x"
                                                                        ),
                                                                        IntegerArgumentType.getInteger(
                                                                                context,
                                                                                "y"
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                        )
                        .then(
                                Commands.literal("move")
                                        .then(
                                                Commands.argument(
                                                        "fromX",
                                                        IntegerArgumentType.integer()
                                                ).then(
                                                        Commands.argument(
                                                                "fromY",
                                                                IntegerArgumentType.integer()
                                                        ).then(
                                                                Commands.argument(
                                                                        "toX",
                                                                        IntegerArgumentType.integer()
                                                                ).then(
                                                                        Commands.argument(
                                                                                "toY",
                                                                                IntegerArgumentType.integer()
                                                                        )
                                                                        .executes(context ->
                                                                                moveModule(
                                                                                        context.getSource()
                                                                                                .getPlayerOrException()
                                                                                                .getMainHandItem(),
                                                                                        IntegerArgumentType.getInteger(
                                                                                                context,
                                                                                                "fromX"
                                                                                        ),
                                                                                        IntegerArgumentType.getInteger(
                                                                                                context,
                                                                                                "fromY"
                                                                                        ),
                                                                                        IntegerArgumentType.getInteger(
                                                                                                context,
                                                                                                "toX"
                                                                                        ),
                                                                                        IntegerArgumentType.getInteger(
                                                                                                context,
                                                                                                "toY"
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                        )
                        .then(
                                Commands.literal("stats")
                                        .executes(context ->
                                                showMatrixStats(context.getSource())
                                        )
                        )
        );

        event.getDispatcher().register(
                Commands.literal("exo_energy_system")
                        .then(
                                Commands.literal("stats")
                                        .executes(context ->
                                                showEnergySystemStats(context.getSource()))
                        )
        );

        event.getDispatcher().register(
                Commands.literal("exo_exoskeleton")
                        .then(
                                Commands.literal("stats")
                                        .executes(context ->
                                                showExoskeletonStats(context.getSource()))
                        )
                        .then(
                                Commands.literal("install_frame")
                                        .executes(context ->
                                                addFrame(
                                                        context.getSource()
                                                                .getPlayerOrException()
                                                                .getMainHandItem()
                                                )
                                        )
                        )
                        .then(
                                Commands.literal("remove_frame")
                                        .executes(context ->
                                                removeFrame(
                                                        context.getSource()
                                                                .getPlayerOrException()
                                                                .getMainHandItem()
                                                )
                                        )
                        )
                        .then(
                                Commands.literal("install_controller")
                                        .executes(context ->
                                                addController(
                                                        context.getSource()
                                                                .getPlayerOrException()
                                                                .getMainHandItem()
                                                )
                                        )
                        )
                        .then(
                                Commands.literal("remove_controller")
                                        .executes(context ->
                                                removeController(
                                                        context.getSource()
                                                                .getPlayerOrException()
                                                                .getMainHandItem()
                                                )
                                        )
                        )
                        .then(
                                Commands.literal("install_energy_system")
                                        .executes(context ->
                                                addEnergySystem(
                                                        context.getSource()
                                                                .getPlayerOrException()
                                                                .getMainHandItem()
                                                )
                                        )
                        )
                        .then(
                                Commands.literal("remove_energy_system")
                                        .executes(context ->
                                                removeEnergySystem(
                                                        context.getSource()
                                                                .getPlayerOrException()
                                                                .getMainHandItem()
                                                )
                                        )
                        )
                        .then(
                                Commands.literal("install_matrix")
                                        .then(
                                                Commands.argument(
                                                        "slot",
                                                        IntegerArgumentType.integer()
                                                ).executes(context ->
                                                        addMatrix(
                                                                context.getSource()
                                                                        .getPlayerOrException(),
                                                                IntegerArgumentType.getInteger(
                                                                        context,
                                                                        "slot"
                                                                )

                                                        )
                                                )
                                        )
                        )
                        .then(
                                Commands.literal("remove_matrix")
                                        .then(
                                                Commands.argument(
                                                        "slot",
                                                        IntegerArgumentType.integer()
                                                )
                                                .executes(context ->
                                                        removeMatrix(
                                                                context.getSource()
                                                                        .getPlayerOrException()
                                                                        .getMainHandItem(),
                                                                IntegerArgumentType.getInteger(
                                                                        context,
                                                                        "slot"
                                                                )
                                                        )
                                                )
                                        )
                        )
                        .then(
                                Commands.literal("state")
                                        .executes(context ->
                                                exoskeletonState(
                                                        context.getSource()
                                                )
                                        )
                        )
        );

        event.getDispatcher().register(
                Commands.literal("exo_controller")
                        .then(
                                Commands.literal("stats")
                                        .executes(context ->
                                                showControllerStats(context.getSource()))
                        )
        );

        event.getDispatcher().register(
                Commands.literal("exo_frame")
                        .then(
                                Commands.literal("stats")
                                        .executes(context ->
                                                showFrameStats(context.getSource()))
                        )
        );
    }

    private static int addModule(
            ItemStack stack,
            int x,
            int y
    ) {
        if (!(stack.getItem() instanceof MatrixItem matrixItem)) {
            return 0;
        }

        MatrixData matrix = matrixItem.getMatrixData(stack);

        MatrixData updated = MatrixOperations.addModule(
                matrix,
                matrixItem.getDefinition(),
                new InstalledModule(
                        ModModules.TEST_MODULE.getId(),
                        x,
                        y,
                        0
                )
        );

        stack.set(
                ModDataComponents.MATRIX_DATA.get(),
                updated
        );

        return 1;
    }

    private static int removeModule(
            ItemStack stack,
            int x,
            int y
    ) {
        MatrixData matrix = stack.get(
                ModDataComponents.MATRIX_DATA.get()
        );

        if (matrix == null) {
            return 0;
        }

        MatrixData updated = MatrixOperations.removeModule(
                matrix,
                x,
                y
        );

        stack.set(
                ModDataComponents.MATRIX_DATA.get(),
                updated
        );

        return 1;
    }

    private static int rotateModule(
            ItemStack stack,
            int x,
            int y
    ) {
        if (!(stack.getItem() instanceof MatrixItem matrixItem)) {
            return 0;
        }

        MatrixData matrix = matrixItem.getMatrixData(stack);

        if (matrix == null) {
            return 0;
        }

        MatrixData updated = MatrixOperations.rotateModule(
                matrix,
                matrixItem.getDefinition(),
                x,
                y
        );

        stack.set(
                ModDataComponents.MATRIX_DATA.get(),
                updated
        );

        return 1;
    }

    private static int moveModule(
            ItemStack stack,
            int fromX,
            int fromY,
            int toX,
            int toY
    ) {
        if (!(stack.getItem() instanceof MatrixItem matrixItem)) {
            return 0;
        }

        MatrixData matrix = matrixItem.getMatrixData(stack);

        if (matrix == null) {
            return 0;
        }

        MatrixData updated = MatrixOperations.moveModule(
                matrix,
                matrixItem.getDefinition(),
                fromX,
                fromY,
                toX,
                toY
        );

        stack.set(
                ModDataComponents.MATRIX_DATA.get(),
                updated
        );

        return 1;
    }

    private static int showMatrixStats(
            CommandSourceStack source
    ) {
        ServerPlayer player = source.getPlayer();

        if (player == null) {
            source.sendFailure(
                    Component.literal(
                            "This command can only be used by a player."
                    )
            );

            return 0;
        }

        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof MatrixItem matrixItem)) {
            source.sendFailure(
                    Component.literal(
                            "You must hold a matrix in your main hand."
                    )
            );

            return 0;
        }

        MatrixData data = matrixItem.getMatrixData(stack);
        MatrixState state = MatrixOperations.calculateState(data);

        source.sendSuccess(
                () -> Component.literal(
                        "=== Matrix Stats ==="
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Energy consumption: " + state.energyConsumption() + " FE/t"
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Heat generation: " + state.heatGeneration()
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Cooling: " + state.cooling()
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Thermal balance: " + state.thermalBalance()
                ),
                false
        );

        return 1;
    }

    private static int showEnergySystemStats(
            CommandSourceStack source
    ) {
        ServerPlayer player = source.getPlayer();

        if (player == null) {
            source.sendFailure(
                    Component.literal(
                            "This command can only be used by a player."
                    )
            );

            return 0;
        }

        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof EnergySystemItem energySystemItem)) {
            source.sendFailure(
                    Component.literal(
                            "You must hold an energy system in your main hand."
                    )
            );

            return 0;
        }

        EnergySystemDefinition definition = energySystemItem.getDefinition(stack);

        source.sendSuccess(
                () -> Component.literal(
                        "=== Energy System Stats ==="
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "ID: " + definition.id()
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Tier: " + definition.tier()
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Max energy input: " + definition.maxInput() + " FE/t"
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Max energy output: " + definition.maxInput() + " FE/t"
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Efficiency: " + definition.efficiency()
                ),
                false
        );

        return 1;
    }

    private static int showControllerStats(
            CommandSourceStack source
    ) {
        ServerPlayer player = source.getPlayer();

        if (player == null) {
            source.sendFailure(
                    Component.literal(
                            "This command can only be used by a player."
                    )
            );

            return 0;
        }

        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof ControllerItem controllerItem)) {
            source.sendFailure(
                    Component.literal(
                            "You must hold an controller in your main hand."
                    )
            );

            return 0;
        }

        ControllerDefinition definition = controllerItem.getDefinition(stack);

        source.sendSuccess(
                () -> Component.literal(
                        "=== Controller Stats ==="
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "ID: " + definition.id()
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Tier: " + definition.tier()
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Max profiles: " + definition.maxProfiles()
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Max active matrices: " + definition.maxActiveMatrices()
                ),
                false
        );

        return 1;
    }

    private static int showExoskeletonStats(
            CommandSourceStack source
    ) {
        ServerPlayer player = source.getPlayer();

        if (player == null) {
            source.sendFailure(
                    Component.literal(
                            "This command can only be used by a player."
                    )
            );

            return 0;
        }

        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof ExoskeletonItem exoskeletonItem)) {
            source.sendFailure(
                    Component.literal(
                            "You must hold an exoskeleton in your main hand."
                    )
            );

            return 0;
        }

        ExoskeletonDefinition definition = exoskeletonItem.getDefinition();
        ExoskeletonData data = exoskeletonItem.getData(stack);

        source.sendSuccess(
                () -> Component.literal(
                        "=== Exoskeleton Stats ==="
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "ID: " + definition.id()
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Frame: " + (data.frame().isPresent() ? data.frame() : "None")
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Controller: " + (data.controller().isPresent() ? data.frame() : "None")
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Energy system: " + (data.energySystem().isPresent() ? data.frame() : "None")
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Matrices:"
                ),
                false
        );

        List<Integer> activeSlots = ExoskeletonState.activeMatrixSlots(data);

        for (int slot : activeSlots) {
            MatrixSlot matrixSlot = data.matrices().get(slot);

            if (matrixSlot.matrix().isPresent()) {
                source.sendSuccess(
                        () -> Component.literal(
                                "Slot " + slot + ": installed"
                        ),
                        false
                );
            } else {
                source.sendSuccess(
                        () -> Component.literal(
                                "Slot " + slot + ": empty"
                        ),
                        false
                );
            }
        }

        List<MatrixData> activeMatrices = ExoskeletonState.activeMatrices(data);

        source.sendSuccess(
                () -> Component.literal(
                        "Profiles: " + activeMatrices
                ),
                false
        );

        return 1;
    }

    private static int showFrameStats(
            CommandSourceStack source
    ) {
        ServerPlayer player = source.getPlayer();

        if (player == null) {
            source.sendFailure(
                    Component.literal(
                            "This command can only be used by a player."
                    )
            );

            return 0;
        }

        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof FrameItem frameItem)) {
            source.sendFailure(
                    Component.literal(
                            "You must hold an energy system in your main hand."
                    )
            );

            return 0;
        }

        FrameDefinition definition = frameItem.getDefinition(stack);

        source.sendSuccess(
                () -> Component.literal(
                        "=== Frame Stats ==="
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "ID: " + definition.id()
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Tier: " + definition.tier()
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Max module size: "
                                + definition.maxModuleSize().width() + "x" + definition.maxModuleSize().height()
                ),
                false
        );

        return 1;
    }

    private static int addFrame(
            ItemStack stack
    ) {
        Frame frame = new Frame(ModFrames.CIVILIAN.getId());

        ExoskeletonData data = ExoskeletonItem.get(stack).getData(stack);

        data = ExoskeletonOperations.installFrame(
                data,
                frame
        );

        stack.set(
                ModDataComponents.EXOSKELETON_DATA.get(),
                data
        );

        return 1;
    }

    private static int removeFrame(
            ItemStack stack
    ) {
        Frame frame = new Frame(ModFrames.CIVILIAN.getId());

        ExoskeletonData data = ExoskeletonItem.get(stack).getData(stack);

        data = ExoskeletonOperations.removeFrame(
                data
        );

        stack.set(
                ModDataComponents.EXOSKELETON_DATA.get(),
                data
        );

        return 1;
    }

    private static int addController(
            ItemStack stack
    ) {
        Controller controller = new Controller(ModControllers.CIVILIAN.getId());

        ExoskeletonData data = ExoskeletonItem.get(stack).getData(stack);

        data = ExoskeletonOperations.installController(
                data,
                controller
        );

        stack.set(
                ModDataComponents.EXOSKELETON_DATA.get(),
                data
        );

        return 1;
    }

    private static int removeController(
            ItemStack stack
    ) {
        Controller controller = new Controller(ModControllers.CIVILIAN.getId());

        ExoskeletonData data = ExoskeletonItem.get(stack).getData(stack);

        data = ExoskeletonOperations.removeController(
                data
        );

        stack.set(
                ModDataComponents.EXOSKELETON_DATA.get(),
                data
        );

        return 1;
    }

    private static int addEnergySystem(
            ItemStack stack
    ) {
        EnergySystem energySystem = new EnergySystem(ModEnergySystems.CIVILIAN.getId());

        ExoskeletonData data = ExoskeletonItem.get(stack).getData(stack);

        data = ExoskeletonOperations.installEnergySystem(
                data,
                energySystem
        );

        stack.set(
                ModDataComponents.EXOSKELETON_DATA.get(),
                data
        );

        return 1;
    }

    private static int removeEnergySystem(
            ItemStack stack
    ) {
        EnergySystem energySystem = new EnergySystem(ModEnergySystems.CIVILIAN.getId());

        ExoskeletonData data = ExoskeletonItem.get(stack).getData(stack);

        data = ExoskeletonOperations.removeEnergySystem(
                data
        );

        stack.set(
                ModDataComponents.EXOSKELETON_DATA.get(),
                data
        );

        return 1;
    }

    private static int addMatrix(
            ServerPlayer player,
            int slot
    ) {
        ItemStack matrixStack = player.getOffhandItem();

        MatrixItem.get(matrixStack);

        MatrixData matrix = matrixStack.get(
                ModDataComponents.MATRIX_DATA.get()
        );

        ItemStack exoskeletonStack = player.getMainHandItem();

        ExoskeletonData exoskeletonData = ExoskeletonItem.get(exoskeletonStack).getData(exoskeletonStack);

        exoskeletonData = ExoskeletonOperations.installMatrix(
                exoskeletonData,
                slot,
                matrix
        );

        exoskeletonStack.set(
                ModDataComponents.EXOSKELETON_DATA.get(),
                exoskeletonData
        );

        return 1;
    }

    private static int removeMatrix(
            ItemStack stack,
            int slot
    ) {
        ExoskeletonData data = ExoskeletonItem.get(stack).getData(stack);

        data = ExoskeletonOperations.removeMatrix(
                data,
                slot
        );

        stack.set(
                ModDataComponents.EXOSKELETON_DATA.get(),
                data
        );

        return 1;
    }

    private static int exoskeletonState(
            CommandSourceStack source
    ) {
        ServerPlayer player = source.getPlayer();

        if (player == null) {
            source.sendFailure(
                    Component.literal(
                            "This command can only be used by a player."
                    )
            );
            return 0;
        }

        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof ExoskeletonItem)) {
            source.sendFailure(
                    Component.literal(
                            "You must hold an exoskeleton in your main hand."
                    )
            );
            return 0;
        }

        ExoskeletonData data = stack.get(ModDataComponents.EXOSKELETON_DATA.get());

        if (data == null) {
            source.sendFailure(
                    Component.literal(
                            "Exoskeleton does not contain data"
                    )
            );
        }

        source.sendSuccess(
                () -> Component.literal(
                        "Active profile: " + data.activeProfile()
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Has active profile: " + ExoskeletonState.hasActiveProfile(data)
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Can activate: " + ExoskeletonState.canActivate(data)
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Active matrix slots: " + ExoskeletonState.activeMatrixSlots(data)
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Active matrix count: " + ExoskeletonState.activeMatrixCount(data)
                ),
                false
        );

        for (int slot = 0; slot < ExoskeletonData.MAX_MATRICES; slot++) {
            final int currentSlot = slot;

            source.sendSuccess(
                    () -> Component.literal(
                            "Matrix slot: "
                                    + currentSlot
                                    + " active: "
                                    + ExoskeletonState.isMatrixActive(
                                            data,currentSlot
                                    )
                    ),
                    false
            );
        }

        return 1;
    }
}
