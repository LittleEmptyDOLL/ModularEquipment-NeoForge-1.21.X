package com.github.littleemptydoll.exoequipment.matrix;

import com.github.littleemptydoll.exoequipment.module.*;
import com.github.littleemptydoll.exoequipment.registry.ModModules;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class MatrixOperations {
    private MatrixOperations() {}

    public static boolean canPlace(MatrixData matrix, MatrixDefinition matrixDefinition,
                                   ModuleDefinition moduleDefinition, int x, int y, int rotation) {
        ModuleSize size = getRotatedSize(moduleDefinition.size(), rotation);
        if (!matrixDefinition.contains(size, x, y)) return false;
        for (InstalledModule installedModule : matrix.modules()) {
            ModuleDefinition installedDefinition = ModModules.getDefinition(installedModule.id());
            ModuleSize installedSize = getRotatedSize(installedDefinition.size(), installedModule.rotation());
            if (intersects(x, y, size, installedModule.x(), installedModule.y(), installedSize)) return false;
        }
        return true;
    }

    public static MatrixData addModule(MatrixData matrix, MatrixDefinition matrixDefinition, InstalledModule module) {
        ModuleDefinition definition = ModModules.getDefinition(module.id());
        if (!canPlace(matrix, matrixDefinition, definition, module.x(), module.y(), module.rotation())) {
            throw new IllegalArgumentException("Module " + module.id() + " cannot be placed at "
                    + module.x() + ", " + module.y());
        }
        List<InstalledModule> modules = new ArrayList<>(matrix.modules());
        modules.add(module);
        return createMatrix(matrix, modules);
    }

    public static MatrixData removeModule(MatrixData matrix, int x, int y) {
        InstalledModule module = getModuleAt(matrix, x, y);
        if (module == null) return matrix;
        List<InstalledModule> modules = new ArrayList<>(matrix.modules());
        modules.remove(module);
        return createMatrix(matrix, modules);
    }

    public static InstalledModule getModuleAt(MatrixData matrix, int x, int y) {
        for (InstalledModule module : matrix.modules()) {
            ModuleDefinition definition = ModModules.getDefinition(module.id());
            ModuleSize size = getRotatedSize(definition.size(), module.rotation());
            if (isInside(x, y, module.x(), module.y(), size)) return module;
        }
        return null;
    }

    public static MatrixData rotateModule(MatrixData matrix, MatrixDefinition matrixDefinition, int x, int y) {
        InstalledModule module = getModuleAt(matrix, x, y);
        if (module == null) return matrix;
        int newRotation = (module.rotation() + 90) % 360;
        InstalledModule rotatedModule = new InstalledModule(
                module.id(),
                module.x(),
                module.y(),
                newRotation,
                module.storedEnergy(),
                module.shieldEnergy(),
                module.shieldRechargeCooldown(),
                module.revivalCooldown(),
                module.emergencyShieldCooldown()
        );
        return replaceModule(matrix, matrixDefinition, module, rotatedModule);
    }

    public static MatrixData moveModule(MatrixData matrix, MatrixDefinition matrixDefinition,
                                        int fromX, int fromY, int toX, int toY) {
        InstalledModule module = getModuleAt(matrix, fromX, fromY);
        if (module == null) return matrix;
        InstalledModule movedModule = new InstalledModule(
                module.id(),
                toX,
                toY,
                module.rotation(),
                module.storedEnergy(),
                module.shieldEnergy(),
                module.shieldRechargeCooldown(),
                module.revivalCooldown(),
                module.emergencyShieldCooldown()
        );
        return replaceModule(matrix, matrixDefinition, module, movedModule);
    }

    private static MatrixData replaceModule(MatrixData matrix, MatrixDefinition matrixDefinition,
                                            InstalledModule oldModule, InstalledModule newModule) {
        List<InstalledModule> modules = new ArrayList<>(matrix.modules());
        modules.remove(oldModule);
        MatrixData withoutModule = createMatrix(matrix, modules);
        ModuleDefinition definition = ModModules.getDefinition(newModule.id());
        if (!canPlace(withoutModule, matrixDefinition, definition, newModule.x(), newModule.y(), newModule.rotation())) {
            throw new IllegalArgumentException("Module " + newModule.id() + " cannot be placed at "
                    + newModule.x() + ", " + newModule.y());
        }
        modules.add(newModule);
        return createMatrix(matrix, modules);
    }

    private static MatrixData createMatrix(MatrixData matrix, List<InstalledModule> modules) {
        return new MatrixData(matrix.id(), modules);
    }

    public static ModuleSize getRotatedSize(ModuleSize size, int rotation) {
        return switch (normalizeRotation(rotation)) {
            case 0, 180 -> size;
            case 90, 270 -> new ModuleSize(size.height(), size.width());
            default -> throw new IllegalArgumentException("Invalid module rotation: " + rotation);
        };
    }

    public static int normalizeRotation(int rotation) {
        int normalized = rotation % 360;
        if (normalized < 0) normalized += 360;
        if (normalized % 90 != 0) throw new IllegalArgumentException("Module rotation must be a multiple of 90 degrees");
        return normalized;
    }

    private static boolean intersects(int x1, int y1, ModuleSize size1,
                                      int x2, int y2, ModuleSize size2) {
        return x1 < x2 + size2.width() && x1 + size1.width() > x2
                && y1 < y2 + size2.height() && y1 + size1.height() > y2;
    }

    private static boolean isInside(int x, int y, int moduleX, int moduleY, ModuleSize size) {
        return x >= moduleX && x < moduleX + size.width()
                && y >= moduleY && y < moduleY + size.height();
    }

    public static int calculateEnergyConsumption(MatrixData matrix) {
        return calculateEnergyConsumption(matrix, module -> true);
    }

    public static int calculateEnergyConsumption(MatrixData matrix, Predicate<InstalledModule> supported) {
        return calculateEnergyConsumption(matrix, supported, Double.NaN);
    }

    public static int calculateEnergyConsumption(MatrixData matrix, Predicate<InstalledModule> supported, double temperature) {
        return matrix.modules().stream()
                .filter(supported)
                .mapToInt(module -> scaled(
                        ModModules.getDefinition(module.id()).energy()
                                .map(EnergyProperties::consumption).orElse(0),
                        temperature,
                        module
                ))
                .sum();
    }

    public static int calculateEnergyGeneration(MatrixData matrix) {
        return calculateEnergyGeneration(matrix, module -> true);
    }

    public static int calculateEnergyGeneration(MatrixData matrix, Predicate<InstalledModule> supported) {
        return calculateEnergyGeneration(matrix, supported, Double.NaN);
    }

    public static int calculateEnergyGeneration(MatrixData matrix, Predicate<InstalledModule> supported, double temperature) {
        return matrix.modules().stream()
                .filter(supported)
                .mapToInt(module -> scaled(
                        ModModules.getDefinition(module.id()).generation()
                                .map(GenerationProperties::generation).orElse(0),
                        temperature,
                        module
                ))
                .sum();
    }

    public static int calculateEnergyStorageCapacity(MatrixData matrix) {
        return calculateEnergyStorageCapacity(matrix, module -> true);
    }

    public static int calculateEnergyStorageCapacity(MatrixData matrix, Predicate<InstalledModule> supported) {
        return calculateEnergyStorageCapacity(matrix, supported, Double.NaN);
    }

    public static int calculateEnergyStorageCapacity(MatrixData matrix, Predicate<InstalledModule> supported, double temperature) {
        return matrix.modules().stream()
                .filter(supported)
                .mapToInt(module -> scaled(
                        ModModules.getDefinition(module.id()).storage()
                                .map(StorageProperties::capacity).orElse(0),
                        temperature,
                        module
                ))
                .sum();
    }

    public static int calculateEnergyStorageInput(MatrixData matrix) {
        return calculateEnergyStorageInput(matrix, module -> true);
    }

    public static int calculateEnergyStorageInput(MatrixData matrix, Predicate<InstalledModule> supported) {
        return calculateEnergyStorageInput(matrix, supported, Double.NaN);
    }

    public static int calculateEnergyStorageInput(MatrixData matrix, Predicate<InstalledModule> supported, double temperature) {
        return matrix.modules().stream()
                .filter(supported)
                .mapToInt(module -> scaled(
                        ModModules.getDefinition(module.id()).storage()
                                .map(StorageProperties::maxInput).orElse(0),
                        temperature,
                        module
                ))
                .sum();
    }

    public static int calculateEnergyStorageOutput(MatrixData matrix) {
        return calculateEnergyStorageOutput(matrix, module -> true);
    }

    public static int calculateEnergyStorageOutput(MatrixData matrix, Predicate<InstalledModule> supported) {
        return calculateEnergyStorageOutput(matrix, supported, Double.NaN);
    }

    public static int calculateEnergyStorageOutput(MatrixData matrix, Predicate<InstalledModule> supported, double temperature) {
        return matrix.modules().stream()
                .filter(supported)
                .mapToInt(module -> scaled(
                        ModModules.getDefinition(module.id()).storage()
                                .map(StorageProperties::maxOutput).orElse(0),
                        temperature,
                        module
                ))
                .sum();
    }

    public static int calculateStoredEnergy(MatrixData matrix) {
        return calculateStoredEnergy(matrix, module -> true);
    }

    public static int calculateStoredEnergy(MatrixData matrix, Predicate<InstalledModule> supported) {
        return calculateStoredEnergy(matrix, supported, Double.NaN);
    }

    public static int calculateStoredEnergy(
            MatrixData matrix,
            Predicate<InstalledModule> supported,
            double temperature
    ) {
        return matrix.modules().stream()
                .filter(supported)
                .mapToInt(module -> {
                    var storage = ModModules.getDefinition(module.id()).storage();
                    if (storage.isEmpty()) {
                        return 0;
                    }

                    int capacity = storage.get().capacity();
                    if (!Double.isNaN(temperature)) {
                        capacity = scaled(capacity, temperature, module);
                    }

                    return Math.max(0, Math.min(module.storedEnergy(), capacity));
                })
                .sum();
    }

    public static double calculateHeatGeneration(MatrixData matrix) {
        return calculateHeatGeneration(matrix, module -> true);
    }

    public static double calculateHeatGeneration(MatrixData matrix, Predicate<InstalledModule> supported) {
        return calculateHeatGeneration(matrix, supported, Double.NaN);
    }

    public static double calculateHeatGeneration(MatrixData matrix, Predicate<InstalledModule> supported, double temperature) {
        return matrix.modules().stream()
                .filter(supported)
                .mapToDouble(module -> scaledThermal(
                        ModModules.getDefinition(module.id()).thermal()
                                .map(ThermalProperties::heatGeneration).orElse(0.0D),
                        temperature,
                        module
                ))
                .sum();
    }

    public static double calculateCooling(MatrixData matrix) {
        return calculateCooling(matrix, module -> true);
    }

    public static double calculateCooling(MatrixData matrix, Predicate<InstalledModule> supported) {
        return calculateCooling(matrix, supported, Double.NaN);
    }

    public static double calculateCooling(MatrixData matrix, Predicate<InstalledModule> supported, double temperature) {
        return matrix.modules().stream()
                .filter(supported)
                .mapToDouble(module -> scaledThermal(
                        ModModules.getDefinition(module.id()).thermal()
                                .map(ThermalProperties::cooling).orElse(0.0D),
                        temperature,
                        module
                ))
                .sum();
    }

    public static MatrixState calculateState(MatrixData matrix) {
        return calculateState(matrix, module -> true);
    }

    public static MatrixState calculateState(MatrixData matrix, Predicate<InstalledModule> supported) {
        return calculateState(matrix, supported, Double.NaN);
    }

    public static MatrixState calculateState(
            MatrixData matrix,
            Predicate<InstalledModule> supported,
            double temperature
    ) {
        return new MatrixState(
                calculateEnergyConsumption(matrix, supported, temperature),
                calculateEnergyGeneration(matrix, supported, temperature),
                calculateEnergyStorageCapacity(matrix, supported, temperature),
                calculateEnergyStorageInput(matrix, supported, temperature),
                calculateEnergyStorageOutput(matrix, supported, temperature),
                (int) Math.round(calculateHeatGeneration(matrix, supported, temperature)),
                (int) Math.round(calculateCooling(matrix, supported, temperature))
        );
    }

    private static double scaledThermal(
            double value,
            double temperature,
            InstalledModule module
    ) {
        if (Double.isNaN(temperature)) {
            return value;
        }

        double efficiency = com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations
                .calculateModuleEfficiency(module.id(), temperature);

        return Math.max(0.0D, value * efficiency);
    }

    private static int scaled(
            int value,
            double temperature,
            InstalledModule module
    ) {
        if (Double.isNaN(temperature)) {
            return value;
        }

        double efficiency = com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations
                .calculateModuleEfficiency(module.id(), temperature);

        return Math.max(0, (int) Math.round(value * efficiency));
    }
}
