#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
out vec4 fragColor;

float hash(vec2 position) {
    return fract(sin(dot(position, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec3 scene = texture(DiffuseSampler, texCoord).rgb;

    // A narrow color key avoids treating ordinary magenta textures as heat.
    vec3 markerColor = vec3(242.0, 13.0, 227.0) / 255.0;
    float marker = 1.0 - smoothstep(0.025, 0.105, distance(scene, markerColor));

    float luminance = dot(scene, vec3(0.2126, 0.7152, 0.0722));
    // Lift dim scene detail slightly without changing the monochrome palette.
    float gray = 0.035 + 0.44 * pow(luminance, 0.65);
    float fineGrain = hash(gl_FragCoord.xy) - 0.5;
    float coarseGrain = hash(floor(gl_FragCoord.xy * 0.35)) - 0.5;
    gray += fineGrain * 0.08 + coarseGrain * 0.035;

    vec2 centered = (texCoord - 0.5) * vec2(1.35, 1.0);
    gray *= 1.0 - 0.24 * dot(centered, centered);

    float heat = smoothstep(0.15, 0.85, marker);
    fragColor = vec4(vec3(mix(clamp(gray, 0.0, 1.0), 0.98, heat)), 1.0);
}
