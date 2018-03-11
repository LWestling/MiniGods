#version 310 es

layout(location=0) in vec3 vertexPos;
layout(location=1) uniform mat4 cameraProjection;
layout(location=2) uniform mat4 model;
out vec3 pos;

void main() {
    gl_Position = cameraProjection * model * vec4(vertexPos, 1.0);
    pos = vertexPos;
}