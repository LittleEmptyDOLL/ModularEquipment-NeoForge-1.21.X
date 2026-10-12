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
in vec3 modelPositionOut;

out vec4 fragColor;

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float hash31(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.yzx + 33.33);
    return fract((p.x + p.y) * p.z);
}

float valueNoise3(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    float n000 = hash31(i + vec3(0.0, 0.0, 0.0));
    float n100 = hash31(i + vec3(1.0, 0.0, 0.0));
    float n010 = hash31(i + vec3(0.0, 1.0, 0.0));
    float n110 = hash31(i + vec3(1.0, 1.0, 0.0));
    float n001 = hash31(i + vec3(0.0, 0.0, 1.0));
    float n101 = hash31(i + vec3(1.0, 0.0, 1.0));
    float n011 = hash31(i + vec3(0.0, 1.0, 1.0));
    float n111 = hash31(i + vec3(1.0, 1.0, 1.0));

    float nx00 = mix(n000, n100, f.x);
    float nx10 = mix(n010, n110, f.x);
    float nx01 = mix(n001, n101, f.x);
    float nx11 = mix(n011, n111, f.x);
    float nxy0 = mix(nx00, nx10, f.y);
    float nxy1 = mix(nx01, nx11, f.y);
    return mix(nxy0, nxy1, f.z);
}

float fbm3(vec3 p) {
    float result = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;

    for (int i = 0; i < 4; i++) {
        result += (valueNoise3(p * frequency) - 0.5) * 2.0 * amplitude;
        frequency *= 2.0;
        amplitude *= 0.5;
    }

    return result;
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

void main() {
    float energy = clamp(vertexColor.a, 0.0, 1.0);
    if (energy <= 0.01) {
        discard;
    }

    vec2 uv = texCoord0;

    // The wave is evaluated in player-relative model space. Rotating or moving
    // the camera therefore cannot drag the front around the shield surface.
    vec3 centered = modelPositionOut - vec3(0.0, 0.30, 0.0);
    vec3 shaped = centered * vec3(1.55, 0.82, 2.20);
    float radial = length(shaped);

    // A slowly evolving 3D noise field gives each front the irregular, energetic
    // edge seen in Draconic Evolution without tying the distortion to skin UVs.
    vec3 noiseCoord = modelPositionOut * 3.4
            + vec3(0.0, Time * 0.006, -Time * 0.004);
    float warpedRadial = radial + fbm3(noiseCoord) * 0.085;

    // Three expanding fronts with one dominant pulse and two weaker echoes.
    // The primary cycle remains roughly twenty-one seconds.
    const float cycle = 1.55;
    float slowTime = Time * 0.072;
    float front0 = -0.12 + mod(slowTime, cycle);
    float front1 = -0.12 + mod(slowTime + 0.52, cycle);
    float front2 = -0.12 + mod(slowTime + 1.04, cycle);

    float d0 = abs(warpedRadial - front0);
    float d1 = abs(warpedRadial - front1);
    float d2 = abs(warpedRadial - front2);

    float body0 = 1.0 - smoothstep(0.055, 0.205, d0);
    float body1 = (1.0 - smoothstep(0.055, 0.190, d1)) * 0.62;
    float body2 = (1.0 - smoothstep(0.055, 0.175, d2)) * 0.38;

    float crest0 = 1.0 - smoothstep(0.018, 0.070, d0);
    float crest1 = (1.0 - smoothstep(0.018, 0.066, d1)) * 0.62;
    float crest2 = (1.0 - smoothstep(0.018, 0.060, d2)) * 0.38;

    float waveBody = max(body0, max(body1, body2));
    float waveCrest = max(crest0, max(crest1, crest2));

    // Keep the established hex size. The crest only compresses it slightly.
    float hexScale = mix(20.0, 21.4, waveCrest);
    float hex = hexLines(uv * hexScale);

    vec2 patchCell = floor(uv * 18.0);
    float patchSeed = hash21(patchCell + vec2(7.2, 19.4));
    float patchMask = smoothstep(0.22, 0.62, patchSeed);
    float revealedHex = hex * waveBody * mix(0.18, 1.0, patchMask);

    // Sparse nodes flash only near the strongest part of the front.
    vec2 nodeUv = uv * 42.0;
    vec2 nodeCell = floor(nodeUv);
    vec2 nodeLocal = fract(nodeUv) - 0.5;
    float nodeSeed = hash21(nodeCell + vec2(31.7, 11.3));
    float nodeShape = 1.0 - smoothstep(0.045, 0.115, length(nodeLocal));
    float nodes = nodeShape * step(0.91, nodeSeed) * waveCrest;

    float smoothEnergy = energy * energy * (3.0 - 2.0 * energy);
    float lowEnergy = 1.0 - energy;

    float flickerSeed = hash21(patchCell + floor(Time * 3.0));
    float stability = mix(
            1.0,
            mix(0.38, 1.0, flickerSeed),
            lowEnergy * lowEnergy * 0.72
    );

    // Unlike the previous version, the wave now has its own visible translucent
    // surface glow. Hexagons decorate the front rather than defining it.
    float primaryWaveAlpha = body0 * 0.060 + crest0 * 0.145;
    float echoWaveAlpha = body1 * 0.032 + crest1 * 0.070
            + body2 * 0.018 + crest2 * 0.040;
    float waveAlpha = (primaryWaveAlpha + echoWaveAlpha)
            * mix(0.20, 1.0, smoothEnergy);

    float idleAlpha = 0.0025 * mix(0.25, 1.0, smoothEnergy);
    float hexAlpha = revealedHex
            * (0.060 + waveCrest * 0.33)
            * mix(0.16, 1.0, smoothEnergy)
            * stability;
    float nodeAlpha = nodes
            * 0.40
            * mix(0.20, 1.0, smoothEnergy)
            * stability;

    float alpha = clamp(
            idleAlpha + waveAlpha + hexAlpha + nodeAlpha,
            0.0,
            0.58
    );

    if (alpha <= 0.003) {
        discard;
    }

    vec3 baseColor = vertexColor.rgb * ColorModulator.rgb;
    float highlight = clamp(
            body0 * 0.16
            + crest0 * 0.34
            + body1 * 0.07
            + crest1 * 0.12
            + revealedHex * 0.20
            + nodes * 0.34,
            0.0,
            0.66
    );
    vec3 color = mix(baseColor, vec3(0.92, 1.0, 1.0), highlight);

    vec4 fieldColor = vec4(color, alpha * ColorModulator.a);

    fragColor = linear_fog(
            fieldColor,
            vertexDistance,
            FogStart,
            FogEnd,
            FogColor
    );
}
