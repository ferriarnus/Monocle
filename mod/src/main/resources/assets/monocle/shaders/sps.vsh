#version 150 core

//#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    //vertexDistance = fog_distance(Position, FogShape);
    vertexDistance = 1.0f;
    vertexColor = Color;
    texCoord0 = UV0;
}