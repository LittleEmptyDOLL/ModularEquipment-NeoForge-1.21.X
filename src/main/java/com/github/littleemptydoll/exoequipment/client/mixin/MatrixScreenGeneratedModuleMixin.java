package com.github.littleemptydoll.exoequipment.client.mixin;

import com.github.littleemptydoll.exoequipment.gui.MatrixMenu;
import com.github.littleemptydoll.exoequipment.gui.MatrixScreen;
import com.github.littleemptydoll.exoequipment.item.GeneratedModuleItem;
import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.module.GeneratedModuleData;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MatrixScreen.class)
abstract class MatrixScreenGeneratedModuleMixin extends AbstractContainerScreen<MatrixMenu> {
    private static final int MATRIX_GRID_OFFSET_Y = 4;
    private static final int MODULE_PREVIEW_BACKGROUND_COLOR = 0x553F7185;
    private static final int MODULE_PREVIEW_BORDER_COLOR = 0xFF9ED9EA;

    @Shadow
    private int carriedModuleRotation;

    @Shadow
    private ModuleItem carriedRotationItem;

    protected MatrixScreenGeneratedModuleMixin(
            MatrixMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
    }

    @Inject(
            method = "renderCarriedModulePreview",
            at = @At("HEAD"),
            cancellable = true
    )
    private void exo$renderGeneratedModulePreview(
            GuiGraphics graphics,
            int gridX,
            int gridY,
            int moduleX,
            int moduleY,
            CallbackInfo callback
    ) {
        ItemStack carried = menu.getCarried();
        if (!(carried.getItem() instanceof GeneratedModuleItem generatedItem)) {
            return;
        }

        ModuleDefinition definition = generatedItem.getDefinition(carried);
        ModuleSize size = MatrixOperations.getRotatedSize(
                definition.size(),
                carriedModuleRotation
        );
        int x = gridX + moduleX * MatrixMenu.CELL_SIZE + 1;
        int y = gridY + moduleY * MatrixMenu.CELL_SIZE + 1;
        int width = size.width() * MatrixMenu.CELL_SIZE - 2;
        int height = size.height() * MatrixMenu.CELL_SIZE - 2;

        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                MODULE_PREVIEW_BACKGROUND_COLOR
        );
        graphics.renderOutline(
                x,
                y,
                width,
                height,
                MODULE_PREVIEW_BORDER_COLOR
        );
        exo$renderScaledItem(graphics, carried, x, y, width, height);
        callback.cancel();
    }

    @Inject(
            method = "getPreviewOrigin",
            at = @At("HEAD"),
            cancellable = true
    )
    private void exo$generatedPreviewOrigin(
            int mouseX,
            int mouseY,
            CallbackInfoReturnable<int[]> callback
    ) {
        ItemStack carried = menu.getCarried();
        if (!(carried.getItem() instanceof GeneratedModuleItem generatedItem)) {
            return;
        }

        if (carriedRotationItem != generatedItem) {
            carriedRotationItem = generatedItem;
            carriedModuleRotation = 0;
        }

        int gridX = leftPos + menu.getGridX();
        int gridY = topPos + MatrixMenu.GRID_Y + MATRIX_GRID_OFFSET_Y;
        int localX = mouseX - gridX;
        int localY = mouseY - gridY;
        if (localX < 0 || localY < 0) {
            callback.setReturnValue(null);
            return;
        }

        int cellX = localX / MatrixMenu.CELL_SIZE;
        int cellY = localY / MatrixMenu.CELL_SIZE;
        if (cellX >= menu.getMatrixWidth() || cellY >= menu.getMatrixHeight()) {
            callback.setReturnValue(null);
            return;
        }

        ModuleSize size = MatrixOperations.getRotatedSize(
                generatedItem.getDefinition(carried).size(),
                carriedModuleRotation
        );
        int maxX = Math.max(0, menu.getMatrixWidth() - size.width());
        int maxY = Math.max(0, menu.getMatrixHeight() - size.height());
        int originX = Math.max(
                0,
                Math.min(cellX - size.width() / 2, maxX)
        );
        int originY = Math.max(
                0,
                Math.min(cellY - size.height() / 2, maxY)
        );
        callback.setReturnValue(new int[]{originX, originY});
    }

    @Inject(
            method = "createModuleStack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void exo$restoreGeneratedModuleStack(
            InstalledModule module,
            CallbackInfoReturnable<ItemStack> callback
    ) {
        GeneratedModuleData generated = module.generated().orElse(null);
        if (generated == null) {
            return;
        }

        var entry = ModModules.find(generated.baseDefinitionId());
        if (entry == null) {
            callback.setReturnValue(ItemStack.EMPTY);
            return;
        }

        ItemStack stack = entry.getItem().getDefaultInstance();
        stack.set(ModDataComponents.GENERATED_MODULE_DATA.get(), generated);
        generated.definition().storage().ifPresent(storage -> {
            int storedEnergy = Math.min(
                    menu.getSyncedStoredEnergy(module.x(), module.y()),
                    storage.capacity()
            );
            if (storedEnergy > 0) {
                stack.set(
                        ModDataComponents.MODULE_STORED_ENERGY.get(),
                        storedEnergy
                );
            }
        });
        callback.setReturnValue(stack);
    }

    private static void exo$renderScaledItem(
            GuiGraphics graphics,
            ItemStack stack,
            int x,
            int y,
            int width,
            int height
    ) {
        float scale = Math.min(width, height) / 16.0F;
        if (scale <= 1.0F) {
            graphics.renderItem(
                    stack,
                    x + Math.max(0, (width - 16) / 2),
                    y + Math.max(0, (height - 16) / 2)
            );
            return;
        }

        int renderedSize = Math.round(16.0F * scale);
        int renderX = x + (width - renderedSize) / 2;
        int renderY = y + (height - renderedSize) / 2;
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(renderX, renderY, 0);
        pose.scale(scale, scale, 1.0F);
        graphics.renderItem(stack, 0, 0);
        pose.popPose();
    }
}
