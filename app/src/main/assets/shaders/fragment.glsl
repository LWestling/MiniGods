#version 310 es

precision highp float;

layout(location=4) uniform vec4 light;
layout(location=5) uniform vec4 camera;

in vec3 pos;
in vec3 outNormal;
out vec4 finalColor;

vec4 calcLight(vec3 color) {
    float lightPower = 300.f;

    vec3 posToLight = light.xyz - pos;
    vec3 posToLightNor = normalize(posToLight);

    float distanceToLight = length(posToLight);
    vec3 toCamera = normalize(camera.xyz - pos);
    vec3 lightReflection = reflect(-posToLightNor, outNormal);

    vec3 ambient = color * 0.22f;
    vec3 diffuse = color * max((dot(posToLightNor, outNormal)), 0.f) * max(lightPower / (distanceToLight * distanceToLight), 0.f);

    float specular = pow(max(dot(lightReflection, toCamera), 0.f), 200.f);

    return vec4(diffuse + ambient, 1.f);
}

void main() {
    vec3 color = vec3(0.1, 0.76, 0.1);

    finalColor = calcLight(color);
}
