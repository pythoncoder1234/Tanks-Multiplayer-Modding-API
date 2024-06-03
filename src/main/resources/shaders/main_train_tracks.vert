#version 120

uniform float obstacleSizeFrac;

void getVertVecs(out vec4 pos, out vec3 normal)
{
    pos = gl_Vertex;
    normal = gl_Normal;
}

vec4 getPos(mat4 transform)
{
    return gl_Vertex + vec4(0, 0, (-1 + obstacleSizeFrac) * 5, 0);
}

vec3 getNormal(mat4 transform)
{
    return (gl_ModelViewProjectionMatrix * vec4(gl_Normal, 0)).xyz;
}

mat4 getTransform()
{
    return mat4(1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0,  0, 0, 0, 1);
}

vec4 getColor(vec4 colorIn)
{
    vec4 pos = getPos(getTransform());
    float offset = sin(2.0 * (pos.x + pos.y) + (0.01 * pos.x * pos.y)) * 0.08f;
    return colorIn + vec4(offset, offset, offset, 0);
}