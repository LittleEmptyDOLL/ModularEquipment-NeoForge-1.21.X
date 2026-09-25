package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;
import java.util.Set;

public final class ExoskeletonAccess {
    private static final String BODY_SLOT = "body";

    private ExoskeletonAccess() {}

    public static Optional<ItemStack> findEquipped(
            LivingEntity entity
    ) {
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(handler -> handler.getStacksHandler(BODY_SLOT))
                .flatMap(stacks -> {
                    for (int slot = 0;
                         slot < stacks.getStacks().getSlots();
                         slot++) {

                        ItemStack stack =
                                stacks.getStacks().getStackInSlot(slot);

                        if (stack.getItem() instanceof ExoskeletonItem) {
                            return Optional.of(stack);
                        }
                    }

                    return Optional.empty();
                });
    }

    public static Optional<Context> findContext(
            LivingEntity entity
    ) {
        return findEquipped(entity)
                .map(ExoskeletonAccess::context);
    }

    public static Context context(
            ItemStack stack
    ) {
        if (!(stack.getItem() instanceof ExoskeletonItem)) {
            throw new IllegalArgumentException(
                    "ItemStack is not an exoskeleton"
            );
        }

        return new Context(
                stack,
                ExoskeletonItem.getData(stack),
                stack.getOrDefault(
                        ModDataComponents.EXOSKELETON_RUNTIME.get(),
                        ExoskeletonRuntimeState.empty()
                )
        );
    }

    public record Context(
            ItemStack stack,
            ExoskeletonData data,
            ExoskeletonRuntimeState runtime
    ) {
        public Set<InstalledModuleReference> poweredModules() {
            return runtime.poweredModules();
        }
    }
}
