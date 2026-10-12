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

float segmentLine(vec2 p, vec2 a, vec2 b, float width) {
    vec2 pa = p - a;
    vec2 ba = b - a;
    float h = clamp(dot(pa, ba) / max(dot(ba, ba), 0.0001), 0.0, 1.0);
    float d = length(pa - ba * h);
    float aa = max(fwidth(d) * 1.4, 0.004);
    return 1.0 - smoothstep(width, width + aa, d);
}

float nodeDot(vec2 p, vec2 center, float radius) {
    float d = length(p - center);
    float aa = max(fwidth(d) * 1.4, 0.004);
    return 1.0 - smoothstep(radius, radius + aa, d);
}

void main() {
    float energy = clamp(vertexColor.a, 0.0, 1.0);
    if (energy <= 0.01) {
        discard;
    }

    vec2 uv = texCoord0;

    // View-space fresnel keeps the shield readable mainly around the silhouette
    // instead of laying an equally strong pattern across the whole character.
    vec3 normal = normalize(viewNormalOut);
    vec3 viewDir = normalize(-viewPositionOut);
    float facing = clamp(abs(dot(normal, viewDir)), 0.0, 1.0);
    float fresnel = pow(1.0 - facing, 2.25);

    // Sparse circuit fragments. Each UV cell can contain a short horizontal,
    // vertical or L-shaped trace, while most cells stay completely empty.
    vec2 circuitUv = uv * 30.0;
    vec2 cellId = floor(circuitUv);
    vec2 local = fract(circuitUv) - 0.5;

    float r0 = hash21(cellId + vec2(2.7, 8.1));
    float r1 = hash21(cellId + vec2(13.4, 1.9));
    float r2 = hash21(cellId + vec2(6.2, 17.7));
    float r3 = hash21(cellId + vec2(23.8, 4.4));

    float activeCell = step(0.69, r0);
    float horizontal = step(0.5, r1);
    float offset = mix(-0.18, 0.18, r2);
    float lengthBias = mix(0.08, 0.30, r3);

    vec2 hA = vec2(-0.36, offset);
    vec2 hB = vec2(lengthBias, offset);
    vec2 vA = vec2(offset, -0.36);
    vec2 vB = vec2(offset, lengthBias);

    float horizontalLine = segmentLine(local, hA, hB, 0.030);
    float verticalLine = segmentLine(local, vA, vB, 0.030);
    float primaryTrace = mix(verticalLine, horizontalLine, horizontal);

    // Some active cells get a small perpendicular branch, producing a circuit
    // motif without rebuilding a continuous mesh around the player.
    float branchEnabled = step(0.76, hash21(cellId + vec2(31.1, 11.6)));
    vec2 branchCenter = mix(vB, hB, horizontal);
    float branchH = segmentLine(
            local,
            branchCenter,
            branchCenter + vec2(mix(-0.20, 0.20, r2), 0.0),
            0.026
    );
    float branchV = segmentLine(
            local,
            branchCenter,
            branchCenter + vec2(0.0, mix(-0.20, 0.20, r2)),
            0.026
    );
    float branchTrace = mix(branchH, branchV, horizontal) * branchEnabled;

    float trace = max(primaryTrace, branchTrace) * activeCell;

    float nodeEnabled = step(0.84, hash21(cellId + vec2(41.7, 29.3)));
    float node = nodeDot(local, branchCenter, 0.065)
            * activeCell
            * nodeEnabled;

    // A soft diagonal power impulse does not move the geometry. It temporarily
    // lights nearby fragments and can reveal a few otherwise dormant traces.
    float sweepPhase = abs(
            fract(uv.y * 2.1 + uv.x * 0.9 - Time * 0.23) - 0.5
    );
    float sweep = 1.0 - smoothstep(0.025, 0.115, sweepPhase);

    float dormantReveal = (1.0 - activeCell) * sweep * step(0.88, r0);
    float visibleTrace = max(trace, primaryTrace * dormantReveal);

    // Energy loss affects the small details more strongly than the silhouette,
    // so a depleted field looks unstable instead of merely becoming uniformly dim.
    float lowEnergy = 1.0 - energy;
    float flickerSeed = hash21(cellId + floor(Time * 5.0));
    float fragmentStability = mix(
            1.0,
            mix(0.32, 1.0, flickerSeed),
            lowEnergy * lowEnergy * 0.82
    );

    float smoothEnergy = energy * energy * (3.0 - 2.0 * energy);

    float rimAlpha = (0.012 + fresnel * 0.16) * mix(0.28, 1.0, smoothEnergy);
    float traceAlpha = visibleTrace
            * (0.15 + sweep * 0.30)
            * mix(0.18, 1.0, smoothEnergy)
            * fragmentStability;
    float nodeAlpha = node
            * (0.20 + sweep * 0.34)
            * mix(0.20, 1.0, smoothEnergy)
            * fragmentStability;

    // Very subtle breathing prevents the idle field from feeling completely static.
    float breathing = 0.96 + 0.04 * sin(Time * 1.7);
    float alpha = clamp(
            (rimAlpha + traceAlpha + nodeAlpha) * breathing,
            0.0,
            0.52
    );

    vec3 baseColor = vertexColor.rgb * ColorModulator.rgb;
    float highlight = clamp(
            fresnel * 0.22 + visibleTrace * 0.22 + sweep * visibleTrace * 0.28 + node * 0.30,
            0.0,
            0.62
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
