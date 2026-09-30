package com.github.littleemptydoll.exoequipment.fabricator;

import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.network.FabricatorCraftPayload;
import com.github.littleemptydoll.exoequipment.registry.EquipmentItem;
import com.github.littleemptydoll.exoequipment.registry.ModFabricatorRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class FabricatorScreen extends AbstractContainerScreen<FabricatorMenu> {
    private static final int BACKGROUND = 0xFF14212B, SLOT = 0xFF35434D, TEXT = 0xFFDEE6E9;
    private static final int VISIBLE = 3;
    private FabricatorCategory category = FabricatorCategory.EXOSKELETON;
    private int subcategory = -1, scroll, selection;

    public FabricatorScreen(FabricatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 256;
        imageHeight = 252;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("×1"), b -> craft(1))
                .bounds(leftPos + 145, topPos + 109, 30, 16).build());
        addRenderableWidget(Button.builder(Component.literal("×5"), b -> craft(5))
                .bounds(leftPos + 180, topPos + 109, 30, 16).build());
        addRenderableWidget(Button.builder(Component.literal("Max"), b -> craft(64))
                .bounds(leftPos + 215, topPos + 109, 33, 16).build());
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
        List<RecipeHolder<FabricatorRecipe>> list = recipes();
        return selection >= 0 && selection < list.size() ? list.get(selection) : null;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, BACKGROUND);
        graphics.fill(x + 7, y + 27, x + 249, y + 32, SLOT);
        int width = 240 * Math.max(0, Math.min(menu.energy(), FabricatorBlockEntity.CAPACITY))
                / FabricatorBlockEntity.CAPACITY;
        graphics.fill(x + 8, y + 28, x + 8 + width, y + 31, 0xFF49B5D6);
        for (int i = 0; i < FabricatorCategory.values().length; i++) {
            int left = x + 8 + i * 40;
            graphics.fill(left, y + 37, left + 38, y + 51,
                    FabricatorCategory.values()[i] == category ? 0xFF35667A : SLOT);
        }
        graphics.fill(x + 7, y + 54, x + 139, y + 108, SLOT);
        graphics.fill(x + 142, y + 54, x + 249, y + 106, SLOT);
        for (int i = 0; i < FabricatorUnlocks.UPGRADE_SLOT_COUNT; i++) {
            slotBackground(graphics, x + 74 + i * 43, y + 135);
        }
        graphics.fill(x + 46, y + 171, x + 210, y + 251, SLOT);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                slotBackground(graphics, x + 47 + col * 18, y + 174 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) slotBackground(graphics, x + 47 + col * 18, y + 232);
    }

    private static void slotBackground(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, SLOT);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF0D151A);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 7, TEXT, false);
        graphics.drawString(font, Component.translatable("gui.exoequipment.fabricator.energy",
                menu.energy(), FabricatorBlockEntity.CAPACITY), 8, 17, TEXT, false);
        FabricatorCategory[] categories = FabricatorCategory.values();
        for (int i = 0; i < categories.length; i++) {
            Component name = Component.translatable(categories[i].translationKey());
            String shortName = font.plainSubstrByWidth(name.getString(), 34);
            graphics.drawString(font, shortName, 11 + i * 40, 40, TEXT, false);
        }
        List<RecipeHolder<FabricatorRecipe>> list = recipes();
        for (int row = 0; row < VISIBLE && scroll + row < list.size(); row++) {
            int index = scroll + row;
            ItemStack output = list.get(index).value().result();
            if (index == selection) graphics.fill(8, 55 + row * 17, 138, 72 + row * 17, 0xFF35667A);
            graphics.renderItem(output, 10, 55 + row * 17);
            graphics.drawString(font,
                    font.plainSubstrByWidth(output.getHoverName().getString(), 106),
                    29, 59 + row * 17, TEXT, false);
        }
        RecipeHolder<FabricatorRecipe> selected = selected();
        if (selected != null) {
            FabricatorRecipe recipe = selected.value();
            graphics.renderItem(recipe.result(), 145, 57);
            graphics.drawString(font, font.plainSubstrByWidth(recipe.result().getHoverName().getString(), 82),
                    163, 60, TEXT, false);
            graphics.drawString(font, Component.translatable("gui.exoequipment.fabricator.cost",
                    recipe.energyCost()), 144, 76, TEXT, false);
            for (int i = 0; i < Math.min(4, recipe.requirements().size()); i++) {
                FabricatorIngredient entry = recipe.requirements().get(i);
                ItemStack[] alternatives = entry.ingredient().getItems();
                if (alternatives.length == 0) continue;
                int ix = 144 + i * 26;
                graphics.renderItem(alternatives[0], ix, 89);
                int available = 0;
                if (minecraft != null && minecraft.player != null) {
                    for (int slot = 0; slot < 36; slot++) {
                        ItemStack held = minecraft.player.getInventory().getItem(slot);
                        if (entry.ingredient().test(held)) available += held.getCount();
                    }
                }
                graphics.drawString(font, Math.min(available, 99) + "/" + entry.count(),
                        ix + 2, 98, available >= entry.count() ? TEXT : 0xFFFF9999, true);
            }
        }
        if (category == FabricatorCategory.MODULE) {
            String name = subcategory < 0 ? Component.translatable("gui.exoequipment.fabricator.all").getString()
                    : ModuleCategory.values()[subcategory].name();
            graphics.drawString(font, "< " + name + " >", 8, 114, TEXT, false);
        }
        graphics.drawString(font, Component.translatable("gui.exoequipment.fabricator.upgrades"),
                8, 131, TEXT, false);
        graphics.drawString(font, playerInventoryTitle, 47, 161, TEXT, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (menu.getCarried().isEmpty()) {
            if (mouseY >= topPos + 37 && mouseY < topPos + 51) {
                for (int i = 0; i < FabricatorCategory.values().length; i++) {
                    if (mouseX >= leftPos + 8 + i * 40 && mouseX < leftPos + 46 + i * 40) {
                        graphics.renderTooltip(font, Component.translatable(
                                FabricatorCategory.values()[i].translationKey()), mouseX, mouseY);
                        return;
                    }
                }
            }
            RecipeHolder<FabricatorRecipe> selected = selected();
            if (selected != null && mouseY >= topPos + 89 && mouseY < topPos + 105) {
                for (int i = 0; i < Math.min(4, selected.value().requirements().size()); i++) {
                    int x = leftPos + 144 + i * 26;
                    if (mouseX >= x && mouseX < x + 16) {
                        FabricatorIngredient entry = selected.value().requirements().get(i);
                        ItemStack[] alternatives = entry.ingredient().getItems();
                        if (alternatives.length > 0) graphics.renderTooltip(font, alternatives[0], mouseX, mouseY);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int i = 0; i < FabricatorCategory.values().length; i++) {
                if (mouseX >= leftPos + 8 + i * 40 && mouseX < leftPos + 46 + i * 40
                        && mouseY >= topPos + 37 && mouseY < topPos + 51) {
                    category = FabricatorCategory.values()[i];
                    subcategory = -1;
                    selection = scroll = 0;
                    return true;
                }
            }
            if (category == FabricatorCategory.MODULE
                    && mouseX >= leftPos + 8 && mouseX < leftPos + 139
                    && mouseY >= topPos + 109 && mouseY < topPos + 126) {
                subcategory = subcategory >= ModuleCategory.values().length - 1 ? -1 : subcategory + 1;
                selection = scroll = 0;
                return true;
            }
            if (mouseX >= leftPos + 8 && mouseX < leftPos + 139
                    && mouseY >= topPos + 54 && mouseY < topPos + 108) {
                int row = ((int) mouseY - topPos - 54) / 17;
                if (row < VISIBLE && scroll + row < recipes().size()) {
                    selection = scroll + row;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= leftPos + 8 && mouseX < leftPos + 139
                && mouseY >= topPos + 54 && mouseY < topPos + 108 && scrollY != 0) {
            scroll = Math.max(0, Math.min(Math.max(0, recipes().size() - VISIBLE),
                    scroll + (scrollY > 0 ? -1 : 1)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
