#version 300 es

layout(location=0) in vec3 vertexPos;
layout(location=1) in vec3 inNormal;
layout(location=2) uniform mat4 model;
layout(location=3) uniform mat4 cpMatrix;
layout(location=4) uniform vec4 light;
layout(location=5) uniform vec4 camera;

out vec3 pos;
out vec4 color;

vec4 calcLight(vec3 color) {
    float lightPower = 300.f;

    vec3 posToLight = light.xyz - pos;
    vec3 posToLightNor = normalize(posToLight);

    float distanceToLight = length(posToLight);
    vec3 toCamera = normalize(camera.xyz - pos);
    vec3 lightReflection = reflect(-posToLightNor, inNormal);

    vec3 ambient = color * 0.1f;
    vec3 diffuse = color * max((dot(posToLightNor, inNormal)), 0.f) /** max(lightPower / (distanceToLight * distanceToLight), 0.f) */;

    float specular = pow(max(dot(lightReflection, toCamera), 0.f), 150.f);

    return vec4(diffuse + ambient + specular, 0.55f);
}

void main() {
    vec4 position = model * vec4(vertexPos, 1.f);
    gl_Position = cpMatrix * position;

    pos = position.xyz;
    color = calcLight(vec3(0.40f, 0.72f, 1.f));
}