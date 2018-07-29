#version 310 es

precision highp float;

layout(location=4) uniform vec4 light;
layout(location=5) uniform vec4 camera;

in vec3 pos;
in vec3 outNormal;
out vec4 finalColor;

vec4 calcLight(vec3 color) {
    vec3 posToLight = normalize(light.xyz - pos);
    vec3 lightReflection = reflect(-posToLight, outNormal);
    vec3 toCamera = normalize(camera.xyz - pos);

    float specular = pow(max(dot(lightReflection, toCamera), 0.f), 100.f);
    vec3 diffuse = color * max((dot(posToLight, outNormal)), 0.f);
    vec3 ambient = color * 0.6f;

    return vec4(color * (dot(posToLight, outNormal) + specular) + ambient, 1.f);
}

void main() {
    vec3 color = vec3(0.0, 0.4, 0.0);

    finalColor = calcLight(color);
}
