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
uniform mat4 ModelMat;
uniform mat3 IViewRotMat;
uniform int FogShape;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;

void main() {
    vec4 viewPosition = ModelViewMat * ModelMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPosition;

    // Match Draconic Evolution's tool_base.vsh. In particular, do not feed
    // the model/view transformed position into fog_distance: with cylindrical
    // fog that makes the field brightness vary strongly with camera pitch.
    vertexDistance = fog_distance(IViewRotMat * Position, FogShape);
    vertexColor = Color;
    texCoord0 = UV0;
}
