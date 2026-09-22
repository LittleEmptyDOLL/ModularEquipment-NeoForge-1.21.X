package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class AttributeOperations {
    private AttributeOperations() {}

    public static Map<AttributeKey, Double> calculate(
            Player player,
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        return calculate(player, data, null, poweredModules);
    }

    public static Map<AttributeKey, Double> calculate(
            Player player,
            ExoskeletonData data,
            Integer matrixSlot,
            Set<InstalledModuleReference> poweredModules
    ) {
        Map<AttributeKey, Double> values = new HashMap<>();

        Iterable<Integer> slots = matrixSlot == null
                ? () -> ExoskeletonState.activeMatrixSlots(data).iterator()
                : java.util.List.of(matrixSlot);

        for (int slot : slots) {
            var matrix = data.matrices().get(slot).matrix().orElse(null);
            if (matrix == null) continue;

            for (int moduleIndex = 0; moduleIndex < matrix.modules().size(); moduleIndex++) {
                InstalledModule module = matrix.modules().get(moduleIndex);

                if (!FrameOperations.isModuleSupported(data, module)) continue;

                var definition = ModModules.getDefinition(module.id());

                if (poweredModules != null
                        && definition.energy()
                        .filter(energy -> energy.consumption() > 0)
                        .isPresent()
                        && !poweredModules.contains(new InstalledModuleReference(slot, moduleIndex))) {
                    continue;
                }

                definition.attributes().ifPresent(attributes ->
                        addAttributes(values, attributes, definition, data)
                );

                definition.conditionalAttributes().ifPresent(conditional ->
                        addConditionalAttributes(player, values, conditional, definition, data)
                );
            }
        }

        return values;
    }

    private static void addAttributes(
            Map<AttributeKey, Double> values,
            AttributeProperties attributes,
            ModuleDefinition definition,
            ExoskeletonData data
    ) {
        attributes.attributes().forEach((attributeId, modifier) -> {
            AttributeKey key = new AttributeKey(attributeId, modifier.operation());
            double efficiency = com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations.calculateModuleEfficiency(definition, data.temperature());
            values.merge(key, modifier.amount() * efficiency, Double::sum);
        });
    }

    private static void addConditionalAttributes(
            Player player,
            Map<AttributeKey, Double> values,
            ConditionalAttributeProperties conditional,
            ModuleDefinition definition,
            ExoskeletonData data
    ) {
        if (conditional.conditions().stream().allMatch(
                condition -> AttributeConditionOperations.matches(player, condition)
        )) {
            addAttributes(values, conditional.attributes(), definition, data);
        }
    }

    public record AttributeKey(
            ResourceLocation attributeId,
            AttributeModifier.Operation operation
    ) {}
}
