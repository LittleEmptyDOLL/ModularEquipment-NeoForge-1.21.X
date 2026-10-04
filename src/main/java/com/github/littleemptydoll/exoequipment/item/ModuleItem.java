package com.github.littleemptydoll.exoequipment.item;

import com.github.littleemptydoll.exoequipment.client.TooltipHelper;
import com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.registry.EquipmentItem;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.util.AttributeNameUtils;
import com.github.littleemptydoll.exoequipment.util.AttributeValueFormatter;
import com.github.littleemptydoll.exoequipment.util.NameUtils;
import com.github.littleemptydoll.exoequipment.util.NumberFormatter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

        if (!TooltipHelper.isShiftDown()) {
            appendSummary(definition, stack, tooltip, efficiency);
            tooltip.add(Component.translatable("tooltip.exoequipment.more_details"));
            return;
        }

        if (definition.energy().isPresent()) {
            var energy = definition.energy().get();
            if (energy.consumption() > 0) {
                tooltip.add(TooltipHelper.energyConsumption(applyEfficiency(energy.consumption(), efficiency), efficiency));
            }
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
            tooltip.add(TooltipHelper.capacity(
                    stored,
                    applyEfficiency(storage.capacity(), efficiency),
                    efficiency
            ));
            tooltip.add(TooltipHelper.input(applyEfficiency(storage.maxInput(), efficiency), efficiency));
            tooltip.add(TooltipHelper.output(applyEfficiency(storage.maxOutput(), efficiency), efficiency));
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
            value.defaultReduction().ifPresent(reduction -> {
                if (reduction > 0.0D) {
                    tooltip.add(TooltipHelper.property(
                            "all damage reduction",
                            NumberFormatter.percent(reduction * efficiency)
                    ));
                }
            });
            value.reductions().forEach((id, reduction) -> {
                if (reduction > 0.0D) {
                    tooltip.add(TooltipHelper.property(
                            Component.translatable("tooltip.exoequipment.property.damage_reduction_type",
                                    NameUtils.toDisplayName(id.getPath())).getString(),
                            NumberFormatter.percent(reduction * efficiency)
                    ));
                }
            });
            value.tagReductions().forEach((id, reduction) -> {
                if (reduction > 0.0D) {
                    tooltip.add(TooltipHelper.property(
                            Component.translatable("tooltip.exoequipment.property.damage_reduction_type",
                                    damageTagLabel(id)).getString(),
                            NumberFormatter.percent(reduction * efficiency)
                    ));
                }
            });
        });

        definition.shield().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("shield capacity", NumberFormatter.format(applyEfficiency(value.capacity(), efficiency))));
            tooltip.add(TooltipHelper.property("shield recharge rate", NumberFormatter.format(applyEfficiency(value.rechargeRate(), efficiency)) + "/t"));
            tooltip.add(TooltipHelper.property("shield recharge delay", formatTicks(value.rechargeDelay())));
        });

        definition.shieldProtection().ifPresent(value ->
                tooltip.add(TooltipHelper.property(
                        "shield protection transfer",
                        NumberFormatter.percent(Math.min(1.0D, value.transfer() * efficiency))
                ))
        );

        definition.emergencyShield().ifPresent(value -> {
            tooltip.add(TooltipHelper.property(
                    "emergency shield restore",
                    NumberFormatter.percent(Math.min(1.0D, value.restore() * efficiency))
            ));
            tooltip.add(TooltipHelper.property("emergency shield cooldown", formatTicks(value.cooldown())));
        });

        definition.cloaking().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("cloaking activation energy", NumberFormatter.format(value.activationEnergy()) + " FE"));
            tooltip.add(TooltipHelper.property("cloaking active consumption", NumberFormatter.format(value.activeConsumption()) + " FE/t"));
            tooltip.add(TooltipHelper.property("cloaking cooldown", formatTicks(value.cooldown())));
        });

        definition.blink().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("blink distance", value.distance()));
            tooltip.add(TooltipHelper.property("blink activation energy", NumberFormatter.format(value.activationEnergy()) + " FE"));
            tooltip.add(TooltipHelper.property("blink cooldown", formatTicks(value.cooldown())));
        });

        definition.flight().ifPresent(value ->
                tooltip.add(TooltipHelper.property("flight active consumption", NumberFormatter.format(value.activeConsumption()) + " FE/t"))
        );

        definition.jetpack().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("vertical thrust", value.verticalThrust()));
            tooltip.add(TooltipHelper.property("horizontal speed", value.horizontalSpeed()));
            tooltip.add(TooltipHelper.property("jetpack energy consumption", NumberFormatter.format(value.energyConsumption()) + " FE/t"));
            value.elytra().ifPresent(elytra -> {
                tooltip.add(TooltipHelper.property("elytra acceleration", elytra.acceleration()));
                tooltip.add(TooltipHelper.property("elytra max speed", elytra.maxSpeed()));
            });
        });

        definition.statusProtection().ifPresent(value -> {
            if (value.harmfulProtection() > 0.0D) {
                tooltip.add(TooltipHelper.property(
                        "harmful status protection",
                        NumberFormatter.percent(Math.min(
                                        1.0D,
                                        value.harmfulProtection() * efficiency
                                ))
                ));
            }

            value.tagProtections().forEach((id, protection) ->
                    tooltip.add(TooltipHelper.property(
                            Component.translatable("tooltip.exoequipment.property.status_protection_type",
                                    statusTagLabel(id)).getString(),
                            NumberFormatter.percent(Math.min(
                                            1.0D,
                                            protection * efficiency
                                    ))
                    ))
            );

            value.protections().forEach((id, protection) ->
                    tooltip.add(TooltipHelper.property(
                            Component.translatable("tooltip.exoequipment.property.status_protection_type",
                                    effectName(id)).getString(),
                            NumberFormatter.percent(Math.min(1.0D, protection * efficiency))
                    ))
            );
        });

        definition.effects().ifPresent(value -> value.effects().forEach((id, amplifier) ->
                tooltip.add(TooltipHelper.property("effect",
                        effectName(id).copy().append(" " + NumberFormatter.format(amplifier + 1))))));

        definition.entityDetection().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("detection range", value.range()));
            tooltip.add(TooltipHelper.property("entity detection active consumption", NumberFormatter.format(value.activeConsumption()) + " FE/t"));
            tooltip.add(TooltipHelper.property("detect players", value.players()));
            tooltip.add(TooltipHelper.property("detect mobs", value.mobs()));
            tooltip.add(TooltipHelper.property("detect hostile", value.hostile()));
        });

        definition.hunger().ifPresent(value ->
                tooltip.add(TooltipHelper.property(
                        "exhaustion reduction",
                        NumberFormatter.percent(Math.min(1.0D, value.exhaustionReduction() * efficiency))
                ))
        );

        definition.revival().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("revival restore health", value.restoreHealth() * efficiency));
            tooltip.add(TooltipHelper.property("revival cooldown", formatTicks(value.cooldown())));
            tooltip.add(TooltipHelper.property("revival invulnerability", formatTicks(value.invulnerabilityTicks())));
        });

        definition.regeneration().ifPresent(value ->
                tooltip.add(TooltipHelper.property("health per second", value.healthPerSecond() * efficiency))
        );

        definition.attributes().ifPresent(value ->
                value.attributes().forEach((id, modifier) ->
                        tooltip.add(TooltipHelper.property(
                                AttributeNameUtils.getName(id).getString(),
                                formatModifier(id, modifier, efficiency)
                        ))
                )
        );

        definition.conditionalAttributes().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("conditional attributes", value.conditions().stream()
                    .map(condition -> Component.translatable("tooltip.exoequipment.condition."
                                    + condition.type().name().toLowerCase(java.util.Locale.ROOT)).getString()
                            + condition.value().map(v -> " " + NumberFormatter.percent(v)).orElse(""))
                    .collect(java.util.stream.Collectors.joining(", "))));
            value.attributes().attributes().forEach((id, modifier) ->
                    tooltip.add(TooltipHelper.property(
                            Component.translatable("tooltip.exoequipment.conditional_prefix").getString() + AttributeNameUtils.getName(id).getString(),
                            formatModifier(id, modifier, efficiency)
                    ))
            );
        });

        definition.pickupMagnet().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("pickup radius", value.radius() * efficiency));
            tooltip.add(TooltipHelper.property("pickup mode", Component.translatable("tooltip.exoequipment.pickup_mode." + value.mode().name().toLowerCase(java.util.Locale.ROOT))));
        });

        definition.temperatureModifier().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("temperature", value.temperature()));
            tooltip.add(TooltipHelper.property("heat resistance", value.heatResistance()));
            tooltip.add(TooltipHelper.property("cold resistance", value.coldResistance()));
            tooltip.add(TooltipHelper.property("thermal resistance", value.thermalResistance()));
        });

        definition.temperatureImpact().ifPresent(value ->
                tooltip.add(TooltipHelper.property("temperature impact resistance",
                        NumberFormatter.percent(value.resistance())))
        );

        definition.bodyDamageProtection().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("body damage block chance",
                    NumberFormatter.percent(Math.min(1.0D, value.chance() * efficiency))));
            tooltip.add(TooltipHelper.property("body damage reduction",
                    NumberFormatter.percent(Math.min(1.0D, value.damageReduction() * efficiency))));
            tooltip.add(TooltipHelper.property(
                    "body parts",
                    value.bodyParts().isEmpty()
                            ? Component.translatable("tooltip.exoequipment.body_part.all").getString()
                            : value.bodyParts().stream()
                            .map(ModuleItem::bodyPartName)
                            .collect(java.util.stream.Collectors.joining(", "))
            ));
        });

        definition.bodyDamageRegeneration().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("body regeneration", value.healthPerSecond() * efficiency));
            tooltip.add(TooltipHelper.property("body regeneration parts",
                    value.bodyParts().isEmpty()
                            ? Component.translatable("tooltip.exoequipment.body_part.all").getString()
                            : value.bodyParts().stream().map(ModuleItem::bodyPartName)
                            .collect(java.util.stream.Collectors.joining(", "))));
        });

        definition.thirst().ifPresent(value ->
                tooltip.add(TooltipHelper.property(
                        "thirst exhaustion reduction",
                        NumberFormatter.percent(Math.min(1.0D, value.exhaustionReduction() * efficiency))
                ))
        );

        definition.blockScanner().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("scanner range", value.range()));
            tooltip.add(TooltipHelper.property("scanner color", String.format(java.util.Locale.ROOT, "#%06X", value.color())));
            tooltip.add(TooltipHelper.property("block scanner active consumption", NumberFormatter.format(value.activeConsumption()) + " FE/t"));
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

        definition.laserDefense().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("laser range", value.range()));
            tooltip.add(TooltipHelper.property("laser damage", value.damage()));
            tooltip.add(TooltipHelper.property("laser cooldown", formatTicks(value.cooldown())));
            tooltip.add(TooltipHelper.property("laser energy", NumberFormatter.format(value.energyCost()) + " FE/shot"));
            tooltip.add(TooltipHelper.property("target players", value.targetPlayers()));
            tooltip.add(TooltipHelper.property("target hostile", value.targetHostile()));
            tooltip.add(TooltipHelper.property("target aggressive", value.targetAggressive()));
        });

        definition.dischargeDefense().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("discharge range", value.range()));
            tooltip.add(TooltipHelper.property("arc jump range", value.jumpRange()));
            tooltip.add(TooltipHelper.property("discharge damage", value.damage()));
            tooltip.add(TooltipHelper.property("damage per bounce", NumberFormatter.percent(value.falloff())));
            tooltip.add(TooltipHelper.property("initial targets", value.targets()));
            tooltip.add(TooltipHelper.property("bounces", value.bounces()));
            tooltip.add(TooltipHelper.property("discharge cooldown", formatTicks(value.cooldown())));
            tooltip.add(TooltipHelper.property("discharge energy", NumberFormatter.format(value.energyCost()) + " FE/use"));
            tooltip.add(TooltipHelper.property("target players", value.targetPlayers()));
            tooltip.add(TooltipHelper.property("target hostile", value.targetHostile()));
            tooltip.add(TooltipHelper.property("target aggressive", value.targetAggressive()));
        });

        definition.thermalVision().ifPresent(value -> {
            tooltip.add(TooltipHelper.property("thermal vision active consumption",
                    NumberFormatter.format(value.activeConsumption()) + " FE/t"));
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
                    tooltip.add(TooltipHelper.property("temperature bonus",
                            NumberFormatter.percent(value.maximumBonus()) + " ("
                                    + NumberFormatter.format(value.minTemperature()) + "–"
                                    + NumberFormatter.format(value.maxTemperature()) + " °C)"))
            );
        }
    }

    private static void appendSummary(ModuleDefinition definition, ItemStack stack,
                                      List<Component> tooltip, double efficiency) {
        definition.energy().filter(e -> e.consumption() > 0).ifPresent(e ->
                tooltip.add(TooltipHelper.energyConsumption(applyEfficiency(e.consumption(), efficiency), efficiency)));
        definition.generation().ifPresent(g ->
                tooltip.add(TooltipHelper.energyGeneration(applyEfficiency(g.generation(), efficiency), efficiency)));
        definition.storage().ifPresent(s -> tooltip.add(TooltipHelper.capacity(
                Math.min(stack.getOrDefault(ModDataComponents.MODULE_STORED_ENERGY.get(), 0), s.capacity()),
                applyEfficiency(s.capacity(), efficiency), efficiency)));
        definition.thermal().ifPresent(t -> {
            if (t.cooling() > 0) tooltip.add(TooltipHelper.cooling(applyEfficiency((int) t.cooling(), efficiency), efficiency));
            if (t.heatGeneration() > 0) tooltip.add(TooltipHelper.heatGeneration(applyEfficiency((int) t.heatGeneration(), efficiency), efficiency));
        });
        definition.shield().ifPresent(s -> tooltip.add(TooltipHelper.property("shield capacity", applyEfficiency(s.capacity(), efficiency))));
        definition.damageReduction().ifPresent(d -> d.defaultReduction().ifPresent(v ->
                tooltip.add(TooltipHelper.property("all damage reduction", NumberFormatter.percent(v * efficiency)))));
        definition.damageReduction().ifPresent(d -> {
            if (d.defaultReduction().isEmpty()) {
                double strongest = java.util.stream.Stream.concat(d.reductions().values().stream(),
                        d.tagReductions().values().stream()).mapToDouble(Double::doubleValue).max().orElse(0);
                if (strongest > 0) tooltip.add(TooltipHelper.property("selective damage reduction",
                        NumberFormatter.percent(strongest * efficiency)));
            }
        });
        definition.shieldProtection().ifPresent(s -> tooltip.add(TooltipHelper.property("shield protection transfer",
                NumberFormatter.percent(s.transfer() * efficiency))));
        definition.emergencyShield().ifPresent(s -> tooltip.add(TooltipHelper.property("emergency shield restore",
                NumberFormatter.percent(Math.min(1, s.restore() * efficiency)))));
        definition.cloaking().ifPresent(c -> tooltip.add(TooltipHelper.property("cloaking activation energy",
                NumberFormatter.format(c.activationEnergy()) + " FE")));
        definition.blink().ifPresent(b -> tooltip.add(TooltipHelper.property("blink distance",
                NumberFormatter.format(b.distance()) + " b")));
        definition.flight().ifPresent(f -> tooltip.add(Component.translatable("tooltip.exoequipment.module.flight")));
        definition.jetpack().ifPresent(j -> tooltip.add(TooltipHelper.property("horizontal speed",
                NumberFormatter.format(j.horizontalSpeed() * 20) + " b/s")));
        definition.entityDetection().ifPresent(d -> tooltip.add(TooltipHelper.property("detection range",
                NumberFormatter.format(d.range()) + " b")));
        definition.blockScanner().ifPresent(s -> tooltip.add(TooltipHelper.property("scanner range",
                NumberFormatter.format(s.range()) + " b")));
        definition.thermalVision().ifPresent(v -> tooltip.add(Component.translatable("tooltip.exoequipment.module.thermal_vision")));
        definition.laserDefense().ifPresent(l -> {
            tooltip.add(TooltipHelper.property("laser damage", l.damage()));
            tooltip.add(TooltipHelper.property("laser energy", NumberFormatter.format(l.energyCost()) + " FE"));
        });
        definition.dischargeDefense().ifPresent(d -> {
            tooltip.add(TooltipHelper.property("discharge damage", d.damage()));
            tooltip.add(TooltipHelper.property("discharge energy", NumberFormatter.format(d.energyCost()) + " FE"));
        });
        definition.revival().ifPresent(r -> tooltip.add(TooltipHelper.property("revival restore health", r.restoreHealth() * efficiency)));
        definition.regeneration().ifPresent(r -> tooltip.add(TooltipHelper.property("health per second", r.healthPerSecond() * efficiency)));
        definition.pickupMagnet().ifPresent(m -> tooltip.add(TooltipHelper.property("pickup radius", m.radius() * efficiency)));
        definition.attributes().ifPresent(a -> a.attributes().forEach((id, modifier) ->
                tooltip.add(TooltipHelper.property(AttributeNameUtils.getName(id).getString(),
                        formatModifier(id, modifier, efficiency)))));
        definition.conditionalAttributes().ifPresent(a -> a.attributes().attributes().forEach((id, modifier) ->
                tooltip.add(TooltipHelper.property(Component.translatable("tooltip.exoequipment.conditional_prefix").getString() + AttributeNameUtils.getName(id).getString(),
                        formatModifier(id, modifier, efficiency)))));
        definition.effects().ifPresent(e -> e.effects().forEach((id, amplifier) ->
                tooltip.add(TooltipHelper.property("effect", effectName(id).copy()
                        .append(" " + NumberFormatter.format(amplifier + 1))))));
        definition.hunger().ifPresent(h -> tooltip.add(TooltipHelper.property("exhaustion reduction",
                NumberFormatter.percent(h.exhaustionReduction() * efficiency))));
        definition.thirst().ifPresent(t -> tooltip.add(TooltipHelper.property("thirst exhaustion reduction",
                NumberFormatter.percent(t.exhaustionReduction() * efficiency))));
        definition.statusProtection().ifPresent(s -> {
            if (s.harmfulProtection() > 0) tooltip.add(TooltipHelper.property("harmful status protection",
                    NumberFormatter.percent(s.harmfulProtection() * efficiency)));
            else {
                double strongest = java.util.stream.Stream.concat(s.protections().values().stream(),
                        s.tagProtections().values().stream()).mapToDouble(Double::doubleValue).max().orElse(0);
                if (strongest > 0) tooltip.add(TooltipHelper.property("selective status protection",
                        NumberFormatter.percent(strongest * efficiency)));
            }
        });
        definition.bodyDamageProtection().ifPresent(b -> {
            if (b.damageReduction() > 0) tooltip.add(TooltipHelper.property("body damage reduction",
                    NumberFormatter.percent(b.damageReduction() * efficiency)));
            if (b.chance() > 0) tooltip.add(TooltipHelper.property("body damage block chance",
                    NumberFormatter.percent(b.chance() * efficiency)));
        });
        definition.bodyDamageRegeneration().ifPresent(b -> tooltip.add(TooltipHelper.property("body regeneration",
                b.healthPerSecond() * efficiency)));
        definition.temperatureImpact().ifPresent(t -> tooltip.add(TooltipHelper.property("temperature impact resistance",
                NumberFormatter.percent(t.resistance()))));
        definition.temperatureModifier().ifPresent(t -> {
            if (t.temperature() != 0) tooltip.add(TooltipHelper.property("temperature", t.temperature()));
            if (t.heatResistance() > 0) tooltip.add(TooltipHelper.property("heat resistance", t.heatResistance()));
            if (t.coldResistance() > 0) tooltip.add(TooltipHelper.property("cold resistance", t.coldResistance()));
            if (t.thermalResistance() > 0) tooltip.add(TooltipHelper.property("thermal resistance", t.thermalResistance()));
        });
    }

    private static String formatTicks(int ticks) {
        return NumberFormatter.format(ticks / 20.0D) + " s";
    }

    private static String formatModifier(ResourceLocation id,
                                         com.github.littleemptydoll.exoequipment.module.AttributeModifierProperties modifier,
                                         double efficiency) {
        String value = AttributeValueFormatter.format(id, modifier, efficiency);
        return modifier.operation() == net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE
                ? value
                : value + " (" + Component.translatable("tooltip.exoequipment.attribute_operation."
                        + modifier.operation().name().toLowerCase(java.util.Locale.ROOT)).getString() + ")";
    }

    private static Component effectName(ResourceLocation id) {
        return BuiltInRegistries.MOB_EFFECT.getHolder(id)
                .map(holder -> (Component) Component.translatable(holder.value().getDescriptionId()))
                .orElseGet(() -> Component.literal(NameUtils.toDisplayName(id.getPath())));
    }

    private static String bodyPartName(com.github.littleemptydoll.exoequipment.module.BodyPart part) {
        return Component.translatable("tooltip.exoequipment.body_part."
                + part.name().toLowerCase(java.util.Locale.ROOT)).getString();
    }

    private static String damageTagLabel(ResourceLocation id) {
        String path = id.getPath();
        if (path.startsWith("is_") && path.length() > 3) {
            path = path.substring(3);
        }
        return NameUtils.toDisplayName(path);
    }

    private static String statusTagLabel(ResourceLocation id) {
        String path = id.getPath();
        int separator = path.lastIndexOf('/');
        if (separator >= 0 && separator + 1 < path.length()) {
            path = path.substring(separator + 1);
        }
        return NameUtils.toDisplayName(path);
    }

    private static int applyEfficiency(int value, double efficiency) {
        return Math.max(0, (int) Math.round(value * efficiency));
    }

    private static double applyEfficiency(double value, double efficiency) {
        return Math.max(0.0D, value * efficiency);
    }
}
