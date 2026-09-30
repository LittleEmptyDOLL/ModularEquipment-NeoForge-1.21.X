package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.fabricator.FabricatorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ExoEquipment.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FabricatorBlockEntity>> FABRICATOR =
            TYPES.register("fabricator", () -> BlockEntityType.Builder.of(
                    FabricatorBlockEntity::new, ModBlocks.FABRICATOR.get()).build(null));

    private ModBlockEntities() {}

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        bus.addListener(ModBlockEntities::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, FABRICATOR.get(),
                (entity, side) -> entity.energyStorage());
    }
}
