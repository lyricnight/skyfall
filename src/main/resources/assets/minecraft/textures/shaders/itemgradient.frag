#version 120

uniform sampler2D texture;
uniform vec2 texelSize;
uniform vec3 color1;
uniform vec3 color2;
uniform vec3 color3;
uniform int colorCount; // 2 or 3
uniform int gradientDirection; // 0=horizontal, 1=vertical, 2=radial, 3=diagonal
uniform float outlineAlpha;
uniform float outlineThickness;
uniform float fillAlpha;
uniform float mixFactor;
uniform vec2 dimensions;
uniform float itemMinU;
uniform float itemMinV;
uniform float itemMaxU;
uniform float itemMaxV;

float getEdgeSmoothness(vec2 uv, float thickness) {
    float centerAlpha = texture2D(texture, uv).a;
    if (centerAlpha > 0.0) {
        return 0.0;
    }

    float maxAlpha = 0.0;
    float totalSamples = 0.0;

    float radius = thickness;
    int samples = int(thickness * 4.0);

    for (int i = 0; i < samples; i++) {
        float angle = float(i) * 6.28318530718 / float(samples); // 2*PI / samples
        float dist = radius * 0.7;

        vec2 offset = vec2(cos(angle) * dist * texelSize.x, sin(angle) * dist * texelSize.y);
        float neighborAlpha = texture2D(texture, uv + offset).a;

        if (neighborAlpha > 0.0) {
            maxAlpha = max(maxAlpha, neighborAlpha);
            totalSamples += 1.0;
        }
    }

    // Return smoothed edge value (0.0 = not an edge, 1.0 = definite edge)
    if (totalSamples > 0.0) {
        float edgeStrength = totalSamples / float(samples);
        return smoothstep(0.1, 0.5, edgeStrength); // Smooth transition
    }

    return 0.0;
}

bool isEdge(vec2 uv, float thickness) {
    return getEdgeSmoothness(uv, thickness) > 0.0;
}

vec3 calculateGradient(vec2 uv) {
    vec2 itemUV = (uv - vec2(itemMinU, itemMinV)) / (vec2(itemMaxU, itemMaxV) - vec2(itemMinU, itemMinV));

    itemUV = clamp(itemUV, 0.0, 1.0);

    float t = 0.0;

    if (gradientDirection == 0) {
        // Horizontal (left to right across ITEM)
        t = itemUV.x;
    } else if (gradientDirection == 1) {
        // Vertical (top to bottom across ITEM)
        t = itemUV.y;
    } else if (gradientDirection == 2) {
        // Radial from center of ITEM
        vec2 center = vec2(0.5, 0.5);
        t = distance(itemUV, center) * 1.414; // Normalize to 0-1 (diagonal distance)
    } else if (gradientDirection == 3) {
        // Diagonal (top-left to bottom-right across ITEM)
        t = (itemUV.x + itemUV.y) * 0.5;
    }

    t = clamp(t, 0.0, 1.0);

    vec3 result;
    if (colorCount == 2) {
        result = mix(color1, color2, t);
    } else {
        // 3 colors: interpolate color1->color2->color3
        if (t < 0.5) {
            result = mix(color1, color2, t * 2.0);
        } else {
            result = mix(color2, color3, (t - 0.5) * 2.0);
        }
    }

    return result;
}

void main() {
    vec4 centerCol = texture2D(texture, gl_TexCoord[0].xy);
    if (centerCol.a > 0.0) {
        vec3 gradientColor = calculateGradient(gl_TexCoord[0].xy);
        vec3 finalColor = mix(centerCol.rgb, gradientColor, mixFactor);
        gl_FragColor = vec4(finalColor, centerCol.a * fillAlpha);
    }
    else {
        float edgeSmoothness = getEdgeSmoothness(gl_TexCoord[0].xy, outlineThickness);

        if (edgeSmoothness > 0.0) {
            vec3 outlineGradientColor = calculateGradient(gl_TexCoord[0].xy);
            gl_FragColor = vec4(outlineGradientColor, outlineAlpha * edgeSmoothness);
        } else {
            gl_FragColor = vec4(0.0);
        }
    }
}

