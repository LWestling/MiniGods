#version 310 es

layout(location=0) in vec3 vertexPos;
layout(location=1) uniform mat4 cpMatrix;

out vec3 pos;
out vec3 normal;

void main() {
    gl_Position = cpMatrix * vec4(vertexPos, 1.0);

    pos = vertexPos;
    normal = vec3(0, 1, 0); // mul on cpu
}