package com.github.littleemptydoll.exoequipment.command;

import com.github.littleemptydoll.exoequipment.item.MatrixItem;
import com.github.littleemptydoll.exoequipment.matrix.MatrixDefinition;
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
}
