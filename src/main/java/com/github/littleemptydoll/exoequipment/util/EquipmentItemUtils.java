package com.github.littleemptydoll.exoequipment.util;

import com.github.littleemptydoll.exoequipment.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class EquipmentItemUtils {
    private EquipmentItemUtils() {}

    public static Component frameName(
            ResourceLocation id
    ) {
        return ModItems.findFrame(id)
                .map(item ->
                        item.getDefaultInstance()
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
        return ModItems.findController(id)
                .map(item ->
                        item.getDefaultInstance()
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
        return ModItems.findEnergySystem(id)
                .map(item ->
                        item.getDefaultInstance()
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
        return ModItems.findMatrix(id)
                .map(item ->
                        item.getDefaultInstance()
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
        return ModItems.findModule(id)
                .map(item ->
                        item.getDefaultInstance()
                                .getHoverName()
                )
                .orElse(
                        Component.literal(
                                id.toString()
                        )
                );
    }
}
