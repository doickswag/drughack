#version 150

#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D InSampler;
in vec2 texCoord;
out vec4 fragColor;

uniform mat4 ProjMat;
uniform vec2 InSize;
uniform vec2 OutSize;
in vec2 oneTexel;
uniform int lineWidth;
uniform float scale;
uniform float time;
uniform vec2 resolution;
uniform float fillAlpha;
uniform float outlineAlpha;
uniform float speed;
uniform vec3 color1;
uniform vec3 color2;

float _step(float value1, float value2, float state) {
    return value1 + state * (value2 - value1);
}

void main() {
    float colorState = ceil(((time * (400. * speed * 2.5)) + (100. + ((((gl_FragCoord.x / resolution.x) + (gl_FragCoord.y / resolution.y)) * scale) * 2.))) / 20.);
    colorState = mod(colorState, 360.) / 360.;
    if (colorState > 0.5) colorState = 1. - colorState;
    colorState *= 2.;

    vec3 gradientColor = vec3(_step(color1.r, color2.r, colorState), _step(color1.g, color2.g, colorState), _step(color1.b, color2.b, colorState));

    vec4 centerCol = texture(InSampler, texCoord);

    if(centerCol.a != 0) {
        fragColor = vec4(gradientColor, fillAlpha);
    } else {
        for (int x = -lineWidth; x < lineWidth + 1; x++) {
            for (int y = -lineWidth; y < lineWidth + 1; y++) {
                vec2 offset = vec2(x, y);
                vec2 coord = texCoord + offset * oneTexel;
                vec4 t = texture(InSampler, coord);
                if (t.a != 0){
                    fragColor = vec4(gradientColor, outlineAlpha);
                    return;
                }
            }
        }
        fragColor = vec4(0., 0., 0., 0.);
    }
}