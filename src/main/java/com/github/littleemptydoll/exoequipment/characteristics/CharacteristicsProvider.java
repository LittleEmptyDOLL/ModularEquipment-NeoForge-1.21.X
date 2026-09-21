package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.energy.EnergyState;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixState;
import com.github.littleemptydoll.exoequipment.module.AttributeOperations;
import com.github.littleemptydoll.exoequipment.module.BodyDamageProtectionOperations;
import com.github.littleemptydoll.exoequipment.module.BodyPart;
import com.github.littleemptydoll.exoequipment.module.DefenseOperations;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.github.littleemptydoll.exoequipment.module.RegenerationOperations;
import com.github.littleemptydoll.exoequipment.module.ShieldOperations;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CharacteristicsProvider {
    private CharacteristicsProvider() {}

    public static List<Characteristic> collect(CharacteristicsContext context) {
        List<Characteristic> result = new ArrayList<>();

        addEnergy(result, context);
        addThermal(result, context);
        addDefense(result, context);
        addShield(result, context);
        addRegeneration(result, context);
        addAttributes(result, context);

        result.sort(
                Comparator.comparing(Characteristic::category)
                        .thenComparing(Characteristic::key)
        );

        return List.copyOf(result);
    }

    private static void addEnergy(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        if (context.isMatrixScope()) {
            MatrixData matrix = getSelectedMatrix(context);
            if (matrix == null) {
                return;
            }

            MatrixState state = calculateMatrixState(context, matrix);
            int storedEnergy = MatrixOperations.calculateStoredEnergy(
                    matrix,
                    module -> FrameOperations.isModuleSupported(context.data(), module),
                    context.data().temperature()
            );

            addEnergyValues(
                    result,
                    state.energyConsumption(),
                    state.energyGeneration(),
                    state.energyStorageCapacity(),
                    state.energyStorageInput(),
                    state.energyStorageOutput(),
                    storedEnergy
            );
            return;
        }

        EnergyState state = EnergyState.calculate(context.data());

        addEnergyValues(
                result,
                state.consumption(),
                state.generation(),
                state.storageCapacity(),
                state.storageInput(),
                state.storageOutput(),
                state.storedEnergy()
        );

        result.add(new Characteristic(
                CharacteristicCategory.ENERGY,
                "max_input",
                CharacteristicType.STATIC,
                state.maxInput()
        ));
        result.add(new Characteristic(
                CharacteristicCategory.ENERGY,
                "max_output",
                CharacteristicType.STATIC,
                state.maxOutput()
        ));
    }

    private static void addEnergyValues(
            List<Characteristic> result,
            int consumption,
            int generation,
            int storageCapacity,
            int storageInput,
            int storageOutput,
            int storedEnergy
    ) {
        result.add(new Characteristic(
                CharacteristicCategory.ENERGY,
                "consumption",
                CharacteristicType.CURRENT,
                consumption
        ));
        result.add(new Characteristic(
                CharacteristicCategory.ENERGY,
                "generation",
                CharacteristicType.CURRENT,
                generation
        ));
        result.add(new Characteristic(
                CharacteristicCategory.ENERGY,
                "storage_capacity",
                CharacteristicType.CURRENT,
                storageCapacity
        ));
        result.add(new Characteristic(
                CharacteristicCategory.ENERGY,
                "storage_input",
                CharacteristicType.CURRENT,
                storageInput
        ));
        result.add(new Characteristic(
                CharacteristicCategory.ENERGY,
                "storage_output",
                CharacteristicType.CURRENT,
                storageOutput
        ));
        result.add(new Characteristic(
                CharacteristicCategory.ENERGY,
                "stored_energy",
                CharacteristicType.CURRENT,
                storedEnergy
        ));
    }

    private static void addThermal(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        MatrixState state;

        if (context.isMatrixScope()) {
            MatrixData matrix = getSelectedMatrix(context);
            if (matrix == null) {
                return;
            }
            state = calculateMatrixState(context, matrix);
        } else {
            state = ExoskeletonState.calculateState(context.data());
        }

        result.add(new Characteristic(
                CharacteristicCategory.THERMAL,
                "heat_generation",
                CharacteristicType.CURRENT,
                state.heatGeneration()
        ));
        result.add(new Characteristic(
                CharacteristicCategory.THERMAL,
                "cooling",
                CharacteristicType.CURRENT,
                state.cooling()
        ));
        result.add(new Characteristic(
                CharacteristicCategory.THERMAL,
                "thermal_balance",
                CharacteristicType.CURRENT,
                state.thermalBalance()
        ));
    }

    private static void addDefense(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        Set<ResourceLocation> damageTypes = context.isMatrixScope()
                ? collectDamageTypes(context.data(), context.matrixSlot())
                : collectDamageTypes(context.data());

        for (ResourceLocation damageType : damageTypes) {
            double multiplier = context.isMatrixScope()
                    ? DefenseOperations.calculateDamageMultiplier(
                            context.data(), context.matrixSlot(), damageType, context.poweredModules())
                    : DefenseOperations.calculateDamageMultiplier(
                            context.data(), damageType, context.poweredModules());

            double reduction = 1.0D - multiplier;

            if (reduction <= 0.0D) {
                continue;
            }

            result.add(new Characteristic(
                    CharacteristicCategory.DEFENSE,
                    "damage_reduction." + damageType,
                    CharacteristicType.CURRENT,
                    reduction
            ));
        }

        for (BodyPart bodyPart : BodyPart.values()) {
            double chance = context.isMatrixScope()
                    ? calculateBodyDamageChance(context, bodyPart)
                    : BodyDamageProtectionOperations.calculateChance(
                            context.data(), bodyPart, context.poweredModules());
            double multiplier = context.isMatrixScope()
                    ? calculateBodyDamageMultiplier(context, bodyPart)
                    : BodyDamageProtectionOperations.calculateDamageMultiplier(
                            context.data(), bodyPart, context.poweredModules());
            double reduction = 1.0D - multiplier;

            if (chance > 0.0D) {
                result.add(new Characteristic(
                        CharacteristicCategory.DEFENSE,
                        "body." + bodyPart.name().toLowerCase() + ".chance",
                        CharacteristicType.CURRENT,
                        chance
                ));
            }

            if (reduction > 0.0D) {
                result.add(new Characteristic(
                        CharacteristicCategory.DEFENSE,
                        "body." + bodyPart.name().toLowerCase() + ".reduction",
                        CharacteristicType.CURRENT,
                        reduction
                ));
            }
        }
    }

    private static Set<ResourceLocation> collectDamageTypes(
            com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData data
    ) {
        Set<ResourceLocation> result = new HashSet<>();

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            collectDamageTypes(data, slot, result);
        }

        return result;
    }

    private static Set<ResourceLocation> collectDamageTypes(
            com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData data,
            int matrixSlot
    ) {
        Set<ResourceLocation> result = new HashSet<>();
        collectDamageTypes(data, matrixSlot, result);
        return result;
    }

    private static void collectDamageTypes(
            com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData data,
            int slot,
            Set<ResourceLocation> result
    ) {
        MatrixData matrix = data.matrices()
                .get(slot)
                .matrix()
                .orElse(null);

        if (matrix == null) {
            return;
        }

        matrix.modules().forEach(module ->
                ModModules.getDefinition(module.id())
                        .damageReduction()
                        .ifPresent(properties ->
                                result.addAll(properties.reductions().keySet())
                        )
        );
    }

    private static double calculateBodyDamageChance(
            CharacteristicsContext context,
            BodyPart bodyPart
    ) {
        MatrixData matrix = getSelectedMatrix(context);
        if (matrix == null) return 0.0D;

        double remainingChance = 1.0D;
        for (int moduleIndex = 0; moduleIndex < matrix.modules().size(); moduleIndex++) {
            var module = matrix.modules().get(moduleIndex);
            if (!FrameOperations.isModuleSupported(context.data(), module)) continue;
            var reference = new InstalledModuleReference(context.matrixSlot(), moduleIndex);
            var definition = ModModules.getDefinition(module.id());
            if (definition.energy().filter(energy -> energy.consumption() > 0).isPresent()
                    && !context.poweredModules().contains(reference)) continue;
            var properties = definition.bodyDamageProtection().orElse(null);
            if (properties == null || !BodyPart.applies(properties.bodyParts(), bodyPart)) continue;
            remainingChance *= 1.0D - properties.chance();
        }
        return Math.max(0.0D, Math.min(1.0D, 1.0D - remainingChance));
    }

    private static double calculateBodyDamageMultiplier(
            CharacteristicsContext context,
            BodyPart bodyPart
    ) {
        MatrixData matrix = getSelectedMatrix(context);
        if (matrix == null) return 1.0D;

        double multiplier = 1.0D;
        for (int moduleIndex = 0; moduleIndex < matrix.modules().size(); moduleIndex++) {
            var module = matrix.modules().get(moduleIndex);
            if (!FrameOperations.isModuleSupported(context.data(), module)) continue;
            var reference = new InstalledModuleReference(context.matrixSlot(), moduleIndex);
            var definition = ModModules.getDefinition(module.id());
            if (definition.energy().filter(energy -> energy.consumption() > 0).isPresent()
                    && !context.poweredModules().contains(reference)) continue;
            var properties = definition.bodyDamageProtection().orElse(null);
            if (properties == null || !BodyPart.applies(properties.bodyParts(), bodyPart)) continue;
            multiplier *= 1.0D - properties.damageReduction();
        }
        return Math.max(0.0D, multiplier);
    }

    private static void addShield(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        ShieldOperations.ShieldStatus status = context.isMatrixScope()
                ? ShieldOperations.getStatus(context.data(), context.matrixSlot())
                : ShieldOperations.getStatus(context.data());

        if (!status.hasShields()) {
            return;
        }

        result.add(new Characteristic(
                CharacteristicCategory.SHIELD,
                "current_energy",
                CharacteristicType.CURRENT,
                status.currentEnergy()
        ));
        result.add(new Characteristic(
                CharacteristicCategory.SHIELD,
                "capacity",
                CharacteristicType.CURRENT,
                status.capacity()
        ));
    }

    private static void addRegeneration(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        double healthPerSecond = context.isMatrixScope()
                ? RegenerationOperations.calculateHealthPerSecond(
                        context.data(), context.matrixSlot(), context.poweredModules())
                : RegenerationOperations.calculateHealthPerSecond(
                        context.data(), context.poweredModules());

        if (healthPerSecond > 0.0D) {
            result.add(new Characteristic(
                    CharacteristicCategory.REGENERATION,
                    "health_per_second",
                    CharacteristicType.CURRENT,
                    healthPerSecond
            ));
        }
    }

    private static void addAttributes(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        if (context.player() == null || context.isMatrixScope()) {
            return;
        }

        AttributeOperations.calculate(
                context.player(),
                context.data(),
                context.poweredModules()
        ).forEach((key, value) -> {
            String operation = switch (key.operation()) {
                case ADD_VALUE -> "add_value";
                case ADD_MULTIPLIED_BASE -> "add_multiplied_base";
                case ADD_MULTIPLIED_TOTAL -> "add_multiplied_total";
            };

            result.add(new Characteristic(
                    CharacteristicCategory.ATTRIBUTES,
                    "attribute." + key.attributeId() + "." + operation,
                    CharacteristicType.CURRENT,
                    value
            ));
        });
    }

    private static MatrixData getSelectedMatrix(CharacteristicsContext context) {
        int slot = context.matrixSlot();
        return context.data().matrices().get(slot).matrix().orElse(null);
    }

    private static MatrixState calculateMatrixState(
            CharacteristicsContext context,
            MatrixData matrix
    ) {
        return MatrixOperations.calculateState(
                matrix,
                module -> FrameOperations.isModuleSupported(context.data(), module),
                context.data().temperature()
        );
    }
}
