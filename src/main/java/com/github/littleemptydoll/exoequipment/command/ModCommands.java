package com.github.littleemptydoll.exoequipment.command;

import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
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
        );
    }

    private static int addModule(
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

        MatrixData updated = MatrixOperations.addModule(
                matrix,
                new InstalledModule(
                        ModModules.TEST_MODULE.id(),
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
        MatrixData matrix = stack.get(
                ModDataComponents.MATRIX_DATA.get()
        );

        if (matrix == null) {
            return 0;
        }

        MatrixData updated = MatrixOperations.rotateModule(
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

    private static int moveModule(
            ItemStack stack,
            int fromX,
            int fromY,
            int toX,
            int toY
    ) {
        MatrixData matrix = stack.get(
                ModDataComponents.MATRIX_DATA.get()
        );

        if (matrix == null) {
            return 0;
        }

        MatrixData updated = MatrixOperations.moveModule(
                matrix,
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
}
