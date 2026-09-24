package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.ShieldOperations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
        value = Dist.CLIENT
)
public final class ShieldHudRenderer {
    private static final ResourceLocation LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "shield_hud"
            );

    private static final int BAR_WIDTH = 81;
    private static final int BAR_HEIGHT = 4;

    private static final int VANILLA_INITIAL_LEFT_HEIGHT = 39;
    private static final int SHIELD_OFFSET_ABOVE_ARMOR = 25;

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
        ShieldOperations.ShieldStatus status =
                ShieldOperations.getStatus(data);

        if (!status.hasShields()) {
            return;
        }

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        int x = screenWidth / 2 - 91;
        int armorY = getVanillaArmorY(minecraft, screenHeight);
        int y = armorY - SHIELD_OFFSET_ABOVE_ARMOR;

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

    private static int getVanillaArmorY(
            Minecraft minecraft,
            int screenHeight
    ) {
        var player = minecraft.player;

        int leftHeight = VANILLA_INITIAL_LEFT_HEIGHT;

        float maxHealth = Math.max(
                (float) player.getAttributeValue(Attributes.MAX_HEALTH),
                player.getHealth()
        );

        int absorption = Mth.ceil(player.getAbsorptionAmount());

        int healthRows = Mth.ceil(
                (maxHealth + absorption) / 2.0F / 10.0F
        );

        int rowHeight = Math.max(
                10 - (healthRows - 2),
                3
        );

        leftHeight += (healthRows - 1) * rowHeight + 10;

        return screenHeight - leftHeight + 10;
    }
}
