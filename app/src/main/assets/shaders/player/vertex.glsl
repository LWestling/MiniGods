#version 300 es

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
    mat4 boneTransform = bones[boneIds[0]] * weights[0];
    for (int i = 1; i < 4; i++) {
        boneTransform += bones[boneIds[i]] * weights[i];
    }

    vec4 position = model * boneTransform * vec4(vertexPos, 1.0);
    gl_Position = cameraProjection * position;

    outNormal = normalize((inverse(transpose(model)) * boneTransform * vec4(normal, 0.f)).xyz); // mul on cpu
    texCoord = inTexCoord;
    pos = vertexPos;
}