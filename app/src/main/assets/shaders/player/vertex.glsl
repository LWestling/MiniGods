#version 310 es

layout(location=0) in vec3 vertexPos;
layout(location=1) in vec3 normal;
layout(location=2) in vec2 inTexCoord;
layout(location=3) in ivec4 boneIds;
layout(location=4) in vec4 weights;

const int MAX_BONES = 50;
layout(location=5) uniform mat4 bones[MAX_BONES];

layout(location=56) uniform mat4 model;
layout(location=57) uniform mat4 cameraProjection;

out vec3 pos;
out vec3 outNormal;
out vec2 texCoord;

void main() {
    vec4 position = vec4(vertexPos, 1.0);

    mat4 boneTransform;
    for (int i = 0; i < 4; i++) {
        boneTransform += bones[boneIds[i]] * weights[i];
    }

    gl_Position = cameraProjection * model * boneTransform * position;

    outNormal = normalize((inverse(transpose(model)) * vec4(normal, 0.f)).xyz); // mul on cpu
    texCoord = inTexCoord;
    pos = vertexPos;
}