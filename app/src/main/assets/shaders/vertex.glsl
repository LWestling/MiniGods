#version 310 es

layout(location=0) in vec3 vertexPos;
layout(location=1) in vec3 normal;
layout(location=2) in vec3 ambient;

layout(location=3) uniform mat4 model;
layout(location=4) uniform mat4 cameraProjection;

out vec3 pos;
out vec3 outNormal;
out vec3 outColor;

void main() {
    vec4 worldPosition = model * vec4(vertexPos, 1.0);
    gl_Position = cameraProjection * worldPosition;

    outNormal = normalize((inverse(transpose(model)) * vec4(normal, 0.f)).xyz); // mul on cpu
    pos = worldPosition.xyz;
    outColor = ambient;
}
