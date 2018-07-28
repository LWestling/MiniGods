#version 310 es

precision lowp float;

in vec3 pos;
in vec3 outNormal;
in vec2 texCoord;
out vec4 finalColor;

uniform sampler2D tex;

vec4 calcLight(vec3 color) {
    vec3 light = vec3(10.f, 0.1f, 10.f);
    vec3 posToLight = normalize(light - pos);
    vec3 ambient = vec3(0.2f, 0.2f, 0.2f) * color;

    return vec4(color * (dot(posToLight, outNormal)) + ambient, 1.f);
}

void main() {
 //   finalColor = calcLight(texture(tex, texCoord).xyz);
    finalColor = texture(tex, texCoord);
}
