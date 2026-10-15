package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class GeneratedModuleVariantRandomizer {
    private static final long VARIANT_SEED_SALT = 0x6D0E4B7A29C15F31L;
    private static final ResourceLocation ELYTRA_FLIGHT = id("apothic_attributes", "elytra_flight");

    private static final List<ResourceLocation> DAMAGE_TYPES = ids("minecraft",
            "in_fire", "lightning_bolt", "on_fire", "lava", "hot_floor",
            "in_wall", "cramming", "drown", "starve", "cactus", "fall",
            "fly_into_wall", "generic", "magic", "wither", "dragon_breath",
            "dry_out", "sweet_berry_bush", "freeze", "stalagmite",
            "falling_block", "falling_anvil", "falling_stalactite", "sting",
            "mob_attack", "player_attack", "arrow", "trident", "mob_projectile",
            "fireworks", "fireball", "wither_skull", "thrown", "indirect_magic",
            "thorns", "explosion", "player_explosion", "sonic_boom",
            "bad_respawn_point"
    );

    private static final List<ResourceLocation> DAMAGE_TAGS = ids("minecraft",
            "is_fire", "is_projectile", "is_explosion", "is_fall",
            "is_drowning", "is_freezing", "is_lightning"
    );

    private static final Set<ResourceLocation> EFFECT_BLACKLIST = Set.of(
            id("minecraft", "instant_health"),
            id("minecraft", "saturation")
    );

    private GeneratedModuleVariantRandomizer() {}

    static ModuleDefinition apply(
            ModuleDefinition base,
            GeneratedModulePlan plan,
            long seed
    ) {
        RandomSource random = RandomSource.create(seed ^ VARIANT_SEED_SALT);

        Optional<DamageReductionProperties> damageReduction = base.damageReduction();
        if (hasAny(plan,
                GeneratedPropertyType.SPECIALIZED_DAMAGE_REDUCTION,
                GeneratedPropertyType.DAMAGE_IMMUNITY)) {
            damageReduction = damageReduction.map(value -> randomizeDamageReduction(value, random));
        }

        Optional<StatusProtectionProperties> statusProtection = base.statusProtection();
        if (has(plan, GeneratedPropertyType.STATUS_PROTECTION)) {
            statusProtection = statusProtection.map(value ->
                    randomizeStatusProtection(value, plan.tier(), random));
        }

        Optional<BodyDamageProtectionProperties> bodyProtection = base.bodyDamageProtection();
        if (has(plan, GeneratedPropertyType.BODY_PROTECTION)) {
            bodyProtection = bodyProtection.map(value -> new BodyDamageProtectionProperties(
                    value.chance(),
                    value.damageReduction(),
                    rollBodyParts(plan.tier(), random, true)
            ));
        }

        Optional<BodyDamageRegenerationProperties> bodyRegeneration = base.bodyDamageRegeneration();
        if (has(plan, GeneratedPropertyType.BODY_REGENERATION)) {
            bodyRegeneration = bodyRegeneration.map(value -> new BodyDamageRegenerationProperties(
                    value.healthPerSecond(),
                    rollBodyParts(plan.tier(), random, false)
            ));
        }

        Optional<AttributeProperties> attributes = base.attributes();
        if (attributes.isPresent()) {
            attributes = Optional.of(randomizeAttributes(
                    attributes.get(),
                    plan,
                    random,
                    false
            ));
        }

        Optional<ConditionalAttributeProperties> conditional = base.conditionalAttributes();
        if (conditional.isPresent()
                && hasAny(plan, GeneratedPropertyType.ADRENALINE, GeneratedPropertyType.REACTIVE_ARMOR)) {
            conditional = Optional.of(randomizeConditional(
                    conditional.get(),
                    plan,
                    random
            ));
        }

        Optional<EffectsProperties> effects = base.effects();
        int extraPassiveEnergy = 0;
        if (effects.isPresent() && has(plan, GeneratedPropertyType.NIGHT_VISION)) {
            EffectRoll roll = randomizeEffect(plan.tier(), random);
            if (roll != null) {
                effects = Optional.of(roll.effects());
                extraPassiveEnergy = (int) Math.ceil(
                        roll.energySurcharge()
                                * GeneratedModuleGenerator.integrationMultiplier(plan.properties().size())
                );
            }
        }

        Optional<EntityDetectionProperties> entityDetection = base.entityDetection();
        if (entityDetection.isPresent() && has(plan, GeneratedPropertyType.ENTITY_SENSOR)) {
            entityDetection = entityDetection.map(value -> randomizeEntityDetection(value, random));
        }

        Optional<BlockScannerProperties> blockScanner = base.blockScanner();
        if (blockScanner.isPresent() && has(plan, GeneratedPropertyType.BLOCK_SCANNER)) {
            blockScanner = blockScanner.map(value -> randomizeBlockScanner(value, plan.tier(), random));
        }

        Optional<EnergyProperties> energy = base.energy();
        if (extraPassiveEnergy > 0) {
            int surcharge = extraPassiveEnergy;
            energy = Optional.of(energy
                    .map(value -> new EnergyProperties(
                            value.consumption() + surcharge,
                            value.priority()
                    ))
                    .orElseGet(() -> new EnergyProperties(surcharge)));
        }

        return new ModuleDefinition(
                base.id(),
                base.properties(),
                base.category(),
                base.size(),
                energy,
                base.generation(),
                base.storage(),
                base.thermal(),
                base.temperature(),
                base.temperatureModifier(),
                base.temperatureImpact(),
                damageReduction,
                base.shield(),
                base.emergencyShield(),
                base.cloaking(),
                base.blink(),
                base.flight(),
                base.jetpack(),
                statusProtection,
                effects,
                entityDetection,
                base.hunger(),
                base.revival(),
                base.regeneration(),
                attributes,
                conditional,
                base.pickupMagnet(),
                bodyProtection,
                bodyRegeneration,
                base.thirst(),
                blockScanner,
                base.shieldProtection(),
                base.thermalVision(),
                base.laserDefense(),
                base.dischargeDefense()
        );
    }

    private static DamageReductionProperties randomizeDamageReduction(
            DamageReductionProperties value,
            RandomSource random
    ) {
        List<Double> specialized = new ArrayList<>();
        specialized.addAll(value.reductions().values());
        specialized.addAll(value.tagReductions().values());

        if (specialized.isEmpty()) {
            return value;
        }

        Map<ResourceLocation, Double> direct = new LinkedHashMap<>();
        Map<ResourceLocation, Double> tags = new LinkedHashMap<>();

        for (double reduction : specialized) {
            if (random.nextFloat() < 0.42F) {
                ResourceLocation key = DAMAGE_TAGS.get(random.nextInt(DAMAGE_TAGS.size()));
                tags.merge(key, reduction, Math::max);
            } else {
                ResourceLocation key = DAMAGE_TYPES.get(random.nextInt(DAMAGE_TYPES.size()));
                direct.merge(key, reduction, Math::max);
            }
        }

        return new DamageReductionProperties(
                value.defaultReduction(),
                direct,
                tags
        );
    }

    private static StatusProtectionProperties randomizeStatusProtection(
            StatusProtectionProperties value,
            EquipmentTier tier,
            RandomSource random
    ) {
        double strength = value.harmfulProtection();
        for (double protection : value.protections().values()) {
            strength = Math.max(strength, protection);
        }
        for (double protection : value.tagProtections().values()) {
            strength = Math.max(strength, protection);
        }

        if (strength <= 0.0D) {
            return value;
        }

        if (tier == EquipmentTier.EXPERIMENTAL && random.nextFloat() < 0.25F) {
            return new StatusProtectionProperties(strength, Map.of(), Map.of());
        }

        List<ResourceLocation> candidates = effectIds(MobEffectCategory.HARMFUL);
        if (candidates.isEmpty()) {
            return value;
        }

        int count = switch (tier) {
            case ENGINEERING -> 1;
            case MILITARY -> 1 + random.nextInt(2);
            case EXPERIMENTAL -> 2 + random.nextInt(3);
            default -> 1;
        };
        count = Math.min(count, candidates.size());

        Map<ResourceLocation, Double> protections = new LinkedHashMap<>();
        for (ResourceLocation effect : takeRandomDistinct(candidates, count, random)) {
            protections.put(effect, strength);
        }
        return new StatusProtectionProperties(0.0D, protections, Map.of());
    }

    private static AttributeProperties randomizeAttributes(
            AttributeProperties value,
            GeneratedModulePlan plan,
            RandomSource random,
            boolean forceReroute
    ) {
        Map<ResourceLocation, AttributeModifierProperties> attributes =
                new LinkedHashMap<>(value.attributes());

        if (has(plan, GeneratedPropertyType.ELYTRA)
                && attributes.containsKey(ELYTRA_FLIGHT)) {
            attributes.put(
                    ELYTRA_FLIGHT,
                    new AttributeModifierProperties(
                            1.0D,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
        }

        List<ResourceLocation> replaceable = attributes.keySet().stream()
                .filter(key -> !key.equals(ELYTRA_FLIGHT))
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .toList();

        double chance = forceReroute
                ? 0.80D
                : plan.tier() == EquipmentTier.EXPERIMENTAL ? 0.50D : 0.35D;
        if (replaceable.isEmpty() || random.nextDouble() >= chance) {
            return new AttributeProperties(attributes);
        }

        ResourceLocation replaced = replaceable.get(random.nextInt(replaceable.size()));
        List<ResourceLocation> candidates = attributeIds(plan.tier()).stream()
                .filter(key -> !attributes.containsKey(key))
                .toList();
        if (candidates.isEmpty()) {
            return new AttributeProperties(attributes);
        }

        ResourceLocation target = candidates.get(random.nextInt(candidates.size()));
        attributes.remove(replaced);
        attributes.put(target, rollAttributeModifier(target, plan.tier(), random));
        return new AttributeProperties(attributes);
    }

    private static ConditionalAttributeProperties randomizeConditional(
            ConditionalAttributeProperties value,
            GeneratedModulePlan plan,
            RandomSource random
    ) {
        int conditionCount = plan.tier() == EquipmentTier.EXPERIMENTAL
                && random.nextFloat() < 0.30F ? 2 : 1;
        List<AttributeCondition> conditions = new ArrayList<>();
        int attempts = 0;
        while (conditions.size() < conditionCount && attempts++ < 20) {
            AttributeCondition next = rollCondition(random);
            if (conditions.stream().noneMatch(existing ->
                    existing.type() == next.type()
                            || conditionsConflict(existing.type(), next.type()))) {
                conditions.add(next);
            }
        }
        if (conditions.isEmpty()) {
            conditions.add(rollCondition(random));
        }

        AttributeProperties rolledAttributes = randomizeAttributes(
                value.attributes(),
                plan,
                random,
                true
        );
        return new ConditionalAttributeProperties(conditions, rolledAttributes);
    }

    private static AttributeCondition rollCondition(RandomSource random) {
        AttributeCondition.Type[] types = AttributeCondition.Type.values();
        AttributeCondition.Type type = types[random.nextInt(types.length)];
        if (type == AttributeCondition.Type.HEALTH_BELOW
                || type == AttributeCondition.Type.HEALTH_ABOVE) {
            double threshold = 0.20D + random.nextDouble() * 0.60D;
            return new AttributeCondition(type, Optional.of(threshold));
        }
        return new AttributeCondition(type, Optional.empty());
    }

    private static boolean conditionsConflict(
            AttributeCondition.Type first,
            AttributeCondition.Type second
    ) {
        return (first == AttributeCondition.Type.DAY && second == AttributeCondition.Type.NIGHT)
                || (first == AttributeCondition.Type.NIGHT && second == AttributeCondition.Type.DAY);
    }

    private static EffectRoll randomizeEffect(
            EquipmentTier tier,
            RandomSource random
    ) {
        List<ResourceLocation> candidates = effectIds(MobEffectCategory.BENEFICIAL).stream()
                .filter(id -> !EFFECT_BLACKLIST.contains(id))
                .toList();
        if (candidates.isEmpty()) {
            return null;
        }

        ResourceLocation effect = candidates.get(random.nextInt(candidates.size()));
        int amplifier = tier == EquipmentTier.EXPERIMENTAL
                && random.nextFloat() < 0.15F ? 1 : 0;

        int surcharge = effectEnergySurcharge(effect, amplifier);
        return new EffectRoll(
                new EffectsProperties(Map.of(effect, amplifier)),
                surcharge
        );
    }

    private static int effectEnergySurcharge(
            ResourceLocation effect,
            int amplifier
    ) {
        if (effect.equals(id("minecraft", "night_vision"))) {
            return amplifier * 10;
        }

        String path = effect.getPath();
        int base = switch (path) {
            case "regeneration", "resistance", "strength", "absorption", "health_boost" -> 45;
            case "speed", "haste", "jump_boost" -> 30;
            default -> 15;
        };
        return base + amplifier * 25;
    }

    private static EntityDetectionProperties randomizeEntityDetection(
            EntityDetectionProperties value,
            RandomSource random
    ) {
        if (random.nextFloat() >= 0.55F) {
            return value;
        }

        boolean players = random.nextBoolean();
        boolean mobs = random.nextBoolean();
        boolean hostile = random.nextBoolean();
        if (!players && !mobs && !hostile) {
            hostile = true;
        }
        return new EntityDetectionProperties(
                value.range(),
                players,
                mobs,
                hostile,
                value.activeConsumption()
        );
    }

    private static BlockScannerProperties randomizeBlockScanner(
            BlockScannerProperties value,
            EquipmentTier tier,
            RandomSource random
    ) {
        double chance = tier == EquipmentTier.EXPERIMENTAL ? 0.55D : 0.35D;
        if (random.nextDouble() >= chance) {
            return value;
        }

        List<ResourceLocation> candidates = BuiltInRegistries.BLOCK.keySet().stream()
                .filter(id -> !id.getPath().contains("air"))
                .filter(id -> !id.equals(id("minecraft", "barrier")))
                .filter(id -> !id.equals(id("minecraft", "structure_void")))
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .toList();
        if (candidates.isEmpty()) {
            return value;
        }

        int count = Math.min(
                1 + random.nextInt(tier == EquipmentTier.EXPERIMENTAL ? 3 : 2),
                candidates.size()
        );
        return new BlockScannerProperties(
                value.range(),
                takeRandomDistinct(candidates, count, random),
                List.of(),
                value.activeConsumption(),
                value.color()
        );
    }

    private static Set<BodyPart> rollBodyParts(
            EquipmentTier tier,
            RandomSource random,
            boolean allowUniversal
    ) {
        double universalChance = switch (tier) {
            case ENGINEERING -> 0.04D;
            case MILITARY -> 0.10D;
            case EXPERIMENTAL -> 0.25D;
            default -> 0.0D;
        };
        if (allowUniversal && random.nextDouble() < universalChance) {
            return Set.of();
        }

        int max = switch (tier) {
            case ENGINEERING -> 2;
            case MILITARY -> 4;
            case EXPERIMENTAL -> 6;
            default -> 1;
        };
        int count = 1 + random.nextInt(max);
        List<BodyPart> all = List.of(BodyPart.values());
        return new LinkedHashSet<>(takeRandomDistinct(all, count, random));
    }

    private static List<ResourceLocation> effectIds(MobEffectCategory category) {
        return BuiltInRegistries.MOB_EFFECT.keySet().stream()
                .filter(id -> BuiltInRegistries.MOB_EFFECT
                        .getHolder(id)
                        .map(holder -> holder.value().getCategory() == category)
                        .orElse(false))
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .toList();
    }

    private static List<ResourceLocation> attributeIds(EquipmentTier tier) {
        return BuiltInRegistries.ATTRIBUTE.keySet().stream()
                .filter(id -> isAllowedAttribute(id, tier))
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .toList();
    }

    private static boolean isAllowedAttribute(
            ResourceLocation id,
            EquipmentTier tier
    ) {
        if (id.equals(ELYTRA_FLIGHT)) {
            return false;
        }
        String path = id.getPath();
        if (path.contains("flight")) {
            return false;
        }
        if (tier != EquipmentTier.EXPERIMENTAL
                && (path.contains("invulner") || path.contains("creative"))) {
            return false;
        }
        return true;
    }

    private static AttributeModifierProperties rollAttributeModifier(
            ResourceLocation attribute,
            EquipmentTier tier,
            RandomSource random
    ) {
        String namespace = attribute.getNamespace();
        String path = attribute.getPath();
        double tierScale = tier == EquipmentTier.EXPERIMENTAL ? 1.30D : 1.0D;

        if (path.equals("generic.luck") || path.equals("luck")) {
            return flat(1.0D + random.nextInt(2), tierScale);
        }
        if (path.contains("armor_toughness")) {
            return flat(0.75D + random.nextDouble() * 2.25D, tierScale);
        }
        if (path.endsWith("armor") || path.contains("armor_value")) {
            return flat(1.5D + random.nextDouble() * 4.5D, tierScale);
        }
        if (path.contains("safe_fall_distance")) {
            return flat(1.0D + random.nextDouble() * 4.0D, tierScale);
        }
        if (path.contains("interaction_range") || path.endsWith("reach")) {
            return flat(0.35D + random.nextDouble() * 1.15D, tierScale);
        }
        if (path.contains("knockback_resistance")) {
            return flat(0.05D + random.nextDouble() * 0.20D, tierScale);
        }
        if (namespace.equals("apothic_attributes")) {
            double amount = switch (path) {
                case "crit_chance" -> 0.04D + random.nextDouble() * 0.12D;
                case "crit_damage" -> 0.10D + random.nextDouble() * 0.30D;
                case "life_steal" -> 0.02D + random.nextDouble() * 0.06D;
                case "overheal" -> 0.04D + random.nextDouble() * 0.10D;
                case "current_hp_damage" -> 0.02D + random.nextDouble() * 0.07D;
                case "armor_pierce", "protection_pierce" -> 0.05D + random.nextDouble() * 0.18D;
                case "armor_shred", "protection_shred" -> 0.04D + random.nextDouble() * 0.14D;
                default -> 0.04D + random.nextDouble() * 0.16D;
            };
            return new AttributeModifierProperties(
                    amount * tierScale,
                    AttributeModifier.Operation.ADD_VALUE
            );
        }

        double percent = (0.05D + random.nextDouble() * 0.17D) * tierScale;
        return new AttributeModifierProperties(
                percent,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );
    }

    private static AttributeModifierProperties flat(double value, double tierScale) {
        return new AttributeModifierProperties(
                value * tierScale,
                AttributeModifier.Operation.ADD_VALUE
        );
    }

    private static boolean has(
            GeneratedModulePlan plan,
            GeneratedPropertyType property
    ) {
        return plan.properties().contains(property);
    }

    private static boolean hasAny(
            GeneratedModulePlan plan,
            GeneratedPropertyType... properties
    ) {
        for (GeneratedPropertyType property : properties) {
            if (has(plan, property)) {
                return true;
            }
        }
        return false;
    }

    private static <T> List<T> takeRandomDistinct(
            List<T> source,
            int count,
            RandomSource random
    ) {
        List<T> values = new ArrayList<>(source);
        int limit = Math.min(count, values.size());
        for (int i = 0; i < limit; i++) {
            int selected = i + random.nextInt(values.size() - i);
            T swap = values.get(i);
            values.set(i, values.get(selected));
            values.set(selected, swap);
        }
        return List.copyOf(values.subList(0, limit));
    }

    private static List<ResourceLocation> ids(
            String namespace,
            String... paths
    ) {
        List<ResourceLocation> ids = new ArrayList<>(paths.length);
        for (String path : paths) {
            ids.add(id(namespace, path));
        }
        return List.copyOf(ids);
    }

    private static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    private record EffectRoll(
            EffectsProperties effects,
            int energySurcharge
    ) {}
}
