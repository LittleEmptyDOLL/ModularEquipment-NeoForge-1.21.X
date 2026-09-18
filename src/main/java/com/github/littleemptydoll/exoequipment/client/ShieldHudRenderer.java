package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.ShieldOperations;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

@EventBusSubscriber(
        modid = ExoEquipment.MODID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD
)
public final class ShieldHudRenderer {
    private static final ResourceLocation LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "shield_hud"
            );

    private static final int BAR_WIDTH = 81;
    private static final int BAR_HEIGHT = 4;

    private ShieldHudRenderer() {}

    @SubscribeEvent
    public static void registerGuiLayer(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                LAYER_ID,
                ShieldHudRenderer::render
        );
    }

    private static void render(
            GuiGraphics guiGraphics,
            net.minecraft.client.DeltaTracker deltaTracker
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null
                || minecraft.options.hideGui
                || minecraft.screen != null) {
            return;
        }

        Optional<ItemStack> exoskeletonStack =
                CuriosApi.getCuriosInventory(minecraft.player)
                        .flatMap(curios ->
                                curios.findFirstCurio(
                                        stack ->
                                                stack.getItem()
                                                        instanceof ExoskeletonItem
                                )
                        )
                        .map(result -> result.stack());

        if (exoskeletonStack.isEmpty()) {
            return;
        }

        ItemStack stack = exoskeletonStack.get();
        ExoskeletonData data = ExoskeletonItem.getData(stack);
        ExoskeletonRuntimeState runtime = stack.get(
                ModDataComponents.EXOSKELETON_RUNTIME.get()
        );

        if (runtime == null) {
            runtime = ExoskeletonRuntimeState.empty();
        }

        ShieldOperations.ShieldStatus status =
                ShieldOperations.getStatus(data, runtime);

        if (!status.hasShields()) {
            return;
        }

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        int x = screenWidth / 2 - 91;
        int y = screenHeight - 64;

        guiGraphics.renderItem(
                new ItemStack(Items.SHIELD),
                x,
                y - 6
        );

        Component value = Component.literal(
                Math.round(status.currentEnergy())
                        + " / "
                        + status.capacity()
        );

        guiGraphics.drawString(
                minecraft.font,
                value,
                x + 18,
                y - 1,
                0xFFFFFF,
                true
        );

        int barX = x + 18;
        int barY = y + 10;

        guiGraphics.fill(
                barX,
                barY,
                barX + BAR_WIDTH,
                barY + BAR_HEIGHT,
                0x80000000
        );

        int filledWidth = (int) Math.round(
                BAR_WIDTH
                        * Math.min(
                                1.0D,
                                status.currentEnergy()
                                        / status.capacity()
                        )
        );

        if (filledWidth > 0) {
            guiGraphics.fill(
                    barX,
                    barY,
                    barX + filledWidth,
                    barY + BAR_HEIGHT,
                    0xFF55FFFF
            );
        }
    }
}
