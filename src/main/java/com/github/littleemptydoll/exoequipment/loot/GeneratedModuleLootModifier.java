package com.github.littleemptydoll.exoequipment.loot;

import com.github.littleemptydoll.exoequipment.module.GeneratedModuleGenerator;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public final class GeneratedModuleLootModifier extends LootModifier {
    public static final MapCodec<GeneratedModuleLootModifier> CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    LootModifier.codecStart(instance).and(instance.group(
                            Codec.FLOAT.fieldOf("chance")
                                    .forGetter(value -> value.chance),
                            Codec.INT.optionalFieldOf("engineering_weight", 0)
                                    .forGetter(value -> value.engineeringWeight),
                            Codec.INT.optionalFieldOf("military_weight", 0)
                                    .forGetter(value -> value.militaryWeight),
                            Codec.INT.optionalFieldOf("experimental_weight", 0)
                                    .forGetter(value -> value.experimentalWeight)
                    )).apply(instance, GeneratedModuleLootModifier::new)
            );

    private final float chance;
    private final int engineeringWeight;
    private final int militaryWeight;
    private final int experimentalWeight;
    private final int totalWeight;

    public GeneratedModuleLootModifier(
            LootItemCondition[] conditions,
            float chance,
            int engineeringWeight,
            int militaryWeight,
            int experimentalWeight
    ) {
        super(conditions);

        if (!Float.isFinite(chance) || chance < 0.0F || chance > 1.0F) {
            throw new IllegalArgumentException(
                    "Generated module loot chance must be between 0 and 1"
            );
        }
        if (engineeringWeight < 0 || militaryWeight < 0 || experimentalWeight < 0) {
            throw new IllegalArgumentException(
                    "Generated module loot tier weights cannot be negative"
            );
        }

        int total = engineeringWeight + militaryWeight + experimentalWeight;
        if (total <= 0) {
            throw new IllegalArgumentException(
                    "Generated module loot requires at least one positive tier weight"
            );
        }

        this.chance = chance;
        this.engineeringWeight = engineeringWeight;
        this.militaryWeight = militaryWeight;
        this.experimentalWeight = experimentalWeight;
        this.totalWeight = total;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(
            ObjectArrayList<ItemStack> generatedLoot,
            LootContext context
    ) {
        RandomSource random = context.getRandom();
        if (chance <= 0.0F || random.nextFloat() >= chance) {
            return generatedLoot;
        }

        generatedLoot.add(
                GeneratedModuleGenerator.createStack(
                        rollTier(random),
                        random
                )
        );
        return generatedLoot;
    }

    private EquipmentTier rollTier(RandomSource random) {
        int roll = random.nextInt(totalWeight);
        if (roll < engineeringWeight) {
            return EquipmentTier.ENGINEERING;
        }
        roll -= engineeringWeight;
        if (roll < militaryWeight) {
            return EquipmentTier.MILITARY;
        }
        return EquipmentTier.EXPERIMENTAL;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
