#version 310 es

layout(location=0) in vec3 vertexPos;
layout(location=1) uniform mat4 cpMatrix;
layout(location=2) uniform mat4 model;

out vec3 pos;
out vec3 normal;

void main() {
    vec4 position = model * vec4(vertexPos, 1.f);
    gl_Position = cpMatrix * position;
    pos = position.xyz;

    normal = vec3(0, 1, 0); // mul on cpu
}