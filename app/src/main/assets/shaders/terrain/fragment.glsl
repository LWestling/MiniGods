#version 310 es

precision lowp float;

in vec3 pos;
in vec3 normal;
out vec4 finalColor;

vec4 calcLight(vec3 color) {
    vec3 light = vec3(6.f, 6.f, 0.1f);
    vec3 posToLight = normalize(light - pos);
    vec3 lightReflection = reflect(-posToLight, normal);
    vec3 toCamera = normalize(-pos);
    float specular = pow(max(dot(lightReflection, toCamera), 0.f), 100.f);

    return vec4(color * (dot(posToLight, normal) + specular), 1.f);
}

void main() {
    vec3 color = vec3(0.0, 0.0, 1.0);

    finalColor = calcLight(color);
}
