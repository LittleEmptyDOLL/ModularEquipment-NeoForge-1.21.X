package com.github.littleemptydoll.exoequipment.command;

import com.github.littleemptydoll.exoequipment.energy.EnergySystemDefinition;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.exoskeleton.SystemStatus;
import com.github.littleemptydoll.exoequipment.frame.FrameDefinition;
import com.github.littleemptydoll.exoequipment.gui.ExoskeletonMenuProvider;
import com.github.littleemptydoll.exoequipment.item.ControllerItem;
import com.github.littleemptydoll.exoequipment.item.EnergySystemItem;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.item.FrameItem;
import com.github.littleemptydoll.exoequipment.item.MatrixItem;
import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixState;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class ModCommands {
    private static final int DEBUG_PERMISSION_LEVEL = 2;

    private ModCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("exoequipment")
                        .requires(source ->
                                source.hasPermission(DEBUG_PERMISSION_LEVEL)
                        )
                        .then(
                                Commands.literal("debug")
                                        .then(
                                                Commands.literal("held")
                                                        .executes(context ->
                                                                showHeldEquipment(
                                                                        context.getSource()
                                                                )
                                                        )
                                        )
                                        .then(
                                                Commands.literal("exoskeleton")
                                                        .executes(context ->
                                                                showEquippedExoskeleton(
                                                                        context.getSource()
                                                                )
                                                        )
                                        )
                        )
        );
    }

    private static int showHeldEquipment(
            CommandSourceStack source
    ) {
        ServerPlayer player = requirePlayer(source);

        if (player == null) {
            return 0;
        }

        ItemStack stack = player.getMainHandItem();

        if (stack.isEmpty()) {
            return fail(
                    source,
                    "Hold a Modular Equipment item in your main hand."
            );
        }

        if (stack.getItem() instanceof ExoskeletonItem) {
            return showExoskeleton(
                    source,
                    stack,
                    "Held Exoskeleton"
            );
        }

        if (stack.getItem() instanceof MatrixItem matrixItem) {
            return showMatrix(
                    source,
                    stack,
                    matrixItem
            );
        }

        if (stack.getItem() instanceof ModuleItem moduleItem) {
            return showModule(
                    source,
                    moduleItem
            );
        }

        if (stack.getItem() instanceof FrameItem frameItem) {
            return showFrame(
                    source,
                    frameItem
            );
        }

        if (stack.getItem() instanceof ControllerItem controllerItem) {
            return showController(
                    source,
                    controllerItem
            );
        }

        if (stack.getItem() instanceof EnergySystemItem energySystemItem) {
            return showEnergySystem(
                    source,
                    energySystemItem
            );
        }

        return fail(
                source,
                "The held item is not Modular Equipment."
        );
    }

    private static int showEquippedExoskeleton(
            CommandSourceStack source
    ) {
        ServerPlayer player = requirePlayer(source);

        if (player == null) {
            return 0;
        }

        return ExoskeletonMenuProvider.findBodyExoskeleton(player)
                .map(stack ->
                        showExoskeleton(
                                source,
                                stack,
                                "Equipped Exoskeleton"
                        )
                )
                .orElseGet(() ->
                        fail(
                                source,
                                "No exoskeleton is equipped in the Curios body slot."
                        )
                );
    }

    private static int showExoskeleton(
            CommandSourceStack source,
            ItemStack stack,
            String title
    ) {
        ExoskeletonItem item = ExoskeletonItem.get(stack);
        ExoskeletonData data = ExoskeletonItem.getData(stack);
        MatrixState state = ExoskeletonState.calculateState(data);
        SystemStatus status = SystemStatus.calculate(data);

        long installedMatrices = data.matrices()
                .stream()
                .filter(slot -> slot.matrix().isPresent())
                .count();

        String activeProfile = ExoskeletonState.hasActiveProfile(data)
                ? ExoskeletonState.activeProfile(data).name()
                : "None";

        sendHeader(source, title);
        sendLine(source, "ID: " + item.getDefinition().id());
        sendLine(source, "Tier: " + item.getDefinition().tier());
        sendLine(
                source,
                "Frame: "
                        + data.frame()
                        .map(frame -> frame.definitionId().toString())
                        .orElse("None")
        );
        sendLine(
                source,
                "Controller: "
                        + data.controller()
                        .map(controller -> controller.definitionId().toString())
                        .orElse("None")
        );
        sendLine(
                source,
                "Energy system: "
                        + data.energySystem()
                        .map(system -> system.definitionId().toString())
                        .orElse("None")
        );
        sendLine(
                source,
                "Matrices: "
                        + installedMatrices
                        + "/"
                        + ExoskeletonData.MAX_MATRICES
        );
        sendLine(source, "Active profile: " + activeProfile);
        sendLine(
                source,
                "Active matrix slots: "
                        + ExoskeletonState.activeMatrixSlots(data)
        );
        sendLine(source, "Temperature: " + data.temperature() + " C");
        sendLine(
                source,
                "Energy: "
                        + state.energyGeneration()
                        + " generated / "
                        + state.energyConsumption()
                        + " consumed FE/t"
        );
        sendLine(
                source,
                "Storage capacity: "
                        + state.energyStorageCapacity()
                        + " FE"
        );
        sendLine(
                source,
                "Thermal balance: "
                        + state.thermalBalance()
        );
        sendLine(
                source,
                "System status: severity="
                        + status.severity()
                        + ", flags="
                        + status.flags()
        );

        return 1;
    }

    private static int showMatrix(
            CommandSourceStack source,
            ItemStack stack,
            MatrixItem item
    ) {
        MatrixData data = item.getMatrixData(stack);
        MatrixState state = MatrixOperations.calculateState(data);

        sendHeader(source, "Matrix");
        sendLine(source, "ID: " + data.id());
        sendLine(source, "Tier: " + item.getDefinition().tier());
        sendLine(
                source,
                "Size: "
                        + item.getDefinition().width()
                        + "x"
                        + item.getDefinition().height()
        );
        sendLine(source, "Modules: " + data.modules().size());
        sendLine(
                source,
                "Energy: "
                        + state.energyGeneration()
                        + " generated / "
                        + state.energyConsumption()
                        + " consumed FE/t"
        );
        sendLine(
                source,
                "Storage capacity: "
                        + state.energyStorageCapacity()
                        + " FE"
        );
        sendLine(
                source,
                "Heat: "
                        + state.heatGeneration()
                        + " generated / "
                        + state.cooling()
                        + " cooling"
        );

        return 1;
    }

    private static int showModule(
            CommandSourceStack source,
            ModuleItem item
    ) {
        ModuleDefinition definition = item.getDefinition();

        sendHeader(source, "Module");
        sendLine(source, "ID: " + definition.id());
        sendLine(source, "Tier: " + definition.tier());
        sendLine(source, "Category: " + definition.category());
        sendLine(
                source,
                "Size: "
                        + definition.size().width()
                        + "x"
                        + definition.size().height()
        );

        definition.energy().ifPresent(properties -> {
            sendLine(
                    source,
                    "Energy consumption: "
                            + properties.consumption()
                            + " FE/t"
            );
            sendLine(
                    source,
                    "Energy priority: "
                            + properties.priority()
            );
        });

        return 1;
    }

    private static int showFrame(
            CommandSourceStack source,
            FrameItem item
    ) {
        FrameDefinition definition = item.getDefinition();

        sendHeader(source, "Frame");
        sendLine(source, "ID: " + definition.id());
        sendLine(source, "Tier: " + definition.tier());
        sendLine(
                source,
                "Max module size: "
                        + definition.maxModuleSize().width()
                        + "x"
                        + definition.maxModuleSize().height()
        );

        return 1;
    }

    private static int showController(
            CommandSourceStack source,
            ControllerItem item
    ) {
        var definition = item.getDefinition();

        sendHeader(source, "Controller");
        sendLine(source, "ID: " + definition.id());
        sendLine(source, "Tier: " + definition.tier());
        sendLine(
                source,
                "Max profiles: "
                        + definition.maxProfiles()
        );
        sendLine(
                source,
                "Max active matrices: "
                        + definition.maxActiveMatrices()
        );

        return 1;
    }

    private static int showEnergySystem(
            CommandSourceStack source,
            EnergySystemItem item
    ) {
        EnergySystemDefinition definition = item.getDefinition();

        sendHeader(source, "Energy System");
        sendLine(source, "ID: " + definition.id());
        sendLine(source, "Tier: " + definition.tier());
        sendLine(
                source,
                "Max input: "
                        + definition.maxInput()
                        + " FE/t"
        );
        sendLine(
                source,
                "Max output: "
                        + definition.maxOutput()
                        + " FE/t"
        );
        sendLine(
                source,
                "Efficiency: "
                        + definition.efficiency()
        );

        return 1;
    }

    private static ServerPlayer requirePlayer(
            CommandSourceStack source
    ) {
        ServerPlayer player = source.getPlayer();

        if (player == null) {
            fail(
                    source,
                    "This command can only be used by a player."
            );
        }

        return player;
    }

    private static void sendHeader(
            CommandSourceStack source,
            String title
    ) {
        sendLine(
                source,
                "=== " + title + " ==="
        );
    }

    private static void sendLine(
            CommandSourceStack source,
            String text
    ) {
        source.sendSuccess(
                () -> Component.literal(text),
                false
        );
    }

    private static int fail(
            CommandSourceStack source,
            String text
    ) {
        source.sendFailure(
                Component.literal(text)
        );

        return 0;
    }
}
