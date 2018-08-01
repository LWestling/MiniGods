#version 310 es

precision highp float;

in vec3 pos;
in vec2 texCoord;
out vec4 finalColor;

uniform sampler2D tex;

void main() {
    //finalColor = vec4(texCoord.xy, 0.f, 1.f);
    finalColor = texture(tex, texCoord) + vec4(0.25f, 0.25f, 0.25f, 1.f);
}
