#version 310 es

precision highp float;

layout(location=4) uniform vec4 light;
layout(location=5) uniform vec4 cam;

in vec3 pos;
in vec3 normal;
in vec2 texCoord;
out vec4 finalColor;

uniform sampler2D tex;

vec4 calcLight(vec3 color) {
    vec3 posToLight = normalize(light.xyz - pos);

    vec3 ambient = color * 0.6f;
    vec3 diffuse = color * max((dot(posToLight, normal)), 0.f);

    return vec4(diffuse + ambient, 1.f);
}

void main() {
    finalColor = calcLight(texture(tex, texCoord).xyz);
}
