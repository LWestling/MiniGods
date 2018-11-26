#version 310 es

precision highp float;


in vec4 color;
in vec2 texCoord;
out vec4 finalColor;

uniform sampler2D tex;

void main() {
    finalColor = color * texture(tex, texCoord);
}
