#version 310 es

precision highp float;

in vec3 pos;
in vec2 texCoord;
out vec4 finalColor;

uniform sampler2D tex;

void main() {
    //finalColor = vec4(texCoord.xy, 0.f, 1.f);
    finalColor = vec4(1.0f, 1.0f, 1.0f, texture(tex, texCoord).a);
}
