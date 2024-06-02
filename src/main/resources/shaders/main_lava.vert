#version 120

uniform int time;
attribute float groundHeight;

mat4 getTransform()
{
    return mat4(1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0,  0, 0, 0, 1);
}

vec4 getPos(mat4 transform)
{
    vec4 p = gl_Vertex;
    return vec4(p.xy, p.z + sin(5.0 * (p.x + p.y) + (0.1f * p.x * p.y) + time / 1000.0f) * 3.0f - 3.0f, p.w);
}

vec3 getNormal(mat4 transform)
{
    return (gl_ModelViewProjectionMatrix * vec4(gl_Normal, 0)).xyz;
}

void getVertVecs(out vec4 pos, out vec3 normal)
{
    pos = getPos(getTransform());
    //    pos.z += sin(5.0 * (pos.x + pos.y) + (0.1f * pos.x * pos.y) + time / 1000.0f) * 3.0f - 6.0f;
    normal = gl_Normal;
}

vec4 getColor(vec4 colorIn)
{
    vec4 pos = getPos(getTransform());
    float offset = sin(8.0 * (pos.x + pos.y) + (0.1 * pos.x * pos.y) + time / 1000.0f) * 0.08f;

    return vec4(colorIn.xyz, colorIn.a);
}