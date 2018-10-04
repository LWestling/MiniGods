#version 310 es

precision highp float;

layout(location=5) uniform vec4 light;
layout(location=6) uniform vec4 camera;

in vec3 pos;
in vec3 outNormal;
in vec3 outColor;
out vec4 finalColor;

vec4 calcLight(vec4 color) {
    vec3 color3 = color.xyz;
    float lightPower = 900.f;

    vec3 posToLight = light.xyz - pos;
    vec3 posToLightNor = normalize(posToLight);

    float distanceToLight = length(posToLight);
    vec3 toCamera = normalize(camera.xyz - pos);
    vec3 lightReflection = reflect(-posToLightNor, outNormal);

    vec3 ambient = color3 * 1.2f;
    vec3 diffuse = color3 * max((dot(posToLightNor, outNormal)), 0.f) * max(lightPower / (distanceToLight * distanceToLight), 0.f);

    float specular = pow(max(dot(lightReflection, toCamera), 0.f), 200.f);

    return vec4(diffuse + ambient, color.a);
}

void main() {
    finalColor = calcLight(vec4(outColor, 1.f));
    // finalColor = calcLight(texture(tex, outUv));
}
