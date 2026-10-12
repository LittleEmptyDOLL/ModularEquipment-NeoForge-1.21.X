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

float fbm(vec2 p) {
    float result = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;

    for (int i = 0; i < 5; i++) {
        result += (valueNoise(p * frequency) - 0.5) * 2.0 * amplitude;
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

    // Draconic Evolution-inspired field geometry: distance from a UV-space
    // emitter point produces curved concentric fronts instead of a flat scan line.
    const vec2 waveCenter = vec2(0.5, 0.34375);
    float radial = distance(uv, waveCenter) * 5.0;

    // Five octaves gently deform the radius. The noise itself evolves very
    // slowly so the front feels like a living energy field rather than water.
    vec2 noiseDrift = vec2(Time * 0.0030, -Time * 0.0022);
    float noise = fbm(uv * 8.0 + noiseDrift) * 0.34;
    float warpedRadial = radial + noise;

    // Same three-offset-front idea as Draconic Evolution, but slowed down to the
    // pace that worked better for this shield. A complete cycle is ~21 seconds.
    float slowTime = Time * 0.28;
    float front0 = -1.0 + mod(slowTime, 6.0);
    float front1 = -1.0 + mod(slowTime + 2.0, 6.0);
    float front2 = -1.0 + mod(slowTime + 4.0, 6.0);

    float d0 = abs(warpedRadial - front0);
    float d1 = abs(warpedRadial - front1);
    float d2 = abs(warpedRadial - front2);

    // Secondary fronts are slightly softer so the field has one dominant pulse
    // instead of reading as several equally strong water ripples.
    float body0 = 1.0 - smoothstep(0.10, 0.58, d0);
    float body1 = (1.0 - smoothstep(0.10, 0.52, d1)) * 0.72;
    float body2 = (1.0 - smoothstep(0.10, 0.48, d2)) * 0.48;

    float crest0 = 1.0 - smoothstep(0.025, 0.16, d0);
    float crest1 = (1.0 - smoothstep(0.025, 0.145, d1)) * 0.72;
    float crest2 = (1.0 - smoothstep(0.025, 0.135, d2)) * 0.48;

    float waveBody = max(body0, max(body1, body2));
    float waveCrest = max(crest0, max(crest1, crest2));

    // Preserve the current preferred hex size. Cells expand only a little at
    // the crest; outside the wave the lattice remains essentially dormant.
    float hexScale = mix(20.0, 21.5, waveCrest);
    float hex = hexLines(uv * hexScale);

    // Patch masking avoids turning the revealed area into a continuous chainmail
    // sheet while still allowing the curved wave shape to read clearly.
    vec2 patchCell = floor(uv * 18.0);
    float patchSeed = hash21(patchCell + vec2(7.2, 19.4));
    float patchMask = smoothstep(0.22, 0.62, patchSeed);
    float revealedHex = hex * waveBody * mix(0.18, 1.0, patchMask);

    // Sparse node flashes sit on the strongest part of the wave.
    vec2 nodeUv = uv * 42.0;
    vec2 nodeCell = floor(nodeUv);
    vec2 nodeLocal = fract(nodeUv) - 0.5;
    float nodeSeed = hash21(nodeCell + vec2(31.7, 11.3));
    float nodeShape = 1.0 - smoothstep(0.045, 0.115, length(nodeLocal));
    float nodes = nodeShape * step(0.91, nodeSeed) * waveCrest;

    float smoothEnergy = energy * energy * (3.0 - 2.0 * energy);
    float lowEnergy = 1.0 - energy;

    // Low charge removes local pieces from the revealed wave instead of dimming
    // the whole shield uniformly.
    float flickerSeed = hash21(patchCell + floor(Time * 3.0));
    float stability = mix(
            1.0,
            mix(0.38, 1.0, flickerSeed),
            lowEnergy * lowEnergy * 0.72
    );

    float idleAlpha = 0.0035 * mix(0.25, 1.0, smoothEnergy);
    float bodyAlpha = waveBody * 0.016 * mix(0.18, 1.0, smoothEnergy);
    float hexAlpha = revealedHex
            * (0.075 + waveCrest * 0.38)
            * mix(0.16, 1.0, smoothEnergy)
            * stability;
    float nodeAlpha = nodes
            * 0.46
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
            waveBody * 0.07
            + revealedHex * 0.22
            + waveCrest * revealedHex * 0.20
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
