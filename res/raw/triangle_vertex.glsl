// triangle_vertex.glsl

uniform mat4 uMVPMatrix;
uniform mat4 uMVMatrixInvT;

attribute vec4 aPosition;
attribute vec4 aNormal;
uniform   vec4 uLight;
uniform float uAlpha;
// uniform float uPointSize;

varying float vAlpha;

void main() {
  gl_Position = uMVPMatrix * aPosition;
  
  vec4 sight = vec4( gl_Position.xyz, 1.0 );
  vAlpha = uAlpha * min(0.6 + 0.4 * max( dot( normalize(sight), normalize(uMVMatrixInvT * aNormal) ), 0.0 ), 1.0);

  // vAlpha = min(0.3 + 0.7*max( dot( uLight, normalize(uMVMatrixInvT * aNormal) ), 0.0 ), 1.0);

  // this is ok for perspective but not for orthogonal
  // vAlpha = min( 0.3 + 0.7 * max( - gl_Position.z, 0.0 ), 1.0 );

  // gl_PointSize = uPointSize;
}
