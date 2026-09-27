#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    float luminance = texture(DiffuseSampler, texCoord).r;
    vec2 centered = (texCoord - 0.5) * vec2(1.35, 1.0);
    float vignette = 1.0 - 0.24 * dot(centered, centered);
    float gray = (0.035 + 0.55 * luminance) * vignette;

    // Keep the heated silhouettes white after pixelation and tone mapping.
    gray = mix(gray, 0.98, smoothstep(0.75, 0.95, luminance));
    fragColor = vec4(vec3(clamp(gray, 0.0, 1.0)), 1.0);
}
