#version 300 es

precision highp float;

layout(location=5) uniform vec4 light;
layout(location=6) uniform vec4 camera;

in vec3 pos;
in vec3 outNormal;
in vec3 outColor;
out vec4 finalColor;

vec4 calcLight(vec4 color) {
    vec3 lightColor = vec3(255.f / 255.f, 114.f / 255.f, 81.f / 255.f);
    vec3 moonColor = vec3(129.f / 255.f, 159.f / 255.f, 1.f);
    float lightPower = 500.f, moonPower = 8000.f;

    vec3 posToLight = light.xyz - pos;
    vec3 posToLightNor = normalize(posToLight);

    float distanceToLight = length(posToLight);
    vec3 toCamera = normalize(camera.xyz - pos);
    vec3 lightReflection = reflect(-posToLightNor, outNormal);

    float ambient = 0.5f;
    float diffuse = max((dot(posToLightNor, outNormal)), 0.f) * max(lightPower / (distanceToLight * distanceToLight), 0.f);
    float moonDiffuse = max((dot(-posToLightNor, outNormal)), 0.12f) * 0.65f;

    return vec4(lightColor * color.rgb * (ambient + diffuse) + moonColor * moonDiffuse, color.a);
}

void main() {
    finalColor = calcLight(vec4(outColor, 1.f));
    // finalColor = calcLight(texture(tex, outUv));
}
