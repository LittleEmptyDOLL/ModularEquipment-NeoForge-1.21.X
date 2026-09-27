#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec3 scene = texture(DiffuseSampler, texCoord).rgb;

    // A narrow color key avoids treating ordinary magenta textures as heat.
    vec3 markerColor = vec3(242.0, 13.0, 227.0) / 255.0;
    float marker = 1.0 - smoothstep(0.025, 0.105, distance(scene, markerColor));

    float heat = smoothstep(0.15, 0.85, marker);
    // Brighten shadows only; the gamma curve washed out already lit surfaces.
    float luminance = dot(scene, vec3(0.2126, 0.7152, 0.0722));
    float shadow = 1.0 - smoothstep(0.03, 0.45, luminance);
    vec3 brightenedScene = min(scene + vec3(0.22 * shadow), vec3(0.68));
    // Ordinary scene colors must stay below the white heat silhouette range.
    fragColor = vec4(mix(brightenedScene, vec3(1.0), heat), 1.0);
}
