#version 310 es

precision highp float;

in vec3 pos;
in vec4 color;
out vec4 finalColor;

void main() {
    //finalColor = calcLight(vec2(0.f, 0.f, 1.f));
    finalColor = color;
}
