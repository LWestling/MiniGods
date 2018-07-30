#version 310 es

precision highp float;

layout(location=3) uniform vec4 light;
layout(location=4) uniform vec4 cam;

in vec3 pos;
in vec3 normal;
out vec4 finalColor;

vec4 calcLight(vec3 color) {
    float lightPower = 900.f;

    vec3 posToLight = light.xyz - pos;
    float distanceToLight = length(posToLight);

    vec3 ambient = color * 0.2f;
    vec3 diffuse = color * max((dot(normalize(posToLight), normal)), 0.f) * max(lightPower / (distanceToLight * distanceToLight), 0.f);

    return vec4(diffuse + ambient, 1.f);
}

void main() {
    finalColor = calcLight(vec3(0.f, 0.f, 1.f));
}
