#version 310 es

precision lowp float;

layout(location=5) uniform vec4 light;
layout(location=6) uniform vec4 cam;

in vec3 pos;
in vec3 outNormal;
in vec2 texCoord;
out vec4 finalColor;

uniform sampler2D tex;

vec4 calcLight(vec3 color) {
    vec3 posToLight = normalize(light.xyz - pos);
    vec3 ambient = vec3(0.2f, 0.2f, 0.2f) * color;

    return vec4(color * (dot(posToLight, outNormal)) + ambient, 1.f);
}

void main() {
    finalColor = calcLight(texture(tex, texCoord).xyz);
}
