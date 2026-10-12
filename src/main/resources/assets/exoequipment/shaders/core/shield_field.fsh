#version 150

#moj_import <minecraft:fog.glsl>

/*
 * Wave core adapted from Draconic Evolution 1.21
 *   assets/draconicevolution/shaders/core/tools/chestpiece_shield.fsh
 * and Brandon's Core math.glsl (snoise helper).
 * Both projects explicitly allow copying portions of their code under their
 * "Don't Be a Jerk" license. This project keeps its source publicly visible.
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

    float value = min(
        min(abs(rad - t1), abs(rad - t2)),
        abs(rad - t3)
    );
    value += level;

    vec4 colour = vec4(
        BaseColor.rgb,
        max(0.1, 1.0 - pow(value, 0.3))
            * (1.0 - level)
            * BaseColor.a
    );

    colour *= vertexColor * ColorModulator;

    fragColor = linear_fog(
        colour,
        vertexDistance,
        FogStart,
        FogEnd,
        FogColor
    );
}
