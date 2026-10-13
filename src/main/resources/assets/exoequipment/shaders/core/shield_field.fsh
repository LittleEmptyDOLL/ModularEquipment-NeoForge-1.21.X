#version 150

/*
 * Wave timing/noise/alpha are adapted from Draconic Evolution 1.21
 *   assets/draconicevolution/shaders/core/tools/chestpiece_shield.fsh
 * and Brandon's Core math.glsl (snoise helper).
 *
 * The wave is evaluated from the dedicated shield mesh UVs, like the original
 * Draconic Evolution effect. Limb pose transforms only move the shield geometry;
 * they do not alter the phase or speed of the procedural field.
 *
 * The hex damage layer is specific to Modular Equipment. Damage is quantized to
 * whole lattice cells so impacts look like broken field elements rather than a
 * smooth circular alpha mask.
 */

uniform vec4 ColorModulator;
uniform vec4 BaseColor;
uniform float Activation;
uniform float Opacity;
// Positive values destroy the lattice; negative values rebuild it.
uniform float BreakProgress;
uniform float Time;
uniform vec4 Damage0;
uniform vec4 Damage1;
uniform vec4 Damage2;
uniform vec4 Damage3;

in vec4 vertexColor;
in vec2 texCoord0;
in vec3 fieldPos;

out vec4 fragColor;

float snoise(vec3 uv, float res) {
    const vec3 s = vec3(1e0, 1e2, 1e3);
    uv *= res;
    vec3 uv0 = floor(mod(uv, res)) * s;
    vec3 uv1 = floor(mod(uv + vec3(1.0), res)) * s;
    vec3 f = fract(uv);
    f = f * f * (3.0 - 2.0 * f);

    vec4 v = vec4(
        uv0.x + uv0.y + uv0.z,
        uv1.x + uv0.y + uv0.z,
        uv0.x + uv1.y + uv0.z,
        uv1.x + uv1.y + uv0.z
    );

    vec4 r = fract(sin(v * 1e-1) * 1e3);
    float r0 = mix(
        mix(r.x, r.y, f.x),
        mix(r.z, r.w, f.x),
        f.y
    );

    r = fract(sin((v + uv1.z - uv0.z) * 1e-1) * 1e3);
    float r1 = mix(
        mix(r.x, r.y, f.x),
        mix(r.z, r.w, f.x),
        f.y
    );

    return mix(r0, r1, f.z) * 2.0 - 1.0;
}

float hexDistance(vec2 p) {
    p = abs(p);
    return max(dot(p, normalize(vec2(1.0, 1.7320508))), p.x);
}

vec2 nearestHexCenter(vec2 p) {
    const vec2 spacing = vec2(1.0, 1.7320508);
    vec2 halfSpacing = spacing * 0.5;

    vec2 a = mod(p, spacing) - halfSpacing;
    vec2 b = mod(p - halfSpacing, spacing) - halfSpacing;
    vec2 cell = dot(a, a) < dot(b, b) ? a : b;
    return p - cell;
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

float cellHash(vec2 cell) {
    vec3 p3 = fract(vec3(cell.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

void applyDamage(
        vec4 damage,
        vec2 cellCenter,
        float hex,
        inout float broken,
        inout float fracture
) {
    if (damage.z <= 0.001 || damage.w <= 0.001) {
        return;
    }

    float strength = clamp(damage.w, 0.0, 1.0);
    vec2 damageCenter = damage.xy * 20.0;
    float distanceToImpact = distance(cellCenter, damageCenter);

    // Every cell gets a stable local variation. The impact is still clustered,
    // but its border is made from missing cells instead of a perfect circle.
    float random = cellHash(cellCenter + floor(damageCenter) * 0.173);
    float effectiveRadius = damage.z * mix(0.24, 1.0, strength);
    float breakRadius = effectiveRadius * mix(0.80, 1.18, random);

    // Cells snap back from the outside inward as strength approaches zero.
    float active = smoothstep(0.08, 0.22, strength);
    float brokenCell = step(distanceToImpact, breakRadius) * active;
    broken = max(broken, brokenCell);

    // Existing hex edges around the irregular break become an unstable bright
    // fracture rim. It remains visible independently of the travelling wave.
    float edgeDistance = abs(distanceToImpact - breakRadius);
    float edgeBand = 1.0 - smoothstep(0.14, 0.82, edgeDistance);
    float flicker = 0.82 + 0.18 * sin(Time * 31.0 + random * 23.0);
    fracture = max(
        fracture,
        edgeBand * hex * strength * flicker
    );
}

void main() {
    /*
     * CodeChickenLib's OBJParser flips V before building the CCModel. Our mesh
     * stores the raw OBJ vt coordinates, so reproduce that import transform.
     */
    vec2 shieldUv = vec2(texCoord0.x, 1.0 - texCoord0.y);
    vec3 coord = vec3(shieldUv, 0.0);

    // Original Draconic Evolution UV-radial field. Activation now remains 1.0
    // while the shield exists; remaining charge no longer shrinks this envelope.
    float rad = distance(coord.xy, vec2(0.5, 0.34375)) * 5.0;
    float level = max(0.0, (rad - (Activation * 5.0)) * 1024.0);

    float density = 8.0;
    float noise = 0.0;

    for (int i = 1; i <= 5; i++) {
        float power = pow(2.0, float(i));
        noise += (2.0 / power) * max(
            snoise(
                (coord * 0.5) + vec3(0.0, 0.0, Time * -0.01),
                power * density
            ),
            -1.0
        );
    }

    noise *= 0.5;

    // Original Draconic Evolution cadence and three offset fronts.
    float t1 = -1.0 + mod(Time, 6.0) + noise;
    float t2 = -1.0 + mod(Time + 2.0, 6.0) + noise;
    float t3 = -1.0 + mod(Time + 4.0, 6.0) + noise;

    float waveDistance = min(
        min(abs(rad - t1), abs(rad - t2)),
        abs(rad - t3)
    );

    float value = waveDistance + level;

    float deAlpha = max(0.1, 1.0 - pow(value, 0.3))
        * (1.0 - level)
        * BaseColor.a;

    float waveReveal = (1.0 - smoothstep(0.08, 0.62, waveDistance))
        * clamp(1.0 - level, 0.0, 1.0);

    vec2 hexPosition = shieldUv * 20.0;
    float hex = hexLines(hexPosition);
    float hexReveal = hex * waveReveal;

    float broken = 0.0;
    float fracture = 0.0;
    vec2 cellCenter = nearestHexCenter(hexPosition);
    applyDamage(Damage0, cellCenter, hex, broken, fracture);
    applyDamage(Damage1, cellCenter, hex, broken, fracture);
    applyDamage(Damage2, cellCenter, hex, broken, fracture);
    applyDamage(Damage3, cellCenter, hex, broken, fracture);

    // Positive transition values destroy the entire lattice in a staggered,
    // cell-by-cell cascade. Stable hashes prevent a smooth spherical fade.
    float breakProgress = max(BreakProgress, 0.0);
    float breakActive = step(0.0001, breakProgress);
    float cellFailure = cellHash(cellCenter + vec2(11.73, 5.91));
    float collapse = smoothstep(0.0, 0.88, breakProgress);
    float failed = step(cellFailure, collapse) * breakActive;
    float collapseEdge = (1.0 - smoothstep(0.025, 0.15,
            abs(cellFailure - collapse))) * (1.0 - failed) * breakActive;

    // Negative transition values rebuild the lattice. The same stable cell
    // order is deliberately offset from destruction so reactivation feels like
    // a fresh field being synthesized instead of the break animation reversed.
    float reactivationProgress = max(-BreakProgress, 0.0);
    float reactivationActive = step(0.0001, reactivationProgress);
    float cellAssembly = cellHash(cellCenter + vec2(4.17, 19.31));
    float assembly = smoothstep(0.0, 0.84, reactivationProgress);
    float assembledDuringTransition = step(cellAssembly, assembly);
    float assembled = mix(
        1.0,
        assembledDuringTransition,
        reactivationActive
    );

    // Highlight only the cells on the current reconstruction front. Because it
    // is gated by reactivationActive, no random bright cells survive afterwards.
    float assemblyEdge = (1.0 - smoothstep(
            0.025,
            0.13,
            abs(cellAssembly - assembly)
    )) * reactivationActive;
    float assemblyFlash = assemblyEdge * hex
        * (0.85 + 0.15 * sin(Time * 27.0 + cellAssembly * 19.0));

    // In the final third of reconstruction send one strong radial activation
    // impulse through the almost-complete field.
    float wavePhase = smoothstep(0.58, 0.98, reactivationProgress);
    float activationWaveCenter = wavePhase * 5.25;
    float activationWave = (1.0 - smoothstep(
            0.10,
            0.48,
            abs(rad - activationWaveCenter)
    )) * smoothstep(0.52, 0.68, reactivationProgress)
        * reactivationActive;

    float hexAlpha = hexReveal * 0.30 * BaseColor.a;
    float intactAlpha = clamp(deAlpha + hexAlpha, 0.0, 1.0)
        * (1.0 - broken) * (1.0 - failed) * assembled;

    // Give the freshly broken perimeter a distinct hot core. The normal field
    // remains unchanged; only the fracture rim becomes brighter and more solid.
    float fractureCore = smoothstep(0.38, 0.92, fracture);
    float fractureAlpha = (fracture * 1.08 + fractureCore * 0.34)
        * BaseColor.a;
    float collapseFlash = collapseEdge * hex * (1.0 - breakProgress) * 1.5;
    float reactivationGlow = (assemblyFlash * 1.22)
        + (activationWave * (0.34 + hex * 0.66));

    float finalAlpha = clamp(
        intactAlpha
            + fractureAlpha * (1.0 - failed) * assembled
            + collapseFlash
            + reactivationGlow * BaseColor.a,
        0.0,
        1.0
    ) * clamp(Opacity, 0.0, 1.0);

    float damageGlow = clamp(
        fracture * 1.18
            + fractureCore * 0.48
            + collapseFlash
            + reactivationGlow,
        0.0,
        1.0
    );
    vec3 colourRgb = mix(
        BaseColor.rgb,
        vec3(0.985, 1.0, 1.0),
        clamp((hexReveal * 0.55) + damageGlow, 0.0, 1.0)
    );

    vec4 colour = vec4(colourRgb, finalAlpha);
    colour *= vertexColor * ColorModulator;

    // Keep the field independent of camera fog. Projection affects geometry only.
    fragColor = colour;
}
