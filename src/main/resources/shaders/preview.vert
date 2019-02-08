uniform vec3 u_WS_EyePos;
uniform mat3 u_CameraONB;
attribute vec3 a_OS_tangent;

varying vec3 v_WS_vertex;
varying vec3 v_WS_normal;
varying vec3 v_WS_tangent;

void main(void) {
	gl_TexCoord[0] = gl_MultiTexCoord0;

	v_WS_vertex = vec3(gl_ModelViewMatrix * gl_Vertex);
	v_WS_normal = vec3(gl_NormalMatrix * gl_Normal);
	v_WS_tangent = vec3(gl_NormalMatrix * a_OS_tangent);

	gl_Position = gl_ProjectionMatrix * vec4((v_WS_vertex - u_WS_EyePos) * u_CameraONB, 1);
}