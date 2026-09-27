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
    // Lift shadow detail but reserve the highest intensities for heat silhouettes.
    vec3 liftedScene = min(pow(scene, vec3(0.40)), vec3(0.68));
    fragColor = vec4(mix(liftedScene, vec3(1.0), heat), 1.0);
}
