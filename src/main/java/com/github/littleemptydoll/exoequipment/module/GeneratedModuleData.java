package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public record GeneratedModuleData(
        long seed,
        ResourceLocation baseDefinitionId,
        ModuleDefinition definition
) {
    public static final Codec<GeneratedModuleData> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.LONG.fieldOf("seed")
                                    .forGetter(GeneratedModuleData::seed),
                            ResourceLocation.CODEC.fieldOf("base_definition")
                                    .forGetter(GeneratedModuleData::baseDefinitionId),
                            ModuleDefinition.CODEC.fieldOf("definition")
                                    .forGetter(GeneratedModuleData::definition)
                    ).apply(instance, GeneratedModuleData::new)
            );

    public GeneratedModuleData {
        if (baseDefinitionId == null) {
            throw new IllegalArgumentException("Generated module base definition cannot be null");
        }
        if (definition == null) {
            throw new IllegalArgumentException("Generated module definition cannot be null");
        }
        if (definition.category() != ModuleCategory.EXPERIMENTAL) {
            throw new IllegalArgumentException(
                    "Generated modules must use the experimental category"
            );
        }
    }

    public static ResourceLocation definitionId(
            EquipmentTier tier,
            long seed
    ) {
        return ResourceLocation.fromNamespaceAndPath(
                ExoEquipment.MODID,
                "generated/"
                        + tier.name().toLowerCase(Locale.ROOT)
                        + "/"
                        + Long.toUnsignedString(seed, 16)
        );
    }

    public String modelCode() {
        String prefix = switch (definition.properties().tier()) {
            case MILITARY -> "M";
            case ENGINEERING -> "E";
            case EXPERIMENTAL -> "X";
            default -> "P";
        };
        int serial = (int) Math.floorMod(seed, 1000L);
        return String.format(Locale.ROOT, "%s-%03d", prefix, serial);
    }

    public boolean matchesTier(EquipmentTier tier) {
        return definition.properties().tier() == tier;
    }
}
