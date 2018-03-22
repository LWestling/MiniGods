#version 310 es

layout(location=0) in vec3 vertexPos;

out vec3 pos;
out vec3 normal;

void main() {
    gl_Position = vec4(vertexPos, 1.0);

    pos = vertexPos;
    normal = vec3(0, 0, 1); // mul on cpu
}