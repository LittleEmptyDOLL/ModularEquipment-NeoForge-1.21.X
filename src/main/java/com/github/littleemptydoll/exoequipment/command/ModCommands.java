package com.github.littleemptydoll.exoequipment.command;

import com.github.littleemptydoll.exoequipment.controller.ControllerDefinition;
import com.github.littleemptydoll.exoequipment.energy.EnergySystemDefinition;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonDefinition;
import com.github.littleemptydoll.exoequipment.frame.FrameDefinition;
import com.github.littleemptydoll.exoequipment.item.*;
import com.github.littleemptydoll.exoequipment.matrix.MatrixState;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

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

        ExoskeletonDefinition definition = exoskeletonItem.getDefinition(stack);

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
}
