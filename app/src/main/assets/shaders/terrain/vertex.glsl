#version 310 es

layout(location=0) in vec3 vertexPos;
layout(location=1) in vec3 inNormal;
layout(location=2) in vec2 inTexCoord;
layout(location=3) uniform mat4 model;
layout(location=4) uniform mat4 cpMatrix;
layout(location=5) uniform vec4 light;
layout(location=6) uniform vec4 camera;

out vec4 color;
out vec2 texCoord;

vec4 calcLight(vec3 pos) {
    vec3 lightColor = vec3(255.f / 255.f, 114.f / 255.f, 81.f / 255.f);
    vec3 moonColor = vec3(129.f / 255.f, 159.f / 255.f, 1.f);
    float lightPower = 600.f, moonPower = 800.f;

    vec3 posToLight = light.xyz - pos;
    vec3 posToLightNor = normalize(posToLight);

    float distanceToLight = length(posToLight);

    vec3 toCamera = normalize(camera.xyz - pos);
    vec3 lightReflection = reflect(-posToLightNor, inNormal);

    float lightFromSun = max(lightPower / (distanceToLight * distanceToLight), 0.1f);

    float ambient = 0.01f;
    float diffuse = max((dot(posToLightNor, inNormal)), 0.1f) * lightFromSun;
    float moonDiffuse = max((dot(-posToLightNor, inNormal)), 0.12f) * 0.65f;
    float specular = pow(max(dot(lightReflection, toCamera), 0.f), lightPower) * 0.1f;

    return vec4(lightColor * (diffuse + ambient) + moonColor * moonDiffuse, 1.f);
}

void main() {
    vec4 position = model * vec4(vertexPos, 1.f);
    gl_Position = cpMatrix * position;

    color = calcLight(position.xyz);
    texCoord = inTexCoord;
}