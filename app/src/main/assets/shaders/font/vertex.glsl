#version 300 es

layout(location=0) in vec3 vertexPos;
layout(location=1) in vec2 inTexCoord;
layout(location=2) uniform mat4 model;

out vec2 texCoord;

void main() {
    gl_Position = model * vec4(vertexPos, 1.0);
    texCoord = inTexCoord;
}