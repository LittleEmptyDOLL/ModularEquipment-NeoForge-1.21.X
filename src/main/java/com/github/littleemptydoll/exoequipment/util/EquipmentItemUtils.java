package com.github.littleemptydoll.exoequipment.util;

import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.registry.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

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
        var entry = ModModules.find(id);
        if (entry == null) {
            return Component.literal(id.toString());
        }

        return entry.getItem()
                .getDefaultInstance()
                .getHoverName();
    }

    public static Component moduleName(
            InstalledModule module
    ) {
        ResourceLocation itemDefinitionId = module.generated()
                .map(value -> value.baseDefinitionId())
                .orElse(module.id());
        var entry = ModModules.find(itemDefinitionId);

        if (entry == null) {
            return Component.literal(module.id().toString());
        }

        ItemStack stack = entry.getItem().getDefaultInstance();
        module.generated().ifPresent(value ->
                stack.set(ModDataComponents.GENERATED_MODULE_DATA.get(), value)
        );
        return stack.getHoverName();
    }
}
