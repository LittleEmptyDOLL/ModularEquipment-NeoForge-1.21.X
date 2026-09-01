package com.github.littleemptydoll.exoequipment.util;

import com.github.littleemptydoll.exoequipment.registry.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class EquipmentItemUtils {
    private EquipmentItemUtils() {}

    public static Component frameName(
            ResourceLocation id
    ) {
        return ModFrames.find(id)
                .map(entry ->
                        entry.getItem()
                                .getDefaultInstance()
                                .getHoverName()
                )
                .orElse(
                        Component.literal(
                                id.toString()
                        )
                );
    }

    public static Component controllerName(
            ResourceLocation id
    ) {
        return ModControllers.find(id)
                .map(entry ->
                        entry.getItem()
                                .getDefaultInstance()
                                .getHoverName()
                )
                .orElse(
                        Component.literal(
                                id.toString()
                        )
                );
    }

    public static Component energySystemName(
            ResourceLocation id
    ) {
        return ModEnergySystems.find(id)
                .map(entry ->
                        entry.getItem()
                                .getDefaultInstance()
                                .getHoverName()
                )
                .orElse(
                        Component.literal(
                                id.toString()
                        )
                );
    }

    public static Component matrixName(
            ResourceLocation id
    ) {
        return ModMatrices.find(id)
                .map(entry ->
                        entry.getItem()
                                .getDefaultInstance()
                                .getHoverName()
                )
                .orElse(
                        Component.literal(
                                id.toString()
                        )
                );
    }

    public static Component moduleName(
            ResourceLocation id
    ) {
        return ModModules.find(id)
                .map(entry ->
                        entry.getItem()
                                .getDefaultInstance()
                                .getHoverName()
                )
                .orElse(
                        Component.literal(
                                id.toString()
                        )
                );
    }
}
