package com.github.littleemptydoll.exoequipment.fabricator;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.network.FabricatorCraftPayload;
import com.github.littleemptydoll.exoequipment.registry.EquipmentItem;
import com.github.littleemptydoll.exoequipment.registry.ModFabricatorRecipes;
import com.github.littleemptydoll.exoequipment.registry.ModItems;
import com.github.littleemptydoll.exoequipment.util.NumberFormatter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class FabricatorScreen extends AbstractContainerScreen<FabricatorMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ExoEquipment.MODID, "textures/gui/fabricator.png");
    private static final int ATLAS = 350, TEXT = 0xFFDEE6E9;
    private static final int TAB_X = 94, TAB_Y = 6, TAB_STEP = 21;
    private static final int CATEGORY_X = 6, CATEGORY_Y = 32, CATEGORY_STEP = 13, CATEGORY_VISIBLE = 7;
    private static final int ITEM_Y = 35, ROW_STEP = 17, VISIBLE = 5, RESOURCE_VISIBLE = 4;
    private static final int RESOURCE_X = 163, RESOURCE_Y = 35;
    private static final int ENERGY_X = 229, ENERGY_Y = 36, ENERGY_HEIGHT = 85;
    private static final int CRAFT_X = 163, CRAFT_Y = 107;
    private static final int MODULE_SCROLL = 1, ITEM_SCROLL = 2, RESOURCE_SCROLL = 3;
    private FabricatorCategory category = FabricatorCategory.COMPONENTS;
    private int subcategory = -1, categoryScroll, scroll, selection, ingredientScroll, dragging;
    private ResourceLocation focusedRecipe;
    private final ArrayDeque<ResourceLocation> recipeHistory = new ArrayDeque<>();
    private final ItemStack[] tabIcons = new ItemStack[FabricatorCategory.values().length];

    public FabricatorScreen(FabricatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 244;
        imageHeight = 237;
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < tabIcons.length; i++) {
            tabIcons[i] = tabIcon(FabricatorCategory.values()[i]);
        }
    }

    private List<RecipeHolder<FabricatorRecipe>> recipes() {
        List<RecipeHolder<FabricatorRecipe>> list = new ArrayList<>();
        if (minecraft == null || minecraft.level == null) return list;
        for (var holder : minecraft.level.getRecipeManager().getAllRecipesFor(ModFabricatorRecipes.TYPE.get())) {
            Item item = holder.value().result().getItem();
            if (!(item instanceof EquipmentItem<?>) && !ModItems.isFabricatorPart(item)) continue;
            if (FabricatorCategory.of(item) != category) continue;
            if (category == FabricatorCategory.MODULE && subcategory >= 0
                    && item instanceof ModuleItem module
                    && module.getDefinition().category() != ModuleCategory.values()[subcategory]) continue;
            list.add(holder);
        }
        list.sort(Comparator
                .comparingInt((RecipeHolder<FabricatorRecipe> holder) -> {
                    Item item = holder.value().result().getItem();
                    return item instanceof ModuleItem module ? module.getDefinition().category().ordinal() : -1;
                })
                .thenComparingInt(holder -> {
                    Item item = holder.value().result().getItem();
                    return item instanceof EquipmentItem<?> equipment ? equipment.getDefinition().tier().ordinal() : -1;
                })
                .thenComparing(holder -> BuiltInRegistries.ITEM.getKey(holder.value().result().getItem()).toString())
                .thenComparing(holder -> holder.id().toString()));
        return list;
    }

    private ItemStack tabIcon(FabricatorCategory tab) {
        if (minecraft == null || minecraft.level == null) return ItemStack.EMPTY;
        return minecraft.level.getRecipeManager().getAllRecipesFor(ModFabricatorRecipes.TYPE.get()).stream()
                .filter(holder -> {
                    Item item = holder.value().result().getItem();
                    return (item instanceof EquipmentItem<?> || ModItems.isFabricatorPart(item))
                            && FabricatorCategory.of(item) == tab;
                })
                .min(Comparator.comparing(holder -> holder.id().toString()))
                .map(holder -> holder.value().result()).orElse(ItemStack.EMPTY);
    }

    private RecipeHolder<FabricatorRecipe> selected() {
        if (focusedRecipe != null && minecraft != null && minecraft.level != null) {
            for (var holder : minecraft.level.getRecipeManager().getAllRecipesFor(ModFabricatorRecipes.TYPE.get())) {
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

    private void craft() {
        RecipeHolder<FabricatorRecipe> chosen = selected();
        if (chosen != null && menu.energy() >= chosen.value().energyCost()) {
            PacketDistributor.sendToServer(new FabricatorCraftPayload(chosen.id(), Screen.hasShiftDown() ? 64 : 1));
        }
    }

    private int listLeft() {
        return category == FabricatorCategory.MODULE ? 70 : 7;
    }

    private static Component moduleCategoryName(int index) {
        if (index < 0) return Component.translatable("gui.exoequipment.fabricator.all");
        return Component.translatable("gui.exoequipment.fabricator.module_category."
                + ModuleCategory.values()[index].name().toLowerCase(Locale.ROOT));
    }

    private static boolean inside(int x, int y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }

    private void blit(GuiGraphics graphics, int x, int y, int u, int v, int width, int height) {
        graphics.blit(TEXTURE, leftPos + x, topPos + y, u, v, width, height, ATLAS, ATLAS);
    }

    private void frame(GuiGraphics graphics, int x, int y, boolean selected, boolean hovered) {
        blit(graphics, x, y, 25, 279, 18, 18);
        if (hovered) blit(graphics, x, y, 25, 297, 18, 18);
        if (selected) blit(graphics, x, y, 43, 279, 18, 18);
    }

    private void scrollbar(GuiGraphics graphics, int x, int offset, int max) {
        blit(graphics, x, 34, 16, 237, 3, 88);
        blit(graphics, x, 34, 19, offset > 0 ? 237 : 241, 3, 4);
        blit(graphics, x, 118, 22, offset < max ? 237 : 241, 3, 4);
        blit(graphics, x, 38 + (max == 0 ? 0 : offset * 56 / max),
                max == 0 ? 22 : 19, 245, 3, 24);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        blit(graphics, 0, 0, 0, 0, imageWidth, imageHeight);
        blit(graphics, -25, 26, 93, 237, 25, 64);
        int mx = mouseX - leftPos, my = mouseY - topPos;
        FabricatorCategory[] tabs = FabricatorCategory.values();
        for (int i = 0; i < tabs.length; i++) {
            int x = TAB_X + i * TAB_STEP;
            frame(graphics, x, TAB_Y, tabs[i] == category, inside(mx, my, x, TAB_Y, 18, 18));
            ItemStack icon = tabIcons[i];
            if (!icon.isEmpty()) graphics.renderItem(icon, leftPos + x + 1, topPos + TAB_Y + 1);
        }
        if (category == FabricatorCategory.MODULE) {
            blit(graphics, 57, 27, 0, 237, 16, 102);
            categoryScroll = Math.min(categoryScroll, maxScroll(MODULE_SCROLL));
            for (int pass = 0; pass < 2; pass++) {
                for (int row = 0; row < CATEGORY_VISIBLE && categoryScroll + row <= ModuleCategory.values().length; row++) {
                    int value = categoryScroll + row - 1;
                    if ((value == subcategory) == (pass == 1)) drawCategory(graphics, mx, my, row, value);
                }
            }
            scrollbar(graphics, 58, categoryScroll, maxScroll(MODULE_SCROLL));
        }
        List<RecipeHolder<FabricatorRecipe>> list = recipes();
        scroll = Math.min(scroll, maxScroll(ITEM_SCROLL));
        for (int pass = 0; pass < 2; pass++) {
            for (int row = 0; row < VISIBLE && scroll + row < list.size(); row++) {
                boolean chosen = scroll + row == selection;
                if (chosen == (pass == 1)) drawItemRow(graphics, mx, my, row, list.get(scroll + row), chosen);
            }
        }
        scrollbar(graphics, 151, scroll, maxScroll(ITEM_SCROLL));
        RecipeHolder<FabricatorRecipe> chosen = selected();
        int count = chosen == null ? 0 : chosen.value().requirements().size();
        ingredientScroll = Math.min(ingredientScroll, Math.max(0, count - RESOURCE_VISIBLE));
        if (chosen != null) {
            for (int row = 0; row < RESOURCE_VISIBLE && ingredientScroll + row < count; row++) {
                drawIngredientRow(graphics, row, chosen.value().requirements().get(ingredientScroll + row));
            }
        }
        scrollbar(graphics, 218, ingredientScroll, Math.max(0, count - RESOURCE_VISIBLE));
        drawEnergy(graphics, chosen, partialTick);
        boolean enabled = chosen != null && menu.energy() >= chosen.value().energyCost();
        blit(graphics, CRAFT_X, CRAFT_Y, 25, 265, 50, 14);
        if (enabled && inside(mx, my, CRAFT_X, CRAFT_Y, 50, 14))
            blit(graphics, CRAFT_X, CRAFT_Y, 25, 251, 50, 14);
        graphics.drawString(font, Component.translatable("gui.exoequipment.fabricator.craft"),
                leftPos + CRAFT_X + 5, topPos + CRAFT_Y + 3, enabled ? TEXT : 0xFF80888E, false);
        if (!recipeHistory.isEmpty()) graphics.drawString(font, "<", leftPos + 137, topPos + 133, TEXT, false);
        if (focusedRecipe != null && chosen != null) {
            graphics.renderItem(chosen.value().result(), leftPos + 147, topPos + 129);
        }
    }

    private void drawCategory(GuiGraphics graphics, int mx, int my, int row, int value) {
        int y = CATEGORY_Y + row * CATEGORY_STEP;
        blit(graphics, CATEGORY_X, y, 25, 265, 50, 14);
        if (inside(mx, my, CATEGORY_X, y, 50, CATEGORY_STEP))
            blit(graphics, CATEGORY_X, y, 25, 251, 50, 14);
        if (value == subcategory) blit(graphics, CATEGORY_X, y, 25, 237, 50, 14);
        graphics.drawString(font, font.plainSubstrByWidth(moduleCategoryName(value).getString(), 44),
                leftPos + CATEGORY_X + 3, topPos + y + 4, TEXT, false);
    }

    private void drawItemRow(GuiGraphics graphics, int mx, int my, int row,
                             RecipeHolder<FabricatorRecipe> holder, boolean chosen) {
        int x = listLeft(), y = ITEM_Y + row * ROW_STEP;
        frame(graphics, x, y, chosen, inside(mx, my, x, y, 18, ROW_STEP));
        ItemStack stack = holder.value().result();
        graphics.renderItem(stack, leftPos + x + 1, topPos + y + 1);
        graphics.drawString(font, font.plainSubstrByWidth(stack.getHoverName().getString(),
                        category == FabricatorCategory.MODULE ? 57 : 120),
                leftPos + x + 21, topPos + y + 8, TEXT, false);
    }

    private void drawIngredientRow(GuiGraphics graphics, int row, FabricatorIngredient entry) {
        int y = RESOURCE_Y + row * ROW_STEP;
        frame(graphics, RESOURCE_X, y, false, false);
        ItemStack[] alternatives = entry.ingredient().getItems();
        if (alternatives.length == 0) return;
        graphics.renderItem(alternatives[0], leftPos + RESOURCE_X + 1, topPos + y + 1);
        int available = 0;
        if (minecraft != null && minecraft.player != null) {
            for (int slot = 0; slot < 36; slot++) {
                ItemStack held = minecraft.player.getInventory().getItem(slot);
                if (entry.ingredient().test(held)) available += held.getCount();
            }
        }
        String amount = NumberFormatter.format(available) + "/" + NumberFormatter.format(entry.count());
        float scale = Math.min(1.0f, 33.0f / Math.max(1, font.width(amount)));
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos + RESOURCE_X + 21, topPos + y + 8, 0);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(font, amount, 0, 0, available >= entry.count() ? TEXT : 0xFFFF9999, false);
        graphics.pose().popPose();
    }

    private void drawEnergy(GuiGraphics graphics, RecipeHolder<FabricatorRecipe> chosen, float partialTick) {
        blit(graphics, ENERGY_X, ENERGY_Y, 75, 237, 9, ENERGY_HEIGHT);
        int energy = Math.max(0, Math.min(menu.energy(), FabricatorBlockEntity.CAPACITY));
        int filled = ENERGY_HEIGHT * energy / FabricatorBlockEntity.CAPACITY;
        if (filled > 0) blit(graphics, ENERGY_X, ENERGY_Y + ENERGY_HEIGHT - filled,
                84, 237 + ENERGY_HEIGHT - filled, 9, filled);
        if (chosen == null) return;
        int costHeight = Math.max(2, (int) Math.ceil((double) chosen.value().energyCost() * ENERGY_HEIGHT
                / FabricatorBlockEntity.CAPACITY));
        int top = ENERGY_Y + ENERGY_HEIGHT - filled;
        int overlayTop = energy >= chosen.value().energyCost() ? top : Math.max(ENERGY_Y, top - costHeight);
        int overlayBottom = energy >= chosen.value().energyCost()
                ? Math.min(ENERGY_Y + ENERGY_HEIGHT, top + costHeight) : Math.max(top, overlayTop + 2);
        overlayTop = Math.max(ENERGY_Y + 1, Math.min(ENERGY_Y + ENERGY_HEIGHT - 2, overlayTop));
        overlayBottom = Math.max(overlayTop + 1, Math.min(ENERGY_Y + ENERGY_HEIGHT - 1, overlayBottom));
        float time = minecraft == null || minecraft.level == null ? 0 : minecraft.level.getGameTime() + partialTick;
        int alpha = 35 + (int) (125 * (0.5 + 0.5 * Math.sin(time * Math.PI / 20)));
        graphics.fill(leftPos + ENERGY_X + 1, topPos + overlayTop,
                leftPos + ENERGY_X + 8, topPos + overlayBottom,
                (alpha << 24) | (energy >= chosen.value().energyCost() ? 0x00E6F6FF : 0x00FF7373));
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 9, 11, TEXT, false);
        graphics.drawString(font, playerInventoryTitle, 42, 132, TEXT, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (!menu.getCarried().isEmpty()) return;
        int x = mouseX - leftPos, y = mouseY - topPos;
        RecipeHolder<FabricatorRecipe> chosen = selected();
        if (inside(x, y, ENERGY_X, ENERGY_Y, 9, ENERGY_HEIGHT)) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("gui.exoequipment.fabricator.energy",
                    NumberFormatter.format(menu.energy()), NumberFormatter.format(FabricatorBlockEntity.CAPACITY)));
            if (chosen != null) lines.add(Component.translatable("gui.exoequipment.fabricator.cost",
                    NumberFormatter.format(chosen.value().energyCost())));
            graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
            return;
        }
        for (int i = 0; i < FabricatorUnlocks.UPGRADE_SLOT_COUNT; i++) {
            if (inside(x, y, -19, 32 + i * 20, 18, 18) && !menu.slots.get(i).hasItem()) {
                String tier = switch (i) {
                    case FabricatorUnlocks.MILITARY_SLOT -> "military";
                    case FabricatorUnlocks.ENGINEERING_SLOT -> "engineering";
                    default -> "experimental";
                };
                graphics.renderTooltip(font, Component.translatable("gui.exoequipment.fabricator." + tier), mouseX, mouseY);
                return;
            }
        }
        for (int i = 0; i < FabricatorCategory.values().length; i++) {
            if (inside(x, y, TAB_X + i * TAB_STEP, TAB_Y, 18, 18)) {
                graphics.renderTooltip(font, Component.translatable(FabricatorCategory.values()[i].translationKey()), mouseX, mouseY);
                return;
            }
        }
        if (category == FabricatorCategory.MODULE && inside(x, y, CATEGORY_X, CATEGORY_Y, 50, 92)) {
            int row = (y - CATEGORY_Y) / CATEGORY_STEP, index = categoryScroll + row - 1;
            if (row < CATEGORY_VISIBLE && index < ModuleCategory.values().length)
                graphics.renderTooltip(font, moduleCategoryName(index), mouseX, mouseY);
            return;
        }
        if (inside(x, y, listLeft(), ITEM_Y, 151 - listLeft(), 86)) {
            int row = (y - ITEM_Y) / ROW_STEP;
            List<RecipeHolder<FabricatorRecipe>> list = recipes();
            if (row < VISIBLE && scroll + row < list.size())
                graphics.renderTooltip(font, list.get(scroll + row).value().result(), mouseX, mouseY);
            return;
        }
        if (chosen == null) return;
        if (focusedRecipe != null && inside(x, y, 147, 129, 16, 16)) {
            graphics.renderTooltip(font, chosen.value().result(), mouseX, mouseY);
            return;
        }
        if (inside(x, y, CRAFT_X, CRAFT_Y, 50, 14) && Screen.hasShiftDown()) {
            graphics.renderTooltip(font, Component.translatable("gui.exoequipment.fabricator.craft_stack"), mouseX, mouseY);
            return;
        }
        if (!recipeHistory.isEmpty() && inside(x, y, 133, 130, 15, 16)) {
            graphics.renderTooltip(font, Component.translatable("gui.exoequipment.back"), mouseX, mouseY);
            return;
        }
        if (inside(x, y, RESOURCE_X, RESOURCE_Y, 18, RESOURCE_VISIBLE * ROW_STEP + 1)) {
            int row = (y - RESOURCE_Y) / ROW_STEP, index = ingredientScroll + row;
            if (row < RESOURCE_VISIBLE && index < chosen.value().requirements().size()) {
                ItemStack[] alternatives = chosen.value().requirements().get(index).ingredient().getItems();
                if (alternatives.length > 0) graphics.renderTooltip(font, alternatives[0], mouseX, mouseY);
            }
        }
    }

    private int maxScroll(int kind) {
        return switch (kind) {
            case MODULE_SCROLL -> Math.max(0, ModuleCategory.values().length + 1 - CATEGORY_VISIBLE);
            case ITEM_SCROLL -> Math.max(0, recipes().size() - VISIBLE);
            case RESOURCE_SCROLL -> {
                RecipeHolder<FabricatorRecipe> chosen = selected();
                yield chosen == null ? 0 : Math.max(0, chosen.value().requirements().size() - RESOURCE_VISIBLE);
            }
            default -> 0;
        };
    }

    private int scrollOffset(int kind) {
        return switch (kind) {
            case MODULE_SCROLL -> categoryScroll;
            case ITEM_SCROLL -> scroll;
            case RESOURCE_SCROLL -> ingredientScroll;
            default -> 0;
        };
    }

    private void setScroll(int kind, int value) {
        int limited = Math.max(0, Math.min(maxScroll(kind), value));
        switch (kind) {
            case MODULE_SCROLL -> categoryScroll = limited;
            case ITEM_SCROLL -> scroll = limited;
            case RESOURCE_SCROLL -> ingredientScroll = limited;
        }
    }

    private boolean clickScrollbar(int x, int y, int kind, int barX) {
        if (!inside(x, y, barX - 2, 34, 7, 88)) return false;
        int offset = scrollOffset(kind);
        if (y < 38) setScroll(kind, offset - 1);
        else if (y >= 118) setScroll(kind, offset + 1);
        else {
            int thumbY = 38 + (maxScroll(kind) == 0 ? 0 : offset * 56 / maxScroll(kind));
            if (y < thumbY || y >= thumbY + 24)
                setScroll(kind, Math.round((y - 50f) * maxScroll(kind) / 56));
            dragging = kind;
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (int) mouseX - leftPos, y = (int) mouseY - topPos;
        if (button == 0) {
            for (int i = 0; i < FabricatorCategory.values().length; i++) {
                if (inside(x, y, TAB_X + i * TAB_STEP, TAB_Y, 18, 18)) {
                    category = FabricatorCategory.values()[i];
                    subcategory = -1;
                    categoryScroll = scroll = selection = 0;
                    resetPartNavigation();
                    return true;
                }
            }
            if (category == FabricatorCategory.MODULE && clickScrollbar(x, y, MODULE_SCROLL, 58)) return true;
            if (clickScrollbar(x, y, ITEM_SCROLL, 151)) return true;
            if (clickScrollbar(x, y, RESOURCE_SCROLL, 218)) return true;
            if (category == FabricatorCategory.MODULE && inside(x, y, CATEGORY_X, CATEGORY_Y, 50, 92)) {
                int row = (y - CATEGORY_Y) / CATEGORY_STEP, index = categoryScroll + row - 1;
                if (row < CATEGORY_VISIBLE && index < ModuleCategory.values().length) {
                    subcategory = index;
                    selection = scroll = 0;
                    resetPartNavigation();
                }
                return true;
            }
            if (inside(x, y, listLeft(), ITEM_Y, 151 - listLeft(), 86)) {
                int row = (y - ITEM_Y) / ROW_STEP;
                if (row < VISIBLE && scroll + row < recipes().size()) {
                    selection = scroll + row;
                    resetPartNavigation();
                }
                return true;
            }
            if (!recipeHistory.isEmpty() && inside(x, y, 133, 130, 15, 16)) {
                ResourceLocation parent = recipeHistory.pop();
                focusedRecipe = recipeHistory.isEmpty() ? null : parent;
                ingredientScroll = 0;
                return true;
            }
            if (inside(x, y, CRAFT_X, CRAFT_Y, 50, 14)) {
                craft();
                return true;
            }
            RecipeHolder<FabricatorRecipe> chosen = selected();
            if (chosen != null && inside(x, y, RESOURCE_X, RESOURCE_Y, 18, RESOURCE_VISIBLE * ROW_STEP + 1)) {
                int row = (y - RESOURCE_Y) / ROW_STEP, index = ingredientScroll + row;
                if (row < RESOURCE_VISIBLE && index < chosen.value().requirements().size()) {
                    ItemStack[] alternatives = chosen.value().requirements().get(index).ingredient().getItems();
                    if (alternatives.length > 0) openPart(alternatives[0]);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && dragging != 0) {
            setScroll(dragging, Math.round(((float) mouseY - topPos - 50) * maxScroll(dragging) / 56));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging != 0) {
            dragging = 0;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int x = (int) mouseX - leftPos, y = (int) mouseY - topPos;
        if (scrollY != 0) {
            int delta = scrollY > 0 ? -1 : 1;
            if (category == FabricatorCategory.MODULE && inside(x, y, CATEGORY_X, CATEGORY_Y, 56, 92)) {
                setScroll(MODULE_SCROLL, categoryScroll + delta);
                return true;
            }
            if (inside(x, y, listLeft(), ITEM_Y, 154 - listLeft(), 86)) {
                setScroll(ITEM_SCROLL, scroll + delta);
                return true;
            }
            if (inside(x, y, RESOURCE_X, RESOURCE_Y, 58, RESOURCE_VISIBLE * ROW_STEP + 1)) {
                setScroll(RESOURCE_SCROLL, ingredientScroll + delta);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
