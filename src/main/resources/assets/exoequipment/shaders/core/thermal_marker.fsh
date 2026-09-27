#version 150

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 markerColor;
out vec4 fragColor;

void main() {
    if (texture(Sampler0, texCoord0).a < 0.1) {
        discard;
    }

    // Uniform marker color is independent of texture RGB and model lighting.
    fragColor = vec4(markerColor.rgb, 1.0);
}
