#version 150

#moj_import <minecraft:fog.glsl>

/*
 * Wave core adapted from Draconic Evolution 1.21
 *   assets/draconicevolution/shaders/core/tools/chestpiece_shield.fsh
 * and Brandon's Core math.glsl (snoise helper).
 * Both projects explicitly allow copying portions of their code under their
 * "Don't Be a Jerk" license. This project keeps its source publicly visible.
 *
 * The hex overlay below is Modular Equipment-specific and is intentionally
 * secondary: it is only revealed around the traveling DE-style wave front.
 */

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

uniform vec4 BaseColor;
uniform float Activation;
uniform float Time;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

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
    vec3 coord = vec3(texCoord0, 0.0);

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

    float t1 = -1.0 + mod(Time, 6.0) + noise;
    float t2 = -1.0 + mod(Time + 2.0, 6.0) + noise;
    float t3 = -1.0 + mod(Time + 4.0, 6.0) + noise;

    float waveDistance = min(
        min(abs(rad - t1), abs(rad - t2)),
        abs(rad - t3)
    );

    float value = waveDistance + level;

    // Preserve the original DE-style visible field. The hex lattice is not the
    // field itself; it only wakes up close to one of the traveling wave crests.
    float deAlpha = max(0.1, 1.0 - pow(value, 0.3))
        * (1.0 - level)
        * BaseColor.a;

    float waveReveal = (1.0 - smoothstep(0.08, 0.62, waveDistance))
        * clamp(1.0 - level, 0.0, 1.0);

    // Keep the cell size from the earlier preferred shield iteration.
    float hex = hexLines(texCoord0 * 20.0);
    float hexReveal = hex * waveReveal;

    float hexAlpha = hexReveal * 0.30 * BaseColor.a;
    float finalAlpha = clamp(deAlpha + hexAlpha, 0.0, 1.0);

    vec3 colourRgb = mix(
        BaseColor.rgb,
        vec3(0.90, 1.0, 1.0),
        clamp(hexReveal * 0.55, 0.0, 0.55)
    );

    vec4 colour = vec4(colourRgb, finalAlpha);
    colour *= vertexColor * ColorModulator;

    fragColor = linear_fog(
        colour,
        vertexDistance,
        FogStart,
        FogEnd,
        FogColor
    );
}
