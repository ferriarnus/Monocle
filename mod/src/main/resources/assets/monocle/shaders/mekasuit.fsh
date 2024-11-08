#version 150

//#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec4 shadedVertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec4 normal;

out vec4 fragColor;

bool shouldTint(float red, float green, float blue);

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.5) {
        discard;
    }
    if (shouldTint(color.r, color.g, color.b)) {
        color *= shadedVertexColor * ColorModulator;
    } else {
        color *= vertexColor * ColorModulator;
    }
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;
    fragColor = color;
    //fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}

bool shouldTint(float red, float green, float blue) {
    float minV = min(min(red, green), blue);
    float maxV = max(max(red, green), blue);
    float delta = maxV - minV;
    //Calculate Saturation and Value components of HSV
    float saturation = maxV == 0.0 ? 0.0 : delta / maxV;
    float value = maxV;
    return value >= 0.48 && saturation <= 0.15;
}