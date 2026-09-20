package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
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
        List<ScanTarget> targets = collectTargets(data, poweredModules);

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

                if (matches(level.getBlockState(pos), target.properties())) {
                    result.add(pos.immutable());
                    break;
                }
            }
        }

        return result.stream()
                .sorted(Comparator.comparingDouble(pos -> pos.distSqr(center)))
                .toList();
    }

    private static List<ScanTarget> collectTargets(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        List<ScanTarget> targets = new ArrayList<>();

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            MatrixData matrix = data.matrices()
                    .get(slot)
                    .matrix()
                    .orElse(null);

            if (matrix == null) {
                continue;
            }

            for (int moduleIndex = 0; moduleIndex < matrix.modules().size(); moduleIndex++) {
                InstalledModule module = matrix.modules().get(moduleIndex);

                if (!FrameOperations.isModuleSupported(data, module)) {
                    continue;
                }

                InstalledModuleReference reference =
                        new InstalledModuleReference(slot, moduleIndex);

                if (!isPowered(module.id(), reference, poweredModules)) {
                    continue;
                }

                ModModules.getDefinition(module.id())
                        .blockScanner()
                        .ifPresent(properties ->
                                targets.add(new ScanTarget(
                                        reference,
                                        module.id(),
                                        properties
                                ))
                        );
            }
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

    private static boolean isPowered(
            ResourceLocation moduleId,
            InstalledModuleReference reference,
            Set<InstalledModuleReference> poweredModules
    ) {
        var energy = ModModules.getDefinition(moduleId).energy();

        return energy.isEmpty()
                || energy.get().consumption() <= 0
                || poweredModules.contains(reference);
    }

    private record ScanTarget(
            InstalledModuleReference reference,
            ResourceLocation moduleId,
            BlockScannerProperties properties
    ) {}
}
