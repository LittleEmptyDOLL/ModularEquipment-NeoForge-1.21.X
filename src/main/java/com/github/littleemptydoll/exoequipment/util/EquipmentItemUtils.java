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
                .getItem()
                .getDefaultInstance()
                .getHoverName();
    }

    public static Component controllerName(
            ResourceLocation id
    ) {
        return ModControllers.find(id)
                .getItem()
                .getDefaultInstance()
                .getHoverName();
    }

    public static Component energySystemName(
            ResourceLocation id
    ) {
        return ModEnergySystems.find(id)
                .getItem()
                .getDefaultInstance()
                .getHoverName();
    }

    public static Component matrixName(
            ResourceLocation id
    ) {
        return ModMatrices.find(id)
                .getItem()
                .getDefaultInstance()
                .getHoverName();
    }

    public static Component moduleName(
            ResourceLocation id
    ) {
        return ModModules.find(id)
                .getItem()
                .getDefaultInstance()
                .getHoverName();
    }
}
