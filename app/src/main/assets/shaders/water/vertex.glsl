#version 310 es

layout(location=0) in vec3 vertexPos;
layout(location=1) uniform mat4 model;
layout(location=2) uniform mat4 cpMatrix;

out vec3 pos;

void main() {
    vec4 position = model * vec4(vertexPos, 1.f);
    gl_Position = cpMatrix * position;

    pos = position.xyz;
}