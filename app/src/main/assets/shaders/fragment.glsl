#version 310 es

precision lowp float;

in vec3 pos;
out vec4 finalColor;

void main() {
    vec3 light = vec3(0, 0, 0.1);
    vec3 color = vec3(0.0, 0.0, 1.0);

    vec3 posToLight = normalize(light - pos);
    vec3 normal = vec3(0, 0, 1); // just a test normal

    finalColor = vec4(color * dot(posToLight, normal), 1.f);
}