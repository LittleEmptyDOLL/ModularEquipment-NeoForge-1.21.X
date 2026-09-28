
Modular Equipment
=======

Мод добавляет модульное снаряжение

# Thermal vision renderer integration

The thermal visor adds a model layer to every `LivingEntityRenderer`, including
renderers registered by other mods. Custom renderers that extend `EntityRenderer`
directly need to draw their visible, textured model a second time while the
thermal effect is active. The Ender Dragon adapter is an example.

On the client, after drawing the normal body and with the same animated model,
pose stack, buffer source, and entity texture:

```java
if (ThermalVisionRenderLayer.shouldRender(entity)) {
    ThermalVisionRenderLayer.renderMarker(model, poseStack, buffers, texture);
}
```

For geometry that does not use `EntityModel`, submit its textured vertices to
`ThermalVisionRenderLayer.markerBuffer(buffers, texture)` with full-bright light,
no overlay, and `ThermalVisionRenderLayer.HEAT_MARKER` as the vertex color. The
marker pass tests scene depth and discards transparent texels, so the geometry
and texture must match the visible entity. Technical entities and projectiles
are not selected: targeting requires a living entity. To exclude an otherwise
eligible type, add it to `exoequipment:thermal_vision_ignored`.
