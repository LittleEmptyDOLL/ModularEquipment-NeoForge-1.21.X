#version 150

#moj_import <minecraft:fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat4 InversePoseMat;
uniform int FogShape;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out vec3 modelPositionOut;

void main() {
    vec4 viewPosition = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPosition;

    vertexDistance = fog_distance(viewPosition.xyz, FogShape);
    vertexColor = Color;
    texCoord0 = UV0;

    // Position has already received the CPU-side PoseStack transform used by
    // PlayerModel. Undo the root pose so the fragment shader sees coordinates
    // relative to the player rather than relative to the camera.
    modelPositionOut = (InversePoseMat * vec4(Position, 1.0)).xyz;
}
