package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;
import java.util.function.Supplier;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ExoEquipment.MODID);

    public static Optional<FrameItem> findFrame(
            ResourceLocation definitionId
    ) {
        return ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(item -> item instanceof FrameItem)
                .map(item -> (FrameItem) item)
                .filter(item -> item.getDefinition().id().equals(definitionId))
                .findFirst();
    }

    public static Optional<ControllerItem> findController(
            ResourceLocation definitionId
    ) {
        return ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(item -> item instanceof ControllerItem)
                .map(item -> (ControllerItem) item)
                .filter(item -> item.getDefinition().id().equals(definitionId))
                .findFirst();
    }

    public static Optional<EnergySystemItem> findEnergySystem(
            ResourceLocation definitionId
    ) {
        return ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(item -> item instanceof EnergySystemItem)
                .map(item -> (EnergySystemItem) item)
                .filter(item -> item.getDefinition().id().equals(definitionId))
                .findFirst();
    }

    public static Optional<MatrixItem> findMatrix(
            ResourceLocation definitionId
    ) {
        return ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(item -> item instanceof MatrixItem)
                .map(item -> (MatrixItem) item)
                .filter(item -> item.getDefinition().id().equals(definitionId))
                .findFirst();
    }

    public static Optional<ModuleItem> findModule(
            ResourceLocation definitionId
    ) {
        return ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(item -> item instanceof ModuleItem)
                .map(item -> (ModuleItem) item)
                .filter(item -> item.getDefinition().id().equals(definitionId))
                .findFirst();
    }

    // Экзоскелет
    public static final Supplier<Item> EXOSKELETON = ITEMS.register(
            "exoskeleton",
            () -> new ExoskeletonItem(ModExoskeletons.BASIC)
    );

    // Рама
    public static final Supplier<Item> CIVILIAN_FRAME = ITEMS.register(
            "civilian_frame",
            () -> new FrameItem(ModFrames.CIVILIAN)
    );

    public static final Supplier<Item> MILITARY_FRAME = ITEMS.register(
            "military_frame",
            () -> new FrameItem(ModFrames.MILITARY)
    );

    public static final Supplier<Item> ENGINEERING_FRAME = ITEMS.register(
            "engineering_frame",
            () -> new FrameItem(ModFrames.ENGINEERING)
    );

    public static final Supplier<Item> EXPERIMENTAL_FRAME = ITEMS.register(
            "experimental_frame",
            () -> new FrameItem(ModFrames.EXPERIMENTAL)
    );

    // Матрица
    public static final Supplier<Item> CIVILIAN_MATRIX = ITEMS.register(
            "civilian_matrix",
            () -> new MatrixItem(ModMatrices.CIVILIAN)
    );

    public static final Supplier<Item> MILITARY_MATRIX = ITEMS.register(
            "military_matrix",
            () -> new MatrixItem(ModMatrices.MILITARY)
    );

    public static final Supplier<Item> ENGINEERING_MATRIX = ITEMS.register(
            "engineering_matrix",
            () -> new MatrixItem(ModMatrices.ENGINEERING)
    );

    public static final Supplier<Item> EXPERIMENTAL_MATRIX = ITEMS.register(
            "experimental_matrix",
            () -> new MatrixItem(ModMatrices.EXPERIMENTAL)
    );

    // Энергетическая система
    public static final Supplier<Item> CIVILIAN_ENERGY_SYSTEM = ITEMS.register(
            "civilian_energy_system",
            () -> new EnergySystemItem(ModEnergySystems.CIVILIAN)
    );

    public static final Supplier<Item> MILITARY_ENERGY_SYSTEM = ITEMS.register(
            "military_energy_system",
            () -> new EnergySystemItem(ModEnergySystems.MILITARY)
    );

    public static final Supplier<Item> ENGINEERING_ENERGY_SYSTEM = ITEMS.register(
            "engineering_energy_system",
            () -> new EnergySystemItem(ModEnergySystems.ENGINEERING)
    );

    public static final Supplier<Item> EXPERIMENTAL_ENERGY_SYSTEM = ITEMS.register(
            "experimental_energy_system",
            () -> new EnergySystemItem(ModEnergySystems.EXPERIMENTAL)
    );

    // Контроллер
    public static final Supplier<Item> CIVILIAN_CONTROLLER = ITEMS.register(
            "civilian_controller",
            () -> new ControllerItem(ModControllers.CIVILIAN)
    );

    public static final Supplier<Item> MILITARY_CONTROLLER = ITEMS.register(
            "military_controller",
            () -> new ControllerItem(ModControllers.MILITARY)
    );

    public static final Supplier<Item> ENGINEERING_CONTROLLER = ITEMS.register(
            "engineering_controller",
            () -> new ControllerItem(ModControllers.ENGINEERING)
    );

    public static final Supplier<Item> EXPERIMENTAL_CONTROLLER = ITEMS.register(
            "experimental_controller",
            () -> new ControllerItem(ModControllers.EXPERIMENTAL)
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
