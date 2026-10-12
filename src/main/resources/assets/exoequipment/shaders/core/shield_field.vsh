#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat4 ModelMat;
uniform mat4 FieldMat;

out vec4 vertexColor;
out vec2 texCoord0;
out vec3 fieldPos;

void main() {
    gl_Position = ProjMat * ModelViewMat * ModelMat * vec4(Position, 1.0);

    // FieldMat contains only the humanoid-part transform relative to the player
    // render root. It deliberately contains no camera/view transform.
    fieldPos = (FieldMat * vec4(Position, 1.0)).xyz;

    vertexColor = Color;
    texCoord0 = UV0;
}
