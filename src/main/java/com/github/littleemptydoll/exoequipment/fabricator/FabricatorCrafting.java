package com.github.littleemptydoll.exoequipment.fabricator;

import com.github.littleemptydoll.exoequipment.registry.ModFabricatorRecipes;
import com.github.littleemptydoll.exoequipment.registry.EquipmentItem;
import com.github.littleemptydoll.exoequipment.registry.ModItems;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** All inventory changes are planned before committing energy, ingredients or output. */
public final class FabricatorCrafting {
    private static final int PLAYER_SLOTS = 36;
    private static final int MAX_BATCH = 64;

    private FabricatorCrafting() {}

    public static int craft(ServerPlayer player, FabricatorBlockEntity machine,
                            ResourceLocation recipeId, int requested) {
        if (!(player.containerMenu instanceof FabricatorMenu menu)
                || menu.machine() != machine || !menu.stillValid(player)
                || player.level().getBlockEntity(machine.getBlockPos()) != machine
                || requested < 1 || requested > MAX_BATCH) return 0;
        RecipeHolder<?> holder = player.serverLevel().getRecipeManager().byKey(recipeId).orElse(null);
        if (holder == null || holder.value().getType() != ModFabricatorRecipes.TYPE.get()
                || !(holder.value() instanceof FabricatorRecipe recipe)
                || recipe.requirements().isEmpty() || recipe.requirements().size() > 32
                || recipe.result().isEmpty() || recipe.energyCost() < 1) return 0;
        Item output = recipe.result().getItem();
        EquipmentTier tier;
        if (output instanceof EquipmentItem<?> equipment) {
            tier = equipment.getDefinition().tier();
        } else if (ModItems.isFabricatorPart(output)) {
            tier = EquipmentTier.CIVILIAN;
        } else {
            return 0;
        }

        Inventory inventory = player.getInventory();
        List<ItemStack> current = new ArrayList<>(PLAYER_SLOTS);
        for (int i = 0; i < PLAYER_SLOTS; i++) current.add(inventory.getItem(i).copy());

        // A batch can yield at most one full output stack, including multi-item recipes.
        int maxCrafts = Math.min(requested,
                Math.max(1, recipe.result().getMaxStackSize() / recipe.result().getCount()));
        int crafted = 0;
        int availableEnergy = machine.storedEnergy();
        while (crafted < maxCrafts && availableEnergy >= recipe.energyCost()
                && machine.canCraft(tier, recipe.energyCost())) {
            List<ItemStack> next = new ArrayList<>(PLAYER_SLOTS);
            for (ItemStack stack : current) next.add(stack.copy());
            int[] consumed = allocate(next, recipe.requirements());
            if (consumed == null) break;
            for (int i = 0; i < PLAYER_SLOTS; i++) {
                if (consumed[i] > 0) next.get(i).shrink(consumed[i]);
            }
            if (!insert(next, recipe.result().copy())) break;
            current = next;
            availableEnergy -= recipe.energyCost();
            crafted++;
        }

        if (crafted == 0 || !machine.spendEnergy(crafted * recipe.energyCost())) return 0;
        for (int slot = 0; slot < PLAYER_SLOTS; slot++) {
            inventory.setItem(slot, current.get(slot));
        }
        inventory.setChanged();
        menu.broadcastChanges();
        return crafted;
    }

    /** Capacity matching handles overlapping tags without allocating one stack twice. */
    private static int[] allocate(List<ItemStack> stacks, List<FabricatorIngredient> needs) {
        int count = needs.size(), sink = 1 + count + PLAYER_SLOTS, vertices = sink + 1;
        int[][] remaining = new int[vertices][vertices];
        int total = 0;
        for (int i = 0; i < count; i++) {
            FabricatorIngredient entry = needs.get(i);
            if (entry.count() < 1) return null;
            remaining[0][1 + i] = entry.count();
            total += entry.count();
            for (int slot = 0; slot < PLAYER_SLOTS; slot++) {
                if (!stacks.get(slot).isEmpty() && entry.ingredient().test(stacks.get(slot))) {
                    remaining[1 + i][1 + count + slot] = Math.min(entry.count(), stacks.get(slot).getCount());
                }
            }
        }
        for (int slot = 0; slot < PLAYER_SLOTS; slot++) {
            remaining[1 + count + slot][sink] = stacks.get(slot).getCount();
        }

        int matched = 0;
        while (matched < total) {
            int[] previous = new int[vertices];
            Arrays.fill(previous, -1);
            previous[0] = 0;
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            queue.add(0);
            while (!queue.isEmpty() && previous[sink] < 0) {
                int node = queue.removeFirst();
                for (int other = 1; other < vertices; other++) {
                    if (previous[other] < 0 && remaining[node][other] > 0) {
                        previous[other] = node;
                        queue.addLast(other);
                    }
                }
            }
            if (previous[sink] < 0) return null;
            int amount = total - matched;
            for (int node = sink; node != 0; node = previous[node]) {
                amount = Math.min(amount, remaining[previous[node]][node]);
            }
            for (int node = sink; node != 0; node = previous[node]) {
                remaining[previous[node]][node] -= amount;
                remaining[node][previous[node]] += amount;
            }
            matched += amount;
        }

        int[] used = new int[PLAYER_SLOTS];
        for (int slot = 0; slot < PLAYER_SLOTS; slot++) {
            for (int ingredient = 0; ingredient < count; ingredient++) {
                used[slot] += remaining[1 + count + slot][1 + ingredient];
            }
        }
        return used;
    }

    private static boolean insert(List<ItemStack> stacks, ItemStack result) {
        int left = result.getCount();
        for (int i = 0; i < PLAYER_SLOTS && left > 0; i++) {
            ItemStack stack = stacks.get(i);
            if (ItemStack.isSameItemSameComponents(stack, result)) {
                int added = Math.min(left, stack.getMaxStackSize() - stack.getCount());
                if (added > 0) {
                    stack.grow(added);
                    left -= added;
                }
            }
        }
        for (int i = 0; i < PLAYER_SLOTS && left > 0; i++) {
            if (stacks.get(i).isEmpty()) {
                int added = Math.min(left, result.getMaxStackSize());
                stacks.set(i, result.copyWithCount(added));
                left -= added;
            }
        }
        return left == 0;
    }
}
