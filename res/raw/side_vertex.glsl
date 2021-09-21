// side_vertex.glsl

uniform mat4 uMVPMatrix;
uniform mat4 uMVMatrixInvT;

attribute vec4 aPosition;
uniform float uAlpha;
// uniform float uPointSize;

varying float vAlpha;

void main() {
  gl_Position = uMVPMatrix * aPosition;
  
  vAlpha = uAlpha;

  // vAlpha = min( 0.3 + 0.7 * max( - gl_Position.z, 0.0 ), 1.0 );

  // gl_PointSize = uPointSize;
}
