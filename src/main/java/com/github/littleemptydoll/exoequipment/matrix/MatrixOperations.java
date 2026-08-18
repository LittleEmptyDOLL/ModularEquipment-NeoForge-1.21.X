package com.github.littleemptydoll.exoequipment.matrix;

import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.registry.ModModules;

import java.util.ArrayList;
import java.util.List;

public final class MatrixOperations {
    private  MatrixOperations() {}

    public static boolean canPlace(
            MatrixData matrix,
            MatrixDefinition matrixDefinition,
            ModuleDefinition moduleDefinition,
            int x,
            int y,
            int rotation
    ) {
        ModuleSize size = getRotatedSize(
                moduleDefinition.size(),
                rotation
        );

        if (!matrixDefinition.contains(
                size,
                x,
                y
        )) {
            return false;
        }

        for (InstalledModule installedModule : matrix.modules()) {
            ModuleDefinition installedDefinition =
                    ModModules.getDefinition(
                            installedModule.id()
                    );

            ModuleSize installedSize = getRotatedSize(
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
        ModuleDefinition definition = ModModules.getDefinition(module.id());

        if (!canPlace(
                matrix,
                matrixDefinition,
                definition,
                module.x(),
                module.y(),
                module.rotation()
        )) {
            throw new IllegalArgumentException(
                    "Module " + module.id()
                            + " cannot be placed at "
                            + module.x()
                            + ", "
                            + module.y()
            );
        }

        List<InstalledModule> modules = new ArrayList<>(matrix.modules());

        modules.add(module);

        return createMatrix(modules);
    }

    public static MatrixData removeModule(
            MatrixData matrix,
            int x,
            int y
    ) {
        InstalledModule module = getModuleAt(matrix, x, y);

        if (module == null) {
            return matrix;
        }

        List<InstalledModule> modules = new ArrayList<>(matrix.modules());

        modules.remove(module);

        return createMatrix(modules);
    }

    public static InstalledModule getModuleAt(
            MatrixData matrix,
            int x,
            int y
    ) {
        for (InstalledModule module : matrix.modules()) {
            ModuleDefinition definition = ModModules.getDefinition(module.id());

            ModuleSize size = getRotatedSize(
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
        InstalledModule module = getModuleAt(
                matrix,
                x,
                y
        );

        if (module == null) {
            return matrix;
        }

        int newRotation = (module.rotation() + 90) % 360;

        InstalledModule rotatedModule =
                new InstalledModule(
                        module.id(),
                        module.x(),
                        module.y(),
                        newRotation
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
        InstalledModule module = getModuleAt(
                matrix,
                fromX,
                fromY
        );

        if (module == null) {
            return matrix;
        }

        InstalledModule movedModule =
                new InstalledModule(
                        module.id(),
                        toX,
                        toY,
                        module.rotation()
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
        List<InstalledModule> modules = new ArrayList<>(matrix.modules());

        modules.remove(oldModule);

        MatrixData withoutModule = createMatrix(modules);

        ModuleDefinition definition = ModModules.getDefinition(newModule.id());

        if (!canPlace(
                withoutModule,
                matrixDefinition,
                definition,
                newModule.x(),
                newModule.y(),
                newModule.rotation()
        )) {
            throw new IllegalArgumentException(
                    "Module " + newModule.id()
                            + " cannot be placed at "
                            + newModule.x()
                            + ", "
                            + newModule.y()
            );
        }

        modules.add(newModule);

        return createMatrix(modules);
    }

    private static MatrixData createMatrix(
            List<InstalledModule> modules
    ) {
        return new MatrixData(modules);
    }

    public static ModuleSize getRotatedSize(
            ModuleSize size,
            int rotation
    ) {
        int normalizedRotation = normalizeRotation(rotation);
        return switch (normalizedRotation) {
            case 0, 180 -> size;
            case 90, 270 ->
                    new ModuleSize(
                            size.height(),
                            size.width()
                    );
            default -> throw new IllegalArgumentException("Invalid module rotation: " + normalizedRotation);
        };
    }

    public static int normalizeRotation(int rotation) {
        int normalized = rotation % 360;

        if (normalized < 0) {
            normalized += 360;
        }

        if (normalized % 90 != 0) {
            throw new IllegalArgumentException("Module rotation must be a multiple of 90 degrees");
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
}
