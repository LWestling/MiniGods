#version 310 es

precision lowp float;

in vec3 pos;
in vec3 normal;
in vec2 texCoord;
out vec4 finalColor;

uniform sampler2D tex;

vec4 calcLight(vec3 color) {
    vec3 light = vec3(6.f, 6.f, 0.1f);
    vec3 posToLight = normalize(light - pos);

    return vec4(color * (dot(posToLight, normal)), 1.f);
}

void main() {
    vec3 color = texture(tex, texCoord).xyz + vec3(0.3f, 0.3f, 0.3f);
    //vec3 color = vec3(texCoord.x, 0, texCoord.y);

    finalColor = calcLight(color);
}
