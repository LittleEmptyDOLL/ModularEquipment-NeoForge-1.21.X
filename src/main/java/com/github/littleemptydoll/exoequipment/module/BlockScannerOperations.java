package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BlockScannerOperations {
    private BlockScannerOperations() {}

    public static List<BlockPos> scanBlocks(
            Level level,
            BlockPos center,
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        List<ScanTarget> targets = collectTargets(
                data,
                poweredModules
        );

        if (targets.isEmpty()) {
            return List.of();
        }

        double maxRange = targets.stream()
                .mapToDouble(target -> target.properties().range())
                .max()
                .orElse(0.0D);

        int radius = (int) Math.ceil(maxRange);
        Set<BlockPos> result = new HashSet<>();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius)
        )) {
            double distanceSqr = pos.distSqr(center);

            for (ScanTarget target : targets) {
                double range = target.properties().range();

                if (distanceSqr > range * range) {
                    continue;
                }

                if (matches(
                        level.getBlockState(pos),
                        target.properties()
                )) {
                    result.add(pos.immutable());
                    break;
                }
            }
        }

        return result.stream()
                .sorted(Comparator.comparingDouble(
                        pos -> pos.distSqr(center)
                ))
                .toList();
    }

    private static List<ScanTarget> collectTargets(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        List<ScanTarget> targets = new ArrayList<>();

        for (ExoskeletonModules.ActiveModule activeModule
                : ExoskeletonModules.activeSupported(data)) {

            if (!ExoskeletonModules.isPowered(
                    activeModule,
                    poweredModules
            )) {
                continue;
            }

            activeModule.definition()
                    .blockScanner()
                    .ifPresent(properties ->
                            targets.add(
                                    new ScanTarget(properties)
                            )
                    );
        }

        return List.copyOf(targets);
    }

    private static boolean matches(
            BlockState state,
            BlockScannerProperties properties
    ) {
        for (ResourceLocation blockId : properties.blocks()) {
            Block block = BuiltInRegistries.BLOCK.get(blockId);

            if (block != Blocks.AIR && state.is(block)) {
                return true;
            }
        }

        for (ResourceLocation tagId : properties.tags()) {
            if (state.is(TagKey.create(Registries.BLOCK, tagId))) {
                return true;
            }
        }

        return false;
    }

    private record ScanTarget(
            BlockScannerProperties properties
    ) {}
}
