#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D SceneDepth;

layout(std140) uniform Spotlights {
    mat4 InverseViewRotation;
    mat4 InverseProjection;
    ivec4 Settings; // Lamp count, zero-to-one clip depth
    vec4 LampPositions[8]; // View-space position, strength
    vec4 LampDirections[8]; // View-space axis, number of occluder boxes
    vec4 OccluderBounds[896]; // Min/max pairs relative to each lamp, in world axes
    vec4 WidthScales[2]; // One component per lamp
};

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

vec3 viewPosition(vec2 uv, float depth) {
    float clipDepth = Settings.y == 1 ? depth : 2.0 * depth - 1.0;
    vec4 p = InverseProjection * vec4(uv * 2.0 - 1.0, clipDepth, 1.0);
    return p.xyz / p.w;
}

bool occluded(int lamp, vec3 viewRay, float distance) {
    vec3 ray = mat3(InverseViewRotation) * viewRay;
    // Use a finite reciprocal even when the segment is parallel to a box face.
    vec3 raySign = mix(vec3(-1.0), vec3(1.0), greaterThanEqual(ray, vec3(0.0)));
    vec3 inverseRay = raySign / max(abs(ray), vec3(0.000001));
    int base = lamp * 112;
    int count = int(LampDirections[lamp].w);
    for (int i = 0; i < count; ++i) {
        vec3 a = OccluderBounds[base + 2 * i].xyz * inverseRay;
        vec3 b = OccluderBounds[base + 2 * i + 1].xyz * inverseRay;
        vec3 near = min(a, b);
        vec3 far = max(a, b);
        float enter = max(max(near.x, near.y), max(near.z, 0.015));
        // End just before the depth surface so its own block doesn't shadow it.
        float exit = min(min(far.x, far.y), min(far.z, distance - 0.045));
        if (enter < exit) return true;
    }
    return false;
}

vec3 surfaceTangent(vec3 surface, vec2 offset) {
    vec2 beforeUv = texCoord - offset;
    vec2 afterUv = texCoord + offset;
    vec3 before = viewPosition(beforeUv, max(texture(SceneDepth, beforeUv).r, 0.000001));
    vec3 after = viewPosition(afterUv, max(texture(SceneDepth, afterUv).r, 0.000001));
    vec3 backward = surface - before;
    vec3 forward = after - surface;
    // Prefer the neighbor on the same surface at a depth discontinuity.
    return abs(backward.z) < abs(forward.z) ? backward : forward;
}

void main() {
    float depth = texture(SceneDepth, texCoord).r;
    // Minecraft clears the reversed-Z depth buffer to zero.
    if (depth <= 0.0) discard;
    vec3 surface = viewPosition(texCoord, depth);
    vec2 pixel = 1.0 / vec2(textureSize(SceneDepth, 0));
    vec3 normal = cross(surfaceTangent(surface, vec2(pixel.x, 0.0)),
                        surfaceTangent(surface, vec2(0.0, pixel.y)));
    float normalLength = length(normal);
    if (normalLength < 0.00000001) discard;
    normal /= normalLength;
    if (dot(normal, -surface) < 0.0) normal = -normal;

    float amount = 0.0;
    for (int i = 0; i < Settings.x; ++i) {
        vec3 fromLamp = surface - LampPositions[i].xyz;
        float distance = length(fromLamp);
        if (distance < 0.05 || distance >= 9.2) continue;
        vec3 ray = fromLamp / distance;
        vec3 axis = LampDirections[i].xyz;
        vec3 right = normalize(cross(axis, transpose(mat3(InverseViewRotation)) * vec3(0.0, 1.0, 0.0)));
        // Compress the lateral component for the cone test to widen the beam horizontally.
        float widthScale = WidthScales[i / 4][i % 4];
        vec3 beamRay = normalize(ray - (1.0 - 0.70 / widthScale) * dot(ray, right) * right);
        float cone = smoothstep(0.94, 0.994, dot(beamRay, axis));
        float incidence = max(0.0, dot(normal, -ray));
        if (cone * incidence < 0.001 || occluded(i, ray, distance)) continue;
        float attenuation = 0.004;
        float falloff = (1.0 - smoothstep(8.0, 9.2, distance)) / (1.0 + attenuation * distance * distance);
        amount += 7.65 * LampPositions[i].w * cone * incidence * falloff;
    }
    // The pipeline multiplies this contribution by the scene color before adding it.
    fragColor = vec4(vec3(1.0, 0.68, 0.37) * amount, 0.0);
}
