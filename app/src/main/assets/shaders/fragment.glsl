#version 310 es

precision lowp float;

in vec3 pos;
in vec3 outNormal;
out vec4 finalColor;

vec4 calcLight(vec3 color) {
    vec3 light = vec3(10.f, 0.1f, 10.f);
    vec3 posToLight = normalize(light - pos);
    vec3 lightReflection = reflect(-posToLight, outNormal);
    vec3 toCamera = normalize(-pos);
    float specular = pow(max(dot(lightReflection, toCamera), 0.f), 100.f);
    vec3 ambient = vec3(0.2f, 0.2f, 0.2f) * color;

    return vec4(color * (dot(posToLight, outNormal) + specular) + ambient, 1.f);
}

void main() {
    vec3 color = vec3(1.0, 0.0, 1.0);

    finalColor = calcLight(color);
}
