#version 150

/*
 * Wave timing/noise/alpha are adapted from Draconic Evolution 1.21
 *   assets/draconicevolution/shaders/core/tools/chestpiece_shield.fsh
 * and Brandon's Core math.glsl (snoise helper).
 *
 * Unlike the original UV-radial implementation, the wave radius below is
 * evaluated from entity-local 3D coordinates. That preserves the DE-style
 * distorted traveling fronts while preventing camera angle / visible UV island
 * changes from moving the wave across the shield.
 */

uniform vec4 ColorModulator;
uniform vec4 BaseColor;
uniform float Activation;
uniform float Time;

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
    /*
     * FieldMat already removed the render-layer root/camera placement. These
     * coordinates therefore describe the actual humanoid shield in a stable
     * local space: head above zero, torso around 0..0.75, legs down to ~1.5.
     * The anisotropic scale shapes the radial front around that humanoid volume
     * while keeping the original DE 0..5 wave domain.
     */
    vec3 radialPos = vec3(
        fieldPos.x * 1.75,
        (fieldPos.y - 0.50) * 1.15,
        fieldPos.z * 1.75
    );
    float rad = length(radialPos) * 3.20;
    float level = max(0.0, (rad - (Activation * 5.0)) * 1024.0);

    // Keep DE's multi-octave distortion, but sample it from stable model-space
    // coordinates rather than a face-dependent UV island.
    vec3 coord = fieldPos * vec3(0.80, 0.55, 0.80)
        + vec3(0.50, 0.25, 0.50);

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

    /*
     * CodeChickenLib flips OBJ V on import. The mesh stores raw vt values, so
     * keep the same orientation here for the hex overlay. UV affects only the
     * decorative lattice now; it no longer controls wave position or phase.
     */
    vec2 shieldUv = vec2(texCoord0.x, 1.0 - texCoord0.y);
    float hex = hexLines(shieldUv * 20.0);
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

    // Deliberately no fog term: the field colour/alpha has no camera-dependent
    // fragment input. Camera matrices are used only to project the geometry.
    fragColor = colour;
}
