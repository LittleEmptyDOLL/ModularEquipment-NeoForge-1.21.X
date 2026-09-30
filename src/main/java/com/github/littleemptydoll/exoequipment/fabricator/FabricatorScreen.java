package com.github.littleemptydoll.exoequipment.fabricator;

import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.network.FabricatorCraftPayload;
import com.github.littleemptydoll.exoequipment.registry.EquipmentItem;
import com.github.littleemptydoll.exoequipment.registry.ModFabricatorRecipes;
import com.github.littleemptydoll.exoequipment.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class FabricatorScreen extends AbstractContainerScreen<FabricatorMenu> {
    private static final int BACKGROUND = 0xFF14212B, SLOT = 0xFF35434D, TEXT = 0xFFDEE6E9;
    private static final int PANEL = 0xFF20313B, SELECTED = 0xFF35667A;
    private static final int VISIBLE = 6, ENERGY_X = 317, ENERGY_Y = 43, ENERGY_HEIGHT = 113;
    private FabricatorCategory category = FabricatorCategory.EXOSKELETON;
    private int subcategory = -1, scroll, selection, ingredientScroll;
    private ResourceLocation focusedRecipe;
    private final ArrayDeque<ResourceLocation> recipeHistory = new ArrayDeque<>();
    private Button craftOne, craftFive, craftMax;

    public FabricatorScreen(FabricatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 336;
        imageHeight = 252;
    }

    @Override
    protected void init() {
        super.init();
        craftOne = addRenderableWidget(Button.builder(Component.literal("×1"), b -> craft(1))
                .bounds(leftPos + 211, topPos + 142, 29, 17).build());
        craftFive = addRenderableWidget(Button.builder(Component.literal("×5"), b -> craft(5))
                .bounds(leftPos + 243, topPos + 142, 29, 17).build());
        craftMax = addRenderableWidget(Button.builder(Component.translatable("gui.exoequipment.fabricator.max"),
                b -> craft(64)).bounds(leftPos + 275, topPos + 142, 30, 17).build());
    }

    private void craft(int amount) {
        RecipeHolder<FabricatorRecipe> selected = selected();
        if (selected != null) {
            PacketDistributor.sendToServer(new FabricatorCraftPayload(selected.id(), amount));
        }
    }

    private List<RecipeHolder<FabricatorRecipe>> recipes() {
        List<RecipeHolder<FabricatorRecipe>> list = new ArrayList<>();
        if (Minecraft.getInstance().level == null) return list;
        for (var holder : Minecraft.getInstance().level.getRecipeManager()
                .getAllRecipesFor(ModFabricatorRecipes.TYPE.get())) {
            if (!(holder.value().result().getItem() instanceof EquipmentItem<?> item)
                    || FabricatorCategory.of(item) != category) continue;
            if (category == FabricatorCategory.MODULE && subcategory >= 0
                    && (item instanceof ModuleItem module)
                    && module.getDefinition().category() != ModuleCategory.values()[subcategory]) continue;
            list.add(holder);
        }
        list.sort(Comparator.comparing(holder -> holder.id().toString()));
        return list;
    }

    private RecipeHolder<FabricatorRecipe> selected() {
        if (focusedRecipe != null && Minecraft.getInstance().level != null) {
            for (var holder : Minecraft.getInstance().level.getRecipeManager()
                    .getAllRecipesFor(ModFabricatorRecipes.TYPE.get())) {
                if (holder.id().equals(focusedRecipe)) return holder;
            }
            return null;
        }
        List<RecipeHolder<FabricatorRecipe>> list = recipes();
        return selection >= 0 && selection < list.size() ? list.get(selection) : null;
    }

    private void openPart(ItemStack stack) {
        if (!ModItems.isFabricatorPart(stack.getItem()) || minecraft == null || minecraft.level == null) return;
        RecipeHolder<FabricatorRecipe> current = selected();
        if (current == null) return;
        for (var holder : minecraft.level.getRecipeManager().getAllRecipesFor(ModFabricatorRecipes.TYPE.get())) {
            if (holder.value().result().is(stack.getItem())) {
                recipeHistory.push(current.id());
                focusedRecipe = holder.id();
                ingredientScroll = 0;
                return;
            }
        }
    }

    private void resetPartNavigation() {
        focusedRecipe = null;
        recipeHistory.clear();
        ingredientScroll = 0;
    }

    private int listLeft() {
        return category == FabricatorCategory.MODULE ? 77 : 29;
    }

    private static Component moduleCategoryName(int index) {
        if (index < 0) return Component.translatable("gui.exoequipment.fabricator.all");
        String name = ModuleCategory.values()[index].name().toLowerCase(Locale.ROOT);
        return Component.translatable("gui.exoequipment.fabricator.module_category." + name);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, BACKGROUND);
        graphics.fill(x + 27, y + 20, x + 309, y + 160, PANEL);
        for (int i = 0; i < FabricatorCategory.values().length; i++) {
            int left = x + 29 + i * 46;
            graphics.fill(left, y + 22, left + 44, y + 39,
                    FabricatorCategory.values()[i] == category ? SELECTED : SLOT);
        }
        if (category == FabricatorCategory.MODULE) {
            graphics.fill(x + 29, y + 41, x + 75, y + 151, SLOT);
            for (int i = -1; i < ModuleCategory.values().length; i++) {
                int top = y + 42 + (i + 1) * 12;
                if (i == subcategory) graphics.fill(x + 30, top, x + 74, top + 12, SELECTED);
            }
        }
        graphics.fill(x + listLeft() - 1, y + 41, x + 158, y + 140, SLOT);
        graphics.fill(x + 160, y + 41, x + 307, y + 140, SLOT);
        for (int i = 0; i < FabricatorUnlocks.UPGRADE_SLOT_COUNT; i++) {
            slotBackground(graphics, x + 6, y + 57 + i * 25);
        }
        graphics.fill(x + ENERGY_X - 2, y + ENERGY_Y - 2,
                x + ENERGY_X + 13, y + ENERGY_Y + ENERGY_HEIGHT + 2, SLOT);
        graphics.fill(x + ENERGY_X, y + ENERGY_Y,
                x + ENERGY_X + 11, y + ENERGY_Y + ENERGY_HEIGHT, 0xFF0D151A);
        int energy = Math.max(0, Math.min(menu.energy(), FabricatorBlockEntity.CAPACITY));
        int filled = ENERGY_HEIGHT * energy / FabricatorBlockEntity.CAPACITY;
        graphics.fill(x + ENERGY_X + 1, y + ENERGY_Y + ENERGY_HEIGHT - filled,
                x + ENERGY_X + 10, y + ENERGY_Y + ENERGY_HEIGHT, 0xFF49B5D6);
        RecipeHolder<FabricatorRecipe> chosen = selected();
        if (chosen != null && minecraft != null && minecraft.level != null
                && (minecraft.level.getGameTime() / 10) % 2 == 0) {
            int cost = chosen.value().energyCost();
            int height = Math.max(2, (int) Math.ceil((double) cost * ENERGY_HEIGHT
                    / FabricatorBlockEntity.CAPACITY));
            int top = y + ENERGY_Y + ENERGY_HEIGHT - filled;
            if (energy >= cost) {
                graphics.fill(x + ENERGY_X + 1, top,
                        x + ENERGY_X + 10, Math.min(top + height, y + ENERGY_Y + ENERGY_HEIGHT), 0xFFE6F6FF);
            } else {
                graphics.fill(x + ENERGY_X + 1, Math.max(y + ENERGY_Y, top - height),
                        x + ENERGY_X + 10, Math.max(y + ENERGY_Y + 2, top), 0xFFE87070);
            }
        }
        graphics.fill(x + 85, y + 173, x + 251, y + 251, SLOT);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                slotBackground(graphics, x + 86 + col * 18, y + 174 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) slotBackground(graphics, x + 86 + col * 18, y + 232);
    }

    private static void slotBackground(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, SLOT);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF0D151A);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 29, 7, TEXT, false);
        FabricatorCategory[] categories = FabricatorCategory.values();
        for (int i = 0; i < categories.length; i++) {
            Component name = Component.translatable(categories[i].translationKey());
            String shortName = font.plainSubstrByWidth(name.getString(), 40);
            graphics.drawString(font, shortName, 32 + i * 46, 26, TEXT, false);
        }
        if (category == FabricatorCategory.MODULE) {
            for (int i = -1; i < ModuleCategory.values().length; i++) {
                graphics.drawString(font, font.plainSubstrByWidth(moduleCategoryName(i).getString(), 42),
                        31, 44 + (i + 1) * 12, TEXT, false);
            }
        }
        List<RecipeHolder<FabricatorRecipe>> list = recipes();
        for (int row = 0; row < VISIBLE && scroll + row < list.size(); row++) {
            int index = scroll + row, top = 42 + row * 16;
            ItemStack output = list.get(index).value().result();
            if (index == selection) graphics.fill(listLeft(), top, 157, top + 16, SELECTED);
            graphics.renderItem(output, listLeft(), top);
            graphics.drawString(font,
                    font.plainSubstrByWidth(output.getHoverName().getString(), 157 - listLeft() - 19),
                    listLeft() + 18, top + 4, TEXT, false);
        }
        RecipeHolder<FabricatorRecipe> selected = selected();
        if (selected != null) {
            FabricatorRecipe recipe = selected.value();
            graphics.renderItem(recipe.result(), 163, 43);
            graphics.drawString(font, font.plainSubstrByWidth(recipe.result().getHoverName().getString(),
                            recipeHistory.isEmpty() ? 121 : 102),
                    183, 47, TEXT, false);
            if (!recipeHistory.isEmpty()) {
                graphics.drawString(font, "<", 293, 47, TEXT, false);
            }
            graphics.drawString(font, Component.translatable("gui.exoequipment.fabricator.cost",
                    recipe.energyCost()), 163, 65, TEXT, false);
            if (recipe.requirements().size() > 4) {
                graphics.drawString(font, (ingredientScroll * 2 + 1) + "-"
                        + Math.min(recipe.requirements().size(), ingredientScroll * 2 + 4)
                        + "/" + recipe.requirements().size(), 266, 76, TEXT, false);
            }
            for (int cell = 0; cell < 4; cell++) {
                int index = ingredientScroll * 2 + cell;
                if (index >= recipe.requirements().size()) break;
                FabricatorIngredient entry = recipe.requirements().get(index);
                ItemStack[] alternatives = entry.ingredient().getItems();
                if (alternatives.length == 0) continue;
                int ix = 163 + (cell % 2) * 73, iy = 85 + (cell / 2) * 25;
                graphics.renderItem(alternatives[0], ix, iy);
                int available = 0;
                if (minecraft != null && minecraft.player != null) {
                    for (int slot = 0; slot < 36; slot++) {
                        ItemStack held = minecraft.player.getInventory().getItem(slot);
                        if (entry.ingredient().test(held)) available += held.getCount();
                    }
                }
                graphics.drawString(font, available + "/" + entry.count(), ix + 19, iy + 4,
                        available >= entry.count() ? TEXT : 0xFFFF9999, false);
            }
        }
        graphics.drawString(font, Component.translatable("gui.exoequipment.fabricator.craft"),
                163, 146, TEXT, false);
        graphics.drawString(font, playerInventoryTitle, 87, 162, TEXT, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        RecipeHolder<FabricatorRecipe> chosen = selected();
        boolean enabled = chosen != null && menu.energy() >= chosen.value().energyCost();
        craftOne.active = craftFive.active = craftMax.active = enabled;
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (!menu.getCarried().isEmpty()) return;
        int x = mouseX - leftPos, y = mouseY - topPos;
        if (x >= ENERGY_X - 2 && x < ENERGY_X + 13
                && y >= ENERGY_Y - 2 && y < ENERGY_Y + ENERGY_HEIGHT + 2) {
            graphics.renderTooltip(font, Component.translatable("gui.exoequipment.fabricator.energy",
                    menu.energy(), FabricatorBlockEntity.CAPACITY), mouseX, mouseY);
            return;
        }
        for (int i = 0; i < FabricatorUnlocks.UPGRADE_SLOT_COUNT; i++) {
            if (x >= 6 && x < 24 && y >= 57 + i * 25 && y < 75 + i * 25
                    && !menu.slots.get(i).hasItem()) {
                String tier = switch (i) {
                    case FabricatorUnlocks.MILITARY_SLOT -> "military";
                    case FabricatorUnlocks.ENGINEERING_SLOT -> "engineering";
                    default -> "experimental";
                };
                graphics.renderTooltip(font, Component.translatable("gui.exoequipment.fabricator." + tier),
                        mouseX, mouseY);
                return;
            }
        }
        if (y >= 22 && y < 39) {
            for (int i = 0; i < FabricatorCategory.values().length; i++) {
                if (x >= 29 + i * 46 && x < 73 + i * 46) {
                    graphics.renderTooltip(font,
                            Component.translatable(FabricatorCategory.values()[i].translationKey()), mouseX, mouseY);
                    return;
                }
            }
        }
        if (category == FabricatorCategory.MODULE && x >= 30 && x < 74 && y >= 42 && y < 150) {
            graphics.renderTooltip(font, moduleCategoryName((y - 42) / 12 - 1), mouseX, mouseY);
            return;
        }
        List<RecipeHolder<FabricatorRecipe>> list = recipes();
        if (x >= listLeft() && x < 157 && y >= 42 && y < 138) {
            int row = (y - 42) / 16;
            if (scroll + row < list.size()) {
                graphics.renderTooltip(font, list.get(scroll + row).value().result(), mouseX, mouseY);
                return;
            }
        }
        if (chosen == null) return;
        if (!recipeHistory.isEmpty() && x >= 289 && x < 305 && y >= 43 && y < 59) {
            graphics.renderTooltip(font, Component.translatable("gui.exoequipment.back"), mouseX, mouseY);
            return;
        }
        if (x >= 163 && x < 179 && y >= 43 && y < 59) {
            graphics.renderTooltip(font, chosen.value().result(), mouseX, mouseY);
            return;
        }
        for (int cell = 0; cell < 4; cell++) {
            int index = ingredientScroll * 2 + cell;
            if (index >= chosen.value().requirements().size()) break;
            int ix = 163 + (cell % 2) * 73, iy = 85 + (cell / 2) * 25;
            if (x >= ix && x < ix + 16 && y >= iy && y < iy + 16) {
                ItemStack[] alternatives = chosen.value().requirements().get(index).ingredient().getItems();
                if (alternatives.length > 0) graphics.renderTooltip(font, alternatives[0], mouseX, mouseY);
                return;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (int) mouseX - leftPos, y = (int) mouseY - topPos;
        if (button == 0) {
            for (int i = 0; i < FabricatorCategory.values().length; i++) {
                if (x >= 29 + i * 46 && x < 73 + i * 46 && y >= 22 && y < 39) {
                    category = FabricatorCategory.values()[i];
                    subcategory = -1;
                    selection = scroll = 0;
                    resetPartNavigation();
                    return true;
                }
            }
            if (category == FabricatorCategory.MODULE
                    && x >= 30 && x < 74 && y >= 42 && y < 150) {
                subcategory = (y - 42) / 12 - 1;
                selection = scroll = 0;
                resetPartNavigation();
                return true;
            }
            if (x >= listLeft() && x < 157 && y >= 42 && y < 138) {
                int row = (y - 42) / 16;
                if (row < VISIBLE && scroll + row < recipes().size()) {
                    selection = scroll + row;
                    resetPartNavigation();
                    return true;
                }
            }
            if (!recipeHistory.isEmpty() && x >= 289 && x < 305 && y >= 43 && y < 59) {
                ResourceLocation parent = recipeHistory.pop();
                focusedRecipe = recipeHistory.isEmpty() ? null : parent;
                ingredientScroll = 0;
                return true;
            }
            RecipeHolder<FabricatorRecipe> shown = selected();
            if (shown != null) {
                for (int cell = 0; cell < 4; cell++) {
                    int index = ingredientScroll * 2 + cell;
                    if (index >= shown.value().requirements().size()) break;
                    int ix = 163 + (cell % 2) * 73, iy = 85 + (cell / 2) * 25;
                    if (x >= ix && x < ix + 16 && y >= iy && y < iy + 16) {
                        ItemStack[] alternatives = shown.value().requirements().get(index).ingredient().getItems();
                        if (alternatives.length > 0) openPart(alternatives[0]);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int x = (int) mouseX - leftPos, y = (int) mouseY - topPos;
        if (x >= listLeft() && x < 157 && y >= 42 && y < 138 && scrollY != 0) {
            scroll = Math.max(0, Math.min(Math.max(0, recipes().size() - VISIBLE),
                    scroll + (scrollY > 0 ? -1 : 1)));
            return true;
        }
        if (x >= 160 && x < 307 && y >= 81 && y < 137 && scrollY != 0) {
            RecipeHolder<FabricatorRecipe> chosen = selected();
            if (chosen != null) {
                int rows = (chosen.value().requirements().size() + 1) / 2;
                ingredientScroll = Math.max(0, Math.min(Math.max(0, rows - 2),
                        ingredientScroll + (scrollY > 0 ? -1 : 1)));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
