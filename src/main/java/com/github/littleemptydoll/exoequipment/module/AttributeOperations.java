package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AttributeOperations {
    private AttributeOperations() {}

    public static Map<AttributeKey, Double> calculate(
            Player player,
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        return calculate(
                player,
                data,
                null,
                poweredModules
        );
    }

    public static Map<AttributeKey, Double> calculate(
            Player player,
            ExoskeletonData data,
            Integer matrixSlot,
            Set<InstalledModuleReference> poweredModules
    ) {
        Map<AttributeKey, Double> values = new HashMap<>();

        List<ExoskeletonModules.ActiveModule> modules =
                matrixSlot == null
                        ? ExoskeletonModules.activeSupported(data)
                        : ExoskeletonModules.supportedInMatrix(
                                data,
                                matrixSlot
                        );

        for (ExoskeletonModules.ActiveModule activeModule : modules) {
            if (!ExoskeletonModules.isPowered(
                    activeModule,
                    poweredModules
            )) {
                continue;
            }

            ModuleDefinition definition = activeModule.definition();

            definition.attributes().ifPresent(attributes ->
                    addAttributes(
                            values,
                            attributes,
                            definition,
                            data
                    )
            );

            definition.conditionalAttributes().ifPresent(conditional ->
                    addConditionalAttributes(
                            player,
                            values,
                            conditional,
                            definition,
                            data
                    )
            );
        }

        return values;
    }

    private static void addAttributes(
            Map<AttributeKey, Double> values,
            AttributeProperties attributes,
            ModuleDefinition definition,
            ExoskeletonData data
    ) {
        double efficiency =
                TemperatureOperations.calculateModuleEfficiency(
                        definition,
                        data.temperature()
                );

        attributes.attributes().forEach((attributeId, modifier) -> {
            AttributeKey key = new AttributeKey(
                    attributeId,
                    modifier.operation()
            );

            values.merge(
                    key,
                    modifier.amount() * efficiency,
                    Double::sum
            );
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
                condition ->
                        AttributeConditionOperations.matches(
                                player,
                                condition
                        )
        )) {
            addAttributes(
                    values,
                    conditional.attributes(),
                    definition,
                    data
            );
        }
    }

    public record AttributeKey(
            ResourceLocation attributeId,
            AttributeModifier.Operation operation
    ) {}
}
