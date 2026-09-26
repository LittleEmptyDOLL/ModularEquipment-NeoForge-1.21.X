package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.client.TooltipHelper;
import com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.registry.EquipmentItem;
import com.github.littleemptydoll.exoequipment.util.AttributeNameUtils;
import com.github.littleemptydoll.exoequipment.util.NameUtils;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class ModuleItem extends EquipmentItem<ModuleDefinition> {

    public ModuleItem(
            DeferredHolder<ModuleDefinition, ModuleDefinition> definition,
            Properties properties
    ) {
        super(definition, properties);
    }

    public static ModuleItem get(ItemStack stack) {
        if (!(stack.getItem() instanceof ModuleItem moduleItem)) {
            throw new IllegalArgumentException("ItemStack is not an module");
        }
        return moduleItem;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        appendHoverTextWithTemperature(stack, tooltip, Double.NaN);
    }

    public void appendHoverTextWithTemperature(
            ItemStack stack,
            List<Component> tooltip,
            double temperature
    ) {
        ModuleDefinition definition = getDefinition();
        double efficiency = definition.temperature().isPresent() && !Double.isNaN(temperature)
                ? TemperatureOperations.calculateModuleEfficiency(definition, temperature)
                : 1.0D;

        appendEquipmentTooltip(tooltip);
        tooltip.add(TooltipHelper.category(definition.category()));
        tooltip.add(TooltipHelper.size(definition.size().width(), definition.size().height()));

        if (definition.energy().isPresent()) {
            var energy = definition.energy().get();
            tooltip.add(TooltipHelper.energyConsumption(applyEfficiency(energy.consumption(), efficiency), efficiency));
            tooltip.add(TooltipHelper.property("priority", energy.priority()));
        }

        if (definition.generation().isPresent()) {
            tooltip.add(TooltipHelper.energyGeneration(
                    applyEfficiency(definition.generation().get().generation(), efficiency), efficiency
            ));
        }

        if (definition.storage().isPresent()) {
            var storage = definition.storage().get();
            int stored = Math.min(
                    stack.getOrDefault(ModDataComponents.MODULE_STORED_ENERGY.get(), 0),
                    storage.capacity()
            );
            tooltip.add(TooltipHelper.input(applyEfficiency(storage.maxInput(), efficiency), efficiency));
            tooltip.add(TooltipHelper.output(applyEfficiency(storage.maxOutput(), efficiency), efficiency));
            tooltip.add(TooltipHelper.capacity(
                    stored,
                    applyEfficiency(storage.capacity(), efficiency),
                    efficiency
            ));
        }

        if (definition.thermal().isPresent()) {
            var thermal = definition.thermal().get();
            if (thermal.cooling() > 0) {
                tooltip.add(TooltipHelper.cooling(applyEfficiency((int) thermal.cooling(), efficiency), efficiency));
            }
            if (thermal.heatGeneration() > 0) {
                tooltip.add(TooltipHelper.heatGeneration(applyEfficiency((int) thermal.heatGeneration(), efficiency), efficiency));
            }
        }

        definition.damageReduction().ifPresent(value -> {
            value.defaultReduction().ifPresent(reduction ->
                    tooltip.add(TooltipHelper.property(
                            "damage all",
                            String.format(java.util.Locale.ROOT, "%.1f%%", reduction * efficiency * 100.0D)
                    ))
            );
            value.reductions().forEach((id, reduction) ->
                    tooltip.add(TooltipHelper.property(
                            "damage " + id.getPath(),
                            String.format(java.util.Locale.ROOT, "%.1f%%", reduction * efficiency * 100.0D)
                    ))
            );
            value.tagReductions().forEach((id, reduction) ->
                    tooltip.add(TooltipHelper.property(
                            "damage #" + id,
                            String.format(java.util.Locale.ROOT, "%.1f%%", reduction * efficiency * 100.0D)
                    ))
            );
        });

        definition.shield().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("shield capacity", applyEfficiency(value.capacity(), efficiency)));
            tooltip.add(TooltipHelper.property("shield recharge rate", applyEfficiency(value.rechargeRate(), efficiency)));
            tooltip.add(TooltipHelper.property("shield recharge delay", value.rechargeDelay()));
        });

        definition.emergencyShield().ifPresent(value -> {
            tooltip.add(TooltipHelper.property(
                    "emergency shield restore",
                    String.format(java.util.Locale.ROOT, "%.1f%%", Math.min(1.0D, value.restore() * efficiency) * 100.0D)
            ));
            tooltip.add(TooltipHelper.property("emergency shield cooldown", value.cooldown()));
        });

        definition.cloaking().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("cloaking activation energy", applyEfficiency(value.activationEnergy(), efficiency)));
            tooltip.add(TooltipHelper.property("cloaking active consumption", applyEfficiency(value.activeConsumption(), efficiency)));
            tooltip.add(TooltipHelper.property("cloaking cooldown", value.cooldown()));
        });

        definition.blink().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("blink distance", value.distance() * efficiency));
            tooltip.add(TooltipHelper.property("blink activation energy", applyEfficiency(value.activationEnergy(), efficiency)));
            tooltip.add(TooltipHelper.property("blink cooldown", value.cooldown()));
        });

        definition.flight().ifPresent(value ->
                tooltip.add(TooltipHelper.property("flight active consumption", applyEfficiency(value.activeConsumption(), efficiency)))
        );

        definition.jetpack().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("vertical thrust", value.verticalThrust() * efficiency));
            tooltip.add(TooltipHelper.property("horizontal speed", value.horizontalSpeed() * efficiency));
            tooltip.add(TooltipHelper.property("jetpack energy consumption", applyEfficiency(value.energyConsumption(), efficiency)));
            value.elytra().ifPresent(elytra -> {
                tooltip.add(TooltipHelper.property("elytra acceleration", elytra.acceleration() * efficiency));
                tooltip.add(TooltipHelper.property("elytra max speed", elytra.maxSpeed() * efficiency));
            });
        });

        definition.statusProtection().ifPresent(value ->
                value.protections().forEach((id, protection) ->
                        tooltip.add(TooltipHelper.property(
                                "status " + id.getPath(),
                                String.format(java.util.Locale.ROOT, "%.1f%%", Math.min(1.0D, protection * efficiency) * 100.0D)
                        ))
                )
        );

        definition.entityDetection().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("detection range", value.range() * efficiency));
            tooltip.add(TooltipHelper.property("detect players", value.players()));
            tooltip.add(TooltipHelper.property("detect mobs", value.mobs()));
            tooltip.add(TooltipHelper.property("detect hostile", value.hostile()));
        });

        definition.hunger().ifPresent(value ->
                tooltip.add(TooltipHelper.property(
                        "exhaustion reduction",
                        String.format(java.util.Locale.ROOT, "%.1f%%", Math.min(1.0D, value.exhaustionReduction() * efficiency) * 100.0D)
                ))
        );

        definition.revival().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("revival restore health", value.restoreHealth() * efficiency));
            tooltip.add(TooltipHelper.property("revival cooldown", value.cooldown()));
            tooltip.add(TooltipHelper.property("revival invulnerability", value.invulnerabilityTicks()));
        });

        definition.regeneration().ifPresent(value ->
                tooltip.add(TooltipHelper.property("health per second", value.healthPerSecond() * efficiency))
        );

        definition.attributes().ifPresent(value ->
                value.attributes().forEach((id, modifier) ->
                        tooltip.add(TooltipHelper.property(
                                AttributeNameUtils.getName(id).getString(),
                                modifier.amount() * efficiency + " (" + NameUtils.toDisplayName(modifier.operation().name()) + ")"
                        ))
                )
        );

        definition.conditionalAttributes().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("conditional attributes", value.conditions().stream()
                    .map(condition -> NameUtils.toDisplayName(condition.type().name())
                            + condition.value().map(v -> " = " + String.format(java.util.Locale.ROOT, "%.0f%%", v * 100.0D)).orElse(""))
                    .collect(java.util.stream.Collectors.joining(", "))));
            value.attributes().attributes().forEach((id, modifier) ->
                    tooltip.add(TooltipHelper.property(
                            "conditional attribute " + AttributeNameUtils.getName(id).getString(),
                            modifier.amount() * efficiency + " (" + NameUtils.toDisplayName(modifier.operation().name()) + ")"
                    ))
            );
        });

        definition.pickupMagnet().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("pickup radius", value.radius() * efficiency));
            tooltip.add(TooltipHelper.property("pickup mode", value.mode().name()));
        });

        definition.temperatureModifier().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("temperature", value.temperature()));
            tooltip.add(TooltipHelper.property("heat resistance", value.heatResistance()));
            tooltip.add(TooltipHelper.property("cold resistance", value.coldResistance()));
            tooltip.add(TooltipHelper.property("thermal resistance", value.thermalResistance()));
        });

        definition.temperatureImpact().ifPresent(value ->
                tooltip.add(TooltipHelper.property("temperature impact resistance",
                        String.format(java.util.Locale.ROOT, "%.1f%%", value.resistance() * 100.0D)))
        );

        definition.bodyDamageProtection().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("body damage chance",
                    String.format(java.util.Locale.ROOT, "%.1f%%", Math.min(1.0D, value.chance() * efficiency) * 100.0D)));
            tooltip.add(TooltipHelper.property("body damage reduction",
                    String.format(java.util.Locale.ROOT, "%.1f%%", Math.min(1.0D, value.damageReduction() * efficiency) * 100.0D)));
            tooltip.add(TooltipHelper.property("body parts",
                    value.bodyParts().stream().map(part -> part.name().toLowerCase(java.util.Locale.ROOT))
                            .collect(java.util.stream.Collectors.joining(", "))));
        });

        definition.bodyDamageRegeneration().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("body regeneration", value.healthPerSecond() * efficiency));
            tooltip.add(TooltipHelper.property("body regeneration parts",
                    value.bodyParts().stream().map(part -> part.name().toLowerCase(java.util.Locale.ROOT))
                            .collect(java.util.stream.Collectors.joining(", "))));
        });

        definition.thirst().ifPresent(value ->
                tooltip.add(TooltipHelper.property(
                        "thirst exhaustion reduction",
                        String.format(java.util.Locale.ROOT, "%.1f%%", Math.min(1.0D, value.exhaustionReduction() * efficiency) * 100.0D)
                ))
        );

        definition.blockScanner().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("scanner range", value.range() * efficiency));
            if (!value.blocks().isEmpty()) {
                tooltip.add(TooltipHelper.property("scanner blocks", value.blocks().stream()
                        .map(com.github.littleemptydoll.exoequipment.util.NameUtils::toDisplayName)
                        .collect(java.util.stream.Collectors.joining(", "))));
            }
            if (!value.tags().isEmpty()) {
                tooltip.add(TooltipHelper.property("scanner tags", value.tags().stream()
                        .map(com.github.littleemptydoll.exoequipment.util.NameUtils::toDisplayName)
                        .collect(java.util.stream.Collectors.joining(", "))));
            }
        });

        if (definition.temperature().isPresent()) {
            var temperatureProperties = definition.temperature().get();
            tooltip.add(TooltipHelper.temperature(
                    temperatureProperties.minTemperature(),
                    temperatureProperties.maxTemperature()
            ));
            temperatureProperties.efficiencyFalloff().ifPresent(value ->
                    tooltip.add(TooltipHelper.property("efficiency falloff", value))
            );
            temperatureProperties.bonus().ifPresent(value ->
                    tooltip.add(TooltipHelper.temperature_bonus(
                            value.minTemperature(),
                            value.maxTemperature()
                    ))
            );
        }
    }

    private static int applyEfficiency(int value, double efficiency) {
        return Math.max(0, (int) Math.round(value * efficiency));
    }

    private static double applyEfficiency(double value, double efficiency) {
        return Math.max(0.0D, value * efficiency);
    }
}
