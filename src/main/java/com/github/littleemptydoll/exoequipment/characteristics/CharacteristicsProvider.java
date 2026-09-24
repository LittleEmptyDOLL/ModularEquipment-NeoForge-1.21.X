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
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.module.JetpackProperties;
import com.github.littleemptydoll.exoequipment.module.ElytraBoostProperties;
import com.github.littleemptydoll.exoequipment.module.RevivalProperties;
import com.github.littleemptydoll.exoequipment.module.EntityDetectionProperties;
import com.github.littleemptydoll.exoequipment.module.PickupMagnetProperties;
import com.github.littleemptydoll.exoequipment.module.CloakingProperties;
import com.github.littleemptydoll.exoequipment.module.EmergencyShieldProperties;
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
        addMobility(result, context);
        addSurvival(result, context);
        addSensors(result, context);
        addUtility(result, context);

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

        if (context.isMatrixScope()) {
            double universalMultiplier = DefenseOperations.calculateDamageMultiplier(
                    context.data(), context.matrixSlot(), null, effectivePoweredModules(context));
            double universalReduction = 1.0D - universalMultiplier;
            if (universalReduction > 0.0D) {
                result.add(new Characteristic(
                        CharacteristicCategory.DEFENSE,
                        "damage_reduction.default",
                        CharacteristicType.CURRENT,
                        universalReduction
                ));
            }
        } else {
            double universalMultiplier = DefenseOperations.calculateDamageMultiplier(
                    context.data(), null, effectivePoweredModules(context));
            double universalReduction = 1.0D - universalMultiplier;
            if (universalReduction > 0.0D) {
                result.add(new Characteristic(
                        CharacteristicCategory.DEFENSE,
                        "damage_reduction.default",
                        CharacteristicType.CURRENT,
                        universalReduction
                ));
            }
        }

        for (ResourceLocation damageType : damageTypes) {
            double multiplier = context.isMatrixScope()
                    ? DefenseOperations.calculateDamageMultiplier(
                            context.data(), context.matrixSlot(), damageType, effectivePoweredModules(context))
                    : DefenseOperations.calculateDamageMultiplier(
                            context.data(), damageType, effectivePoweredModules(context));

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
                    ? BodyDamageProtectionOperations.calculateChance(
                            context.data(), context.matrixSlot(), bodyPart, effectivePoweredModules(context))
                    : BodyDamageProtectionOperations.calculateChance(
                            context.data(), bodyPart, effectivePoweredModules(context));
            double multiplier = context.isMatrixScope()
                    ? BodyDamageProtectionOperations.calculateDamageMultiplier(
                            context.data(), context.matrixSlot(), bodyPart, effectivePoweredModules(context))
                    : BodyDamageProtectionOperations.calculateDamageMultiplier(
                            context.data(), bodyPart, effectivePoweredModules(context));
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
                        )
        );
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
                        context.data(), context.matrixSlot(), effectivePoweredModules(context))
                : RegenerationOperations.calculateHealthPerSecond(
                        context.data(), effectivePoweredModules(context));

        if (healthPerSecond > 0.0D) {
            result.add(new Characteristic(
                    CharacteristicCategory.REGENERATION,
                    "health_per_second",
                    CharacteristicType.CURRENT,
                    healthPerSecond
            ));
        }
    }

    private static void addMobility(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        int matrixStart = context.isMatrixScope() ? context.matrixSlot() : 0;
        int matrixEnd = context.isMatrixScope()
                ? context.matrixSlot() + 1
                : context.data().matrices().size();

        boolean flight = false;
        double jetpackThrust = 0.0D;
        double jetpackSpeed = 0.0D;
        double elytraAcceleration = 0.0D;
        double elytraMaxSpeed = 0.0D;
        double blinkDistance = 0.0D;
        int blinkEnergy = 0;
        int blinkCooldown = Integer.MAX_VALUE;

        for (int slot = matrixStart; slot < matrixEnd; slot++) {
            MatrixData matrix = context.data().matrices().get(slot).matrix().orElse(null);
            if (matrix == null) continue;

            for (InstalledModule module : matrix.modules()) {
                if (!FrameOperations.isModuleSupported(context.data(), module)) continue;

                var definition = ModModules.getDefinition(module.id());

                flight |= definition.flight().isPresent();

                if (definition.jetpack().isPresent()) {
                    JetpackProperties jetpack = definition.jetpack().get();
                    jetpackThrust = Math.max(jetpackThrust, jetpack.verticalThrust());
                    jetpackSpeed = Math.max(jetpackSpeed, jetpack.horizontalSpeed());

                    if (jetpack.elytra().isPresent()) {
                        ElytraBoostProperties elytra = jetpack.elytra().get();
                        elytraAcceleration = Math.max(
                                elytraAcceleration,
                                elytra.acceleration()
                        );
                        elytraMaxSpeed = Math.max(
                                elytraMaxSpeed,
                                elytra.maxSpeed()
                        );
                    }
                }

                if (definition.blink().isPresent()) {
                    var blink = definition.blink().get();
                    blinkDistance = Math.max(blinkDistance, blink.distance());
                    blinkEnergy = Math.max(blinkEnergy, blink.activationEnergy());
                    blinkCooldown = Math.min(blinkCooldown, blink.cooldown());
                }
            }
        }

        if (flight) {
            result.add(new Characteristic(
                    CharacteristicCategory.MOBILITY,
                    "flight",
                    CharacteristicType.STATIC,
                    1.0D
            ));
        }

        if (jetpackThrust > 0.0D) {
            result.add(new Characteristic(
                    CharacteristicCategory.MOBILITY,
                    "jetpack.vertical_thrust",
                    CharacteristicType.STATIC,
                    jetpackThrust
            ));
            result.add(new Characteristic(
                    CharacteristicCategory.MOBILITY,
                    "jetpack.horizontal_speed",
                    CharacteristicType.STATIC,
                    jetpackSpeed
            ));
        }

        if (elytraAcceleration > 0.0D) {
            result.add(new Characteristic(
                    CharacteristicCategory.MOBILITY,
                    "elytra.acceleration",
                    CharacteristicType.STATIC,
                    elytraAcceleration
            ));
            result.add(new Characteristic(
                    CharacteristicCategory.MOBILITY,
                    "elytra.max_speed",
                    CharacteristicType.STATIC,
                    elytraMaxSpeed
            ));
        }

        if (blinkDistance > 0.0D) {
            result.add(new Characteristic(
                    CharacteristicCategory.MOBILITY,
                    "blink.distance",
                    CharacteristicType.STATIC,
                    blinkDistance
            ));
            result.add(new Characteristic(
                    CharacteristicCategory.MOBILITY,
                    "blink.activation_energy",
                    CharacteristicType.STATIC,
                    blinkEnergy
            ));
            result.add(new Characteristic(
                    CharacteristicCategory.MOBILITY,
                    "blink.cooldown",
                    CharacteristicType.STATIC,
                    blinkCooldown
            ));
        }
    }

    private static void addSurvival(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        double hunger = context.isMatrixScope()
                ? 0.0D
                : com.github.littleemptydoll.exoequipment.module.HungerOperations.calculateExhaustionReduction(
                        context.data(), effectivePoweredModules(context));
        if (hunger > 0.0D) {
            result.add(new Characteristic(CharacteristicCategory.SURVIVAL, "hunger.exhaustion_reduction", CharacteristicType.CURRENT, hunger));
        }

        double revivalRestore = 0.0D;
        int revivalCooldown = Integer.MAX_VALUE;
        double thirst = 0.0D;

        for (int slot = context.isMatrixScope() ? context.matrixSlot() : 0;
             slot < (context.isMatrixScope() ? context.matrixSlot() + 1 : context.data().matrices().size()); slot++) {
            MatrixData matrix = context.data().matrices().get(slot).matrix().orElse(null);
            if (matrix == null) continue;
            for (InstalledModule module : matrix.modules()) {
                if (!FrameOperations.isModuleSupported(context.data(), module)) continue;
                var definition = ModModules.getDefinition(module.id());
                if (definition.revival().isPresent()) {
                    RevivalProperties p = definition.revival().get();
                    revivalRestore = Math.max(revivalRestore, p.restoreHealth());
                    revivalCooldown = Math.min(revivalCooldown, p.cooldown());
                }
                if (definition.thirst().isPresent()) thirst = Math.max(thirst, definition.thirst().get().exhaustionReduction());
            }
        }

        if (revivalRestore > 0.0D) {
            result.add(new Characteristic(CharacteristicCategory.SURVIVAL, "revival.restore_health", CharacteristicType.STATIC, revivalRestore));
            result.add(new Characteristic(CharacteristicCategory.SURVIVAL, "revival.cooldown", CharacteristicType.STATIC, revivalCooldown));
        }
        if (thirst > 0.0D) result.add(new Characteristic(CharacteristicCategory.SURVIVAL, "thirst.exhaustion_reduction", CharacteristicType.STATIC, thirst));
    }

    private static void addSensors(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        double entityRange = 0.0D;
        boolean players = false;
        boolean mobs = false;
        boolean hostile = false;
        double blockRange = 0.0D;

        for (int slot = context.isMatrixScope() ? context.matrixSlot() : 0;
             slot < (context.isMatrixScope() ? context.matrixSlot() + 1 : context.data().matrices().size()); slot++) {
            MatrixData matrix = context.data().matrices().get(slot).matrix().orElse(null);
            if (matrix == null) continue;
            for (int index = 0; index < matrix.modules().size(); index++) {
                InstalledModule module = matrix.modules().get(index);
                if (!FrameOperations.isModuleSupported(context.data(), module)) continue;
                var definition = ModModules.getDefinition(module.id());
                if (definition.entityDetection().isPresent()) {
                    EntityDetectionProperties p = definition.entityDetection().get();
                    entityRange = Math.max(entityRange, p.range());
                    players |= p.players(); mobs |= p.mobs(); hostile |= p.hostile();
                }
                if (definition.blockScanner().isPresent()) {
                    blockRange = Math.max(blockRange, definition.blockScanner().get().range());
                }
            }
        }

        if (entityRange > 0.0D) {
            result.add(new Characteristic(CharacteristicCategory.SENSOR, "entity_detection.range", CharacteristicType.STATIC, entityRange));
            result.add(new Characteristic(CharacteristicCategory.SENSOR, "entity_detection.players", CharacteristicType.STATIC, players ? 1.0D : 0.0D));
            result.add(new Characteristic(CharacteristicCategory.SENSOR, "entity_detection.mobs", CharacteristicType.STATIC, mobs ? 1.0D : 0.0D));
            result.add(new Characteristic(CharacteristicCategory.SENSOR, "entity_detection.hostile", CharacteristicType.STATIC, hostile ? 1.0D : 0.0D));
        }
        if (blockRange > 0.0D) result.add(new Characteristic(CharacteristicCategory.SENSOR, "block_scanner.range", CharacteristicType.STATIC, blockRange));
    }

    private static void addUtility(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        var magnet = context.isMatrixScope()
                ? java.util.Optional.<PickupMagnetProperties>empty()
                : com.github.littleemptydoll.exoequipment.module.PickupMagnetOperations.findProperties(
                        context.data(), effectivePoweredModules(context));
        magnet.ifPresent(p -> {
            result.add(new Characteristic(CharacteristicCategory.UTILITY, "pickup_magnet.radius", CharacteristicType.CURRENT, p.radius()));
            result.add(new Characteristic(CharacteristicCategory.UTILITY, "pickup_magnet.items", CharacteristicType.STATE, p.mode().acceptsItems() ? 1.0D : 0.0D));
            result.add(new Characteristic(CharacteristicCategory.UTILITY, "pickup_magnet.experience", CharacteristicType.STATE, p.mode().acceptsExperience() ? 1.0D : 0.0D));
        });

        double cloakConsumption = 0.0D;
        boolean cloaking = false;
        double emergencyRestore = 0.0D;
        int emergencyCooldown = Integer.MAX_VALUE;
        for (int slot = context.isMatrixScope() ? context.matrixSlot() : 0;
             slot < (context.isMatrixScope() ? context.matrixSlot() + 1 : context.data().matrices().size()); slot++) {
            MatrixData matrix = context.data().matrices().get(slot).matrix().orElse(null);
            if (matrix == null) continue;
            for (InstalledModule module : matrix.modules()) {
                if (!FrameOperations.isModuleSupported(context.data(), module)) continue;
                var definition = ModModules.getDefinition(module.id());
                if (definition.cloaking().isPresent()) {
                    CloakingProperties p = definition.cloaking().get();
                    cloakConsumption = Math.max(cloakConsumption, p.activeConsumption());
                    cloaking |= module.active();
                }
                if (definition.emergencyShield().isPresent()) {
                    EmergencyShieldProperties p = definition.emergencyShield().get();
                    emergencyRestore = Math.max(emergencyRestore, p.restore());
                    emergencyCooldown = Math.min(emergencyCooldown, p.cooldown());
                }
            }
        }
        if (cloakConsumption > 0.0D || cloaking) {
            result.add(new Characteristic(CharacteristicCategory.UTILITY, "cloaking.active_consumption", CharacteristicType.STATIC, cloakConsumption));
            result.add(new Characteristic(CharacteristicCategory.UTILITY, "cloaking.active", CharacteristicType.STATE, cloaking ? 1.0D : 0.0D));
        }
        if (emergencyRestore > 0.0D) {
            result.add(new Characteristic(CharacteristicCategory.UTILITY, "emergency_shield.restore", CharacteristicType.STATIC, emergencyRestore));
            result.add(new Characteristic(CharacteristicCategory.UTILITY, "emergency_shield.cooldown", CharacteristicType.STATIC, emergencyCooldown));
        }
    }

    private static void addAttributes(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        if (context.player() == null) {
            return;
        }

        AttributeOperations.calculate(
                context.player(),
                context.data(),
                context.isMatrixScope() ? context.matrixSlot() : null,
                effectivePoweredModules(context)
        ).forEach((key, value) -> {
            String operation = switch (key.operation()) {
                case ADD_VALUE -> "add_value";
                case ADD_MULTIPLIED_BASE -> "add_multiplied_base";
                case ADD_MULTIPLIED_TOTAL -> "add_multiplied_total";
            };

            CharacteristicCategory category = attributeCategory(key.attributeId());
            result.add(new Characteristic(
                    category,
                    "attribute." + key.attributeId() + "." + operation,
                    CharacteristicType.CURRENT,
                    value
            ));
        });
    }

    private static CharacteristicCategory attributeCategory(ResourceLocation attributeId) {
        return switch (attributeId.toString()) {
            case "minecraft:generic.movement_speed" -> CharacteristicCategory.MOBILITY;
            case "minecraft:generic.attack_damage",
                 "minecraft:generic.attack_speed" -> CharacteristicCategory.COMBAT;
            case "minecraft:generic.armor",
                 "minecraft:generic.armor_toughness" -> CharacteristicCategory.DEFENSE;
            case "minecraft:generic.max_health",
                 "minecraft:generic.knockback_resistance" -> CharacteristicCategory.SURVIVAL;
            default -> CharacteristicCategory.ATTRIBUTES;
        };
    }


    private static Set<InstalledModuleReference> effectivePoweredModules(
            CharacteristicsContext context
    ) {
        if (!context.isMatrixScope()) {
            return context.poweredModules();
        }

        Set<InstalledModuleReference> result = new HashSet<>();

        int slot = context.matrixSlot();
        if (slot < 0 || slot >= context.data().matrices().size()) {
            return result;
        }

        MatrixData matrix = context.data().matrices().get(slot).matrix().orElse(null);
        if (matrix == null) {
            return result;
        }

        for (int index = 0; index < matrix.modules().size(); index++) {
            result.add(new InstalledModuleReference(slot, index));
        }

        return result;
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
