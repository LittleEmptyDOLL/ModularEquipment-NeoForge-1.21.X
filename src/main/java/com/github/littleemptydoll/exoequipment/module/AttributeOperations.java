package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class AttributeOperations {
    private AttributeOperations() {}

    public static Map<AttributeKey, Double> calculate(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        Map<AttributeKey, Double> values = new HashMap<>();

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
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
                        attributes.modifiers().forEach((attributeId, modifier) -> {
                            AttributeKey key = new AttributeKey(attributeId, modifier.operation());
                            values.merge(key, modifier.amount(), Double::sum);
                        })
                );
            }
        }

        return values;
    }

    public record AttributeKey(
            ResourceLocation attributeId,
            AttributeModifier.Operation operation
    ) {}
}
