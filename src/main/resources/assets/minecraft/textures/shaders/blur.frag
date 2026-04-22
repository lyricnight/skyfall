#version 120

uniform sampler2D textureIn;
uniform vec2 texelSize, direction;
uniform float radius;
uniform float weights[256];

#define offset texelSize * direction

void main() {
    vec4 blr = texture2D(textureIn, gl_TexCoord[0].st) * weights[0];

    for (float f = 1.0; f <= radius; f++) {
        blr += texture2D(textureIn, gl_TexCoord[0].st + f * offset) * weights[int(abs(f))];
        blr += texture2D(textureIn, gl_TexCoord[0].st - f * offset) * weights[int(abs(f))];
    }

    gl_FragColor = vec4(clamp(blr.rgb, 0.0, 1.0), 1.0);
}
