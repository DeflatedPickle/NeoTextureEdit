uniform vec3 u_WS_EyePos;

uniform sampler2D _2dTex0;
uniform sampler2D _2dNormalMap;
uniform sampler2D _2dSpecWeightMap;
uniform sampler2D _2dHeightMap;

varying vec3 v_WS_vertex;
varying vec3 v_WS_normal;
varying vec3 v_WS_tangent;

uniform float u_SpecularPower;
uniform float u_POM_Strength;
uniform vec2 u_TexScale;
uniform float u_Ambient;

void main(void){
    vec3 lightPos = vec3(20.0, 60.0, 40.0);

    vec3 lightDir = normalize(lightPos - v_WS_vertex);

    mat3 onb;
    onb[2] = normalize(v_WS_normal);
    onb[0] = normalize(v_WS_tangent - dot(onb[2], v_WS_tangent) * onb[2]);
    onb[1] = cross(onb[2], onb[0]);

    vec3 rayDir = v_WS_vertex - u_WS_EyePos;
    vec3 eyeDir = normalize(rayDir * onb);

    vec3 pos = vec3(gl_TexCoord[0].xy * u_TexScale, 1.0);

    rayDir = eyeDir;
    if (rayDir.z > 0.0) rayDir.z = -rayDir.z;
        if (u_POM_Strength > 0.0) {
            rayDir.z /= 0.25 * u_POM_Strength;
            rayDir.x *= 0.5;
            rayDir.y *= 0.5;

            for (int i = 0; i < 512; i++) {
                if (pos.z < 1.0 - (texture2D(_2dHeightMap, pos.xy).r)) break;

                pos += rayDir * 0.001;
            }
    }

    vec3 normal = onb * normalize(texture2D(_2dNormalMap, pos.xy).rgb * 2.0 - 1.0);

    vec3 refl = reflect(lightDir, normal);
    float SpecWeightyness = texture2D(_2dSpecWeightMap, pos.xy).x;
    float spec = pow(max(dot(refl, eyeDir), 0.0), u_SpecularPower) * 0.5 * SpecWeightyness;
    float diff = dot(lightDir, normal);

    if (diff < 0.0) diff = 0.0;
    diff = min(diff + u_Ambient, 1.0);

    vec4 color = vec4(texture2D(_2dTex0, pos.xy).xyz, 0.0) * diff + spec;

    gl_FragColor = color;
}