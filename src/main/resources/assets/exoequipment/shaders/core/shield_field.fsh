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
in vec3 viewPositionOut;
in vec3 viewNormalOut;

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

float thinBand(float value, float center, float width) {
    float d = abs(fract(value) - center);
    return 1.0 - smoothstep(width * 0.45, width, d);
}

void main() {
    float energy = clamp(vertexColor.a, 0.0, 1.0);
    if (energy <= 0.01) {
        discard;
    }

    vec2 uv = texCoord0;

    // Keep a very weak silhouette glow while the shield is idle. The field's
    // actual surface pattern is driven by the moving wave below.
    vec3 normal = normalize(viewNormalOut);
    vec3 viewDir = normalize(-viewPositionOut);
    float facing = clamp(abs(dot(normal, viewDir)), 0.0, 1.0);
    float fresnel = pow(1.0 - facing, 2.8);

    // Draconic-style expanding fronts. Mild noise bends the wave so it does not
    // look like a perfectly mathematical ring sweeping over the UV islands.
    vec2 centeredUv = (uv - vec2(0.5, 0.46)) * vec2(1.0, 0.78);
    float radial = length(centeredUv) * 6.2;
    float noise = (valueNoise(uv * 9.0 + vec2(Time * 0.07, -Time * 0.05)) - 0.5) * 0.42;
    float waveCoord = radial + noise;

    float phase0 = mod(Time * 0.72, 5.4);
    float phase1 = mod(Time * 0.72 + 1.8, 5.4);
    float phase2 = mod(Time * 0.72 + 3.6, 5.4);

    float distanceToWave = min(
            abs(waveCoord - phase0),
            min(abs(waveCoord - phase1), abs(waveCoord - phase2))
    );

    // Broad halo gives the front some body; the narrow crest is the bright edge.
    float waveHalo = 1.0 - smoothstep(0.08, 0.46, distanceToWave);
    float waveCrest = 1.0 - smoothstep(0.018, 0.115, distanceToWave);

    // Hex cells are completely dormant outside the wave. As the crest reaches
    // them their scale increases slightly, which makes the cells visually
    // contract and then relax as the front passes.
    float hexScale = mix(20.0, 24.0, waveCrest);
    float hex = hexLines(uv * hexScale);

    // Break up the honeycomb so the wave reveals patches rather than a solid
    // chain-mail sheet. The broad halo can still hint at neighbouring cells.
    vec2 patchCell = floor(uv * 18.0);
    float patchSeed = hash21(patchCell + vec2(7.2, 19.4));
    float patchMask = smoothstep(0.30, 0.58, patchSeed);
    float revealedHex = hex * waveHalo * mix(0.24, 1.0, patchMask);

    // Fast tracer lines run through the active band. They provide obvious motion
    // even though the hex geometry itself remains attached to the shield surface.
    float tracerA = thinBand(uv.y * 3.1 + uv.x * 1.25 - Time * 0.78, 0.50, 0.055);
    float tracerB = thinBand(uv.y * 4.4 - uv.x * 0.85 - Time * 1.06, 0.50, 0.040);
    float tracerC = thinBand(uv.x * 5.2 + Time * 1.32, 0.50, 0.032);
    float tracers = max(tracerA, max(tracerB * 0.75, tracerC * 0.55)) * waveHalo;

    // A few bright nodes flash where the moving crest intersects the field.
    vec2 nodeUv = uv * 42.0;
    vec2 nodeCell = floor(nodeUv);
    vec2 nodeLocal = fract(nodeUv) - 0.5;
    float nodeSeed = hash21(nodeCell + vec2(31.7, 11.3));
    float nodeShape = 1.0 - smoothstep(0.045, 0.115, length(nodeLocal));
    float nodes = nodeShape * step(0.88, nodeSeed) * waveCrest;

    float smoothEnergy = energy * energy * (3.0 - 2.0 * energy);
    float lowEnergy = 1.0 - energy;

    // Depleted shields lose pieces of the traveling pattern rather than simply
    // dimming uniformly.
    float flickerSeed = hash21(patchCell + floor(Time * 5.0));
    float stability = mix(
            1.0,
            mix(0.30, 1.0, flickerSeed),
            lowEnergy * lowEnergy * 0.80
    );

    float rimAlpha = fresnel * 0.075 * mix(0.30, 1.0, smoothEnergy);
    float haloAlpha = waveHalo * 0.020 * mix(0.18, 1.0, smoothEnergy);
    float hexAlpha = revealedHex
            * (0.16 + waveCrest * 0.30)
            * mix(0.16, 1.0, smoothEnergy)
            * stability;
    float tracerAlpha = tracers
            * (0.16 + waveCrest * 0.34)
            * mix(0.18, 1.0, smoothEnergy)
            * stability;
    float nodeAlpha = nodes
            * 0.50
            * mix(0.20, 1.0, smoothEnergy)
            * stability;

    float alpha = clamp(
            rimAlpha + haloAlpha + hexAlpha + tracerAlpha + nodeAlpha,
            0.0,
            0.58
    );

    if (alpha <= 0.003) {
        discard;
    }

    vec3 baseColor = vertexColor.rgb * ColorModulator.rgb;
    float highlight = clamp(
            fresnel * 0.12
            + waveHalo * 0.10
            + revealedHex * 0.20
            + tracers * 0.32
            + nodes * 0.42,
            0.0,
            0.66
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
