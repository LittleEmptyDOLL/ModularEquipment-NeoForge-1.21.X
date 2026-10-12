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

float valueNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));

    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
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
    float aa = max(fwidth(edgeDistance) * 1.25, 0.004);
    return 1.0 - smoothstep(0.020, 0.020 + aa, edgeDistance);
}

float wrappedDistance(float a, float b, float period) {
    float d = abs(a - b);
    return min(d, period - d);
}

void main() {
    float energy = clamp(vertexColor.a, 0.0, 1.0);
    if (energy <= 0.01) {
        discard;
    }

    vec2 uv = texCoord0;

    // The wave is intentionally based only on UV coordinates and time.
    // Camera/view-space data does not participate, so rotating the camera can
    // no longer drag the energy front around the player.
    const float cycleLength = 5.8;
    float noise = (valueNoise(uv * 7.0 + vec2(0.0, Time * 0.018)) - 0.5) * 0.24;
    float waveCoord = uv.y * 5.15 + uv.x * 0.38 + noise;

    // One slow primary front instead of several equally strong repeating waves.
    // At 0.28 units/second a full cycle takes a little over twenty seconds.
    float phase = mod(Time * 0.28, cycleLength);
    float distanceToWave = wrappedDistance(waveCoord, phase, cycleLength);

    // A broad body and a soft inner crest make this read as a heavy energy pulse
    // instead of concentric water ripples.
    float waveBody = 1.0 - smoothstep(0.16, 0.78, distanceToWave);
    float waveCrest = 1.0 - smoothstep(0.045, 0.22, distanceToWave);
    float waveCore = 1.0 - smoothstep(0.020, 0.105, distanceToWave);

    // Keep the user's preferred cell size. The wave only changes it slightly,
    // creating a subtle compression effect as the front passes.
    float hexScale = mix(20.0, 21.6, waveCrest);
    float hex = hexLines(uv * hexScale);

    // The lattice is dormant outside the pulse. A patch mask keeps the revealed
    // area from becoming a uniform chain-mail sheet while preserving hex size.
    vec2 patchCell = floor(uv * 18.0);
    float patchSeed = hash21(patchCell + vec2(7.2, 19.4));
    float patchMask = smoothstep(0.24, 0.60, patchSeed);
    float revealedHex = hex * waveBody * mix(0.20, 1.0, patchMask);

    // Sparse nodes only appear near the strongest part of the front.
    vec2 nodeUv = uv * 42.0;
    vec2 nodeCell = floor(nodeUv);
    vec2 nodeLocal = fract(nodeUv) - 0.5;
    float nodeSeed = hash21(nodeCell + vec2(31.7, 11.3));
    float nodeShape = 1.0 - smoothstep(0.045, 0.115, length(nodeLocal));
    float nodes = nodeShape * step(0.90, nodeSeed) * waveCrest;

    float smoothEnergy = energy * energy * (3.0 - 2.0 * energy);
    float lowEnergy = 1.0 - energy;

    // At low charge, parts of the wave drop out rather than the whole field
    // simply becoming uniformly transparent.
    float flickerSeed = hash21(patchCell + floor(Time * 3.0));
    float stability = mix(
            1.0,
            mix(0.38, 1.0, flickerSeed),
            lowEnergy * lowEnergy * 0.72
    );

    // Idle state is nearly invisible. The wave itself is the main visual event.
    float idleAlpha = 0.004 * mix(0.25, 1.0, smoothEnergy);
    float bodyAlpha = waveBody * 0.018 * mix(0.18, 1.0, smoothEnergy);
    float hexAlpha = revealedHex
            * (0.08 + waveCrest * 0.34 + waveCore * 0.10)
            * mix(0.16, 1.0, smoothEnergy)
            * stability;
    float nodeAlpha = nodes
            * (0.18 + waveCore * 0.30)
            * mix(0.20, 1.0, smoothEnergy)
            * stability;

    float alpha = clamp(
            idleAlpha + bodyAlpha + hexAlpha + nodeAlpha,
            0.0,
            0.54
    );

    if (alpha <= 0.003) {
        discard;
    }

    vec3 baseColor = vertexColor.rgb * ColorModulator.rgb;
    float highlight = clamp(
            waveBody * 0.08
            + revealedHex * 0.22
            + waveCrest * revealedHex * 0.18
            + nodes * 0.36,
            0.0,
            0.58
    );
    vec3 color = mix(baseColor, vec3(0.90, 1.0, 1.0), highlight);

    vec4 fieldColor = vec4(color, alpha * ColorModulator.a);

    fragColor = linear_fog(
            fieldColor,
            vertexDistance,
            FogStart,
            FogEnd,
            FogColor
    );
}
