package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

@EventBusSubscriber(modid = ExoEquipment.MODID, value = Dist.CLIENT)
public final class ThermalVisionClientRenderer {
    private static ShaderInstance markerShader;

    private ThermalVisionClientRenderer() {}

    static ShaderInstance markerShader() {
        return markerShader;
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, "thermal_marker"),
                        DefaultVertexFormat.NEW_ENTITY),
                shader -> markerShader = shader);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (var type : event.getEntityTypes()) {
            if (event.getRenderer(type) instanceof LivingEntityRenderer<?, ?> renderer) {
                addLayer(renderer);
            }
        }
        for (PlayerSkin.Model skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof LivingEntityRenderer<?, ?> renderer) {
                addLayer(renderer);
            }
        }
    }

    private static <T extends LivingEntity, M extends EntityModel<T>> void addLayer(
            LivingEntityRenderer<T, M> renderer
    ) {
        renderer.addLayer(new ThermalVisionRenderLayer<>(renderer));
    }
}
