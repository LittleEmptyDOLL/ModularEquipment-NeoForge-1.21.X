#version 150

#moj_import <minecraft:fog.glsl>

uniform vec4 ColorModulator;
uniform vec4 FogColor;
uniform float FogStart;
uniform float FogEnd;
uniform float Time;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float hexDistance(vec2 p) {
    p = abs(p);
    return max(dot(p, normalize(vec2(1.0, 1.7320508))), p.x);
}

float hexLines(vec2 p) {
    const vec2 spacing = vec2(1.0, 1.7320508);
    vec2 halfSpacing = spacing * 0.5;

    vec2 a = mod(p, spacing) - halfSpacing;
    vec2 b = mod(p - halfSpacing, spacing) - halfSpacing;
    vec2 cell = dot(a, a) < dot(b, b) ? a : b;

    float edgeDistance = 0.5 - hexDistance(cell);
    float aa = max(fwidth(edgeDistance) * 1.35, 0.006);

    return 1.0 - smoothstep(0.018, 0.018 + aa, edgeDistance);
}

void main() {
    float energy = clamp(vertexColor.a, 0.0, 1.0);
    if (energy <= 0.01) {
        discard;
    }

    vec2 uv = texCoord0;

    // Dense procedural field. This scale controls pattern density independently
    // from any texture resolution.
    float grid = hexLines(uv * 48.0);

    // Sparse bright junctions make the field read more like powered circuitry
    // instead of a perfectly uniform honeycomb.
    vec2 nodeUv = uv * 64.0;
    vec2 nodeCell = floor(nodeUv);
    vec2 nodeLocal = fract(nodeUv) - 0.5;
    float nodeShape = 1.0 - smoothstep(0.055, 0.14, length(nodeLocal));
    float node = nodeShape * step(0.82, hash21(nodeCell));

    // A narrow diagonal energy wave sweeps through the otherwise stationary
    // lattice. Only brightness changes, so the pattern stays attached to armor.
    float sweepPhase = abs(
            fract(uv.y * 3.0 + uv.x * 1.35 - Time * 0.42) - 0.5
    );
    float sweep = 1.0 - smoothstep(0.0, 0.085, sweepPhase);

    // Low-energy shields become slightly unstable cell-by-cell. At full charge
    // the multiplier converges to one and the lattice remains clean.
    float lowEnergy = 1.0 - energy;
    float flickerSeed = hash21(nodeCell + floor(Time * 4.0));
    float flicker = mix(
            1.0,
            mix(0.48, 1.0, flickerSeed),
            lowEnergy * lowEnergy * 0.75
    );

    float intensity = 0.022;
    intensity += grid * 0.42;
    intensity += node * 0.30;
    intensity += sweep * (0.035 + grid * 0.26 + node * 0.20);

    float energyAlpha = mix(
            0.10,
            0.78,
            energy * energy * (3.0 - 2.0 * energy)
    );

    float alpha = clamp(
            intensity * energyAlpha * flicker,
            0.0,
            0.78
    );

    vec3 baseColor = vertexColor.rgb * ColorModulator.rgb;
    float highlight = clamp(grid * 0.24 + sweep * 0.20 + node * 0.28, 0.0, 0.48);
    vec3 color = mix(baseColor, vec3(0.88, 1.0, 1.0), highlight);

    vec4 fieldColor = vec4(
            color,
            alpha * ColorModulator.a
    );

    fragColor = linear_fog(
            fieldColor,
            vertexDistance,
            FogStart,
            FogEnd,
            FogColor
    );
}
