#version 310 es

precision lowp float;

layout(location=5) uniform vec4 light;
layout(location=6) uniform vec4 cam;

in vec3 pos;
in vec3 outNormal;
in vec2 texCoord;
out vec4 finalColor;

uniform sampler2D tex;

vec4 calcLight(vec4 color) {
    vec3 posToLight = normalize(light.xyz - pos);
    vec3 ambient = 0.5f * color.rgb;

    return vec4(color.rgb * (dot(posToLight, outNormal)) + ambient, color.a);
}

void main() {
    vec4 texColor = texture(tex, texCoord);
    finalColor = calcLight(texColor);
}
