package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.matrix.MatrixDefinition;
import com.github.littleemptydoll.exoequipment.matrix.MatrixType;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public final class ModMatrices {
    private ModMatrices() {}

    private static final Map<ResourceLocation, MatrixDefinition> MATRICES = new HashMap<>();

    private static MatrixDefinition register(
            String id,
            MatrixType type,
            int width,
            int height
    ) {
        ResourceLocation location =
                ResourceLocation.fromNamespaceAndPath(
                        ExoEquipment.MODID,
                        id
                );

        MatrixDefinition definition =
                new MatrixDefinition(
                        type,
                        width,
                        height
                );

        if (MATRICES.putIfAbsent(
                location,
                definition
        ) != null) {
            throw new IllegalStateException(
                    "Duplicate matrix id: "
                            + location
            );
        }

        return  definition;
    }

    public static MatrixDefinition getDefinition(
            ResourceLocation id
    ) {
        MatrixDefinition definition = MATRICES.get(id);

        if (definition == null) {
            throw new IllegalArgumentException(
                    "Unknown matrix: " + id
            );
        }

        return definition;
    }

    private static final MatrixDefinition CIVILIAN = register(
            "civilian",
            MatrixType.CIVILIAN,
            5,
            5
    );

    private static final MatrixDefinition MILITARY = register(
            "military",
            MatrixType.MILITARY,
            5,
            5
    );

    private static final MatrixDefinition EXPERIMENTAL = register(
            "experimental",
            MatrixType.EXPERIMENTAL,
            5,
            5
    );
}
