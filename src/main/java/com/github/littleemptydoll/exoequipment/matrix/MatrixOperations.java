package com.github.littleemptydoll.exoequipment.matrix;

import com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations;
import com.github.littleemptydoll.exoequipment.module.*;
import com.github.littleemptydoll.exoequipment.registry.ModModules;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class MatrixOperations {
    private MatrixOperations() {}

    public static boolean canPlace(
            MatrixData matrix,
            MatrixDefinition matrixDefinition,
            ModuleDefinition moduleDefinition,
            int x,
            int y,
            int rotation
    ) {
        ModuleSize size =
                getRotatedSize(
                        moduleDefinition.size(),
                        rotation
                );

        if (!matrixDefinition.contains(size, x, y)) {
            return false;
        }

        for (InstalledModule installedModule : matrix.modules()) {
            ModuleDefinition installedDefinition =
                    ModModules.getDefinition(
                            installedModule.id()
                    );

            ModuleSize installedSize =
                    getRotatedSize(
                            installedDefinition.size(),
                            installedModule.rotation()
                    );

            if (intersects(
                    x,
                    y,
                    size,
                    installedModule.x(),
                    installedModule.y(),
                    installedSize
            )) {
                return false;
            }
        }

        return true;
    }

    public static MatrixData addModule(
            MatrixData matrix,
            MatrixDefinition matrixDefinition,
            InstalledModule module
    ) {
        ModuleDefinition definition =
                ModModules.getDefinition(module.id());

        if (!canPlace(
                matrix,
                matrixDefinition,
                definition,
                module.x(),
                module.y(),
                module.rotation()
        )) {
            throw new IllegalArgumentException(
                    "Module "
                            + module.id()
                            + " cannot be placed at "
                            + module.x()
                            + ", "
                            + module.y()
            );
        }

        List<InstalledModule> modules =
                new ArrayList<>(matrix.modules());

        modules.add(module);

        return createMatrix(matrix, modules);
    }

    public static MatrixData removeModule(
            MatrixData matrix,
            int x,
            int y
    ) {
        InstalledModule module =
                getModuleAt(matrix, x, y);

        if (module == null) {
            return matrix;
        }

        List<InstalledModule> modules =
                new ArrayList<>(matrix.modules());

        modules.remove(module);

        return createMatrix(matrix, modules);
    }

    public static InstalledModule getModuleAt(
            MatrixData matrix,
            int x,
            int y
    ) {
        for (InstalledModule module : matrix.modules()) {
            ModuleDefinition definition =
                    ModModules.getDefinition(module.id());

            ModuleSize size =
                    getRotatedSize(
                            definition.size(),
                            module.rotation()
                    );

            if (isInside(
                    x,
                    y,
                    module.x(),
                    module.y(),
                    size
            )) {
                return module;
            }
        }

        return null;
    }

    public static MatrixData rotateModule(
            MatrixData matrix,
            MatrixDefinition matrixDefinition,
            int x,
            int y
    ) {
        InstalledModule module =
                getModuleAt(matrix, x, y);

        if (module == null) {
            return matrix;
        }

        int newRotation =
                (module.rotation() + 90) % 360;

        InstalledModule rotatedModule =
                new InstalledModule(
                        module.id(),
                        module.x(),
                        module.y(),
                        newRotation,
                        module.storedEnergy(),
                        module.shieldEnergy(),
                        module.shieldRechargeCooldown(),
                        module.revivalCooldown(),
                        module.emergencyShieldCooldown(),
                        module.active(),
                        module.abilityCooldown(),
                        module.flightActive()
                );

        return replaceModule(
                matrix,
                matrixDefinition,
                module,
                rotatedModule
        );
    }

    public static MatrixData moveModule(
            MatrixData matrix,
            MatrixDefinition matrixDefinition,
            int fromX,
            int fromY,
            int toX,
            int toY
    ) {
        InstalledModule module =
                getModuleAt(matrix, fromX, fromY);

        if (module == null) {
            return matrix;
        }

        InstalledModule movedModule =
                new InstalledModule(
                        module.id(),
                        toX,
                        toY,
                        module.rotation(),
                        module.storedEnergy(),
                        module.shieldEnergy(),
                        module.shieldRechargeCooldown(),
                        module.revivalCooldown(),
                        module.emergencyShieldCooldown(),
                        module.active(),
                        module.abilityCooldown(),
                        module.flightActive()
                );

        return replaceModule(
                matrix,
                matrixDefinition,
                module,
                movedModule
        );
    }

    private static MatrixData replaceModule(
            MatrixData matrix,
            MatrixDefinition matrixDefinition,
            InstalledModule oldModule,
            InstalledModule newModule
    ) {
        List<InstalledModule> modules =
                new ArrayList<>(matrix.modules());

        modules.remove(oldModule);

        MatrixData withoutModule =
                createMatrix(matrix, modules);

        ModuleDefinition definition =
                ModModules.getDefinition(
                        newModule.id()
                );

        if (!canPlace(
                withoutModule,
                matrixDefinition,
                definition,
                newModule.x(),
                newModule.y(),
                newModule.rotation()
        )) {
            throw new IllegalArgumentException(
                    "Module "
                            + newModule.id()
                            + " cannot be placed at "
                            + newModule.x()
                            + ", "
                            + newModule.y()
            );
        }

        modules.add(newModule);

        return createMatrix(matrix, modules);
    }

    private static MatrixData createMatrix(
            MatrixData matrix,
            List<InstalledModule> modules
    ) {
        return new MatrixData(
                matrix.id(),
                modules
        );
    }

    public static ModuleSize getRotatedSize(
            ModuleSize size,
            int rotation
    ) {
        return switch (normalizeRotation(rotation)) {
            case 0, 180 -> size;
            case 90, 270 ->
                    new ModuleSize(
                            size.height(),
                            size.width()
                    );
            default ->
                    throw new IllegalArgumentException(
                            "Invalid module rotation: "
                                    + rotation
                    );
        };
    }

    public static int normalizeRotation(int rotation) {
        int normalized = rotation % 360;

        if (normalized < 0) {
            normalized += 360;
        }

        if (normalized % 90 != 0) {
            throw new IllegalArgumentException(
                    "Module rotation must be a multiple of 90 degrees"
            );
        }

        return normalized;
    }

    private static boolean intersects(
            int x1,
            int y1,
            ModuleSize size1,
            int x2,
            int y2,
            ModuleSize size2
    ) {
        return x1 < x2 + size2.width()
                && x1 + size1.width() > x2
                && y1 < y2 + size2.height()
                && y1 + size1.height() > y2;
    }

    private static boolean isInside(
            int x,
            int y,
            int moduleX,
            int moduleY,
            ModuleSize size
    ) {
        return x >= moduleX
                && x < moduleX + size.width()
                && y >= moduleY
                && y < moduleY + size.height();
    }

    public static int calculateEnergyConsumption(
            MatrixData matrix
    ) {
        return calculateEnergyConsumption(
                matrix,
                module -> true
        );
    }

    public static int calculateEnergyConsumption(
            MatrixData matrix,
            Predicate<InstalledModule> supported
    ) {
        return calculateEnergyConsumption(
                matrix,
                supported,
                Double.NaN
        );
    }

    public static int calculateEnergyConsumption(
            MatrixData matrix,
            Predicate<InstalledModule> supported,
            double temperature
    ) {
        return matrix.modules()
                .stream()
                .filter(supported)
                .mapToInt(module -> {
                    ModuleDefinition definition =
                            ModModules.getDefinition(
                                    module.id()
                            );

                    return scaled(
                            definition.energy()
                                    .map(
                                            EnergyProperties::consumption
                                    )
                                    .orElse(0),
                            temperature,
                            definition
                    );
                })
                .sum();
    }

    public static int calculateEnergyGeneration(
            MatrixData matrix
    ) {
        return calculateEnergyGeneration(
                matrix,
                module -> true
        );
    }

    public static int calculateEnergyGeneration(
            MatrixData matrix,
            Predicate<InstalledModule> supported
    ) {
        return calculateEnergyGeneration(
                matrix,
                supported,
                Double.NaN
        );
    }

    public static int calculateEnergyGeneration(
            MatrixData matrix,
            Predicate<InstalledModule> supported,
            double temperature
    ) {
        return matrix.modules()
                .stream()
                .filter(supported)
                .mapToInt(module -> {
                    ModuleDefinition definition =
                            ModModules.getDefinition(
                                    module.id()
                            );

                    return scaled(
                            definition.generation()
                                    .map(
                                            GenerationProperties::generation
                                    )
                                    .orElse(0),
                            temperature,
                            definition
                    );
                })
                .sum();
    }

    public static int calculateEnergyStorageCapacity(
            MatrixData matrix
    ) {
        return calculateEnergyStorageCapacity(
                matrix,
                module -> true
        );
    }

    public static int calculateEnergyStorageCapacity(
            MatrixData matrix,
            Predicate<InstalledModule> supported
    ) {
        return calculateEnergyStorageCapacity(
                matrix,
                supported,
                Double.NaN
        );
    }

    public static int calculateEnergyStorageCapacity(
            MatrixData matrix,
            Predicate<InstalledModule> supported,
            double temperature
    ) {
        return matrix.modules()
                .stream()
                .filter(supported)
                .mapToInt(module -> {
                    ModuleDefinition definition =
                            ModModules.getDefinition(
                                    module.id()
                            );

                    return scaled(
                            definition.storage()
                                    .map(
                                            StorageProperties::capacity
                                    )
                                    .orElse(0),
                            temperature,
                            definition
                    );
                })
                .sum();
    }

    public static int calculateEnergyStorageInput(
            MatrixData matrix
    ) {
        return calculateEnergyStorageInput(
                matrix,
                module -> true
        );
    }

    public static int calculateEnergyStorageInput(
            MatrixData matrix,
            Predicate<InstalledModule> supported
    ) {
        return calculateEnergyStorageInput(
                matrix,
                supported,
                Double.NaN
        );
    }

    public static int calculateEnergyStorageInput(
            MatrixData matrix,
            Predicate<InstalledModule> supported,
            double temperature
    ) {
        return matrix.modules()
                .stream()
                .filter(supported)
                .mapToInt(module -> {
                    ModuleDefinition definition =
                            ModModules.getDefinition(
                                    module.id()
                            );

                    return scaled(
                            definition.storage()
                                    .map(
                                            StorageProperties::maxInput
                                    )
                                    .orElse(0),
                            temperature,
                            definition
                    );
                })
                .sum();
    }

    public static int calculateEnergyStorageOutput(
            MatrixData matrix
    ) {
        return calculateEnergyStorageOutput(
                matrix,
                module -> true
        );
    }

    public static int calculateEnergyStorageOutput(
            MatrixData matrix,
            Predicate<InstalledModule> supported
    ) {
        return calculateEnergyStorageOutput(
                matrix,
                supported,
                Double.NaN
        );
    }

    public static int calculateEnergyStorageOutput(
            MatrixData matrix,
            Predicate<InstalledModule> supported,
            double temperature
    ) {
        return matrix.modules()
                .stream()
                .filter(supported)
                .mapToInt(module -> {
                    ModuleDefinition definition =
                            ModModules.getDefinition(
                                    module.id()
                            );

                    return scaled(
                            definition.storage()
                                    .map(
                                            StorageProperties::maxOutput
                                    )
                                    .orElse(0),
                            temperature,
                            definition
                    );
                })
                .sum();
    }

    public static int calculateStoredEnergy(
            MatrixData matrix
    ) {
        return calculateStoredEnergy(
                matrix,
                module -> true
        );
    }

    public static int calculateStoredEnergy(
            MatrixData matrix,
            Predicate<InstalledModule> supported
    ) {
        return calculateStoredEnergy(
                matrix,
                supported,
                Double.NaN
        );
    }

    public static int calculateStoredEnergy(
            MatrixData matrix,
            Predicate<InstalledModule> supported,
            double temperature
    ) {
        return matrix.modules()
                .stream()
                .filter(supported)
                .mapToInt(module -> {
                    ModuleDefinition definition =
                            ModModules.getDefinition(
                                    module.id()
                            );

                    var storage =
                            definition.storage();

                    if (storage.isEmpty()) {
                        return 0;
                    }

                    int capacity =
                            storage.get().capacity();

                    if (!Double.isNaN(temperature)) {
                        capacity = scaled(
                                capacity,
                                temperature,
                                definition
                        );
                    }

                    return Math.max(
                            0,
                            Math.min(
                                    module.storedEnergy(),
                                    capacity
                            )
                    );
                })
                .sum();
    }

    public static double calculateHeatGeneration(
            MatrixData matrix
    ) {
        return calculateHeatGeneration(
                matrix,
                module -> true
        );
    }

    public static double calculateHeatGeneration(
            MatrixData matrix,
            Predicate<InstalledModule> supported
    ) {
        return calculateHeatGeneration(
                matrix,
                supported,
                Double.NaN
        );
    }

    public static double calculateHeatGeneration(
            MatrixData matrix,
            Predicate<InstalledModule> supported,
            double temperature
    ) {
        return matrix.modules()
                .stream()
                .filter(supported)
                .mapToDouble(module -> {
                    ModuleDefinition definition =
                            ModModules.getDefinition(
                                    module.id()
                            );

                    return scaledThermal(
                            definition.thermal()
                                    .map(
                                            ThermalProperties::heatGeneration
                                    )
                                    .orElse(0.0D),
                            temperature,
                            definition
                    );
                })
                .sum();
    }

    public static double calculateCooling(
            MatrixData matrix
    ) {
        return calculateCooling(
                matrix,
                module -> true
        );
    }

    public static double calculateCooling(
            MatrixData matrix,
            Predicate<InstalledModule> supported
    ) {
        return calculateCooling(
                matrix,
                supported,
                Double.NaN
        );
    }

    public static double calculateCooling(
            MatrixData matrix,
            Predicate<InstalledModule> supported,
            double temperature
    ) {
        return matrix.modules()
                .stream()
                .filter(supported)
                .mapToDouble(module -> {
                    ModuleDefinition definition =
                            ModModules.getDefinition(
                                    module.id()
                            );

                    return scaledThermal(
                            definition.thermal()
                                    .map(
                                            ThermalProperties::cooling
                                    )
                                    .orElse(0.0D),
                            temperature,
                            definition
                    );
                })
                .sum();
    }

    public static MatrixState calculateState(
            MatrixData matrix
    ) {
        return calculateState(
                matrix,
                module -> true
        );
    }

    public static MatrixState calculateState(
            MatrixData matrix,
            Predicate<InstalledModule> supported
    ) {
        return calculateState(
                matrix,
                supported,
                Double.NaN
        );
    }

    public static MatrixState calculateState(
            MatrixData matrix,
            Predicate<InstalledModule> supported,
            double temperature
    ) {
        int energyConsumption = 0;
        int energyGeneration = 0;
        int energyStorageCapacity = 0;
        int energyStorageInput = 0;
        int energyStorageOutput = 0;
        double heatGeneration = 0.0D;
        double cooling = 0.0D;

        boolean applyEfficiency =
                !Double.isNaN(temperature);

        for (InstalledModule module : matrix.modules()) {
            if (!supported.test(module)) {
                continue;
            }

            ModuleDefinition definition =
                    ModModules.getDefinition(module.id());

            double efficiency =
                    applyEfficiency
                            ? TemperatureOperations
                            .calculateModuleEfficiency(
                                    definition,
                                    temperature
                            )
                            : 1.0D;

            energyConsumption += scaled(
                    definition.energy()
                            .map(
                                    EnergyProperties::consumption
                            )
                            .orElse(0),
                    applyEfficiency,
                    efficiency
            );

            energyGeneration += scaled(
                    definition.generation()
                            .map(
                                    GenerationProperties::generation
                            )
                            .orElse(0),
                    applyEfficiency,
                    efficiency
            );

            energyStorageCapacity += scaled(
                    definition.storage()
                            .map(
                                    StorageProperties::capacity
                            )
                            .orElse(0),
                    applyEfficiency,
                    efficiency
            );

            energyStorageInput += scaled(
                    definition.storage()
                            .map(
                                    StorageProperties::maxInput
                            )
                            .orElse(0),
                    applyEfficiency,
                    efficiency
            );

            energyStorageOutput += scaled(
                    definition.storage()
                            .map(
                                    StorageProperties::maxOutput
                            )
                            .orElse(0),
                    applyEfficiency,
                    efficiency
            );

            heatGeneration += scaledThermal(
                    definition.thermal()
                            .map(
                                    ThermalProperties::heatGeneration
                            )
                            .orElse(0.0D),
                    applyEfficiency,
                    efficiency
            );

            cooling += scaledThermal(
                    definition.thermal()
                            .map(
                                    ThermalProperties::cooling
                            )
                            .orElse(0.0D),
                    applyEfficiency,
                    efficiency
            );
        }

        return new MatrixState(
                energyConsumption,
                energyGeneration,
                energyStorageCapacity,
                energyStorageInput,
                energyStorageOutput,
                (int) Math.round(heatGeneration),
                (int) Math.round(cooling)
        );
    }

    private static double scaledThermal(
            double value,
            double temperature,
            ModuleDefinition definition
    ) {
        if (Double.isNaN(temperature)) {
            return value;
        }

        return scaledThermal(
                value,
                true,
                TemperatureOperations
                        .calculateModuleEfficiency(
                                definition,
                                temperature
                        )
        );
    }

    private static double scaledThermal(
            double value,
            boolean applyEfficiency,
            double efficiency
    ) {
        if (!applyEfficiency) {
            return value;
        }

        return Math.max(
                0.0D,
                value * efficiency
        );
    }

    private static int scaled(
            int value,
            double temperature,
            ModuleDefinition definition
    ) {
        if (Double.isNaN(temperature)) {
            return value;
        }

        return scaled(
                value,
                true,
                TemperatureOperations
                        .calculateModuleEfficiency(
                                definition,
                                temperature
                        )
        );
    }

    private static int scaled(
            int value,
            boolean applyEfficiency,
            double efficiency
    ) {
        if (!applyEfficiency) {
            return value;
        }

        return Math.max(
                0,
                (int) Math.round(value * efficiency)
        );
    }
}
